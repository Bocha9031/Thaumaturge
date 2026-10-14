package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayNetwork;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEssentiaTransport;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdvancedFurnaceCutoffTest {
    private ServerLevel level;
    private RegistryAccess registries;
    private RandomSource random;
    private Holder<IAspect> order;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> aspects = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(aspects, TTAspects.ORDO, mock(IAspect.class));
        order = aspects.getHolderOrThrow(TTAspects.ORDO);
        registries = new RegistryAccess.ImmutableRegistryAccess(
                        Stream.concat(BuiltInRegistries.REGISTRY.stream(), Stream.of(aspects))
                                .toList())
                .freeze();
        level = mock(ServerLevel.class);
        random = mock(RandomSource.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.getRandom()).thenReturn(random);
        when(random.nextFloat()).thenReturn(0.5F);
        when(level.getGameTime()).thenReturn(1L);
    }

    private BlockEntityAdvancedAlchemicalFurnace furnace(int heat, int perditio, int aqua) {
        BlockEntityAdvancedAlchemicalFurnace furnace = spy(new BlockEntityAdvancedAlchemicalFurnace(
                BlockPos.ZERO, TTBlocks.ADVANCED_ALCHEMICAL_FURNACE.get().defaultBlockState()));
        furnace.setLevel(level);
        CompoundTag saved = new CompoundTag();
        saved.putBoolean("Assembled", true);
        saved.putInt("Heat", heat);
        saved.putInt("Perditio", perditio);
        saved.putInt("Aqua", aqua);
        furnace.loadAdditional(saved, registries);
        return furnace;
    }

    private void offer(BlockEntityAdvancedAlchemicalFurnace furnace, int points) {
        ItemStack input = new ItemStack(Items.DIRT, 8);
        try (var index = mockStatic(AspectIndexAccess.class)) {
            index.when(() -> AspectIndexAccess.of(input)).thenReturn(AspectList.EMPTY.add(order, points));
            assertTrue(furnace.insertInput(input));
        }
        assertEquals(8, input.getCount());
    }

    private void tick(BlockEntityAdvancedAlchemicalFurnace furnace) {
        try (var structure = mockStatic(AdvancedAlchemicalFurnaceStructure.class)) {
            structure
                    .when(() -> AdvancedAlchemicalFurnaceStructure.isLoaded(level, BlockPos.ZERO))
                    .thenReturn(true);
            structure
                    .when(() -> AdvancedAlchemicalFurnaceStructure.isFormed(eq(level), eq(BlockPos.ZERO), any()))
                    .thenReturn(true);
            BlockEntityAdvancedAlchemicalFurnace.serverTick(level, BlockPos.ZERO, furnace.getBlockState(), furnace);
        }
    }

    @Test
    void integerFloorCooldownAndExactBubbleSoundUsePostPaymentHeat() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace(500, 500, 500);
        offer(furnace, 2);
        clearInvocations(furnace, level);
        tick(furnace);
        assertEquals(496, furnace.heat());
        assertEquals(498, furnace.perditio());
        assertEquals(498, furnace.aqua());
        assertEquals(2, furnace.aspects().amountOf(order));
        assertEquals(5, furnace.cooldown());
        assertEquals(5, furnace.cycleDuration());
        verify(level)
                .playSound(null, BlockPos.ZERO, TTSounds.BUBBLE.get(), SoundSource.BLOCKS, 0.3F, 0.9F + 0.5F * 0.3F);
        verify(level, times(1)).sendBlockUpdated(any(), any(), any(), eq(Block.UPDATE_ALL));
        verify(furnace, times(1)).setChanged();
    }

    @Test
    void maximumAffordableOneItemUsesAllHeatAndFullCooldownRange() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace(500, 500, 500);
        offer(furnace, 250);
        tick(furnace);
        assertEquals(0, furnace.heat());
        assertEquals(250, furnace.perditio());
        assertEquals(250, furnace.aqua());
        assertEquals(250, furnace.aspects().totalAmount());
        assertEquals(105, furnace.cooldown());
        assertEquals(105, furnace.cycleDuration());
        assertTrue(furnace.input().isEmpty());
    }

    @Test
    void completedCooldownRetainsCycleDurationAndDoesNotSyncProgressEveryTick() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace(500, 500, 500);
        assertTrue(furnace.isAssembled());
        CompoundTag saved = furnace.getUpdateTag(registries);
        saved.putInt("Cooldown", 1);
        saved.putInt("CycleDuration", 17);
        furnace.loadAdditional(saved, registries);
        clearInvocations(furnace, level);
        tick(furnace);
        assertEquals(0, furnace.cooldown());
        assertEquals(17, furnace.cycleDuration());
        verify(furnace, times(1)).setChanged();
        verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
        clearInvocations(furnace, level);
        tick(furnace);
        verify(furnace, never()).setChanged();
        assertEquals(17, furnace.cycleDuration());
    }

    @Test
    void waitingInputProcessesOnTickThatCooldownReachesZero() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace(500, 500, 500);
        CompoundTag saved = furnace.getUpdateTag(registries);
        saved.putInt("Cooldown", 1);
        saved.putInt("CycleDuration", 17);
        furnace.loadAdditional(saved, registries);
        offer(furnace, 3);
        clearInvocations(furnace, level);
        tick(furnace);
        assertTrue(furnace.input().isEmpty());
        assertEquals(3, furnace.aspects().totalAmount());
        assertEquals(6, furnace.cooldown());
        assertEquals(6, furnace.cycleDuration());
        verify(furnace, times(1)).setChanged();
        verify(level, times(1)).sendBlockUpdated(any(), any(), any(), anyInt());
    }

    @Test
    void loadedInputThatNowHasNoAspectsIsNotSilentlyConsumedOrGivenCooldown() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace(500, 500, 500);
        CompoundTag saved = furnace.getUpdateTag(registries);
        TTNbt.store(saved, "Input", ItemStack.CODEC, registries, new ItemStack(Items.DIRT));
        furnace.loadAdditional(saved, registries);
        clearInvocations(furnace, level);
        try (var index = mockStatic(AspectIndexAccess.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY);
            tick(furnace);
            tick(furnace);
            index.verify(() -> AspectIndexAccess.of(any(ItemStack.class)), times(1));
        }
        assertTrue(furnace.input().is(Items.DIRT));
        assertTrue(furnace.aspects().isEmpty());
        assertEquals(500, furnace.heat());
        assertEquals(0, furnace.cooldown());
        verify(furnace, never()).setChanged();
        verify(level, never())
                .playSound(
                        isNull(),
                        eq(BlockPos.ZERO),
                        eq(TTSounds.BUBBLE.get()),
                        eq(SoundSource.BLOCKS),
                        anyFloat(),
                        anyFloat());
    }

    @Test
    void rechargeTracksIndividualReserveChangesEvenWhenTotalSumDoesNotMove() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace(400, 400, 500);
        when(level.getGameTime()).thenReturn(5L);
        clearInvocations(furnace, level);
        try (var relay = mockStatic(VisRelayNetwork.class)) {
            relay.when(() -> VisRelayNetwork.drainEverySourceNear(level, BlockPos.ZERO, TTAspects.PERDITIO, 50))
                    .thenReturn(1);
            tick(furnace);
            relay.verify(() -> VisRelayNetwork.drainEverySourceNear(level, BlockPos.ZERO, TTAspects.IGNIS, 50));
            relay.verify(() -> VisRelayNetwork.drainNodesNear(level, BlockPos.ZERO, TTAspects.IGNIS, 50));
            relay.verify(() -> VisRelayNetwork.drainNodesNear(level, BlockPos.ZERO, TTAspects.PERDITIO, 49));
            relay.verify(
                    () -> VisRelayNetwork.drainEverySourceNear(
                            eq(level), eq(BlockPos.ZERO), eq(TTAspects.AQUA), anyInt()),
                    never());
        }
        assertEquals(399, furnace.heat());
        assertEquals(401, furnace.perditio());
        assertEquals(500, furnace.aqua());
        verify(level, times(1)).sendBlockUpdated(any(), any(), any(), anyInt());
        verify(furnace, times(2)).setChanged();
    }

    @Test
    void unchangedRechargeDoesNotDirtyAndReservesAreRechargedBeforeInputPayment() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace(500, 500, 500);
        when(level.getGameTime()).thenReturn(5L);
        clearInvocations(furnace, level);
        try (var relay = mockStatic(VisRelayNetwork.class)) {
            relay.when(() -> VisRelayNetwork.drainEverySourceNear(level, BlockPos.ZERO, TTAspects.IGNIS, 1))
                    .thenReturn(1);
            tick(furnace);
            verify(furnace, never()).setChanged();
            verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
            offer(furnace, 2);
            tick(furnace);
        }
        assertEquals(496, furnace.heat());
        assertEquals(5, furnace.cooldown());
    }

    @Test
    void nativeLoadPreservesSchemaClampsBadCountersAndClearsAspectLookupCache() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace(500, 500, 500);
        offer(furnace, 2);
        CompoundTag saved = furnace.getUpdateTag(registries);
        assertEquals(
                Set.of("Aspects", "Input", "Heat", "Perditio", "Aqua", "Cooldown", "CycleDuration", "Assembled"),
                saved.getAllKeys());
        saved.putInt("Heat", -5);
        saved.putInt("Perditio", Integer.MAX_VALUE);
        saved.putInt("Aqua", 501);
        saved.putInt("Cooldown", 7);
        saved.putInt("CycleDuration", -10);
        furnace.loadAdditional(saved, registries);
        try (var index = mockStatic(AspectIndexAccess.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY.add(order, 3));
            assertEquals(3, furnace.inputCost());
            assertEquals(3, furnace.inputCost());
            index.verify(() -> AspectIndexAccess.of(any(ItemStack.class)), times(1));
        }
        assertEquals(0, furnace.heat());
        assertEquals(500, furnace.perditio());
        assertEquals(500, furnace.aqua());
        assertEquals(7, furnace.cooldown());
        assertEquals(7, furnace.cycleDuration());
        CompoundTag roundTrip = furnace.getUpdateTag(registries);
        BlockEntityAdvancedAlchemicalFurnace loaded = furnace(0, 0, 0);
        loaded.loadAdditional(roundTrip, registries);
        assertEquals(roundTrip, loaded.getUpdateTag(registries));
        loaded.loadAdditional(new CompoundTag(), registries);
        assertFalse(loaded.isAssembled());
        assertTrue(loaded.input().isEmpty());
        assertTrue(loaded.aspects().isEmpty());
    }

    @Test
    void nativeRemovalDropsCopiedInputAndPollutesStoredEssentiaOnlyOnce() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace(500, 500, 500);
        CompoundTag saved = furnace.getUpdateTag(registries);
        TTNbt.store(saved, "Input", ItemStack.CODEC, registries, new ItemStack(Items.DIRT));
        TTNbt.store(saved, "Aspects", AspectList.CODEC, registries, AspectList.EMPTY.add(order, 9));
        furnace.loadAdditional(saved, registries);
        ItemStack original = furnace.input();
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(furnace);
        BlockAdvancedAlchemicalFurnace block =
                (BlockAdvancedAlchemicalFurnace) TTBlocks.ADVANCED_ALCHEMICAL_FURNACE.get();
        try (var aura = mockStatic(AuraHelper.class);
                var structure = mockStatic(AdvancedAlchemicalFurnaceStructure.class);
                var drops = mockConstruction(
                        ItemEntity.class,
                        (entity, context) -> when(entity.getItem())
                                .thenReturn((ItemStack) context.arguments().get(4)))) {
            furnace.setRemoved();
            aura.verifyNoInteractions();
            block.onRemove(
                    furnace.getBlockState(),
                    level,
                    BlockPos.ZERO,
                    furnace.getBlockState().setValue(BlockAdvancedAlchemicalFurnace.LIT, true),
                    false);
            assertEquals(9, furnace.aspects().totalAmount());
            block.onRemove(furnace.getBlockState(), level, BlockPos.ZERO, Blocks.AIR.defaultBlockState(), false);
            furnace.dropContents(BlockPos.ZERO);
            assertEquals(1, drops.constructed().size());
            ItemEntity drop = drops.constructed().getFirst();
            assertNotSame(original, drop.getItem());
            assertTrue(ItemStack.isSameItemSameComponents(original, drop.getItem()));
            verify(level).addFreshEntity(drop);
            aura.verify(() -> AuraHelper.polluteAura(level, BlockPos.ZERO, 9, true), times(1));
            structure.verify(() -> AdvancedAlchemicalFurnaceStructure.restore(level, BlockPos.ZERO), times(1));
        }
        assertTrue(furnace.input().isEmpty());
        assertTrue(furnace.aspects().isEmpty());
    }

    @Test
    void realStructureRefreshInvalidatesAllFourNozzlesAndKeepsPassiveSimulationPure() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace(500, 500, 500);
        assertTrue(furnace.isAssembled());
        CompoundTag saved = furnace.getUpdateTag(registries);
        TTNbt.store(saved, "Aspects", AspectList.CODEC, registries, AspectList.EMPTY.add(order, 9));
        furnace.loadAdditional(saved, registries);
        when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(furnace);
        clearInvocations(furnace, level);
        try (var tubes = mockStatic(BlockEssentiaTransport.class)) {
            AdvancedAlchemicalFurnaceStructure.refreshNozzles(level, BlockPos.ZERO);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos mouth = BlockPos.ZERO.relative(side);
                verify(level).invalidateCapabilities(mouth);
                tubes.verify(() -> BlockEssentiaTransport.refreshConnectionsAround(level, mouth));
            }
        }
        assertEquals(9, furnace.aspects().totalAmount());
        verify(furnace, never()).setChanged();
        verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
    }

    @Test
    void realStructureUsesExactPlaceholderLayoutAndRestoresOnlyMatchingParts() {
        Map<BlockPos, BlockState> cells = new HashMap<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                boolean corner = x != 0 && z != 0;
                cells.put(
                        new BlockPos(x, 0, z),
                        (corner
                                        ? TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER
                                        : TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE)
                                .get()
                                .defaultBlockState());
                cells.put(
                        new BlockPos(x, 1, z),
                        (corner
                                        ? TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER
                                        : TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER)
                                .get()
                                .defaultBlockState());
            }
        }
        when(level.getBlockState(any(BlockPos.class)))
                .thenAnswer(call -> cells.getOrDefault(
                        ((BlockPos) call.getArgument(0)).immutable(), Blocks.AIR.defaultBlockState()));
        assertTrue(AdvancedAlchemicalFurnaceStructure.isFormed(level, BlockPos.ZERO, new BlockPos.MutableBlockPos()));
        cells.put(new BlockPos(-1, 1, -1), Blocks.STONE.defaultBlockState());
        assertFalse(AdvancedAlchemicalFurnaceStructure.isFormed(level, BlockPos.ZERO, new BlockPos.MutableBlockPos()));
        AdvancedAlchemicalFurnaceStructure.restore(level, BlockPos.ZERO);
        verify(level, never()).setBlock(eq(new BlockPos(-1, 1, -1)), any(), anyInt());
        verify(level).setBlock(new BlockPos(1, 1, 1), TTBlocks.ALEMBIC.get().defaultBlockState(), Block.UPDATE_ALL);
        verify(level)
                .setBlock(
                        new BlockPos(1, 1, 0),
                        TTBlocks.ALCHEMICAL_CONSTRUCT.get().defaultBlockState(),
                        Block.UPDATE_ALL);
        verify(level)
                .setBlock(
                        new BlockPos(1, 0, 0),
                        TTBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get().defaultBlockState(),
                        Block.UPDATE_ALL);
    }
}
