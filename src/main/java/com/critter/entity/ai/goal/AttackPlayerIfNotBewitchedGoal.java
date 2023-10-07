package com.critter.entity.ai.goal;

import com.critter.CritterMod;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;

public class AttackPlayerIfNotBewitchedGoal extends ActiveTargetGoal<PlayerEntity> {
    public AttackPlayerIfNotBewitchedGoal(HostileEntity hostileEntity) {
        super(hostileEntity, PlayerEntity.class, true, __ -> !hostileEntity.hasStatusEffect(CritterMod.BEWITCHED));
    }

    @Override
    public boolean shouldContinue() {
        return !this.mob.hasStatusEffect(CritterMod.BEWITCHED);
    }
}
