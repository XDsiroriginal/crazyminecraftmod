package com.xd.crazymod.init;

import com.xd.crazymod.CrazyMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class ModItems {
	// Raw Materials & Waste
	public static final Item COAL_DUST = registerItem("coal_dust", new Item(new Item.Properties()));
	public static final Item BOILER_SLAG = registerItem("boiler_slag", new Item(new Item.Properties()));
	public static final Item FLY_ASH = registerItem("fly_ash", new Item(new Item.Properties()));
	public static final Item FIREBRICK = registerItem("firebrick", new Item(new Item.Properties()));

	// Nuclear Materials
	public static final Item RAW_URANIUM = registerItem("raw_uranium", new Item(new Item.Properties()));
	public static final Item URANIUM_INGOT = registerItem("uranium_ingot", new Item(new Item.Properties()));
	public static final Item YELLOWCAKE = registerItem("yellowcake", new Item(new Item.Properties()));
	public static final Item URANIUM_DIOXIDE_PELLET = registerItem("uranium_dioxide_pellet", new Item(new Item.Properties()));
	public static final Item ZIRCONIUM_INGOT = registerItem("zirconium_ingot", new Item(new Item.Properties()));
	public static final Item FUEL_ROD = registerItem("fuel_rod", new Item(new Item.Properties()));
	public static final Item FUEL_ASSEMBLY = registerItem("fuel_assembly", new Item(new Item.Properties().stacksTo(1)));
	public static final Item DEPLETED_FUEL_ASSEMBLY = registerItem("depleted_fuel_assembly", new Item(new Item.Properties().stacksTo(1)));
	public static final Item CONTROL_ROD = registerItem("control_rod", new Item(new Item.Properties().stacksTo(16)));

	// Electrical & Mechanical Components
	public static final Item COPPER_COIL = registerItem("copper_coil", new Item(new Item.Properties()));
	public static final Item TURBINE_ROTOR_BLADE = registerItem("turbine_rotor_blade", new Item(new Item.Properties()));

	private static Item registerItem(String name, Item item) {
		return Registry.register(BuiltInRegistries.ITEM, CrazyMod.id(name), item);
	}

	public static void registerItems() {
		CrazyMod.LOGGER.info("Registered CrazyMod items");
	}
}
