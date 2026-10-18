package com.leclowndu93150.thaumaturge.content.device.bore;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.junit.jupiter.api.Test;

class ArcaneBoreCoreTest {
    @Test
    void aimingUsesTheCenterOfTheTargetBlock() {
        ArcaneBoreHost host = mock(ArcaneBoreHost.class);
        BlockPos target = new BlockPos(4, 8, -2);

        ArcaneBoreCore.aim(host, target);

        verify(host).aimBore(4.5, 8.5, -1.5, ArcaneBoreCore.IDLE_YAW_STEP, ArcaneBoreCore.DIG_PITCH_STEP);
    }

    @Test
    void idleAimStartsFromEyeAndUsesActiveDistance() {
        ArcaneBoreHost host = mock(ArcaneBoreHost.class);
        when(host.boreActive()).thenReturn(true);
        when(host.boreFacing()).thenReturn(Direction.EAST);
        when(host.boreEye()).thenReturn(new Vec3(1.5, 10.75, 2.5));

        ArcaneBoreCore.aim(host, null);

        verify(host).aimBore(3.5, 10.75, 2.5, ArcaneBoreCore.IDLE_YAW_STEP, ArcaneBoreCore.IDLE_PITCH_STEP);
    }

    @Test
    void targetSightlineIgnoresTheBoreAndTargetButRejectsAnIntermediateCollision() {
        ArcaneBoreHost host = mock(ArcaneBoreHost.class);
        ServerLevel level = mock(ServerLevel.class);
        BlockPos target = new BlockPos(3, 0, 0);
        when(host.boreEye()).thenReturn(new Vec3(0.5, 0.5, 0.5));
        when(host.borePos()).thenReturn(BlockPos.ZERO);
        when(host.boreCollisionContext()).thenReturn(CollisionContext.empty());
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());

        assertTrue(ArcaneBoreCore.canSee(host, level, target));

        when(level.getBlockState(new BlockPos(1, 0, 0))).thenReturn(Blocks.STONE.defaultBlockState());

        assertFalse(ArcaneBoreCore.canSee(host, level, target));
    }
}
