package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.content.entity.EntityFollowingItem;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

class InfusionHarvestPerkEventTest {
    @Test
    void collectorKeepsFollowersInDropsWithoutSpawningBeforeExpansion() {
        ServerLevel level = mock(ServerLevel.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        Player player = player(false);
        ItemStack held = player.getMainHandItem();
        BlockState state = mock(BlockState.class);
        ItemEntity original = mock(ItemEntity.class);
        ItemStack originalStack = new ItemStack(Items.RAW_IRON, 3);
        Vec3 motion = new Vec3(0.25, 0.5, -0.25);
        when(original.getItem()).thenReturn(originalStack);
        when(original.getDeltaMovement()).thenReturn(motion);
        when(original.getX()).thenReturn(1.0);
        when(original.getY()).thenReturn(2.0);
        when(original.getZ()).thenReturn(3.0);
        List<ItemEntity> drops = new ArrayList<>(List.of(original));
        BlockDropsEvent event = event(level, state, player, drops);
        List<String> chronology = new ArrayList<>();
        List<ItemStack> followerStacks = new ArrayList<>();
        BlockState neighbour = mock(BlockState.class);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(neighbour);
        when(neighbour.getDestroySpeed(eq(level), any(BlockPos.class))).thenReturn(0.0F);
        when(held.getDestroySpeed(neighbour)).thenReturn(2.0F);
        when(held.isCorrectToolForDrops(state)).thenReturn(true);
        when(player.getViewVector(1.0F)).thenReturn(new Vec3(0.0, 0.0, 1.0));
        when(level.isEmptyBlock(BlockPos.ZERO)).thenReturn(true);

        try (MockedStatic<InfusionEnchantmentHelper> enchantments = mockStatic(InfusionEnchantmentHelper.class);
                MockedStatic<EnchantMining> mining = mockStatic(EnchantMining.class);
                MockedConstruction<EntityFollowingItem> followers =
                        mockConstruction(EntityFollowingItem.class, (follow, context) -> {
                            chronology.add("follow");
                            followerStacks.add((ItemStack) context.arguments().get(4));
                        })) {
            enchantments
                    .when(() -> InfusionEnchantmentHelper.has(held, InfusionEnchantment.COLLECTOR))
                    .thenReturn(true);
            enchantments
                    .when(() -> InfusionEnchantmentHelper.has(held, InfusionEnchantment.DESTRUCTIVE))
                    .thenReturn(true);
            enchantments
                    .when(() -> InfusionEnchantmentHelper.has(held, InfusionEnchantment.LAMPLIGHT))
                    .thenReturn(true);
            when(level.setBlock(eq(BlockPos.ZERO), any(BlockState.class), eq(Block.UPDATE_ALL)))
                    .thenAnswer(invocation -> {
                        chronology.add("lamp");
                        return Boolean.TRUE;
                    });
            mining.when(() -> EnchantMining.harvestBlock(eq(level), eq(player), any(BlockPos.class), eq(false)))
                    .thenAnswer(invocation -> {
                        chronology.add("expand");
                        return Boolean.TRUE;
                    });

            InfusionEnchantmentEvents.onBlockDrops(event);

            assertEquals(1, followers.constructed().size());
            EntityFollowingItem follow = followers.constructed().get(0);
            assertEquals(List.of(follow), drops);
            assertNotSame(originalStack, followerStacks.get(0));
            assertEquals(Items.RAW_IRON, followerStacks.get(0).getItem());
            assertEquals(3, followerStacks.get(0).getCount());
            verify(follow).setDeltaMovement(motion);
            verify(follow).setDefaultPickUpDelay();
            assertTrue(chronology.indexOf("follow") >= 0);
            assertTrue(chronology.indexOf("follow") < chronology.indexOf("lamp"));
            assertTrue(chronology.indexOf("lamp") < chronology.indexOf("expand"));
            verify(level, never()).addFreshEntity(any(Entity.class));
        }
    }

    @Test
    void collectorDoesNotReplaceDropsWhenPlayerSneaks() {
        ServerLevel level = mock(ServerLevel.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        Player player = player(true);
        ItemStack held = player.getMainHandItem();
        BlockState state = mock(BlockState.class);
        ItemEntity original = mock(ItemEntity.class);
        List<ItemEntity> drops = new ArrayList<>(List.of(original));
        BlockDropsEvent event = event(level, state, player, drops);

        try (MockedStatic<InfusionEnchantmentHelper> enchantments = mockStatic(InfusionEnchantmentHelper.class);
                MockedConstruction<EntityFollowingItem> followers = mockConstruction(EntityFollowingItem.class)) {
            enchantments
                    .when(() -> InfusionEnchantmentHelper.has(held, InfusionEnchantment.COLLECTOR))
                    .thenReturn(true);

            InfusionEnchantmentEvents.onBlockDrops(event);

            assertEquals(List.of(original), drops);
            assertTrue(followers.constructed().isEmpty());
            verify(level, never()).addFreshEntity(any(Entity.class));
        }
    }

    @Test
    void nonCollectorLeavesDropsForNativeEventDispatch() {
        ServerLevel level = mock(ServerLevel.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        Player player = player(false);
        ItemStack held = player.getMainHandItem();
        BlockState state = mock(BlockState.class);
        ItemEntity original = mock(ItemEntity.class);
        List<ItemEntity> drops = new ArrayList<>(List.of(original));
        BlockDropsEvent event = event(level, state, player, drops);

        try (MockedStatic<InfusionEnchantmentHelper> enchantments = mockStatic(InfusionEnchantmentHelper.class);
                MockedConstruction<EntityFollowingItem> followers = mockConstruction(EntityFollowingItem.class)) {
            enchantments
                    .when(() -> InfusionEnchantmentHelper.has(held, InfusionEnchantment.COLLECTOR))
                    .thenReturn(false);

            InfusionEnchantmentEvents.onBlockDrops(event);

            assertSame(original, drops.get(0));
            assertTrue(followers.constructed().isEmpty());
            verify(level, never()).addFreshEntity(any(Entity.class));
        }
    }

    @Test
    void destructiveExpansionStopsAfterTheToolBreaks() {
        ServerLevel level = mock(ServerLevel.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        Player player = player(false);
        ItemStack held = player.getMainHandItem();
        AtomicBoolean broken = new AtomicBoolean();
        when(held.isEmpty()).thenAnswer(invocation -> broken.get());
        doAnswer(invocation -> {
                    broken.set(true);
                    return null;
                })
                .when(held)
                .hurtAndBreak(anyInt(), eq(player), eq(EquipmentSlot.MAINHAND));

        BlockState state = mock(BlockState.class);
        BlockState neighbour = mock(BlockState.class);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(neighbour);
        when(neighbour.getDestroySpeed(eq(level), any(BlockPos.class))).thenReturn(0.0F);
        when(held.getDestroySpeed(neighbour)).thenReturn(2.0F);
        when(held.isCorrectToolForDrops(state)).thenReturn(true);
        when(player.getViewVector(1.0F)).thenReturn(new Vec3(0.0, 0.0, 1.0));

        BlockDropsEvent event = event(level, state, player, new ArrayList<>());
        try (MockedStatic<InfusionEnchantmentHelper> enchantments = mockStatic(InfusionEnchantmentHelper.class);
                MockedStatic<EnchantMining> mining = mockStatic(EnchantMining.class)) {
            enchantments
                    .when(() -> InfusionEnchantmentHelper.has(held, InfusionEnchantment.DESTRUCTIVE))
                    .thenReturn(true);
            mining.when(() -> EnchantMining.harvestBlock(eq(level), eq(player), any(BlockPos.class), eq(false)))
                    .thenReturn(true);

            InfusionEnchantmentEvents.onBlockDrops(event);

            mining.verify(() -> EnchantMining.harvestBlock(eq(level), eq(player), any(BlockPos.class), eq(false)));
        }
    }

    private static Player player(boolean sneaking) {
        Player player = mock(Player.class);
        ItemStack held = mock(ItemStack.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        when(player.getMainHandItem()).thenReturn(held);
        when(player.isShiftKeyDown()).thenReturn(sneaking);
        when(held.getEnchantmentLevel(any())).thenReturn(0);
        return player;
    }

    private static BlockDropsEvent event(ServerLevel level, BlockState state, Player player, List<ItemEntity> drops) {
        BlockDropsEvent event = mock(BlockDropsEvent.class);
        when(event.getLevel()).thenReturn(level);
        when(event.getState()).thenReturn(state);
        when(event.getPos()).thenReturn(BlockPos.ZERO);
        when(event.getBreaker()).thenReturn(player);
        when(event.getDrops()).thenReturn(drops);
        return event;
    }
}
