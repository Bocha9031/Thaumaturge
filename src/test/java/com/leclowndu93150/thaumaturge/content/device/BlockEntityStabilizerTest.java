package com.leclowndu93150.thaumaturge.content.device;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class BlockEntityStabilizerTest {
    @Test
    void shieldSpendingPreservesLegacyChargeDataAndRejectsInvalidNegativeCosts() {
        BlockEntityStabilizer stabilizer = new BlockEntityStabilizer(
                BlockPos.ZERO, TTBlocks.STABILIZER.get().defaultBlockState());
        HolderLookup.Provider registries = mock(HolderLookup.Provider.class);
        CompoundTag saved = new CompoundTag();
        saved.putInt("energy", 4);

        stabilizer.loadAdditional(saved, registries);

        assertTrue(stabilizer.spendForShield(0));
        assertTrue(stabilizer.spendForShield(-2));
        assertFalse(stabilizer.spendForShield(5));
        assertEquals(4, stabilizer.charge());
        assertTrue(stabilizer.spendForShield(3));
        assertEquals(1, stabilizer.charge());

        CompoundTag resaved = new CompoundTag();
        stabilizer.saveAdditional(resaved, registries);
        assertEquals(1, resaved.getInt("energy"));
    }
}
