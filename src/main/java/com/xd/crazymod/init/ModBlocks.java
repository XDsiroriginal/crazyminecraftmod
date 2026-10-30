package com.xd.crazymod.init;

import com.xd.crazymod.CrazyMod;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

public class ModBlocks {
	// Structural Blocks
	public static final Block REINFORCED_CONCRETE = registerBlock("reinforced_concrete",
			new Block(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).strength(15.0f, 1200.0f).sound(SoundType.STONE)));

	public static final Block FIREBRICK_BLOCK = registerBlock("firebrick_block",
			new Block(FabricBlockSettings.copyOf(Blocks.BRICKS).strength(4.0f, 20.0f).sound(SoundType.NETHER_BRICKS)));

	public static final Block BOILER_CASING = registerBlock("boiler_casing",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(6.0f, 30.0f).sound(SoundType.METAL)));

	public static final Block BOILER_PIPE_CASING = registerBlock("boiler_pipe_casing",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(5.0f, 20.0f).sound(SoundType.METAL)));

	public static final Block TURBINE_CASING = registerBlock("turbine_casing",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(7.0f, 40.0f).sound(SoundType.NETHERITE_BLOCK)));

	public static final Block GENERATOR_CASING = registerBlock("generator_casing",
			new Block(FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).strength(6.0f, 30.0f).sound(SoundType.COPPER)));

	public static final Block REACTOR_PRESSURE_VESSEL_BLOCK = registerBlock("reactor_pressure_vessel_block",
			new Block(FabricBlockSettings.copyOf(Blocks.NETHERITE_BLOCK).strength(25.0f, 2000.0f).sound(SoundType.NETHERITE_BLOCK)));

	public static final Block REACTOR_CONTROL_ROD_MECHANISM = registerBlock("reactor_control_rod_mechanism",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(8.0f, 50.0f).sound(SoundType.ANVIL)));

	public static final Block CONTAINMENT_BLOCK = registerBlock("containment_block",
			new Block(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).strength(20.0f, 1500.0f).sound(SoundType.STONE)));

	public static final Block COOLING_TOWER_CASING = registerBlock("cooling_tower_casing",
			new Block(FabricBlockSettings.copyOf(Blocks.STONE).strength(4.0f, 15.0f).sound(SoundType.STONE)));

	public static final Block COOLING_TOWER_FILL = registerBlock("cooling_tower_fill",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BARS).strength(2.0f, 5.0f).sound(SoundType.SCAFFOLDING)));

	// Multiblock Controllers
	public static final Block COAL_BOILER_CONTROLLER = registerBlock("coal_boiler_controller",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(5.0f, 30.0f).sound(SoundType.METAL)));

	public static final Block OIL_BOILER_CONTROLLER = registerBlock("oil_boiler_controller",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(5.0f, 30.0f).sound(SoundType.METAL)));

	public static final Block GAS_TURBINE_CONTROLLER = registerBlock("gas_turbine_controller",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(6.0f, 40.0f).sound(SoundType.METAL)));

	public static final Block HRSG_CONTROLLER = registerBlock("hrsg_controller",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(5.0f, 30.0f).sound(SoundType.METAL)));

	public static final Block VVER_REACTOR_CONTROLLER = registerBlock("vver_reactor_controller",
			new Block(FabricBlockSettings.copyOf(Blocks.NETHERITE_BLOCK).strength(15.0f, 1000.0f).sound(SoundType.METAL)));

	public static final Block STEAM_TURBINE_CONTROLLER = registerBlock("steam_turbine_controller",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(6.0f, 40.0f).sound(SoundType.METAL)));

	public static final Block GENERATOR_CONTROLLER = registerBlock("generator_controller",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(6.0f, 40.0f).sound(SoundType.COPPER)));

	public static final Block COOLING_TOWER_CONTROLLER = registerBlock("cooling_tower_controller",
			new Block(FabricBlockSettings.copyOf(Blocks.STONE).strength(4.0f, 15.0f).sound(SoundType.STONE)));

	// Multiblock Input/Output Ports
	public static final Block FLUID_INPUT_PORT = registerBlock("fluid_input_port",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(4.0f, 20.0f).sound(SoundType.METAL)));

	public static final Block FLUID_OUTPUT_PORT = registerBlock("fluid_output_port",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(4.0f, 20.0f).sound(SoundType.METAL)));

	public static final Block ITEM_INPUT_PORT = registerBlock("item_input_port",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(4.0f, 20.0f).sound(SoundType.METAL)));

	public static final Block ITEM_OUTPUT_PORT = registerBlock("item_output_port",
			new Block(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).strength(4.0f, 20.0f).sound(SoundType.METAL)));

	public static final Block ENERGY_OUTPUT_PORT = registerBlock("energy_output_port",
			new Block(FabricBlockSettings.copyOf(Blocks.COPPER_BLOCK).strength(5.0f, 30.0f).sound(SoundType.COPPER)));

	private static Block registerBlock(String name, Block block) {
		Registry.register(BuiltInRegistries.BLOCK, CrazyMod.id(name), block);
		Registry.register(BuiltInRegistries.ITEM, CrazyMod.id(name), new BlockItem(block, new Item.Properties()));
		return block;
	}

	public static void registerBlocks() {
		CrazyMod.LOGGER.info("Registered CrazyMod blocks");
	}
}
