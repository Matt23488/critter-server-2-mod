package com.critter.item;

import java.util.List;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;

public class DiamondShard extends Item {

    public DiamondShard(Settings settings) {
        super(settings);
    }

    // @Override
    // public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
    //     playerEntity.playSound(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, 1.0F, 1.0F);
    //     return TypedActionResult.success(playerEntity.getStackInHand(hand));
    // }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.critter.diamond_shard.tooltip"));
    }
}
