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
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(BlazeEntity.class)
public abstract class BewitchedBlazeMixin {
    // private static final Logger LOGGER = LoggerFactory.getLogger(CritterMod.MODID);

    @Redirect(method = "initGoals()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 5))
    private void overrideRevengeGoal(GoalSelector selector, int priority, Goal goal) {
        var blaze = (BlazeEntity)(Object)this;
        selector.add(priority, new RevengeExceptBewitchedHostileEntitiesGoal(blaze));
    }

    @Redirect(method = "initGoals()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/ai/goal/GoalSelector;add(ILnet/minecraft/entity/ai/goal/Goal;)V", ordinal = 6))
    private void overrideAttackPlayerGoalAndExtraGoals(GoalSelector selector, int priority, Goal goal) {
        var blaze = (BlazeEntity)(Object)this;
        
        var attackPlayerIfNotBewitched = new AttackIfNotBewitchedGoal<>(blaze, PlayerEntity.class);
        selector.add(priority, attackPlayerIfNotBewitched);

        selector.add(1, new RevengePlayerGoal(blaze, target -> blaze.hasStatusEffect(CritterMod.BEWITCHED) && !target.hasStatusEffect(CritterMod.BEWITCHED)));
        selector.add(2, new AttackHostileEntitiesGoal(blaze, target -> blaze.hasStatusEffect(CritterMod.BEWITCHED) && !target.hasStatusEffect(CritterMod.BEWITCHED)));

        var followPlayerIfBewitched = new FollowPlayerGoal(blaze, 1.5, __ -> blaze.hasStatusEffect(CritterMod.BEWITCHED));
        selector.add(5, followPlayerIfBewitched);
    }
}
