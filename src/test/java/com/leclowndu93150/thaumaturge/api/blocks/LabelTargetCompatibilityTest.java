package com.leclowndu93150.thaumaturge.api.blocks;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.item.LabelItem;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LabelTargetCompatibilityTest {
    private Level level;
    private Player player;
    private UseOnContext context;
    private ItemStack labels;
    private BlockState state;

    @BeforeEach
    void setUp() {
        level = mock(Level.class);
        player = mock(Player.class);
        context = mock(UseOnContext.class);
        state = mock(BlockState.class);
        labels = new ItemStack(TTItems.LABEL.get(), 2);
        when(context.getLevel()).thenReturn(level);
        when(context.getPlayer()).thenReturn(player);
        when(context.getClickedPos()).thenReturn(BlockPos.ZERO);
        when(context.getClickedFace()).thenReturn(Direction.EAST);
        when(context.getItemInHand()).thenReturn(labels);
        when(level.getBlockState(BlockPos.ZERO)).thenReturn(state);
    }

    private LabelTarget blockTarget(boolean accepted) {
        Block block = mock(Block.class, withSettings().extraInterfaces(LabelTarget.class));
        when(state.getBlock()).thenReturn(block);
        LabelTarget target = (LabelTarget) block;
        when(target.attachLabel(level, BlockPos.ZERO, Direction.EAST, player, labels))
                .thenReturn(accepted);
        return target;
    }

    @Test
    void legacyInterfaceForwardsArgumentsAndBothReturnValuesThroughItsDefault() {
        AtomicInteger calls = new AtomicInteger();
        ILabelable legacy = (actualPlayer, pos, face, label) -> {
            assertSame(player, actualPlayer);
            assertEquals(BlockPos.ZERO, pos);
            assertEquals(Direction.EAST, face);
            assertSame(labels, label);
            return calls.incrementAndGet() == 1;
        };
        LabelTarget target = legacy;
        assertTrue(target.attachLabel(level, BlockPos.ZERO, Direction.EAST, player, labels));
        assertFalse(target.attachLabel(level, BlockPos.ZERO, Direction.EAST, player, labels));
        assertEquals(2, calls.get());
        verifyNoInteractions(level, player);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void blockTargetOwnsTheClickWithoutConsultingItsBlockEntity(boolean accepted) {
        LabelTarget target = blockTarget(accepted);
        InteractionResult result = TTItems.LABEL.get().useOn(context);
        assertEquals(accepted ? InteractionResult.SUCCESS : InteractionResult.PASS, result);
        assertEquals(accepted, result.shouldSwing());
        assertEquals(accepted, result.consumesAction());
        assertEquals(accepted, result.indicateItemUse());
        assertEquals(accepted ? 1 : 2, labels.getCount());
        verify(target).attachLabel(level, BlockPos.ZERO, Direction.EAST, player, labels);
        verify(level, never()).getBlockEntity(any());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void blockEntityTargetIsUsedWhenTheBlockIsNotATargetAndCreativeLabelsArePreserved(boolean creative) {
        when(state.getBlock()).thenReturn(mock(Block.class));
        BlockEntity entity = mock(BlockEntity.class, withSettings().extraInterfaces(LabelTarget.class));
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(entity);
        LabelTarget target = (LabelTarget) entity;
        when(target.attachLabel(level, BlockPos.ZERO, Direction.EAST, player, labels))
                .thenReturn(true);
        when(player.hasInfiniteMaterials()).thenReturn(creative);
        assertEquals(InteractionResult.SUCCESS, TTItems.LABEL.get().useOn(context));
        assertEquals(creative ? 2 : 1, labels.getCount());
        verify(target).attachLabel(level, BlockPos.ZERO, Direction.EAST, player, labels);
    }

    @Test
    void clientAcknowledgesAnActualTargetWithoutCallingItOrConsumingLabels() {
        LabelTarget target = blockTarget(true);
        when(level.isClientSide()).thenReturn(true);
        assertEquals(InteractionResult.SUCCESS, TTItems.LABEL.get().useOn(context));
        verify(target, never()).attachLabel(any(), any(), any(), any(), any());
        assertEquals(2, labels.getCount());
        verify(level, never()).getBlockEntity(any());
    }

    @Test
    void clientPassesThroughClicksOnNonTargetsAndClicksWithoutPlayers() {
        when(level.isClientSide()).thenReturn(true);
        when(state.getBlock()).thenReturn(mock(Block.class));
        assertEquals(InteractionResult.PASS, TTItems.LABEL.get().useOn(context));
        assertEquals(2, labels.getCount());
        LabelTarget target = blockTarget(true);
        when(context.getPlayer()).thenReturn(null);
        assertEquals(InteractionResult.PASS, TTItems.LABEL.get().useOn(context));
        verify(target, never()).attachLabel(any(), any(), any(), any(), any());
        assertEquals(2, labels.getCount());
    }

    @Test
    void blockEntityRefusalDoesNotConsumeALabel() {
        when(state.getBlock()).thenReturn(mock(Block.class));
        BlockEntity entity = mock(BlockEntity.class, withSettings().extraInterfaces(LabelTarget.class));
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(entity);
        assertEquals(InteractionResult.PASS, TTItems.LABEL.get().useOn(context));
        assertEquals(2, labels.getCount());
    }

    @Test
    void unregisteredAspectHoldersUseTheCutoffArgumentFailure() {
        Holder<IAspect> direct = Holder.direct(mock(IAspect.class));
        IllegalArgumentException failure =
                assertThrows(IllegalArgumentException.class, () -> LabelItem.withAspect(direct));
        assertTrue(failure.getMessage().startsWith("Aspect holder has no registry key: "));
    }
}
