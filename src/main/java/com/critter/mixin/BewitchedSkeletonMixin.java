package com.critter.mixin;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.critter.CritterMod;
import com.critter.entity.ai.goal.AttackHostileEntitiesGoal;
import com.critter.entity.ai.goal.AttackIfNotBewitchedGoal;
import com.critter.entity.ai.goal.FollowPlayerGoal;
import com.critter.entity.ai.goal.RevengeExceptBewitchedHostileEntitiesGoal;
import com.critter.entity.ai.goal.RevengePlayerGoal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(AbstractSkeletonEntity.class)
public abstract class BewitchedSkeletonMixin {
    // private static final Logger LOGGER = LoggerFactory.getLogger(CritterMod.MODID);

    @Redirect(method = "initGoals()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 6))
    private void overrideRevengeGoal(GoalSelector selector, int priority, Goal goal) {
        var skeleton = (AbstractSkeletonEntity)(Object)this;
        selector.add(priority, new RevengeExceptBewitchedHostileEntitiesGoal(skeleton));
    }

    @Redirect(method = "initGoals()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 7))
    private void overrideAttackPlayerGoal(GoalSelector selector, int priority, Goal goal) {
        var skeleton = (AbstractSkeletonEntity)(Object)this;
        
        var attackPlayerIfNotBewitched = new AttackIfNotBewitchedGoal<>(skeleton, PlayerEntity.class);
        selector.add(priority, attackPlayerIfNotBewitched);
    }

    @Redirect(method = "initGoals()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 8))
    private void overrideAttackIronGolemGoal(GoalSelector selector, int priority, Goal goal) {
        var skeleton = (AbstractSkeletonEntity)(Object)this;
        
        var attackIronGolemIfNotBewitched = new AttackIfNotBewitchedGoal<>(skeleton, IronGolemEntity.class);
        selector.add(priority, attackIronGolemIfNotBewitched);
    }

    @Redirect(method = "initGoals()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 9))
    private void overrideAttackTurtleGoalAndExtraGoals(GoalSelector selector, int priority, Goal goal) {
        var skeleton = (AbstractSkeletonEntity)(Object)this;

        var attackTurtleIfNotBewitched = new AttackIfNotBewitchedGoal<>(skeleton, TurtleEntity.class);
        selector.add(priority, attackTurtleIfNotBewitched);

        selector.add(1, new RevengePlayerGoal(skeleton, target -> skeleton.hasStatusEffect(CritterMod.BEWITCHED) && !target.hasStatusEffect(CritterMod.BEWITCHED)));
        selector.add(2, new AttackHostileEntitiesGoal(skeleton, target -> skeleton.hasStatusEffect(CritterMod.BEWITCHED) && !target.hasStatusEffect(CritterMod.BEWITCHED)));

        var followPlayerIfBewitched = new FollowPlayerGoal(skeleton, 1.5, __ -> skeleton.hasStatusEffect(CritterMod.BEWITCHED));
        selector.add(5, followPlayerIfBewitched);
    }
}
