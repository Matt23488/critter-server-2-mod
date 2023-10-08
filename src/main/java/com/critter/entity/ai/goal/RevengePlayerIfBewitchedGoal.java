package com.critter.entity.ai.goal;

import com.critter.CritterMod;

import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.HostileEntity;

public class RevengePlayerIfBewitchedGoal extends RevengeGoal {
    public RevengePlayerIfBewitchedGoal(HostileEntity hostileEntity) {
        super(hostileEntity, new Class[0]);
    }

    @Override
    public boolean canStart() {
        if (!this.mob.hasStatusEffect(CritterMod.BEWITCHED))
            return false;

        var player = this.mob.getWorld().getClosestPlayer(this.mob, 32.0);
        if (player == null)
            return false;

        var attacker = player.getAttacker();
        if (attacker == null)
            return false;

        return this.mob.canTarget(attacker) && attacker instanceof HostileEntity && !attacker.hasStatusEffect(CritterMod.BEWITCHED);
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
        if (!this.mob.hasStatusEffect(CritterMod.BEWITCHED))
            return false;

        var precondition = super.shouldContinue();

        var player = this.mob.getWorld().getClosestPlayer(this.mob, 32.0);
        if (player == null)
            return precondition;

        var attacker = player.getAttacker();
        if (attacker == null)
            return precondition;

        return this.target == attacker && !attacker.hasStatusEffect(CritterMod.BEWITCHED) && precondition;
    }
}
