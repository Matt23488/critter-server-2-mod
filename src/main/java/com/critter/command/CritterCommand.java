package com.critter.command;

import com.critter.CritterMod;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;


public class CritterCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("critter").requires(scs -> scs.hasPermissionLevel(2))
                .then(literal("withered_companion")
                    .then(literal("give")
                        .then(argument("player", EntityArgumentType.player())
                            .executes(CritterCommand::summon_withered_companion)
                        )
                    )
                    .then(literal("take")
                        .then(argument("player", EntityArgumentType.player())
                            .executes(CritterCommand::kill_withered_companion)
                        )
                    )
                )
        );
    }

    // TODO: I might not make them invincible but maybe give them a ton of health.
    //       Then if I can make custom power types (etc) for origins, I can tap
    //       into its health in place of a resource power type
    private static int summon_withered_companion(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        var world = ctx.getSource().getWorld();
        var player = EntityArgumentType.getPlayer(ctx, "player");
        var withered_companion = new WitherSkeletonEntity(EntityType.WITHER_SKELETON, world);
        withered_companion.setStatusEffect(new StatusEffectInstance(CritterMod.BEWITCHED, StatusEffectInstance.INFINITE), player);
        withered_companion.setCustomName(Text.of(player.getEntityName() + "'s Withered Companion"));
        withered_companion.setPosition(player.getPos());
        withered_companion.setInvulnerable(true);

        var swordNbt = new NbtCompound();
        swordNbt.putString("id", "minecraft:netherite_sword");
        swordNbt.putInt("Count", 1);
        withered_companion.equipStack(EquipmentSlot.MAINHAND, ItemStack.fromNbt(swordNbt));

        player.addCommandTag("wc=" + withered_companion.getUuidAsString());

        world.spawnEntity(withered_companion);

        return Command.SINGLE_SUCCESS;
    }

    private static int kill_withered_companion(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        var world = ctx.getSource().getWorld();
        var player = EntityArgumentType.getPlayer(ctx, "player");

        var withered_companion_uuid = "";
        var tags = player.getCommandTags();
        for (var tag : tags) {
            if (tag.startsWith("wc=")) {
                withered_companion_uuid = tag.substring(3);
            }
        }

        if (withered_companion_uuid.isEmpty())
            throw new SimpleCommandExceptionType(Text.of(player.getEntityName() + " doesn't have a Withered Companion")).create();

        tags.remove("wc=" + withered_companion_uuid);

        var withered_companion = world.getEntity(UUID.fromString(withered_companion_uuid));
        if (withered_companion == null)
            throw new SimpleCommandExceptionType(Text.of("Couldn't find " + player.getEntityName() + "'s Withered Companion")).create();

        withered_companion.kill();

        return Command.SINGLE_SUCCESS;
    }
}
