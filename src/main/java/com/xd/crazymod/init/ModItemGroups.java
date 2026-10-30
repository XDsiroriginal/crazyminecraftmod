package com.xd.crazymod.init;

import com.xd.crazymod.CrazyMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups {
	public static CreativeModeTab CRAZY_MOD_TAB;

	public static void registerItemGroups() {
		CRAZY_MOD_TAB = Registry.register(
				BuiltInRegistries.CREATIVE_MODE_TAB,
				CrazyMod.id("crazymod_tab"),
				FabricItemGroup.builder()
						.title(Component.translatable("itemGroup.crazymod.tab"))
						.icon(() -> new ItemStack(ModBlocks.REINFORCED_CONCRETE))
						.displayItems((parameters, output) -> {
							// Construction Blocks
							output.accept(ModBlocks.REINFORCED_CONCRETE);
							output.accept(ModBlocks.FIREBRICK_BLOCK);
							output.accept(ModBlocks.BOILER_CASING);
							output.accept(ModBlocks.BOILER_PIPE_CASING);
							output.accept(ModBlocks.TURBINE_CASING);
							output.accept(ModBlocks.GENERATOR_CASING);
							output.accept(ModBlocks.REACTOR_PRESSURE_VESSEL_BLOCK);
							output.accept(ModBlocks.REACTOR_CONTROL_ROD_MECHANISM);
							output.accept(ModBlocks.CONTAINMENT_BLOCK);
							output.accept(ModBlocks.COOLING_TOWER_CASING);
							output.accept(ModBlocks.COOLING_TOWER_FILL);

							// Controllers
							output.accept(ModBlocks.COAL_BOILER_CONTROLLER);
							output.accept(ModBlocks.OIL_BOILER_CONTROLLER);
							output.accept(ModBlocks.GAS_TURBINE_CONTROLLER);
							output.accept(ModBlocks.HRSG_CONTROLLER);
							output.accept(ModBlocks.VVER_REACTOR_CONTROLLER);
							output.accept(ModBlocks.STEAM_TURBINE_CONTROLLER);
							output.accept(ModBlocks.GENERATOR_CONTROLLER);
							output.accept(ModBlocks.COOLING_TOWER_CONTROLLER);

							// Ports
							output.accept(ModBlocks.FLUID_INPUT_PORT);
							output.accept(ModBlocks.FLUID_OUTPUT_PORT);
							output.accept(ModBlocks.ITEM_INPUT_PORT);
							output.accept(ModBlocks.ITEM_OUTPUT_PORT);
							output.accept(ModBlocks.ENERGY_OUTPUT_PORT);

							// Piping & Cables
							output.accept(ModBlocks.STEAM_PIPE);
							output.accept(ModBlocks.WATER_PIPE);
							output.accept(ModBlocks.ENERGY_CABLE);

							// Items & Materials
							output.accept(ModItems.COAL_DUST);
							output.accept(ModItems.BOILER_SLAG);
							output.accept(ModItems.FLY_ASH);
							output.accept(ModItems.RAW_URANIUM);
							output.accept(ModItems.URANIUM_INGOT);
							output.accept(ModItems.YELLOWCAKE);
							output.accept(ModItems.URANIUM_DIOXIDE_PELLET);
							output.accept(ModItems.ZIRCONIUM_INGOT);
							output.accept(ModItems.FUEL_ROD);
							output.accept(ModItems.FUEL_ASSEMBLY);
							output.accept(ModItems.DEPLETED_FUEL_ASSEMBLY);
							output.accept(ModItems.CONTROL_ROD);
							output.accept(ModItems.COPPER_COIL);
							output.accept(ModItems.TURBINE_ROTOR_BLADE);
							output.accept(ModItems.FIREBRICK);
						})
						.build()
		);
	}
}
