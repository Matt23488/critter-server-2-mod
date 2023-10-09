package com.critter.command;

import com.critter.CritterMod;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.argument;


public class CritterCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(CritterMod.MODID);

    private static final String TAG_PREFIX = "critter.wc=";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("critter").requires(scs -> scs.hasPermissionLevel(2))
                .then(literal("companion")
                    .then(literal("summon")
                        .then(
                            getCompanionNode(CritterCommand::summon_withered_companion)
                        )
                    )
                    .then(literal("kill")
                        .then(
                            getCompanionNode(CritterCommand::kill_withered_companion)
                        )
                    )
                )
        );
    }

    private static RequiredArgumentBuilder<ServerCommandSource, EntitySelector> getCompanionNode(IExecutor executes) {
        return argument("player", EntityArgumentType.player())
            .then(literal("withered")
                .executes(executes::executes)
            );
    }

    private static int summon_withered_companion(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        var world = ctx.getSource().getWorld();
        var player = EntityArgumentType.getPlayer(ctx, "player");

        for (var tag : player.getCommandTags()) {
            if (tag.startsWith(TAG_PREFIX)) {
                var message = player.getEntityName() + " already has a Withered Companion";
                LOGGER.warn(message);
                throw new SimpleCommandExceptionType(Text.of(message)).create();
            }
        }

        var withered_companion = new WitherSkeletonEntity(EntityType.WITHER_SKELETON, world);
        withered_companion.setStatusEffect(new StatusEffectInstance(CritterMod.BEWITCHED, StatusEffectInstance.INFINITE), player);
        withered_companion.setCustomName(Text.of(player.getEntityName() + "'s Withered Companion"));
        withered_companion.setPosition(player.getPos());
        withered_companion.setInvulnerable(true);

        var powerNbt = new NbtCompound();
        powerNbt.putString("id", "minecraft:power");
        powerNbt.putInt("lvl", 5);

        var punchNbt = new NbtCompound();
        punchNbt.putString("id", "minecraft:punch");
        punchNbt.putInt("lvl", 2);

        var bowEnchantmentNbt = new NbtList();
        bowEnchantmentNbt.add(powerNbt);
        bowEnchantmentNbt.add(punchNbt);

        var bowNbt = new NbtCompound();
        bowNbt.putString("id", "minecraft:bow");
        bowNbt.putInt("Count", 1);
        bowNbt.put("Enchantments", bowEnchantmentNbt);
        withered_companion.equipStack(EquipmentSlot.MAINHAND, ItemStack.fromNbt(bowNbt));

        LOGGER.info(bowNbt.asString());

        player.addCommandTag(TAG_PREFIX + withered_companion.getUuidAsString());

        world.spawnEntity(withered_companion);

        withered_companion.playSound(SoundEvent.of(new Identifier("entity.illusioner.cast_spell")), 1, 1);

        return Command.SINGLE_SUCCESS;
    }

    private static int kill_withered_companion(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        var world = ctx.getSource().getWorld();
        var player = EntityArgumentType.getPlayer(ctx, "player");

        var withered_companion_uuid = "";
        var tags = player.getCommandTags();
        for (var tag : tags) {
            if (tag.startsWith(TAG_PREFIX)) {
                withered_companion_uuid = tag.substring(TAG_PREFIX.length());
            }
        }

        if (withered_companion_uuid.isEmpty()) {
            var message = player.getEntityName() + " doesn't have a Withered Companion";
            LOGGER.warn(message);
            throw new SimpleCommandExceptionType(Text.of(message)).create();
        }

        tags.remove(TAG_PREFIX + withered_companion_uuid);

        var withered_companion = world.getEntity(UUID.fromString(withered_companion_uuid));
        if (withered_companion == null) {
            var message = "Couldn't find " + player.getEntityName() + "'s Withered Companion";
            LOGGER.warn(message);
            throw new SimpleCommandExceptionType(Text.of(message)).create();
        }

        withered_companion.kill();

        return Command.SINGLE_SUCCESS;
    }

    private interface IExecutor {
        int executes(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException;
    }
}
