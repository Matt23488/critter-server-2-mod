package com.critter.mixin;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.critter.CritterMod;
import com.critter.entity.ai.goal.AttackHostileEntitiesIfBewitchedGoal;
import com.critter.entity.ai.goal.AttackPlayerIfNotBewitchedGoal;
import com.critter.entity.ai.goal.FollowPlayerGoal;
import com.critter.entity.ai.goal.RevengeExceptBewitchedHostileEntitiesGoal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.AbstractSkeletonEntity;

@Mixin(AbstractSkeletonEntity.class)
public abstract class BewitchedSkeletonMixin {
    // private static final Logger LOGGER = LoggerFactory.getLogger("critter");

    @Redirect(method = "initGoals()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 6))
    private void overrideRevengeGoal(GoalSelector selector, int priority, Goal goal) {
        AbstractSkeletonEntity skeleton = (AbstractSkeletonEntity)(Object)this;
        selector.add(priority, new RevengeExceptBewitchedHostileEntitiesGoal(skeleton));
    }

    @Redirect(method = "initGoals()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 7))
    private void overrideAttackPlayerGoal(GoalSelector selector, int priority, Goal goal) {
        AbstractSkeletonEntity skeleton = (AbstractSkeletonEntity)(Object)this;
        
        Goal attackPlayerIfNotBewitched = new AttackPlayerIfNotBewitchedGoal(skeleton);
        selector.add(priority, attackPlayerIfNotBewitched);
    }

    @Redirect(method = "initGoals()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 9))
    private void extraGoals(GoalSelector selector, int priority, Goal goal) {
        AbstractSkeletonEntity skeleton = (AbstractSkeletonEntity)(Object)this;

        // We just wanted a reference to the GoalSelector, so I'm going to apply the original method call
        selector.add(priority, goal);

        selector.add(1, new AttackHostileEntitiesIfBewitchedGoal(skeleton));

        Goal followPlayerIfBewitched = new FollowPlayerGoal<>(skeleton, 1, __ -> skeleton.hasStatusEffect(CritterMod.BEWITCHED));
        selector.add(5, followPlayerIfBewitched);
    }
}
