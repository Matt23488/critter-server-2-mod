package com.critter.entity.ai.goal;

import com.critter.CritterMod;

import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;

public class AttackPlayerIfNotBewitchedGoal extends ActiveTargetGoal<PlayerEntity> {
    public AttackPlayerIfNotBewitchedGoal(AbstractSkeletonEntity skeleton) {
        super(skeleton, PlayerEntity.class, true, __ -> !skeleton.hasStatusEffect(CritterMod.BEWITCHED_SKELETON));
    }

    @Override
    public boolean shouldContinue() {
        return !this.mob.hasStatusEffect(CritterMod.BEWITCHED_SKELETON);
    }
}
