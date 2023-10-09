package com.critter.entity;

import com.critter.entity.ai.goal.AttackHostileEntitiesGoal;
import com.critter.entity.ai.goal.FollowPlayerGoal;
import com.critter.entity.ai.goal.RevengePlayerGoal;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.world.World;

public class GoatCritterEntity extends GoatEntity {
    public GoatCritterEntity(World world) {
        super(EntityType.GOAT, world);
    }

    @Override
    protected void initGoals() {
        this.targetSelector.add(1, new RevengePlayerGoal(this));
        this.targetSelector.add(2, new AttackHostileEntitiesGoal(this));
        this.targetSelector.add(5, new FollowPlayerGoal(this, 1.5));
    }
}
