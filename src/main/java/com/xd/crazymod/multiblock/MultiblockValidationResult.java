package com.xd.crazymod.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class MultiblockValidationResult {
	private final boolean success;
	private final Component message;
	private final BlockPos errorPos;

	private MultiblockValidationResult(boolean success, Component message, BlockPos errorPos) {
		this.success = success;
		this.message = message;
		this.errorPos = errorPos;
	}

	public static MultiblockValidationResult ok() {
		return new MultiblockValidationResult(true, Component.translatable("multiblock.crazymod.valid"), null);
	}

	public static MultiblockValidationResult error(Component message, BlockPos pos) {
		return new MultiblockValidationResult(false, message, pos);
	}

	public boolean isSuccess() {
		return success;
	}

	public Component getMessage() {
		return message;
	}

	public BlockPos getErrorPos() {
		return errorPos;
	}
}
