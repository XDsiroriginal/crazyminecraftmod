package com.xd.crazymod.block.entity;

import com.xd.crazymod.block.CoolingTowerControllerBlock;
import com.xd.crazymod.init.ModBlockEntities;
import com.xd.crazymod.init.ModBlocks;
import com.xd.crazymod.multiblock.IMultiblockController;
import com.xd.crazymod.multiblock.MultiblockValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class CoolingTowerBlockEntity extends BlockEntity implements IMultiblockController {
	private boolean formed = false;
	private final List<BlockPos> connectedPorts = new ArrayList<>();

	private int waterLevel = 0; // mB of circulating water
	private int coolingRate = 0; // mB/t
	private static final int MAX_WATER = 64000;

	public CoolingTowerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.COOLING_TOWER_BLOCK_ENTITY, pos, state);
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

	public int getWaterLevel() {
		return waterLevel;
	}

	public int getCoolingRate() {
		return coolingRate;
	}

	public void addCirculatingWater(int amount) {
		this.waterLevel = Math.min(MAX_WATER, this.waterLevel + amount);
		setChanged();
	}

	public void tick(Level level, BlockPos pos, BlockState state) {
		if (level.isClientSide) return;

		if (!formed) {
			coolingRate = 0;
			return;
		}

		// Circulating cooling cycle
		boolean isCooling = waterLevel > 0;
		if (isCooling) {
			coolingRate = Math.min(waterLevel, 500); // 500 mB/t cooling capacity
			// Plume of evaporated vapor from the top of the tower
			if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % 4 == 0) {
				Direction facing = state.getValue(CoolingTowerControllerBlock.FACING);
				BlockPos topCenter = pos.relative(facing.getOpposite(), 2).above(6);
				serverLevel.sendParticles(ParticleTypes.CLOUD,
						topCenter.getX() + 0.5, topCenter.getY() + 0.5, topCenter.getZ() + 0.5,
						3, 0.4, 0.6, 0.4, 0.02);
			}
		} else {
			coolingRate = 0;
		}

		if (state.getValue(CoolingTowerControllerBlock.COOLING) != isCooling) {
			level.setBlock(pos, state.setValue(CoolingTowerControllerBlock.COOLING, isCooling), Block.UPDATE_ALL);
		}

		setChanged();
	}

	@Override
	public MultiblockValidationResult checkStructure(Level level, BlockPos controllerPos) {
		connectedPorts.clear();

		Direction facing = getBlockState().getValue(CoolingTowerControllerBlock.FACING);
		Direction back = facing.getOpposite();
		Direction right = facing.getClockWise();

		BlockPos centerBase = controllerPos.relative(back, 2);

		boolean hasFluidIn = false;
		boolean hasFluidOut = false;

		// 5x5 base, height 7
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				for (int dy = 0; dy < 7; dy++) {
					BlockPos cur = centerBase.relative(right, dx).relative(back, dz).above(dy);
					if (cur.equals(controllerPos)) continue;

					boolean isPerimeter = (Math.abs(dx) == 2 || Math.abs(dz) == 2);
					BlockState blockState = level.getBlockState(cur);

					if (dy == 0) {
						// Floor
						if (!isValidTowerCasing(blockState)) {
							return MultiblockValidationResult.error(
									Component.translatable("multiblock.crazymod.error.invalid_tower_block", cur.toShortString()), cur);
						}
					} else if (dy == 1 && !isPerimeter) {
						// Interior fill layer (film fill / ороситель)
						if (!blockState.is(ModBlocks.COOLING_TOWER_FILL)) {
							return MultiblockValidationResult.error(
									Component.translatable("multiblock.crazymod.error.missing_tower_fill", cur.toShortString()), cur);
						}
					} else if (dy > 1 && !isPerimeter) {
						// Chimney interior must be air
						if (!blockState.isAir()) {
							return MultiblockValidationResult.error(
									Component.translatable("multiblock.crazymod.error.interior_not_empty", cur.toShortString()), cur);
						}
					} else {
						// Outer wall
						if (isValidTowerCasing(blockState)) {
							if (blockState.is(ModBlocks.FLUID_INPUT_PORT)) {
								hasFluidIn = true;
								connectedPorts.add(cur);
							} else if (blockState.is(ModBlocks.FLUID_OUTPUT_PORT)) {
								hasFluidOut = true;
								connectedPorts.add(cur);
							}
						} else {
							return MultiblockValidationResult.error(
									Component.translatable("multiblock.crazymod.error.invalid_tower_block", cur.toShortString()), cur);
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

	private boolean isValidTowerCasing(BlockState state) {
		return state.is(ModBlocks.COOLING_TOWER_CASING) ||
				state.is(ModBlocks.REINFORCED_CONCRETE) ||
				state.is(ModBlocks.FLUID_INPUT_PORT) ||
				state.is(ModBlocks.FLUID_OUTPUT_PORT);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putBoolean("Formed", formed);
		tag.putInt("WaterLevel", waterLevel);
		tag.putInt("CoolingRate", coolingRate);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.formed = tag.getBoolean("Formed");
		this.waterLevel = tag.getInt("WaterLevel");
		this.coolingRate = tag.getInt("CoolingRate");
	}
}
