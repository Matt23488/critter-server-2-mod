package com.critter.command;

import com.critter.CritterMod;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CritterCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(CritterMod.MODID);

    private static final String TAG_PREFIX = "critter.";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("critter")
                .then(literal("summon").requires(scs -> scs.hasPermissionLevel(2))
                    .then(argument("player", EntityArgumentType.player())
                        .then(argument("critter_type", StringArgumentType.word())
                            .suggests(new CritterSuggestionProvider())
                            .executes(CritterCommand::summon_critter)
                            .then(argument("nbt", NbtCompoundArgumentType.nbtCompound())
                                .executes(CritterCommand::summon_critter)
                            )
                        )
                    )
                )
                .then(literal("kill").requires(scs -> scs.hasPermissionLevel(2))
                    .then(argument("player", EntityArgumentType.player())
                        .then(argument("critter_type", StringArgumentType.word())
                            .suggests(new CritterSuggestionProvider())
                            .executes(CritterCommand::kill_critter)
                        )
                    )
                )
                .then(literal("tpIfFar").requires(scs -> scs.hasPermissionLevel(2))
                    .then(argument("player", EntityArgumentType.player())
                        .then(argument("critter_type", StringArgumentType.word())
                            .suggests(new CritterSuggestionProvider())
                            .executes(CritterCommand::tp_critter_if_far)
                        )
                    )
                )
                .then(literal("tp")
                    .then(argument("player", EntityArgumentType.player()).requires(scs -> scs.hasPermissionLevel(2))
                        .executes(CritterCommand::tp_critter)
                    )
                    .executes(CritterCommand::tp_critter)
                )
        );
    }

    private static int summon_critter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        var critterType = StringArgumentType.getString(ctx, "critter_type");
        if (!CritterSuggestionProvider.isValid(critterType))
            throw new SimpleCommandExceptionType(Text.of("Invalid Critter type: '" + critterType + "'")).create();

        var world = ctx.getSource().getWorld();
        var player = EntityArgumentType.getPlayer(ctx, "player");
        var nbt = getNbt(ctx);

        var prefix = TAG_PREFIX + critterType;
        for (var tag : player.getCommandTags()) {
            if (tag.startsWith(prefix)) {
                var message = player.getEntityName() + " already has that Critter";
                LOGGER.warn(message);
                throw new SimpleCommandExceptionType(Text.of(message)).create();
            }
        }

        var critter = CritterSuggestionProvider.createCritter(critterType, world, player, nbt);

        world.spawnEntity(critter);
        critter.playSound(SoundEvent.of(new Identifier("entity.illusioner.cast_spell")), 1, 1);

        nbt.putUuid("UUID", critter.getUuid());
        player.addCommandTag(TAG_PREFIX + critterType + nbt.asString());


        return Command.SINGLE_SUCCESS;
    }

    private static int kill_critter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        var critterType = StringArgumentType.getString(ctx, "critter_type");
        if (!CritterSuggestionProvider.isValid(critterType))
            throw new SimpleCommandExceptionType(Text.of("Invalid Critter type: '" + critterType + "'")).create();

        var player = EntityArgumentType.getPlayer(ctx, "player");

        var critter = findCritter(ctx, player, critterType);
        if (critter == null)
            return Command.SINGLE_SUCCESS;

        var tags = player.getCommandTags();
        var critterTag = "";
        var prefix = TAG_PREFIX + critterType;
        for (var tag : tags) {
            if (tag.startsWith(prefix))
                critterTag = tag;
        }
        tags.remove(critterTag);

        critter.kill();

        return Command.SINGLE_SUCCESS;
    }

    private static int tp_critter_if_far(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        var critterType = StringArgumentType.getString(ctx, "critter_type");
        if (!CritterSuggestionProvider.isValid(critterType))
            throw new SimpleCommandExceptionType(Text.of("Invalid Critter type: '" + critterType + "'")).create();

        var world = ctx.getSource().getWorld();
        var player = EntityArgumentType.getPlayer(ctx, "player");

        var critter = findCritter(ctx, player, critterType);
        if (critter == null)
            return Command.SINGLE_SUCCESS;

        if (critter.getEntityWorld() != player.getEntityWorld())
            critter.teleport(world, player.getX(), player.getY(), player.getZ(), PositionFlag.VALUES, 0F, 0F);
        else if (critter.squaredDistanceTo(player) >= 2500) // 50 blocks
            critter.teleport(player.getX(), player.getY(), player.getZ());

        return Command.SINGLE_SUCCESS;
    }

    private static int tp_critter(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        var world = ctx.getSource().getWorld();

        ServerPlayerEntity player = null;
        try {
            player = EntityArgumentType.getPlayer(ctx, "player");
        } catch (Exception e) {
            player = ctx.getSource().getPlayer();
        }

        if (player == null)
            throw new SimpleCommandExceptionType(Text.of("If you aren't a player you must specify one.")).create();

        for (var critterType : CritterSuggestionProvider.CRITTER_TYPES) {
            var critter = findCritter(ctx, player, critterType);
            if (critter == null)
                continue;

            critter.teleport(world, player.getX(), player.getY(), player.getZ(), PositionFlag.VALUES, 0F, 0F);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static Entity findCritter(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity player, String critterType) throws CommandSyntaxException {
        var critterNbtString = "";
        var tags = player.getCommandTags();
        var prefix = TAG_PREFIX + critterType;
        for (var tag : tags) {
            if (tag.startsWith(prefix)) {
                critterNbtString = tag.substring(prefix.length());
            }
        }

        if (critterNbtString.isEmpty())
            return null;

        var critterNbt = StringNbtReader.parse(critterNbtString);
        var critterUuid = critterNbt.getUuid("UUID");
        Entity critter = null;
        for (var world : ctx.getSource().getServer().getWorlds()) {
            critter = world.getEntity(critterUuid);
            if (critter != null)
                break;
        }

        if (critter == null) {
            LOGGER.warn(player.getEntityName() + "'s Critter despawned. Respawning...");

            var world = player.getServerWorld();
            critter = CritterSuggestionProvider.createCritter(critterType, world, player, critterNbt);
            world.spawnEntity(critter);
        }

        return critter;
    }

    private static NbtCompound getNbt(CommandContext<ServerCommandSource> ctx) {
        try {
            return NbtCompoundArgumentType.getNbtCompound(ctx, "nbt");
        } catch (Exception e) {
            return new NbtCompound();
        }
    }
}
