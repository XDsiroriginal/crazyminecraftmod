package com.xd.crazymod.block.entity;

import com.xd.crazymod.block.GeneratorControllerBlock;
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

public class GeneratorBlockEntity extends BlockEntity implements IMultiblockController {
	private boolean formed = false;
	private final List<BlockPos> connectedPorts = new ArrayList<>();

	private long energyStored = 0;
	private static final long MAX_ENERGY = 10_000_000; // 10M FE/E
	private int powerOutput = 0; // E/t

	public GeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.GENERATOR_BLOCK_ENTITY, pos, state);
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

	public long getEnergyStored() {
		return energyStored;
	}

	public int getPowerOutput() {
		return powerOutput;
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide) return;

		if (!formed) {
			powerOutput = 0;
			return;
		}

		// Find adjacent steam turbine along the shaft axis
		Direction facing = state.getValue(GeneratorControllerBlock.FACING);
		Direction shaftAxis = facing.getClockWise();

		int detectedRpm = 0;
		for (Direction dir : List.of(shaftAxis, shaftAxis.getOpposite(), facing.getOpposite())) {
			for (int dist = 1; dist <= 6; dist++) {
				BlockPos checkPos = pos.relative(dir, dist);
				BlockEntity be = level.getBlockEntity(checkPos);
				if (be instanceof SteamTurbineBlockEntity turbine) {
					detectedRpm = turbine.getRpm();
					break;
				}
			}
			if (detectedRpm > 0) break;
		}

		if (detectedRpm > 0) {
			// Faraday generation: 3000 RPM -> ~8,000 - 16,000 E/t
			powerOutput = (int) ((detectedRpm / 3000.0) * 12800);
			energyStored = Math.min(MAX_ENERGY, energyStored + powerOutput);
		} else {
			powerOutput = 0;
		}

		boolean isGenerating = powerOutput > 0;
		if (state.getValue(GeneratorControllerBlock.GENERATING) != isGenerating) {
			level.setBlock(pos, state.setValue(GeneratorControllerBlock.GENERATING, isGenerating), Block.UPDATE_ALL);
		}

		setChanged();
	}

	@Override
	public MultiblockValidationResult checkStructure(Level level, BlockPos controllerPos) {
		connectedPorts.clear();

		Direction facing = getBlockState().getValue(GeneratorControllerBlock.FACING);
		Direction back = facing.getOpposite();
		Direction right = facing.getClockWise();

		BlockPos centerBase = controllerPos.relative(back);
		boolean hasEnergyPort = false;

		// 3x3x4 turbogenerator stator housing
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = 0; dz < 3; dz++) {
				for (int dy = 0; dy < 3; dy++) {
					BlockPos cur = centerBase.relative(right, dx).relative(back, dz).above(dy);
					if (cur.equals(controllerPos)) continue;

					BlockState blockState = level.getBlockState(cur);
					if (isValidGeneratorComponent(blockState)) {
						if (blockState.is(ModBlocks.ENERGY_OUTPUT_PORT)) {
							hasEnergyPort = true;
							connectedPorts.add(cur);
						}
					} else {
						return MultiblockValidationResult.error(
								Component.translatable("multiblock.crazymod.error.invalid_generator_block", cur.toShortString()), cur);
					}
				}
			}
		}

		if (!hasEnergyPort) {
			return MultiblockValidationResult.error(
					Component.translatable("multiblock.crazymod.error.missing_energy_port"), controllerPos);
		}

		return MultiblockValidationResult.ok();
	}

	private boolean isValidGeneratorComponent(BlockState state) {
		return state.is(ModBlocks.GENERATOR_CASING) ||
				state.is(ModBlocks.ENERGY_OUTPUT_PORT);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putBoolean("Formed", formed);
		tag.putLong("EnergyStored", energyStored);
		tag.putInt("PowerOutput", powerOutput);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.formed = tag.getBoolean("Formed");
		this.energyStored = tag.getLong("EnergyStored");
		this.powerOutput = tag.getInt("PowerOutput");
	}
}
