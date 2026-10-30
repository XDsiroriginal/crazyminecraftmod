package com.xd.crazymod.block.entity;

import com.xd.crazymod.block.OilBoilerControllerBlock;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class OilBoilerBlockEntity extends BlockEntity implements IMultiblockController {
	private boolean formed = false;
	private final List<BlockPos> connectedPorts = new ArrayList<>();

	private int temperature = 20;
	private int fuelStored = 0; // mB of heavy fuel oil
	private int waterStored = 0;
	private int steamStored = 0;
	private static final int MAX_FLUID = 32000;
	private static final int MAX_STEAM = 128000;

	public OilBoilerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.OIL_BOILER_BLOCK_ENTITY, pos, state);
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

	public int getFuelStored() {
		return fuelStored;
	}

	public int getSteamStored() {
		return steamStored;
	}

	public void addFuel(int amount) {
		this.fuelStored = Math.min(MAX_FLUID, this.fuelStored + amount);
		setChanged();
	}

	public void addWater(int amount) {
		this.waterStored = Math.min(MAX_FLUID, this.waterStored + amount);
		setChanged();
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide) return;

		if (!formed) {
			if (temperature > 20 && level.getGameTime() % 20 == 0) {
				temperature--;
				setChanged();
			}
			return;
		}

		boolean isBurning = false;
		if (waterStored > 0) {
			isBurning = true;
			// Fast heating: fuel oil burns very hot (up to 600°C)
			if (temperature < 600) {
				temperature += 2;
			}
		} else if (temperature > 20 && level.getGameTime() % 40 == 0) {
			temperature--;
		}

		// Steam production
		if (temperature >= 100 && waterStored >= 10) {
			int boilRate = Math.min(waterStored, Math.max(1, (temperature - 100) / 15));
			int steamProduced = boilRate * 12;
			if (steamStored + steamProduced <= MAX_STEAM) {
				waterStored -= boilRate;
				steamStored += steamProduced;
			}
		}

		boolean currentLit = state.getValue(OilBoilerControllerBlock.LIT);
		if (currentLit != isBurning) {
			level.setBlock(pos, state.setValue(OilBoilerControllerBlock.LIT, isBurning), Block.UPDATE_ALL);
		}

		setChanged();
	}

	@Override
	public MultiblockValidationResult checkStructure(Level level, BlockPos controllerPos) {
		connectedPorts.clear();

		Direction facing = getBlockState().getValue(OilBoilerControllerBlock.FACING);
		Direction back = facing.getOpposite();
		Direction right = facing.getClockWise();

		BlockPos centerBase = controllerPos.relative(back);

		boolean hasFluidIn = false;
		boolean hasFluidOut = false;

		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				for (int dy = 0; dy < 4; dy++) {
					BlockPos cur = centerBase.relative(right, dx).relative(back, dz).above(dy);
					if (cur.equals(controllerPos)) continue;

					boolean isInner = (dx == 0 && dz == 0 && (dy == 1 || dy == 2));
					BlockState blockState = level.getBlockState(cur);

					if (isInner) {
						if (!blockState.isAir() && !blockState.is(Blocks.FIRE)) {
							return MultiblockValidationResult.error(
									Component.translatable("multiblock.crazymod.error.interior_not_empty", cur.toShortString()), cur);
						}
					} else {
						if (isValidBoilerComponent(blockState)) {
							if (blockState.is(ModBlocks.FLUID_INPUT_PORT)) {
								hasFluidIn = true;
								connectedPorts.add(cur);
							} else if (blockState.is(ModBlocks.FLUID_OUTPUT_PORT)) {
								hasFluidOut = true;
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
				state.is(ModBlocks.FLUID_OUTPUT_PORT);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putBoolean("Formed", formed);
		tag.putInt("Temperature", temperature);
		tag.putInt("FuelStored", fuelStored);
		tag.putInt("WaterStored", waterStored);
		tag.putInt("SteamStored", steamStored);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.formed = tag.getBoolean("Formed");
		this.temperature = tag.getInt("Temperature");
		this.fuelStored = tag.getInt("FuelStored");
		this.waterStored = tag.getInt("WaterStored");
		this.steamStored = tag.getInt("SteamStored");
	}
}
