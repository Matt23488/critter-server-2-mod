package com.critter.entity.ai.goal;

import com.critter.CritterMod;

import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.HostileEntity;

public class RevengeExceptBewitchedHostileEntitiesGoal extends RevengeGoal {
    public RevengeExceptBewitchedHostileEntitiesGoal(HostileEntity hostileEntity) {
        super(hostileEntity, new Class[0]);
    }

    @Override
    public boolean shouldContinue() {
        return !this.mob.hasStatusEffect(CritterMod.BEWITCHED);
    }
}
