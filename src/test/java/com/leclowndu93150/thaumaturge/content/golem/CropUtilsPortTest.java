package com.leclowndu93150.thaumaturge.content.golem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import org.junit.jupiter.api.Test;

class CropUtilsPortTest {
    @Test
    void onlySupportedCropsExposeAPlantingSupport() {
        assertEquals(Optional.of(Direction.DOWN), CropUtils.plantingSupport(Blocks.WHEAT.defaultBlockState()));
        assertEquals(Optional.of(Direction.DOWN), CropUtils.plantingSupport(Blocks.NETHER_WART.defaultBlockState()));
        assertEquals(
                Optional.of(Direction.WEST),
                CropUtils.plantingSupport(
                        Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.FACING, Direction.WEST)));
        assertTrue(CropUtils.plantingSupport(Blocks.SWEET_BERRY_BUSH.defaultBlockState())
                .isEmpty());
    }
}
