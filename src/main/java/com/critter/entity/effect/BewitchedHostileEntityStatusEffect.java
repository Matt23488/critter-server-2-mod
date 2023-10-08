package com.critter.entity.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class BewitchedHostileEntityStatusEffect extends StatusEffect {
    public BewitchedHostileEntityStatusEffect() {
        super(StatusEffectCategory.BENEFICIAL, 3939635);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        // We can disable all the checking since this doesn't do anything directly to
        // the affected entity. Instead it's interacted with via other systems
        // querying for this effect.
        return false;
    }
}
