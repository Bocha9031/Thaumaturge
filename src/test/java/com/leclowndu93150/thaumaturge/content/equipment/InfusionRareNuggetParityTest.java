package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

class InfusionRareNuggetParityTest {
    @Test
    void untaggedBlockDoesNotConsumeRandomness() {
        RandomSource random = mock(RandomSource.class);
        ServerLevel level = level(random);
        BlockState state = mock(BlockState.class);
        BlockDropsEvent event = event(level, state, null, new ArrayList<>());

        InfusionEnchantmentEvents.onBlockDrops(event);

        verify(random, never()).nextDouble();
        verify(random, never()).nextFloat();
    }

    @Test
    void taggedBlockUsesStrictDoubleChanceBoundary() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextDouble()).thenReturn(0.05);
        ServerLevel level = level(random);
        BlockState state = mock(BlockState.class);
        when(state.is(BlockTags.DIAMOND_ORES)).thenReturn(true);
        List<ItemEntity> drops = new ArrayList<>();
        BlockDropsEvent event = event(level, state, null, drops);

        try (MockedConstruction<ItemEntity> nuggets = mockConstruction(ItemEntity.class)) {
            InfusionEnchantmentEvents.onBlockDrops(event);

            assertTrue(nuggets.constructed().isEmpty());
            assertTrue(drops.isEmpty());
        }
        verify(random).nextDouble();
        verify(random, never()).nextFloat();
    }

    @Test
    void overlappingOreTagsUseHighestChance() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextDouble()).thenReturn(0.06);
        ServerLevel level = level(random);
        BlockState state = mock(BlockState.class);
        when(state.is(BlockTags.DIAMOND_ORES)).thenReturn(true);
        when(state.is(BlockTags.EMERALD_ORES)).thenReturn(true);
        List<ItemEntity> drops = new ArrayList<>();
        BlockDropsEvent event = event(level, state, null, drops);

        try (MockedConstruction<ItemEntity> nuggets = mockConstruction(ItemEntity.class)) {
            InfusionEnchantmentEvents.onBlockDrops(event);

            assertEquals(1, nuggets.constructed().size());
            assertEquals(List.of(nuggets.constructed().get(0)), drops);
        }
        verify(random).nextDouble();
        verify(random, never()).nextFloat();
    }

    @Test
    void refiningRollRunsBeforeRareNuggetRoll() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.9F);
        when(random.nextDouble()).thenReturn(0.9);
        ServerLevel level = level(random);
        BlockState state = mock(BlockState.class);
        when(state.is(BlockTags.DIAMOND_ORES)).thenReturn(true);
        Player player = mock(Player.class);
        ItemStack held = mock(ItemStack.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        when(player.getMainHandItem()).thenReturn(held);
        when(held.getEnchantmentLevel(any())).thenReturn(0);
        when(held.isCorrectToolForDrops(state)).thenReturn(true);
        List<ItemEntity> drops = new ArrayList<>(List.of(mock(ItemEntity.class)));
        BlockDropsEvent event = event(level, state, player, drops);
        Item cluster = mock(Item.class);

        try (MockedStatic<InfusionEnchantmentHelper> enchantments = mockStatic(InfusionEnchantmentHelper.class);
                MockedStatic<RefiningResults> refiningResults = mockStatic(RefiningResults.class)) {
            enchantments
                    .when(() -> InfusionEnchantmentHelper.has(held, InfusionEnchantment.REFINING))
                    .thenReturn(true);
            enchantments
                    .when(() -> InfusionEnchantmentHelper.level(held, InfusionEnchantment.REFINING))
                    .thenReturn(1);
            refiningResults.when(() -> RefiningResults.clusterFor(state)).thenReturn(cluster);

            InfusionEnchantmentEvents.onBlockDrops(event);

            InOrder randomOrder = inOrder(random);
            randomOrder.verify(random).nextFloat();
            randomOrder.verify(random).nextDouble();
        }
        assertEquals(1, drops.size());
    }

    @Test
    void refiningChangesProducedDropsEvenWhenToolCannotHarvestBlock() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextFloat()).thenReturn(0.0F);
        ServerLevel level = level(random);
        BlockState state = mock(BlockState.class);
        Player player = mock(Player.class);
        ItemStack held = mock(ItemStack.class);
        ItemEntity drop = mock(ItemEntity.class);
        when(held.getEnchantmentLevel(any())).thenReturn(0);
        when(drop.getItem()).thenReturn(new ItemStack(Items.RAW_IRON));
        List<ItemEntity> drops = new ArrayList<>(List.of(drop));
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        when(player.getMainHandItem()).thenReturn(held);
        when(held.isCorrectToolForDrops(state)).thenReturn(false);
        BlockDropsEvent event = event(level, state, player, drops);

        try (MockedStatic<InfusionEnchantmentHelper> enchantments = mockStatic(InfusionEnchantmentHelper.class);
                MockedStatic<RefiningResults> refiningResults = mockStatic(RefiningResults.class)) {
            enchantments
                    .when(() -> InfusionEnchantmentHelper.has(held, InfusionEnchantment.REFINING))
                    .thenReturn(true);
            enchantments
                    .when(() -> InfusionEnchantmentHelper.level(held, InfusionEnchantment.REFINING))
                    .thenReturn(1);
            refiningResults.when(() -> RefiningResults.clusterFor(state)).thenReturn(Items.IRON_INGOT);

            InfusionEnchantmentEvents.onBlockDrops(event);
        }

        verify(drop).setItem(argThat(stack -> stack.is(Items.IRON_INGOT)));
        assertEquals(List.of(drop), drops);
    }

    @Test
    void silkTouchSkipsRareNuggetRoll() {
        RandomSource random = mock(RandomSource.class);
        ServerLevel level = level(random);
        BlockState state = mock(BlockState.class);
        when(state.is(BlockTags.DIAMOND_ORES)).thenReturn(true);
        Player player = mock(Player.class);
        ItemStack held = mock(ItemStack.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        when(player.getMainHandItem()).thenReturn(held);
        when(held.getEnchantmentLevel(any())).thenReturn(1);
        List<ItemEntity> drops = new ArrayList<>();
        BlockDropsEvent event = event(level, state, player, drops);

        try (MockedStatic<InfusionEnchantmentHelper> enchantments = mockStatic(InfusionEnchantmentHelper.class)) {
            InfusionEnchantmentEvents.onBlockDrops(event);
        }

        verify(random, never()).nextDouble();
        verify(random, never()).nextFloat();
        assertTrue(drops.isEmpty());
    }

    private static ServerLevel level(RandomSource random) {
        ServerLevel level = mock(ServerLevel.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        when(level.getRandom()).thenReturn(random);
        return level;
    }

    private static BlockDropsEvent event(ServerLevel level, BlockState state, Player breaker, List<ItemEntity> drops) {
        BlockDropsEvent event = mock(BlockDropsEvent.class);
        when(event.getLevel()).thenReturn(level);
        when(event.getState()).thenReturn(state);
        when(event.getPos()).thenReturn(BlockPos.ZERO);
        when(event.getBreaker()).thenReturn(breaker);
        when(event.getDrops()).thenReturn(drops);
        return event;
    }
}
