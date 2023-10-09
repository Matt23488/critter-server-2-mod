package com.critter.command;

import com.critter.CritterMod;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class CritterCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(CritterMod.MODID);

    private static final String TAG_PREFIX = "critter.";

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("critter").requires(scs -> scs.hasPermissionLevel(2))
                .then(literal("companion")
                    .then(literal("summon")
                        .then(
                            getCompanionNode(CritterCommand::summon_companion)
                        )
                    )
                    .then(literal("kill")
                        .then(
                            getCompanionNode(CritterCommand::kill_companion)
                        )
                    )
                )
        );
    }

    private static RequiredArgumentBuilder<ServerCommandSource, EntitySelector> getCompanionNode(IExecutor executor) {
        return argument("player", EntityArgumentType.player())
            .then(argument("companion_type", StringArgumentType.word())
                .suggests(new CompanionSuggestionProvider())
                .executes(executor::execute)
            );
    }

    private static int summon_companion(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        var companionType = StringArgumentType.getString(ctx, "companion_type");
        LOGGER.warn("companionType=" + companionType);
        if (!CompanionSuggestionProvider.isValid(companionType))
            throw new SimpleCommandExceptionType(Text.of("Invalid Critter type: '" + companionType + "'")).create();

        var world = ctx.getSource().getWorld();
        var player = EntityArgumentType.getPlayer(ctx, "player");

        for (var tag : player.getCommandTags()) {
            var prefix = TAG_PREFIX + companionType;
            if (tag.startsWith(prefix)) {
                var message = player.getEntityName() + " already has that Critter";
                LOGGER.warn(message);
                throw new SimpleCommandExceptionType(Text.of(message)).create();
            }
        }

        LOGGER.warn("companionType=" + companionType);
        var companion = CompanionSuggestionProvider.createCompanion(companionType, world, player);

        player.addCommandTag(TAG_PREFIX + companionType + companion.getUuidAsString());
        world.spawnEntity(companion);
        companion.playSound(SoundEvent.of(new Identifier("entity.illusioner.cast_spell")), 1, 1);

        return Command.SINGLE_SUCCESS;
    }

    private static int kill_companion(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        var companionType = StringArgumentType.getString(ctx, "companion_type");
        if (!CompanionSuggestionProvider.isValid(companionType))
            throw new SimpleCommandExceptionType(Text.of("Invalid Critter type: '" + companionType + "'")).create();

        var world = ctx.getSource().getWorld();
        var player = EntityArgumentType.getPlayer(ctx, "player");

        var companion_uuid = "";
        var tags = player.getCommandTags();
        for (var tag : tags) {
            var prefix = TAG_PREFIX + companionType;
            if (tag.startsWith(prefix)) {
                companion_uuid = tag.substring(prefix.length());
            }
        }

        if (companion_uuid.isEmpty()) {
            var message = player.getEntityName() + " doesn't have that Critter";
            LOGGER.warn(message);
            throw new SimpleCommandExceptionType(Text.of(message)).create();
        }

        tags.remove(TAG_PREFIX + companionType + companion_uuid);

        var withered_companion = world.getEntity(UUID.fromString(companion_uuid));
        if (withered_companion == null) {
            var message = "Couldn't find " + player.getEntityName() + "'s Critter";
            LOGGER.warn(message);
            throw new SimpleCommandExceptionType(Text.of(message)).create();
        }

        withered_companion.kill();

        return Command.SINGLE_SUCCESS;
    }

    private interface IExecutor {
        int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException;
    }
}
