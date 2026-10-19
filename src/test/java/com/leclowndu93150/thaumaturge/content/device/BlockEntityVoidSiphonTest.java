package com.leclowndu93150.thaumaturge.content.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

class BlockEntityVoidSiphonTest {
    @Test
    void disabledSiphonProducesFromStoredProgressWithoutDrainingRifts() {
        ServerLevel level = mock(ServerLevel.class);
        BlockState disabled =
                TTBlocks.VOID_SIPHON.get().defaultBlockState().setValue(BlockStateProperties.ENABLED, false);
        BlockEntityVoidSiphon siphon = new BlockEntityVoidSiphon(BlockPos.ZERO, disabled);
        CompoundTag saved = new CompoundTag();
        saved.putInt("progress", BlockEntityVoidSiphon.PROGRESS_REQUIRED);
        siphon.loadAdditional(saved, mock(HolderLookup.Provider.class));

        for (int tick = 0; tick < 20; tick++) {
            BlockEntityVoidSiphon.serverTick(level, BlockPos.ZERO, disabled, siphon);
        }

        assertEquals(0, siphon.progress());
        assertEquals(1, siphon.output().getStackInSlot(0).getCount());
        assertEquals(TTItems.VOID_SEED.get(), siphon.output().getStackInSlot(0).getItem());
        verify(level, never()).getEntitiesOfClass(eq(EntityFluxRift.class), any(AABB.class));
    }
}
