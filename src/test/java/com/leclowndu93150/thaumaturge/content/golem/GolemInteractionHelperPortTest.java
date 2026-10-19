package com.leclowndu93150.thaumaturge.content.golem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class GolemInteractionHelperPortTest {
    @Test
    void onlyMovesBlockPlacerWhenAdjacentPlacementSpaceIntersectsGolem() {
        ItemStack block = new ItemStack(Items.STONE);
        BlockPos target = BlockPos.ZERO;

        assertFalse(GolemInteractionHelper.shouldMoveForPlacement(
                block, target, Direction.UP, new AABB(0.2D, 0.2D, 0.2D, 0.8D, 0.9D, 0.8D)));
        assertTrue(GolemInteractionHelper.shouldMoveForPlacement(
                block, target, Direction.EAST, new AABB(0.8D, 0.2D, 0.2D, 1.4D, 0.9D, 0.8D)));
    }

    @Test
    void nonBlockItemsNeverTriggerPlacementRelocation() {
        assertFalse(GolemInteractionHelper.shouldMoveForPlacement(
                new ItemStack(Items.STICK),
                BlockPos.ZERO,
                Direction.EAST,
                new AABB(0.8D, 0.2D, 0.2D, 1.4D, 0.9D, 0.8D)));
    }

    @Test
    void interactionHitPointLiesOnSelectedFace() {
        assertEquals(new Vec3(0.5D, 0.5D, 0.0D), GolemInteractionHelper.hitLocation(BlockPos.ZERO, Direction.NORTH));
        assertEquals(new Vec3(0.5D, 1.0D, 0.5D), GolemInteractionHelper.hitLocation(BlockPos.ZERO, Direction.UP));
    }
}
