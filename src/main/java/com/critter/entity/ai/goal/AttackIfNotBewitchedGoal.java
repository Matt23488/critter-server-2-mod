package com.critter.entity.ai.goal;

import com.critter.CritterMod;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;

public class AttackIfNotBewitchedGoal<T extends LivingEntity> extends ActiveTargetGoal<T> {

    public AttackIfNotBewitchedGoal(HostileEntity hostileEntity, Class<T> targetClass) {
        super(hostileEntity, targetClass, true, __ -> !hostileEntity.hasStatusEffect(CritterMod.BEWITCHED));
    }

    @Override
    public boolean shouldContinue() {
        return !this.mob.hasStatusEffect(CritterMod.BEWITCHED) && super.shouldContinue();
    }
}
