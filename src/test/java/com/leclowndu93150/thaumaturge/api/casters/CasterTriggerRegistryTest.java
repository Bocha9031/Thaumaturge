package com.leclowndu93150.thaumaturge.api.casters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

class CasterTriggerRegistryTest {
    @Test
    void defaultRegistrationUsesThaumaturgeGroup() {
        BlockState state = mock(BlockState.class);
        CasterTriggerRegistry.registerCasterBlockTrigger((level, stack, player, pos, side, event) -> true, 9, state);
        assertTrue(CasterTriggerRegistry.hasTrigger(state));
        assertTrue(CasterTriggerRegistry.hasTrigger(state, "thaumaturge"));
        assertFalse(CasterTriggerRegistry.hasTrigger(state, "default"));
    }

    @Test
    void stateTriggersKeepOverallRegistrationOrderAcrossGroups() {
        BlockState state = mock(BlockState.class);
        List<Integer> seen = new ArrayList<>();
        ICasterTriggerManager manager = (level, stack, player, pos, side, event) -> {
            seen.add(event);
            return event == 3;
        };
        CasterTriggerRegistry.registerCasterBlockTrigger(manager, 1, state, "addon_a");
        CasterTriggerRegistry.registerCasterBlockTrigger(manager, 2, state, "addon_b");
        CasterTriggerRegistry.registerCasterBlockTrigger(manager, 3, state, "addon_a");
        CasterTriggerRegistry.registerCasterBlockTrigger(manager, 4, state, "addon_b");
        assertTrue(
                CasterTriggerRegistry.performTrigger(null, ItemStack.EMPTY, null, BlockPos.ZERO, Direction.UP, state));
        assertEquals(List.of(1, 2, 3), seen);
        seen.clear();
        assertFalse(CasterTriggerRegistry.performTrigger(
                null, ItemStack.EMPTY, null, BlockPos.ZERO, Direction.UP, state, "addon_b"));
        assertEquals(List.of(2, 4), seen);
    }

    @Test
    void stateFailureFallsThroughToTagButGroupedLookupDoesNot() {
        BlockState state = mock(BlockState.class);
        TagKey<Block> tag =
                TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("test", "caster_trigger"));
        when(state.is(tag)).thenReturn(true);
        List<Integer> seen = new ArrayList<>();
        ICasterTriggerManager manager = (level, stack, player, pos, side, event) -> {
            seen.add(event);
            return event == 2;
        };
        CasterTriggerRegistry.registerCasterBlockTrigger(manager, 1, state, "test_group");
        CasterTriggerRegistry.registerCasterBlockTagTrigger(manager, 2, tag);
        assertTrue(
                CasterTriggerRegistry.performTrigger(null, ItemStack.EMPTY, null, BlockPos.ZERO, Direction.UP, state));
        assertEquals(List.of(1, 2), seen);
        seen.clear();
        assertFalse(CasterTriggerRegistry.performTrigger(
                null, ItemStack.EMPTY, null, BlockPos.ZERO, Direction.UP, state, "test_group"));
        assertEquals(List.of(1), seen);
    }

    @Test
    void unknownStateAndGroupDoNotInvokeHandlers() {
        BlockState unknown = mock(BlockState.class);
        assertFalse(CasterTriggerRegistry.hasTrigger(unknown));
        assertFalse(CasterTriggerRegistry.hasTrigger(unknown, "missing"));
        assertFalse(CasterTriggerRegistry.performTrigger(
                null, ItemStack.EMPTY, null, BlockPos.ZERO, Direction.UP, unknown));
        assertFalse(CasterTriggerRegistry.performTrigger(
                null, ItemStack.EMPTY, null, BlockPos.ZERO, Direction.UP, unknown, "missing"));
    }
}
