package com.xd.crazymod.block.entity;

import com.xd.crazymod.block.HRSGControllerBlock;
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

public class HRSGBlockEntity extends BlockEntity implements IMultiblockController {
	private boolean formed = false;
	private final List<BlockPos> connectedPorts = new ArrayList<>();

	private int waterStored = 0;
	private int steamStored = 0;
	private int steamOutputRate = 0;
	private static final int MAX_WATER = 32000;
	private static final int MAX_STEAM = 128000;

	public HRSGBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.HRSG_BLOCK_ENTITY, pos, state);
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

	public int getSteamOutputRate() {
		return steamOutputRate;
	}

	public int getSteamStored() {
		return steamStored;
	}

	public void addWater(int amount) {
		this.waterStored = Math.min(MAX_WATER, this.waterStored + amount);
		setChanged();
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide) return;

		if (!formed) {
			steamOutputRate = 0;
			return;
		}

		// Combined-cycle steam generation from exhaust heat
		if (waterStored > 10) {
			int boilRate = Math.min(waterStored, 20);
			waterStored -= boilRate;
			steamOutputRate = boilRate * 10;
			steamStored = Math.min(MAX_STEAM, steamStored + steamOutputRate);
		} else {
			steamOutputRate = 0;
		}

		boolean isActive = steamOutputRate > 0;
		if (state.getValue(HRSGControllerBlock.ACTIVE) != isActive) {
			level.setBlock(pos, state.setValue(HRSGControllerBlock.ACTIVE, isActive), Block.UPDATE_ALL);
		}

		setChanged();
	}

	@Override
	public MultiblockValidationResult checkStructure(Level level, BlockPos controllerPos) {
		connectedPorts.clear();

		Direction facing = getBlockState().getValue(HRSGControllerBlock.FACING);
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
					if (isValidHRSGComponent(blockState)) {
						if (blockState.is(ModBlocks.FLUID_INPUT_PORT)) {
							hasFluidIn = true;
							connectedPorts.add(cur);
						} else if (blockState.is(ModBlocks.FLUID_OUTPUT_PORT)) {
							hasFluidOut = true;
							connectedPorts.add(cur);
						}
					} else {
						return MultiblockValidationResult.error(
								Component.translatable("multiblock.crazymod.error.invalid_hrsg_block", cur.toShortString()), cur);
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

	private boolean isValidHRSGComponent(BlockState state) {
		return state.is(ModBlocks.BOILER_CASING) ||
				state.is(ModBlocks.BOILER_PIPE_CASING) ||
				state.is(ModBlocks.FLUID_INPUT_PORT) ||
				state.is(ModBlocks.FLUID_OUTPUT_PORT);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putBoolean("Formed", formed);
		tag.putInt("WaterStored", waterStored);
		tag.putInt("SteamStored", steamStored);
		tag.putInt("SteamOutputRate", steamOutputRate);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.formed = tag.getBoolean("Formed");
		this.waterStored = tag.getInt("WaterStored");
		this.steamStored = tag.getInt("SteamStored");
		this.steamOutputRate = tag.getInt("SteamOutputRate");
	}
}
