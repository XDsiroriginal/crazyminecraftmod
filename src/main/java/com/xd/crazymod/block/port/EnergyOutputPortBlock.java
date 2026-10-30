package com.xd.crazymod.block.port;

import com.xd.crazymod.block.entity.GeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EnergyOutputPortBlock extends Block {
	public EnergyOutputPortBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;

		for (Direction dir : Direction.values()) {
			for (int dist = 1; dist <= 4; dist++) {
				BlockEntity be = level.getBlockEntity(pos.relative(dir, dist));
				if (be instanceof GeneratorBlockEntity generator) {
					player.sendSystemMessage(Component.translatable("chat.crazymod.port.energy_status",
							generator.getPowerOutput(), generator.getEnergyStored()));
					return InteractionResult.CONSUME;
				}
			}
		}

		player.sendSystemMessage(Component.translatable("chat.crazymod.port.unconnected"));
		return InteractionResult.CONSUME;
	}
}
