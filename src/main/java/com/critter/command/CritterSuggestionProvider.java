package com.critter.command;

import java.util.concurrent.CompletableFuture;

import com.critter.CritterMod;
import com.critter.entity.GoatCritterEntity;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class CritterSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        builder.suggest("allay", Text.of("An Allay Critter"));
        builder.suggest("goat", Text.of("A Goat Critter"));
        builder.suggest("withered", Text.of("A Withered Skeleton Critter"));

        return builder.buildFuture();
    }

    public static boolean isValid(String companionType) {
        return companionType.equals("withered") || companionType.equals("allay") || companionType.equals("goat");
    }

    public static Entity createCritter(String companionType, ServerWorld world, ServerPlayerEntity player, NbtCompound nbt) throws CommandSyntaxException {
        switch (companionType) {
            case "allay":
                var allayCritter = new AllayEntity(EntityType.ALLAY, world);
                allayCritter.readNbt(nbt);
                allayCritter.setCustomName(Text.of(player.getEntityName() + "'s Allay Critter"));
                allayCritter.setPosition(player.getPos());
                allayCritter.setInvulnerable(true);

                return allayCritter;
            case "withered":
                var witheredCritter = new WitherSkeletonEntity(EntityType.WITHER_SKELETON, world);
                witheredCritter.readNbt(nbt);
                witheredCritter.setStatusEffect(new StatusEffectInstance(CritterMod.BEWITCHED, StatusEffectInstance.INFINITE), player);
                witheredCritter.setCustomName(Text.of(player.getEntityName() + "'s Withered Critter"));
                witheredCritter.setPosition(player.getPos());
                witheredCritter.setInvulnerable(true);

                return witheredCritter;
            case "goat":
                var goatCritter = new GoatCritterEntity(world);
                goatCritter.readNbt(nbt);
                goatCritter.setCustomName(Text.of(player.getEntityName() + "'s Goat Critter"));
                goatCritter.setPosition(player.getPos());
                goatCritter.setInvulnerable(true);

                return goatCritter;
            default:
                throw new SimpleCommandExceptionType(Text.of("Invalid Critter type: '" + companionType + "'")).create();
        }
    }
}
