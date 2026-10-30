package com.xd.crazymod.block;

import com.xd.crazymod.block.entity.HRSGBlockEntity;
import com.xd.crazymod.init.ModBlockEntities;
import com.xd.crazymod.multiblock.MultiblockValidationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class HRSGControllerBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty FORMED = BooleanProperty.create("formed");
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

	public HRSGControllerBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(FORMED, false)
				.setValue(ACTIVE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FORMED, ACTIVE);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new HRSGBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, ModBlockEntities.HRSG_BLOCK_ENTITY,
				(lvl, pos, st, be) -> be.tick(lvl, pos, st));
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;

		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof HRSGBlockEntity hrsg) {
			MultiblockValidationResult result = hrsg.checkStructure(level, pos);
			if (result.isSuccess()) {
				hrsg.setFormed(true);
				level.setBlock(pos, state.setValue(FORMED, true), Block.UPDATE_ALL);
				player.sendSystemMessage(Component.translatable("chat.crazymod.hrsg.formed",
						hrsg.getSteamOutputRate(), hrsg.getSteamStored()));
			} else {
				hrsg.setFormed(false);
				level.setBlock(pos, state.setValue(FORMED, false), Block.UPDATE_ALL);
				player.sendSystemMessage(Component.translatable("chat.crazymod.hrsg.unformed", result.getMessage()));
			}
		}

		return InteractionResult.CONSUME;
	}
}
