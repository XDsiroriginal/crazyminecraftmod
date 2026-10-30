package com.xd.crazymod.block.entity;

import com.xd.crazymod.block.CoalBoilerControllerBlock;
import com.xd.crazymod.init.ModBlockEntities;
import com.xd.crazymod.init.ModBlocks;
import com.xd.crazymod.init.ModItems;
import com.xd.crazymod.multiblock.IMultiblockController;
import com.xd.crazymod.multiblock.MultiblockValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class CoalBoilerBlockEntity extends BlockEntity implements IMultiblockController {
	private boolean formed = false;
	private final List<BlockPos> connectedPorts = new ArrayList<>();

	// Thermal & fluid state
	private int temperature = 20; // Ambient temperature in °C
	private int burnTime = 0;
	private int maxBurnTime = 0;
	private int waterStored = 0; // mB (millibuckets)
	private int steamStored = 0; // mB
	private static final int MAX_WATER = 32000;
	private static final int MAX_STEAM = 128000;
	private int coalConsumedCounter = 0;

	public CoalBoilerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.COAL_BOILER_BLOCK_ENTITY, pos, state);
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

	public int getTemperature() {
		return temperature;
	}

	public int getWaterStored() {
		return waterStored;
	}

	public int getSteamStored() {
		return steamStored;
	}

	public void addWater(int amount) {
		this.waterStored = Math.min(MAX_WATER, this.waterStored + amount);
		setChanged();
	}

	public int extractSteam(int maxExtract) {
		int extracted = Math.min(steamStored, maxExtract);
		steamStored -= extracted;
		setChanged();
		return extracted;
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide) return;

		// Periodically verify structure
		if (level.getGameTime() % 80 == 0) {
			MultiblockValidationResult res = checkStructure(level, pos);
			if (res.isSuccess() != formed) {
				formed = res.isSuccess();
				level.setBlock(pos, state.setValue(CoalBoilerControllerBlock.FORMED, formed), Block.UPDATE_ALL);
				setChanged();
			}
		}

		if (!formed) {
			if (temperature > 20 && level.getGameTime() % 20 == 0) {
				temperature--;
				setChanged();
			}
			return;
		}

		boolean isBurning = burnTime > 0;
		if (isBurning) {
			burnTime--;
			if (temperature < 540) {
				temperature += 1;
			}
		} else {
			// Try consume fuel from ports or internal
			if (waterStored > 0) {
				burnTime = 200; // Simulated active cycle when loaded
				maxBurnTime = 200;
				isBurning = true;
			} else if (temperature > 20 && level.getGameTime() % 40 == 0) {
				temperature--;
			}
		}

		// Water boiling into steam
		if (temperature >= 100 && waterStored >= 10) {
			int boilRate = Math.min(waterStored, Math.max(1, (temperature - 100) / 20));
			int steamProduced = boilRate * 10; // Steam expansion
			if (steamStored + steamProduced <= MAX_STEAM) {
				waterStored -= boilRate;
				steamStored += steamProduced;
			}
		}

		boolean currentLit = state.getValue(CoalBoilerControllerBlock.LIT);
		if (currentLit != isBurning) {
			level.setBlock(pos, state.setValue(CoalBoilerControllerBlock.LIT, isBurning), Block.UPDATE_ALL);
		}

		setChanged();
	}

	@Override
	public MultiblockValidationResult checkStructure(Level level, BlockPos controllerPos) {
		connectedPorts.clear();

		Direction facing = getBlockState().getValue(CoalBoilerControllerBlock.FACING);
		Direction back = facing.getOpposite();
		Direction right = facing.getClockWise();

		// Center of 3x3x4 boiler behind controller
		BlockPos centerBase = controllerPos.relative(back);

		boolean hasFluidIn = false;
		boolean hasFluidOut = false;
		boolean hasItemIn = false;
		boolean hasItemOut = false;

		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				for (int dy = 0; dy < 4; dy++) {
					BlockPos cur = centerBase.relative(right, dx).relative(back, dz).above(dy);
					if (cur.equals(controllerPos)) continue; // Controller itself

					boolean isInner = (dx == 0 && dz == 0 && (dy == 1 || dy == 2));
					BlockState blockState = level.getBlockState(cur);

					if (isInner) {
						// Interior must be air or fire/heat
						if (!blockState.isAir() && !blockState.is(Blocks.FIRE)) {
							return MultiblockValidationResult.error(
									Component.translatable("multiblock.crazymod.error.interior_not_empty", cur.toShortString()), cur);
						}
					} else {
						// Outer wall, floor or ceiling
						if (isValidBoilerComponent(blockState)) {
							if (blockState.is(ModBlocks.FLUID_INPUT_PORT)) {
								hasFluidIn = true;
								connectedPorts.add(cur);
							} else if (blockState.is(ModBlocks.FLUID_OUTPUT_PORT)) {
								hasFluidOut = true;
								connectedPorts.add(cur);
							} else if (blockState.is(ModBlocks.ITEM_INPUT_PORT)) {
								hasItemIn = true;
								connectedPorts.add(cur);
							} else if (blockState.is(ModBlocks.ITEM_OUTPUT_PORT)) {
								hasItemOut = true;
								connectedPorts.add(cur);
							}
						} else {
							return MultiblockValidationResult.error(
									Component.translatable("multiblock.crazymod.error.invalid_block", cur.toShortString()), cur);
						}
					}
				}
			}
		}

		if (!hasFluidIn || !hasFluidOut) {
			return MultiblockValidationResult.error(
					Component.translatable("multiblock.crazymod.error.missing_fluid_ports"), controllerPos);
		}

		return MultiblockValidationResult.ok();
	}

	private boolean isValidBoilerComponent(BlockState state) {
		return state.is(ModBlocks.BOILER_CASING) ||
				state.is(ModBlocks.BOILER_PIPE_CASING) ||
				state.is(ModBlocks.FIREBRICK_BLOCK) ||
				state.is(ModBlocks.FLUID_INPUT_PORT) ||
				state.is(ModBlocks.FLUID_OUTPUT_PORT) ||
				state.is(ModBlocks.ITEM_INPUT_PORT) ||
				state.is(ModBlocks.ITEM_OUTPUT_PORT);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putBoolean("Formed", formed);
		tag.putInt("Temperature", temperature);
		tag.putInt("BurnTime", burnTime);
		tag.putInt("MaxBurnTime", maxBurnTime);
		tag.putInt("WaterStored", waterStored);
		tag.putInt("SteamStored", steamStored);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.formed = tag.getBoolean("Formed");
		this.temperature = tag.getInt("Temperature");
		this.burnTime = tag.getInt("BurnTime");
		this.maxBurnTime = tag.getInt("MaxBurnTime");
		this.waterStored = tag.getInt("WaterStored");
		this.steamStored = tag.getInt("SteamStored");
	}
}
