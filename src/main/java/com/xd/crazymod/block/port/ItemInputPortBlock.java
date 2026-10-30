package com.xd.crazymod.block.port;

import com.xd.crazymod.block.entity.CoalBoilerBlockEntity;
import com.xd.crazymod.block.entity.VverReactorBlockEntity;
import com.xd.crazymod.init.ModItems;
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

public class ItemInputPortBlock extends Block {
	public ItemInputPortBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;

		ItemStack held = player.getItemInHand(hand);
		BlockEntity target = findAdjacentController(level, pos);
		if (target == null) {
			player.sendSystemMessage(Component.translatable("chat.crazymod.port.unconnected"));
			return InteractionResult.CONSUME;
		}

		if (target instanceof CoalBoilerBlockEntity boiler) {
			if (held.is(Items.COAL) || held.is(Items.CHARCOAL) || held.is(ModItems.COAL_DUST)) {
				if (!player.getAbilities().instabuild) {
					held.shrink(1);
				}
				level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
				player.sendSystemMessage(Component.translatable("chat.crazymod.port.coal_loaded"));
				return InteractionResult.CONSUME;
			}
		} else if (target instanceof VverReactorBlockEntity reactor) {
			if (held.is(ModItems.FUEL_ASSEMBLY)) {
				if (!player.getAbilities().instabuild) {
					held.shrink(1);
				}
				level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.7f, 1.2f);
				player.sendSystemMessage(Component.translatable("chat.crazymod.port.fuel_assembly_loaded"));
				return InteractionResult.CONSUME;
			}
		}

		player.sendSystemMessage(Component.translatable("chat.crazymod.port.item_ready"));
		return InteractionResult.CONSUME;
	}

	private BlockEntity findAdjacentController(Level level, BlockPos pos) {
		for (Direction dir : Direction.values()) {
			for (int dist = 1; dist <= 4; dist++) {
				BlockPos checkPos = pos.relative(dir, dist);
				BlockEntity be = level.getBlockEntity(checkPos);
				if (be instanceof CoalBoilerBlockEntity || be instanceof VverReactorBlockEntity) {
					return be;
				}
			}
		}
		return null;
	}
}
