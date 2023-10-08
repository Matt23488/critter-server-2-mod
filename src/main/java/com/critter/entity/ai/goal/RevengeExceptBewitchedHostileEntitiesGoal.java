package com.critter.entity.ai.goal;

import com.critter.CritterMod;

import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;

public class RevengeExceptBewitchedHostileEntitiesGoal extends RevengeGoal {
    public RevengeExceptBewitchedHostileEntitiesGoal(HostileEntity hostileEntity) {
        super(hostileEntity, new Class[0]);
    }

    @Override
    public boolean canStart() {
        var attacker = this.mob.getAttacker();
        if (attacker == null)
            return false;
            
        var precondition = super.canStart();
        if (this.mob.hasStatusEffect(CritterMod.BEWITCHED))
            return !attacker.hasStatusEffect(CritterMod.BEWITCHED) && !(attacker instanceof PlayerEntity) && precondition;
            
        return precondition;
    }

    @Override
    public boolean shouldContinue() {
        var precondition = super.shouldContinue();
        if (this.mob.hasStatusEffect(CritterMod.BEWITCHED))
            return !this.target.hasStatusEffect(CritterMod.BEWITCHED) && precondition;
        
        return precondition;
    }
}
