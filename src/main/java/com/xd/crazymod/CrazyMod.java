package com.xd.crazymod;

import com.xd.crazymod.init.ModBlockEntities;
import com.xd.crazymod.init.ModBlocks;
import com.xd.crazymod.init.ModItemGroups;
import com.xd.crazymod.init.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrazyMod implements ModInitializer {
	public static final String MOD_ID = "crazymod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Crazy Minecraft Mod - Realistic Thermal & Nuclear Power Plants");

		ModBlocks.registerBlocks();
		ModBlockEntities.registerBlockEntities();
		ModItems.registerItems();
		ModItemGroups.registerItemGroups();

		LOGGER.info("Crazy Minecraft Mod initialized successfully!");
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
