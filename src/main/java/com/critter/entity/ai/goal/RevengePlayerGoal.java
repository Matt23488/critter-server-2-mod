package com.critter.entity.ai.goal;

import java.util.function.Predicate;

import com.critter.CritterMod;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public class RevengePlayerGoal extends RevengeGoal {
    private final Predicate<LivingEntity> predicate;

    public RevengePlayerGoal(PathAwareEntity entity) {
        this(entity, __ -> true);
    }
    
    public RevengePlayerGoal(PathAwareEntity entity, Predicate<LivingEntity> predicate) {
        super(entity, new Class[0]);
        this.predicate = predicate;
    }

    @Override
    public boolean canStart() {
        var player = this.mob.getWorld().getClosestPlayer(this.mob, 32.0);
        if (player == null)
            return false;

        var attacker = player.getAttacker();
        if (attacker == null)
            return false;

        if (!this.predicate.test(attacker))
            return false;

        return this.mob.canTarget(attacker);// && attacker instanceof HostileEntity && !attacker.hasStatusEffect(CritterMod.BEWITCHED);
    }

    @Override
    public void start() {
        super.start();

        var player = this.mob.getWorld().getClosestPlayer(this.mob, 32.0);
        this.mob.setTarget(player.getAttacker());
        this.target = this.mob.getTarget();
    }

    @Override
    public boolean shouldContinue() {
        var precondition = super.shouldContinue();

        var player = this.mob.getWorld().getClosestPlayer(this.mob, 32.0);
        if (player == null)
            return precondition;

        var attacker = player.getAttacker();
        if (attacker == null)
            return precondition;

        if (!this.predicate.test(attacker))
            return false;

        return this.target == attacker && !attacker.hasStatusEffect(CritterMod.BEWITCHED) && precondition;
    }
}
