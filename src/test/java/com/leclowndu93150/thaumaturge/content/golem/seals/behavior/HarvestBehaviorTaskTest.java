package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class HarvestBehaviorTaskTest {
    @Test
    void explicitTaskDataIdentifiesReplantWork() {
        Task task = Task.atBlock(null, BlockPos.ZERO);
        task.setData(HarvestBehavior.REPLANT_JOB);

        assertTrue(HarvestBehavior.isReplantTask(task, null));
    }

    @Test
    void matchingLegacySiteTaskIdStillIdentifiesReplantWork() {
        Task task = Task.atBlock(null, BlockPos.ZERO);
        task.assignId(7);
        ReplantSite site = new ReplantSite(BlockPos.ZERO, Direction.DOWN, new ItemStack(Items.WHEAT_SEEDS), false);
        site.assign(task.id());

        assertTrue(HarvestBehavior.isReplantTask(task, site));
    }

    @Test
    void unrelatedHarvestTaskIsNotMistakenForReplantWork() {
        Task task = Task.atBlock(null, BlockPos.ZERO);
        task.assignId(8);
        ReplantSite site = new ReplantSite(BlockPos.ZERO, Direction.DOWN, new ItemStack(Items.WHEAT_SEEDS), false);
        site.assign(7);

        assertFalse(HarvestBehavior.isReplantTask(task, site));
    }

    @Test
    void cocoaSupportUsesTheSolidSideWhenStoredFacingPointsIntoAir() {
        assertEquals(Direction.EAST, HarvestBehavior.resolveSupport(Direction.WEST, true, false));
    }

    @Test
    void cocoaSupportKeepsStoredFacingWhenItIsAlreadySolid() {
        assertEquals(Direction.WEST, HarvestBehavior.resolveSupport(Direction.WEST, false, true));
    }

    @Test
    void verticalPlantSupportIsNormalizedToTheBlockBelow() {
        assertEquals(Direction.DOWN, HarvestBehavior.resolveSupport(Direction.UP, false, false));
    }
}
