package com.critter.entity.ai.goal;

import com.critter.CritterMod;

import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.AbstractSkeletonEntity;

public class RevengeExceptBewitchedSkeletonsGoal extends RevengeGoal {
    public RevengeExceptBewitchedSkeletonsGoal(AbstractSkeletonEntity skeleton) {
        super(skeleton, new Class[0]);
    }

    @Override
    public boolean shouldContinue() {
        return !this.mob.hasStatusEffect(CritterMod.BEWITCHED_SKELETON);
    }
}
