package com.xd.crazymod.init;

import com.xd.crazymod.CrazyMod;
import com.xd.crazymod.block.entity.CoalBoilerBlockEntity;
import com.xd.crazymod.block.entity.CoolingTowerBlockEntity;
import com.xd.crazymod.block.entity.GasTurbineBlockEntity;
import com.xd.crazymod.block.entity.GeneratorBlockEntity;
import com.xd.crazymod.block.entity.HRSGBlockEntity;
import com.xd.crazymod.block.entity.OilBoilerBlockEntity;
import com.xd.crazymod.block.entity.SteamTurbineBlockEntity;
import com.xd.crazymod.block.entity.VverReactorBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {
	public static BlockEntityType<CoalBoilerBlockEntity> COAL_BOILER_BLOCK_ENTITY;
	public static BlockEntityType<SteamTurbineBlockEntity> STEAM_TURBINE_BLOCK_ENTITY;
	public static BlockEntityType<GeneratorBlockEntity> GENERATOR_BLOCK_ENTITY;
	public static BlockEntityType<CoolingTowerBlockEntity> COOLING_TOWER_BLOCK_ENTITY;
	public static BlockEntityType<OilBoilerBlockEntity> OIL_BOILER_BLOCK_ENTITY;
	public static BlockEntityType<GasTurbineBlockEntity> GAS_TURBINE_BLOCK_ENTITY;
	public static BlockEntityType<HRSGBlockEntity> HRSG_BLOCK_ENTITY;
	public static BlockEntityType<VverReactorBlockEntity> VVER_REACTOR_BLOCK_ENTITY;

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

		COOLING_TOWER_BLOCK_ENTITY = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				CrazyMod.id("cooling_tower"),
				FabricBlockEntityTypeBuilder.create(CoolingTowerBlockEntity::new, ModBlocks.COOLING_TOWER_CONTROLLER).build()
		);

		OIL_BOILER_BLOCK_ENTITY = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				CrazyMod.id("oil_boiler"),
				FabricBlockEntityTypeBuilder.create(OilBoilerBlockEntity::new, ModBlocks.OIL_BOILER_CONTROLLER).build()
		);

		GAS_TURBINE_BLOCK_ENTITY = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				CrazyMod.id("gas_turbine"),
				FabricBlockEntityTypeBuilder.create(GasTurbineBlockEntity::new, ModBlocks.GAS_TURBINE_CONTROLLER).build()
		);

		HRSG_BLOCK_ENTITY = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				CrazyMod.id("hrsg"),
				FabricBlockEntityTypeBuilder.create(HRSGBlockEntity::new, ModBlocks.HRSG_CONTROLLER).build()
		);

		VVER_REACTOR_BLOCK_ENTITY = Registry.register(
				BuiltInRegistries.BLOCK_ENTITY_TYPE,
				CrazyMod.id("vver_reactor"),
				FabricBlockEntityTypeBuilder.create(VverReactorBlockEntity::new, ModBlocks.VVER_REACTOR_CONTROLLER).build()
		);

		CrazyMod.LOGGER.info("Registered CrazyMod BlockEntities");
	}
}
