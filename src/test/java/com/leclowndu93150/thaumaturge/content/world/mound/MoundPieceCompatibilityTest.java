package com.leclowndu93150.thaumaturge.content.world.mound;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class MoundPieceCompatibilityTest {
    @Test
    void lootRollsUseIntegerPercentThresholdsForBothContainerAndTier() throws Exception {
        assertEquals(TTBlocks.LOOT_CRATE_RARE.get(), lootContainer(9, 29).getBlock());
        assertEquals(TTBlocks.LOOT_URN_UNCOMMON.get(), lootContainer(10, 30).getBlock());
        assertEquals(TTBlocks.LOOT_CRATE_UNCOMMON.get(), lootContainer(32, 29).getBlock());
        assertEquals(TTBlocks.LOOT_URN_COMMON.get(), lootContainer(33, 30).getBlock());
    }

    @Test
    void trappedMoundChestDoesNotOverwriteAnExistingChestOrPlaceItsTnt() throws Exception {
        MoundPiece piece = new MoundPiece(RandomSource.create(1), 0, 0);
        WorldGenLevel level = mock(WorldGenLevel.class);
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(3)).thenReturn(0);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.CHEST.defaultBlockState());

        invokePlaceChest(piece, level, random, new BoundingBox(-100, -100, -100, 100, 300, 100));

        verify(level, never()).setBlock(any(BlockPos.class), any(BlockState.class), anyInt());
    }

    @Test
    void successfullyPlacedTrappedMoundChestPlacesTntBelowIt() throws Exception {
        MoundPiece piece = new MoundPiece(RandomSource.create(1), 0, 0);
        WorldGenLevel level = mock(WorldGenLevel.class);
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(3)).thenReturn(0);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());

        invokePlaceChest(piece, level, random, new BoundingBox(-100, -100, -100, 100, 300, 100));

        ArgumentCaptor<BlockPos> positions = ArgumentCaptor.forClass(BlockPos.class);
        ArgumentCaptor<BlockState> states = ArgumentCaptor.forClass(BlockState.class);
        verify(level, times(2)).setBlock(positions.capture(), states.capture(), anyInt());
        assertEquals(Blocks.TRAPPED_CHEST, states.getAllValues().get(0).getBlock());
        assertEquals(Blocks.TNT, states.getAllValues().get(1).getBlock());
        assertEquals(
                positions.getAllValues().get(0).below(2),
                positions.getAllValues().get(1));
    }

    @Test
    void moundChestOutsideTheCurrentChunkIsNotAttempted() throws Exception {
        MoundPiece piece = new MoundPiece(RandomSource.create(1), 0, 0);
        WorldGenLevel level = mock(WorldGenLevel.class);
        RandomSource random = mock(RandomSource.class);

        invokePlaceChest(piece, level, random, new BoundingBox(500, 500, 500, 600, 600, 600));

        verifyNoInteractions(level, random);
    }

    private static BlockState lootContainer(int tierRoll, int crateRoll) throws Exception {
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(100)).thenReturn(tierRoll, crateRoll);
        Method lootContainer = MoundPiece.class.getDeclaredMethod("lootContainer", RandomSource.class);
        lootContainer.setAccessible(true);
        return (BlockState) lootContainer.invoke(null, random);
    }

    private static void invokePlaceChest(
            MoundPiece piece, WorldGenLevel level, RandomSource random, BoundingBox chunkBB) throws Exception {
        Method placeChest = MoundPiece.class.getDeclaredMethod(
                "placeChest", WorldGenLevel.class, RandomSource.class, BoundingBox.class);
        placeChest.setAccessible(true);
        placeChest.invoke(piece, level, random, chunkBB);
    }
}
