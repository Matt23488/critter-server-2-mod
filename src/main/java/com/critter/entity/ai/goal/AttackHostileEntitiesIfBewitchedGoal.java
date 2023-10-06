package com.critter.entity.ai.goal;

import com.critter.CritterMod;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.HostileEntity;

public class AttackHostileEntitiesIfBewitchedGoal extends ActiveTargetGoal<HostileEntity> {
    private static final TargetPredicate TEMPTING_ENTITY_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(64.0).ignoreVisibility();
    private final AbstractSkeletonEntity skeleton;

    public AttackHostileEntitiesIfBewitchedGoal(AbstractSkeletonEntity skeleton) {
        super(skeleton, HostileEntity.class, true, e -> !e.hasStatusEffect(CritterMod.BEWITCHED_SKELETON) && skeleton.hasStatusEffect(CritterMod.BEWITCHED_SKELETON));
        this.skeleton = skeleton;
    }

    @Override
    public boolean shouldContinue() {
        var target = this.skeleton.getTarget();
        if (target == null) {
            return super.shouldContinue();
        }

        if (!this.skeleton.canTarget(target))
            return false;

        if (this.skeleton.squaredDistanceTo(target) >= 100.0)
            return false;

        var closestPlayer = this.mob.getWorld().getClosestPlayer(TEMPTING_ENTITY_PREDICATE, this.mob);
        if (closestPlayer == null || this.skeleton.squaredDistanceTo(closestPlayer) >= 900.0) {
            return false;
        }

        return this.skeleton.hasStatusEffect(CritterMod.BEWITCHED_SKELETON) && !target.hasStatusEffect(CritterMod.BEWITCHED_SKELETON) && super.shouldContinue();
    }
}
