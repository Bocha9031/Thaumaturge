package com.leclowndu93150.thaumaturge.api.research.scan;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;

class ScanningPickBlockTest {
    private static final BlockPos POSITION = new BlockPos(3, 64, 7);

    @Test
    void multipartPickReceivesTheActualSubBlockHitOnClientAndServer() {
        for (boolean client : new boolean[] {false, true}) {
            Level level = level(client);
            Player player = player(level, new Vec3(3.5, 64.6, 5), new Vec3(0, 0, 1));
            BlockState state = state(Shapes.box(0.375, 0.375, 0.375, 0.625, 0.625, 0.625));
            ItemStack cable = new ItemStack(Items.STRING);
            when(state.getCloneItemStack(any(), same(level), eq(POSITION), same(player)))
                    .thenAnswer(invocation -> {
                        HitResult hit = invocation.getArgument(0);
                        assertNotNull(hit); // Multipart blocks dereference this, just like AE2's cable bus.
                        assertInstanceOf(BlockHitResult.class, hit);
                        BlockHitResult blockHit = (BlockHitResult) hit;
                        assertEquals(POSITION, blockHit.getBlockPos());
                        assertEquals(Direction.NORTH, blockHit.getDirection());
                        assertEquals(3.5, hit.getLocation().x, 0.00001);
                        assertEquals(64.6, hit.getLocation().y, 0.00001);
                        assertEquals(7.375, hit.getLocation().z, 0.00001);
                        return cable;
                    });

            assertSame(cable, ScanningManager.blockStack(player, POSITION, state));
            verify(level, never()).clip(any());
            verify(level, never()).getChunk(anyInt(), anyInt());
        }
    }

    @Test
    void positionOnlyOrEmptyOutlineTargetsStillHaveANonNullHit() {
        Level level = level(true);
        Player player = player(level, new Vec3(0, 66, 0), new Vec3(0, 0, -1));
        for (VoxelShape outline : new VoxelShape[] {Shapes.block(), Shapes.empty()}) {
            BlockState state = state(outline);
            when(state.getCloneItemStack(any(), same(level), eq(POSITION), same(player)))
                    .thenAnswer(invocation -> {
                        BlockHitResult hit = invocation.getArgument(0);
                        assertNotNull(hit);
                        assertEquals(POSITION, hit.getBlockPos());
                        assertEquals(Vec3.atCenterOf(POSITION), hit.getLocation());
                        return ItemStack.EMPTY;
                    });
            assertTrue(ScanningManager.blockStack(player, POSITION, state).isEmpty());
        }
    }

    @Test
    void fluidBucketFallbackIsPreservedAndDoesNotReplaceANonEmptyPick() {
        Level level = level(true);
        Player player = player(level, new Vec3(0, 66, 0), new Vec3(0, 0, -1));
        BlockState state = state(Shapes.empty());
        when(state.getFluidState()).thenReturn(Fluids.WATER.defaultFluidState());
        when(state.getCloneItemStack(any(), same(level), eq(POSITION), same(player)))
                .thenReturn(ItemStack.EMPTY);
        assertTrue(ScanningManager.blockStack(player, POSITION, state).is(Items.WATER_BUCKET));

        ItemStack pick = new ItemStack(Items.STONE);
        when(state.getCloneItemStack(any(), same(level), eq(POSITION), same(player)))
                .thenReturn(pick);
        assertSame(pick, ScanningManager.blockStack(player, POSITION, state));
    }

    private static Level level(boolean client) {
        Level level = mock(Level.class);
        when(level.isClientSide()).thenReturn(client);
        doCallRealMethod().when(level).clipWithInteractionOverride(any(), any(), any(), any(), any());
        return level;
    }

    private static Player player(Level level, Vec3 eye, Vec3 direction) {
        Player player = mock(Player.class);
        when(player.level()).thenReturn(level);
        when(player.getEyePosition()).thenReturn(eye);
        when(player.getViewVector(1.0F)).thenReturn(direction);
        when(player.blockInteractionRange()).thenReturn(5.0);
        return player;
    }

    private static BlockState state(VoxelShape outline) {
        BlockState state = mock(BlockState.class);
        when(state.getShape(any(), any(), any())).thenReturn(outline);
        when(state.getInteractionShape(any(), any())).thenReturn(Shapes.empty());
        when(state.getFluidState()).thenReturn(Fluids.EMPTY.defaultFluidState());
        return state;
    }
}
