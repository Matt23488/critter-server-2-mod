package com.critter.entity.ai.goal;

import java.util.function.Predicate;

import com.critter.CritterMod;

import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;

public class AttackHostileEntitiesGoal extends ActiveTargetGoal<HostileEntity> {
    private static final TargetPredicate TEMPTING_ENTITY_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(64.0).ignoreVisibility();

    public AttackHostileEntitiesGoal(PathAwareEntity entity) {
        this(entity, __ -> true);
    }
    
    public AttackHostileEntitiesGoal(PathAwareEntity entity, Predicate<HostileEntity> goalPredicate) {
        super(entity, HostileEntity.class, true, e -> e instanceof HostileEntity && goalPredicate.test((HostileEntity)e));
    }

    @Override
    public boolean shouldContinue() {
        var target = this.mob.getTarget();
        if (target == null) {
            return super.shouldContinue();
        }

        if (!this.mob.canTarget(target))
            return false;

        if (this.mob.squaredDistanceTo(target) >= 100.0)
            return false;

        var closestPlayer = this.mob.getWorld().getClosestPlayer(TEMPTING_ENTITY_PREDICATE, this.mob);
        if (closestPlayer == null || this.mob.squaredDistanceTo(closestPlayer) >= 900.0) {
            return false;
        }

        return this.mob.hasStatusEffect(CritterMod.BEWITCHED) && !target.hasStatusEffect(CritterMod.BEWITCHED) && super.shouldContinue();
    }
}
