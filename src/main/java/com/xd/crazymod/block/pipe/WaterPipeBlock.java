package com.xd.crazymod.block.pipe;

import com.xd.crazymod.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class WaterPipeBlock extends BasePipeBlock {
	public WaterPipeBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
		BlockState state = level.getBlockState(pos);
		return state.getBlock() instanceof WaterPipeBlock ||
				state.is(ModBlocks.FLUID_INPUT_PORT) ||
				state.is(ModBlocks.FLUID_OUTPUT_PORT) ||
				state.is(ModBlocks.COOLING_TOWER_CONTROLLER);
	}
}
