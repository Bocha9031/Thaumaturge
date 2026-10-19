package com.leclowndu93150.thaumaturge.content.spell.block;

import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class BlockEffectSapTickTest {
    @Test
    void scheduledExpiryRemovesTheTemporaryField() {
        BlockEffectSap sap = mock(BlockEffectSap.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
        ServerLevel level = mock(ServerLevel.class);
        BlockPos pos = new BlockPos(4, 5, 6);

        sap.tick(mock(BlockState.class), level, pos, mock(RandomSource.class));

        verify(level).removeBlock(pos, false);
    }
}
