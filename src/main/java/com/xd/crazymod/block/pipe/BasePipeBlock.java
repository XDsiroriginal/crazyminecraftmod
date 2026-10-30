package com.xd.crazymod.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public abstract class BasePipeBlock extends Block {
	public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	public static final BooleanProperty EAST = BlockStateProperties.EAST;
	public static final BooleanProperty WEST = BlockStateProperties.WEST;
	public static final BooleanProperty UP = BlockStateProperties.UP;
	public static final BooleanProperty DOWN = BlockStateProperties.DOWN;

	private static final VoxelShape CORE_SHAPE = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);
	private static final Map<Direction, VoxelShape> ARM_SHAPES = new EnumMap<>(Direction.class);

	static {
		ARM_SHAPES.put(Direction.DOWN, Block.box(4.0, 0.0, 4.0, 12.0, 4.0, 12.0));
		ARM_SHAPES.put(Direction.UP, Block.box(4.0, 12.0, 4.0, 12.0, 16.0, 12.0));
		ARM_SHAPES.put(Direction.NORTH, Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 4.0));
		ARM_SHAPES.put(Direction.SOUTH, Block.box(4.0, 4.0, 12.0, 12.0, 12.0, 16.0));
		ARM_SHAPES.put(Direction.WEST, Block.box(0.0, 4.0, 4.0, 4.0, 12.0, 12.0));
		ARM_SHAPES.put(Direction.EAST, Block.box(12.0, 4.0, 4.0, 16.0, 12.0, 12.0));
	}

	public BasePipeBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(NORTH, false)
				.setValue(SOUTH, false)
				.setValue(EAST, false)
				.setValue(WEST, false)
				.setValue(UP, false)
				.setValue(DOWN, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
	}

	public abstract boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction);

	public BlockState updateConnections(BlockGetter level, BlockPos pos, BlockState state) {
		return state
				.setValue(DOWN, canConnectTo(level, pos.below(), Direction.DOWN))
				.setValue(UP, canConnectTo(level, pos.above(), Direction.UP))
				.setValue(NORTH, canConnectTo(level, pos.north(), Direction.NORTH))
				.setValue(SOUTH, canConnectTo(level, pos.south(), Direction.SOUTH))
				.setValue(WEST, canConnectTo(level, pos.west(), Direction.WEST))
				.setValue(EAST, canConnectTo(level, pos.east(), Direction.EAST));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return updateConnections(context.getLevel(), context.getClickedPos(), this.defaultBlockState());
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		return updateConnections(level, pos, state);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		VoxelShape shape = CORE_SHAPE;
		if (state.getValue(DOWN)) shape = Shapes.or(shape, ARM_SHAPES.get(Direction.DOWN));
		if (state.getValue(UP)) shape = Shapes.or(shape, ARM_SHAPES.get(Direction.UP));
		if (state.getValue(NORTH)) shape = Shapes.or(shape, ARM_SHAPES.get(Direction.NORTH));
		if (state.getValue(SOUTH)) shape = Shapes.or(shape, ARM_SHAPES.get(Direction.SOUTH));
		if (state.getValue(WEST)) shape = Shapes.or(shape, ARM_SHAPES.get(Direction.WEST));
		if (state.getValue(EAST)) shape = Shapes.or(shape, ARM_SHAPES.get(Direction.EAST));
		return shape;
	}
}
