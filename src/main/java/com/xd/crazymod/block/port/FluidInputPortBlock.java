package com.xd.crazymod.block.port;

import com.xd.crazymod.block.entity.CoalBoilerBlockEntity;
import com.xd.crazymod.block.entity.CoolingTowerBlockEntity;
import com.xd.crazymod.block.entity.HRSGBlockEntity;
import com.xd.crazymod.block.entity.OilBoilerBlockEntity;
import com.xd.crazymod.block.entity.SteamTurbineBlockEntity;
import com.xd.crazymod.block.entity.VverReactorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class FluidInputPortBlock extends Block {
	public FluidInputPortBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;

		ItemStack held = player.getItemInHand(hand);

		// Scan adjacent blocks (radius 4) for connected multiblock controller
		BlockEntity targetController = findAdjacentController(level, pos);
		if (targetController == null) {
			player.sendSystemMessage(Component.translatable("chat.crazymod.port.unconnected"));
			return InteractionResult.CONSUME;
		}

		if (held.is(Items.WATER_BUCKET)) {
			boolean inserted = false;
			if (targetController instanceof CoalBoilerBlockEntity boiler) {
				boiler.addWater(1000);
				inserted = true;
			} else if (targetController instanceof OilBoilerBlockEntity oilBoiler) {
				oilBoiler.addWater(1000);
				inserted = true;
			} else if (targetController instanceof HRSGBlockEntity hrsg) {
				hrsg.addWater(1000);
				inserted = true;
			} else if (targetController instanceof CoolingTowerBlockEntity tower) {
				tower.addCirculatingWater(1000);
				inserted = true;
			} else if (targetController instanceof VverReactorBlockEntity reactor) {
				reactor.addSecondaryWater(1000);
				inserted = true;
			}

			if (inserted) {
				if (!player.getAbilities().instabuild) {
					player.setItemInHand(hand, new ItemStack(Items.BUCKET));
				}
				level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
				player.sendSystemMessage(Component.translatable("chat.crazymod.port.water_added"));
				return InteractionResult.CONSUME;
			}
		}

		player.sendSystemMessage(Component.translatable("chat.crazymod.port.status_ready"));
		return InteractionResult.CONSUME;
	}

	public static BlockEntity findAdjacentController(Level level, BlockPos pos) {
		for (Direction dir : Direction.values()) {
			for (int dist = 1; dist <= 4; dist++) {
				BlockPos checkPos = pos.relative(dir, dist);
				BlockEntity be = level.getBlockEntity(checkPos);
				if (be instanceof CoalBoilerBlockEntity ||
						be instanceof OilBoilerBlockEntity ||
						be instanceof HRSGBlockEntity ||
						be instanceof CoolingTowerBlockEntity ||
						be instanceof SteamTurbineBlockEntity ||
						be instanceof VverReactorBlockEntity) {
					return be;
				}
			}
		}
		return null;
	}
}
