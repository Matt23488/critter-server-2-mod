package com.critter;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.critter.command.CritterCommand;
import com.critter.entity.effect.BewitchedHostileEntityStatusEffect;
import com.critter.item.DiamondShard;
import com.critter.item.HuggyWuggy;

public class CritterMod implements ModInitializer {
	public static final String MODID = "critter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static final Block DIAMOND_SHARD_ORE = Registry.register(Registries.BLOCK, CritterMod.identifier("diamond_shard_ore"), new ExperienceDroppingBlock(AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).instrument(Instrument.BASEDRUM).requiresTool().strength(3.0f, 3.0f), UniformIntProvider.create(3, 7)));

	public static final HuggyWuggy HUGGY_WUGGY = Registry.register(Registries.ITEM, CritterMod.identifier("huggy_wuggy"), new HuggyWuggy(new FabricItemSettings().maxCount(16)));
	public static final DiamondShard DIAMOND_SHARD = Registry.register(Registries.ITEM, CritterMod.identifier("diamond_shard"), new DiamondShard(new FabricItemSettings()));

	public static final ItemGroup CRITTER_ITEM_GROUP = Registry.register(Registries.ITEM_GROUP, CritterMod.identifier("critter_item_group"),
		FabricItemGroup.builder()
			.icon(() -> new ItemStack(HUGGY_WUGGY))
			.displayName(Text.translatable("itemGroup.critter.critter_item_group"))
			.entries((context, entries) -> {
				entries.add(HUGGY_WUGGY);
				entries.add(DIAMOND_SHARD);
			})
			.build());


	public static final StatusEffect BEWITCHED = Registry.register(Registries.STATUS_EFFECT, CritterMod.identifier("bewitched"), new BewitchedHostileEntityStatusEffect());

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			CritterCommand.register(dispatcher);
		});
	}

	public static Identifier identifier(String path) {
		return new Identifier(CritterMod.MODID, path);
	}
}