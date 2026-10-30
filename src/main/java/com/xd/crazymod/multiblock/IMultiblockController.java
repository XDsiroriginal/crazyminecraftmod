package com.xd.crazymod.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.List;

public interface IMultiblockController {
	boolean isFormed();
	void setFormed(boolean formed);
	MultiblockValidationResult checkStructure(Level level, BlockPos controllerPos);
	List<BlockPos> getConnectedPorts();
}
