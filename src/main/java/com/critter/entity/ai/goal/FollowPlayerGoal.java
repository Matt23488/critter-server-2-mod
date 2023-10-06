package com.critter.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A target goal that finds a target by entity class when the goal starts.
 */
public class FollowPlayerGoal<T extends LivingEntity>
extends Goal {
    private static final TargetPredicate FOLLOW_TARGET_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(32.0).ignoreVisibility();
    private final TargetPredicate predicate;
    private final Predicate<?> goalPredicate;
    protected final PathAwareEntity mob;
    private final double speed;
    @Nullable
    protected PlayerEntity closestPlayer;
    private int cooldown;
    private boolean active;

    public FollowPlayerGoal(PathAwareEntity entity, double speed, Predicate<?> goalPredicate) {
        this.mob = entity;
        this.speed = speed;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
        this.predicate = FOLLOW_TARGET_PREDICATE;
        this.goalPredicate = goalPredicate;
    }

    @Override
    public boolean canStart() {
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }
        this.closestPlayer = this.mob.getWorld().getClosestPlayer(this.predicate, this.mob);
        return this.closestPlayer != null;
    }

    @Override
    public boolean shouldContinue() {
        if (!this.goalPredicate.test(null))
            return false;
        return this.canStart();
    }

    // protected boolean canBeScared() {
    //     return this.canBeScared;
    // }

    @Override
    public void start() {
        this.active = true;
    }

    @Override
    public void stop() {
        this.closestPlayer = null;
        this.mob.getNavigation().stop();
        this.cooldown = TemptGoal.toGoalTicks(100);
        this.active = false;
    }

    @Override
    public void tick() {
        // this.mob.getLookControl().lookAt(this.closestPlayer, this.mob.getMaxHeadRotation() + 20, this.mob.getMaxLookPitchChange());
        if (this.mob.squaredDistanceTo(this.closestPlayer) < 6.25) {
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().startMovingTo(this.closestPlayer, this.speed);
        }
    }

    public boolean isActive() {
        return this.active;
    }
}

