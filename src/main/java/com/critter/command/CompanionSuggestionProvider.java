package com.critter.command;

import java.util.concurrent.CompletableFuture;

import com.critter.CritterMod;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class CompanionSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        builder.suggest("withered", Text.of("Withered Critter"));
        builder.suggest("allay", Text.of("Allay Critter"));

        return builder.buildFuture();
    }

    public static boolean isValid(String companionType) {
        return companionType.equals("withered") || companionType.equals("allay");
    }

    public static Entity createCompanion(String companionType, ServerWorld world, ServerPlayerEntity player) throws CommandSyntaxException {
        switch (companionType) {
            case "withered":
                var withered_companion = new WitherSkeletonEntity(EntityType.WITHER_SKELETON, world);
                withered_companion.setStatusEffect(new StatusEffectInstance(CritterMod.BEWITCHED, StatusEffectInstance.INFINITE), player);
                withered_companion.setCustomName(Text.of(player.getEntityName() + "'s Withered Critter"));
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

                return withered_companion;
            case "allay":
                var allay_companion = new AllayEntity(EntityType.ALLAY, world);
                allay_companion.setCustomName(Text.of(player.getEntityName() + "'s Allay Critter"));
                allay_companion.setPosition(player.getPos());
                allay_companion.setInvulnerable(true);
                return allay_companion;
            default:
                throw new SimpleCommandExceptionType(Text.of("Invalid Critter type: '" + companionType + "'")).create();
        }
    }
}
