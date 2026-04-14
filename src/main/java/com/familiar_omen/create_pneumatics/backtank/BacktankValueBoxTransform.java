package com.familiar_omen.create_pneumatics.backtank;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.content.kinetics.speedController.SpeedControllerBlock;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;

import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.math.AngleHelper;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import dev.engine_room.flywheel.lib.transform.TransformStack;

public class BacktankValueBoxTransform extends ValueBoxTransform {

    @Override
    public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
        return rotateHorizontally(state, VecHelper.voxelSpace(8, 9f, 13.1f));
    }

    @Override
	public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack ms) {
        Direction direction = state.getValue(HorizontalKineticBlock.HORIZONTAL_FACING);

        float yRot = AngleHelper.horizontalAngle(direction) + 180;

        TransformStack.of(ms).rotateYDegrees(yRot);
    }

    @Override
    public float getScale() {
        return 0.4f;
    }
}