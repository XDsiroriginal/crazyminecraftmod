package com.xd.crazymod.init;

import com.xd.crazymod.CrazyMod;
import com.xd.crazymod.block.entity.CoalBoilerBlockEntity;
import com.xd.crazymod.block.entity.GeneratorBlockEntity;
import com.xd.crazymod.block.entity.SteamTurbineBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
	public static BlockEntityType<CoalBoilerBlockEntity> COAL_BOILER_BLOCK_ENTITY;
	public static BlockEntityType<SteamTurbineBlockEntity> STEAM_TURBINE_BLOCK_ENTITY;
	public static BlockEntityType<GeneratorBlockEntity> GENERATOR_BLOCK_ENTITY;

	public static void registerBlockEntities() {
		COAL_BOILER_BLOCK_ENTITY = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				CrazyMod.id("coal_boiler"),
				FabricBlockEntityTypeBuilder.create(CoalBoilerBlockEntity::new, ModBlocks.COAL_BOILER_CONTROLLER).build()
		);

		STEAM_TURBINE_BLOCK_ENTITY = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				CrazyMod.id("steam_turbine"),
				FabricBlockEntityTypeBuilder.create(SteamTurbineBlockEntity::new, ModBlocks.STEAM_TURBINE_CONTROLLER).build()
		);

		GENERATOR_BLOCK_ENTITY = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				CrazyMod.id("generator"),
				FabricBlockEntityTypeBuilder.create(GeneratorBlockEntity::new, ModBlocks.GENERATOR_CONTROLLER).build()
		);

		CrazyMod.LOGGER.info("Registered CrazyMod BlockEntities");
	}
}
