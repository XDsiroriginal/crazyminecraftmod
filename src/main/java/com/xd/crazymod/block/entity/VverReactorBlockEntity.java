package com.xd.crazymod.block.entity;

import com.xd.crazymod.block.VverReactorControllerBlock;
import com.xd.crazymod.init.ModBlockEntities;
import com.xd.crazymod.init.ModBlocks;
import com.xd.crazymod.multiblock.IMultiblockController;
import com.xd.crazymod.multiblock.MultiblockValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class VverReactorBlockEntity extends BlockEntity implements IMultiblockController {
	private boolean formed = false;
	private final List<BlockPos> connectedPorts = new ArrayList<>();

	// Nuclear physics state
	private boolean scrammed = true; // Emergency protection АЗ-5 initially engaged
	private int controlRodInsertion = 100; // 100% = fully inserted (subcritical)
	private int coreTemperature = 280; // Operating range: 280 - 330 °C
	private int thermalPower = 0; // MWth
	private int primaryCoolant = 32000; // Pressurized borated water (16 MPa)
	private int secondaryWater = 0; // Feed water for steam generation
	private int steamStored = 0;
	private int steamOutputRate = 0;
	private static final int MAX_FLUID = 64000;
	private static final int MAX_STEAM = 256000;

	public VverReactorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.VVER_REACTOR_BLOCK_ENTITY, pos, state);
	}

	@Override
	public boolean isFormed() {
		return formed;
	}

	@Override
	public void setFormed(boolean formed) {
		this.formed = formed;
		setChanged();
	}

	@Override
	public List<BlockPos> getConnectedPorts() {
		return connectedPorts;
	}

	public boolean isScrammed() {
		return scrammed;
	}

	public void toggleScram() {
		scrammed = !scrammed;
		controlRodInsertion = scrammed ? 100 : 25; // 25% inserted = nominal critical power
		setChanged();
	}

	public int getCoreTemperature() {
		return coreTemperature;
	}

	public int getThermalPower() {
		return thermalPower;
	}

	public int getSteamOutputRate() {
		return steamOutputRate;
	}

	public void addSecondaryWater(int amount) {
		this.secondaryWater = Math.min(MAX_FLUID, this.secondaryWater + amount);
		setChanged();
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide) return;

		if (!formed) {
			thermalPower = 0;
			return;
		}

		// Nuclear chain reaction calculation
		if (!scrammed && controlRodInsertion < 90) {
			// Thermal power scales with rod withdrawal (100% - insertion)
			thermalPower = (100 - controlRodInsertion) * 32; // up to ~3200 MWth nominal VVER-1200
			if (coreTemperature < 325) {
				coreTemperature++;
			}
		} else {
			thermalPower = 0;
			if (coreTemperature > 280 && level.getGameTime() % 20 == 0) {
				coreTemperature--;
			}
		}

		// Steam Generator (ПГВ) heat exchange: 1st loop heats 2nd loop
		if (coreTemperature >= 280 && secondaryWater >= 20) {
			int boilRate = Math.min(secondaryWater, Math.max(10, thermalPower / 25));
			secondaryWater -= boilRate;
			steamOutputRate = boilRate * 16;
			steamStored = Math.min(MAX_STEAM, steamStored + steamOutputRate);
		} else {
			steamOutputRate = 0;
		}

		boolean isCritical = thermalPower > 500;
		if (state.getValue(VverReactorControllerBlock.CRITICAL) != isCritical) {
			level.setBlock(pos, state.setValue(VverReactorControllerBlock.CRITICAL, isCritical), Block.UPDATE_ALL);
		}

		setChanged();
	}

	@Override
	public MultiblockValidationResult checkStructure(Level level, BlockPos controllerPos) {
		connectedPorts.clear();

		Direction facing = getBlockState().getValue(VverReactorControllerBlock.FACING);
		Direction back = facing.getOpposite();
		Direction right = facing.getClockWise();

		BlockPos centerBase = controllerPos.relative(back, 2);

		boolean hasFluidIn = false;
		boolean hasFluidOut = false;
		boolean hasControlRodMech = false;

		// 5x5 base, height 7
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				for (int dy = 0; dy < 7; dy++) {
					BlockPos cur = centerBase.relative(right, dx).relative(back, dz).above(dy);
					if (cur.equals(controllerPos)) continue;

					boolean isPerimeter = (Math.abs(dx) == 2 || Math.abs(dz) == 2);
					BlockState blockState = level.getBlockState(cur);

					if (dy == 6 && !isPerimeter) {
						// Top head with CRDM control rod drive mechanisms
						if (blockState.is(ModBlocks.REACTOR_CONTROL_ROD_MECHANISM)) {
							hasControlRodMech = true;
						} else if (!isValidVverComponent(blockState)) {
							return MultiblockValidationResult.error(
									Component.translatable("multiblock.crazymod.error.missing_crdm", cur.toShortString()), cur);
						}
					} else if (!isPerimeter && dy > 0 && dy < 6) {
						// Core interior: must be air/fluid/containment
						if (!blockState.isAir() && !isValidVverComponent(blockState)) {
							return MultiblockValidationResult.error(
									Component.translatable("multiblock.crazymod.error.interior_not_empty", cur.toShortString()), cur);
						}
					} else {
						// Outer reactor pressure vessel / containment
						if (isValidVverComponent(blockState)) {
							if (blockState.is(ModBlocks.FLUID_INPUT_PORT)) {
								hasFluidIn = true;
								connectedPorts.add(cur);
							} else if (blockState.is(ModBlocks.FLUID_OUTPUT_PORT)) {
								hasFluidOut = true;
								connectedPorts.add(cur);
							}
						} else {
							return MultiblockValidationResult.error(
									Component.translatable("multiblock.crazymod.error.invalid_vver_block", cur.toShortString()), cur);
						}
					}
				}
			}
		}

		if (!hasFluidIn || !hasFluidOut) {
			return MultiblockValidationResult.error(
					Component.translatable("multiblock.crazymod.error.missing_fluid_ports"), controllerPos);
		}

		if (!hasControlRodMech) {
			return MultiblockValidationResult.error(
					Component.translatable("multiblock.crazymod.error.missing_crdm_head"), controllerPos);
		}

		return MultiblockValidationResult.ok();
	}

	private boolean isValidVverComponent(BlockState state) {
		return state.is(ModBlocks.REACTOR_PRESSURE_VESSEL_BLOCK) ||
				state.is(ModBlocks.CONTAINMENT_BLOCK) ||
				state.is(ModBlocks.REINFORCED_CONCRETE) ||
				state.is(ModBlocks.REACTOR_CONTROL_ROD_MECHANISM) ||
				state.is(ModBlocks.FLUID_INPUT_PORT) ||
				state.is(ModBlocks.FLUID_OUTPUT_PORT) ||
				state.is(ModBlocks.ITEM_INPUT_PORT) ||
				state.is(ModBlocks.ITEM_OUTPUT_PORT);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putBoolean("Formed", formed);
		tag.putBoolean("Scrammed", scrammed);
		tag.putInt("ControlRodInsertion", controlRodInsertion);
		tag.putInt("CoreTemperature", coreTemperature);
		tag.putInt("ThermalPower", thermalPower);
		tag.putInt("SecondaryWater", secondaryWater);
		tag.putInt("SteamStored", steamStored);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.formed = tag.getBoolean("Formed");
		this.scrammed = tag.getBoolean("Scrammed");
		this.controlRodInsertion = tag.getInt("ControlRodInsertion");
		this.coreTemperature = tag.getInt("CoreTemperature");
		this.thermalPower = tag.getInt("ThermalPower");
		this.secondaryWater = tag.getInt("SecondaryWater");
		this.steamStored = tag.getInt("SteamStored");
	}
}
