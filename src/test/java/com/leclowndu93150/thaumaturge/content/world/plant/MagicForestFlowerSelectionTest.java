package com.leclowndu93150.thaumaturge.content.world.plant;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class MagicForestFlowerSelectionTest {
    @Test
    void biomeNoiseSelectsStableFlowerBandsWithinTheAvailableList() {
        Set<Integer> selected = new HashSet<>();
        boolean hasNeighboringBand = false;

        for (int x = -256; x <= 256; x += 8) {
            for (int z = -256; z <= 256; z += 8) {
                BlockPos pos = new BlockPos(x, 64, z);
                int index = MagicForestFloraFeature.flowerIndexAt(pos, 5);

                assertTrue(index >= 0 && index < 5, "Flower index must stay within the configured list");
                assertEquals(index, MagicForestFloraFeature.flowerIndexAt(pos, 5));
                selected.add(index);
                if (MagicForestFloraFeature.flowerIndexAt(pos.offset(1, 0, 0), 5) == index) {
                    hasNeighboringBand = true;
                }
            }
        }

        assertTrue(selected.size() > 1, "The biome noise should distribute more than one flower type");
        assertTrue(hasNeighboringBand, "Nearby positions should share flower bands to form visible clusters");
    }
}
