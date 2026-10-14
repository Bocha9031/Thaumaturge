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

class CasterBlockActionsTest {
    @Test
    void legacyAndCurrentRegistrationsShareOneOrderedRegistry() {
        BlockState state = mock(BlockState.class);
        List<Integer> seen = new ArrayList<>();
        CasterBlockActions.onState(state, "first", use -> {
            seen.add(1);
            return false;
        });
        CasterTriggerRegistry.registerCasterBlockTrigger(
                (level, stack, player, pos, side, event) -> {
                    seen.add(event);
                    return false;
                },
                2,
                state,
                "second");
        CasterBlockActions.onState(state, "third", use -> {
            seen.add(3);
            return true;
        });
        assertTrue(CasterTriggerRegistry.hasTrigger(state, "first"));
        assertTrue(CasterBlockActions.hasAction(state, "second"));
        assertTrue(CasterBlockActions.dispatch(use(state)));
        assertEquals(List.of(1, 2, 3), seen);
        seen.clear();
        assertTrue(
                CasterTriggerRegistry.performTrigger(null, ItemStack.EMPTY, null, BlockPos.ZERO, Direction.UP, state));
        assertEquals(List.of(1, 2, 3), seen);
    }

    @Test
    void groupedActionsNeverDispatchTagActions() {
        BlockState state = mock(BlockState.class);
        TagKey<Block> tag =
                TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("test", "caster_actions"));
        when(state.is(tag)).thenReturn(true);
        List<String> seen = new ArrayList<>();
        CasterBlockActions.onTag(tag, use -> {
            seen.add("tag");
            return true;
        });
        CasterBlockActions.onState(state, "one", use -> {
            seen.add("state");
            return false;
        });
        assertFalse(CasterBlockActions.dispatch(use(state), "one"));
        assertEquals(List.of("state"), seen);
        seen.clear();
        assertTrue(CasterBlockActions.dispatch(use(state)));
        assertEquals(List.of("state", "tag"), seen);
    }

    @Test
    void duplicateActionsRemainAndSuccessfulActionStopsLaterActions() {
        BlockState state = mock(BlockState.class);
        List<CasterBlockUse> seen = new ArrayList<>();
        CasterBlockAction action = use -> {
            seen.add(use);
            return false;
        };
        CasterBlockActions.onState(state, action);
        CasterBlockActions.onState(state, action);
        CasterBlockActions.onState(state, use -> true);
        CasterBlockActions.onState(state, use -> fail("Must stop after successful action"));
        CasterBlockUse click = use(state);
        assertTrue(CasterBlockActions.hasAction(state, CasterBlockActions.DEFAULT_GROUP));
        assertTrue(CasterBlockActions.dispatch(click));
        assertEquals(List.of(click, click), seen);
        assertSame(click, seen.get(0));
    }

    @Test
    void actionExceptionPropagatesWithoutTryingAnotherAction() {
        BlockState state = mock(BlockState.class);
        RuntimeException failure = new IllegalStateException("action failure");
        CasterBlockActions.onState(state, use -> {
            throw failure;
        });
        CasterBlockActions.onState(state, use -> fail("Must not run after thrown exception"));
        assertSame(failure, assertThrows(RuntimeException.class, () -> CasterBlockActions.dispatch(use(state))));
    }

    @Test
    void unknownStateAndGroupHaveNoAction() {
        BlockState state = mock(BlockState.class);
        assertFalse(CasterBlockActions.hasAction(state));
        assertFalse(CasterBlockActions.hasAction(state, "missing"));
        assertFalse(CasterBlockActions.dispatch(use(state)));
        assertFalse(CasterBlockActions.dispatch(use(state), "missing"));
    }

    private static CasterBlockUse use(BlockState state) {
        return new CasterBlockUse(null, ItemStack.EMPTY, null, BlockPos.ZERO, Direction.UP, state);
    }
}
