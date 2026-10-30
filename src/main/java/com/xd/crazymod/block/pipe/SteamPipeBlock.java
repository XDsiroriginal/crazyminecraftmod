package com.xd.crazymod.block.pipe;

import com.xd.crazymod.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class SteamPipeBlock extends BasePipeBlock {
	public SteamPipeBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
		BlockState state = level.getBlockState(pos);
		return state.getBlock() instanceof SteamPipeBlock ||
				state.is(ModBlocks.FLUID_INPUT_PORT) ||
				state.is(ModBlocks.FLUID_OUTPUT_PORT) ||
				state.is(ModBlocks.COAL_BOILER_CONTROLLER) ||
				state.is(ModBlocks.STEAM_TURBINE_CONTROLLER) ||
				state.is(ModBlocks.OIL_BOILER_CONTROLLER) ||
				state.is(ModBlocks.HRSG_CONTROLLER) ||
				state.is(ModBlocks.VVER_REACTOR_CONTROLLER);
	}
}
