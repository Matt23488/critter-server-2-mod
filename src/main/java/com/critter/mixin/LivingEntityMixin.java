package com.critter.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.critter.CritterMod;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Redirect(method = "damage(Lnet/minecraft/entity/damage/DamageSource;F)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isInvulnerableTo(Lnet/minecraft/entity/damage/DamageSource;)Z", ordinal = 0))
    private boolean injectBewitchedMobImmunityLogic(LivingEntity target, DamageSource damageSource) {
        var attacker = damageSource.getAttacker();
        if (attacker == null)
            return target.isInvulnerableTo(damageSource);

        var targetIsPlayer = target instanceof PlayerEntity;
        var targetIsBewitched = target.hasStatusEffect(CritterMod.BEWITCHED);
        var attackerIsPlayer = attacker instanceof PlayerEntity;
        var attackerIsBewitched = attacker instanceof HostileEntity && ((HostileEntity)attacker).hasStatusEffect(CritterMod.BEWITCHED);

        var isPlayerAndShouldBeImmune = targetIsPlayer && attackerIsBewitched;
        var isBewitchedAndShouldBeImmune = targetIsBewitched && (attackerIsBewitched || attackerIsPlayer);

        if (isPlayerAndShouldBeImmune || isBewitchedAndShouldBeImmune)
            return true;

        return target.isInvulnerableTo(damageSource);
    }
}
