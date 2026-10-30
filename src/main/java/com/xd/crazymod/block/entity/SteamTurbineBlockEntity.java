package com.xd.crazymod.block.entity;

import com.xd.crazymod.block.SteamTurbineControllerBlock;
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

public class SteamTurbineBlockEntity extends BlockEntity implements IMultiblockController {
	private boolean formed = false;
	private final List<BlockPos> connectedPorts = new ArrayList<>();

	private int rpm = 0; // Synchronous speed up to 3000 RPM (50 Hz)
	private int steamInputRate = 0; // mB/t
	private int steamStored = 0;
	private static final int MAX_STEAM = 64000;

	public SteamTurbineBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.STEAM_TURBINE_BLOCK_ENTITY, pos, state);
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

	public int getSteamInputRate() {
		return steamInputRate;
	}

	public void injectSteam(int amount) {
		this.steamStored = Math.min(MAX_STEAM, this.steamStored + amount);
		setChanged();
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide) return;

		if (!formed) {
			if (rpm > 0) {
				rpm = Math.max(0, rpm - 20);
				setChanged();
			}
			return;
		}

		// Pull steam from adjacent coal boilers or ports
		if (steamStored > 50) {
			int consumed = Math.min(steamStored, 200);
			steamStored -= consumed;
			steamInputRate = consumed;

			// Accelerate up to 3000 RPM
			if (rpm < 3000) {
				rpm = Math.min(3000, rpm + 25);
			}
		} else {
			steamInputRate = 0;
			if (rpm > 0) {
				rpm = Math.max(0, rpm - 10); // Inertia slowdown
			}
		}

		boolean isRunning = rpm > 500;
		if (state.getValue(SteamTurbineControllerBlock.RUNNING) != isRunning) {
			level.setBlock(pos, state.setValue(SteamTurbineControllerBlock.RUNNING, isRunning), Block.UPDATE_ALL);
		}

		setChanged();
	}

	@Override
	public MultiblockValidationResult checkStructure(Level level, BlockPos controllerPos) {
		connectedPorts.clear();

		Direction facing = getBlockState().getValue(SteamTurbineControllerBlock.FACING);
		Direction back = facing.getOpposite();
		Direction right = facing.getClockWise();

		// Horizontal 3x3x5 turbine rotor & stator casing
		BlockPos centerBase = controllerPos.relative(back);

		boolean hasFluidIn = false;
		boolean hasFluidOut = false;

		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = 0; dz < 4; dz++) {
				for (int dy = 0; dy < 3; dy++) {
					BlockPos cur = centerBase.relative(right, dx).relative(back, dz).above(dy);
					if (cur.equals(controllerPos)) continue;

					BlockState blockState = level.getBlockState(cur);
					if (isValidTurbineComponent(blockState)) {
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

	private boolean isValidTurbineComponent(BlockState state) {
		return state.is(ModBlocks.TURBINE_CASING) ||
				state.is(ModBlocks.FLUID_INPUT_PORT) ||
				state.is(ModBlocks.FLUID_OUTPUT_PORT);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putBoolean("Formed", formed);
		tag.putInt("RPM", rpm);
		tag.putInt("SteamStored", steamStored);
		tag.putInt("SteamInputRate", steamInputRate);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.formed = tag.getBoolean("Formed");
		this.rpm = tag.getInt("RPM");
		this.steamStored = tag.getInt("SteamStored");
		this.steamInputRate = tag.getInt("SteamInputRate");
	}
}
