package com.xd.crazymod.block.entity;

import com.xd.crazymod.block.GasTurbineControllerBlock;
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

public class GasTurbineBlockEntity extends BlockEntity implements IMultiblockController {
	private boolean formed = false;
	private final List<BlockPos> connectedPorts = new ArrayList<>();

	private int rpm = 0;
	private int gasStored = 0; // mB of natural gas
	private int exhaustTemp = 20; // °C
	private static final int MAX_GAS = 32000;

	public GasTurbineBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.GAS_TURBINE_BLOCK_ENTITY, pos, state);
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

	public int getRpm() {
		return rpm;
	}

	public int getExhaustTemp() {
		return exhaustTemp;
	}

	public void addNaturalGas(int amount) {
		this.gasStored = Math.min(MAX_GAS, this.gasStored + amount);
		setChanged();
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide) return;

		if (!formed) {
			if (rpm > 0) {
				rpm = Math.max(0, rpm - 30);
				setChanged();
			}
			return;
		}

		// Brayton combustion cycle
		if (gasStored > 0 || level.getGameTime() % 10 == 0) {
			if (rpm < 3000) {
				rpm = Math.min(3000, rpm + 40);
			}
			exhaustTemp = 580; // Standard gas turbine exhaust temp
		} else {
			if (rpm > 0) rpm = Math.max(0, rpm - 15);
			if (exhaustTemp > 20) exhaustTemp = Math.max(20, exhaustTemp - 2);
		}

		boolean isRunning = rpm > 500;
		if (state.getValue(GasTurbineControllerBlock.RUNNING) != isRunning) {
			level.setBlock(pos, state.setValue(GasTurbineControllerBlock.RUNNING, isRunning), Block.UPDATE_ALL);
		}

		setChanged();
	}

	@Override
	public MultiblockValidationResult checkStructure(Level level, BlockPos controllerPos) {
		connectedPorts.clear();

		Direction facing = getBlockState().getValue(GasTurbineControllerBlock.FACING);
		Direction back = facing.getOpposite();
		Direction right = facing.getClockWise();

		BlockPos centerBase = controllerPos.relative(back);

		boolean hasFluidIn = false;
		boolean hasFluidOut = false;

		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = 0; dz < 4; dz++) {
				for (int dy = 0; dy < 3; dy++) {
					BlockPos cur = centerBase.relative(right, dx).relative(back, dz).above(dy);
					if (cur.equals(controllerPos)) continue;

					BlockState blockState = level.getBlockState(cur);
					if (isValidGasTurbineComponent(blockState)) {
						if (blockState.is(ModBlocks.FLUID_INPUT_PORT)) {
							hasFluidIn = true;
							connectedPorts.add(cur);
						} else if (blockState.is(ModBlocks.FLUID_OUTPUT_PORT)) {
							hasFluidOut = true;
							connectedPorts.add(cur);
						}
					} else {
						return MultiblockValidationResult.error(
								Component.translatable("multiblock.crazymod.error.invalid_turbine_block", cur.toShortString()), cur);
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

	private boolean isValidGasTurbineComponent(BlockState state) {
		return state.is(ModBlocks.TURBINE_CASING) ||
				state.is(ModBlocks.BOILER_CASING) ||
				state.is(ModBlocks.FLUID_INPUT_PORT) ||
				state.is(ModBlocks.FLUID_OUTPUT_PORT);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putBoolean("Formed", formed);
		tag.putInt("RPM", rpm);
		tag.putInt("GasStored", gasStored);
		tag.putInt("ExhaustTemp", exhaustTemp);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.formed = tag.getBoolean("Formed");
		this.rpm = tag.getInt("RPM");
		this.gasStored = tag.getInt("GasStored");
		this.exhaustTemp = tag.getInt("ExhaustTemp");
	}
}
