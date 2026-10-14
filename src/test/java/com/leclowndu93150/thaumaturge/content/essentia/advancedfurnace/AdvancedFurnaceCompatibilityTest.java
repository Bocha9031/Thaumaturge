package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayNetwork;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Lifecycle;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdvancedFurnaceCompatibilityTest {
    private ServerLevel level;
    private RegistryAccess registries;
    private Holder<IAspect> order;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, TTAspects.ORDO, mock(IAspect.class));
        order = registry.getHolderOrThrow(TTAspects.ORDO);
        registries = new RegistryAccess.ImmutableRegistryAccess(
                        Stream.concat(BuiltInRegistries.REGISTRY.stream(), Stream.of(registry))
                                .toList())
                .freeze();
        level = mock(ServerLevel.class);
        when(level.registryAccess()).thenReturn(registries);
        when(level.getGameTime()).thenReturn(1L);
        when(level.getRandom()).thenReturn(mock(RandomSource.class));
    }

    private BlockEntityAdvancedAlchemicalFurnace furnace() {
        BlockEntityAdvancedAlchemicalFurnace furnace = new BlockEntityAdvancedAlchemicalFurnace(
                BlockPos.ZERO, TTBlocks.ADVANCED_ALCHEMICAL_FURNACE.get().defaultBlockState());
        furnace.setLevel(level);
        return furnace;
    }

    private CompoundTag reserves(boolean assembled, int amount) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Assembled", assembled);
        tag.putInt("Heat", amount);
        tag.putInt("Aqua", amount);
        tag.putInt("Perditio", amount);
        return tag;
    }

    @Test
    void nativeNbtRoundTripsAllStoredStateAndClampsReserves() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace();
        CompoundTag tag = reserves(true, 400);
        tag.putInt("Cooldown", 7);
        tag.putInt("CycleDuration", 15);
        TTNbt.store(tag, "Input", ItemStack.CODEC, registries, new ItemStack(Items.DIRT));
        TTNbt.store(tag, "Aspects", AspectList.CODEC, registries, AspectList.EMPTY.add(order, 12));
        furnace.loadAdditional(tag, registries);
        assertEquals(
                Set.of("Aspects", "Input", "Heat", "Perditio", "Aqua", "Cooldown", "CycleDuration", "Assembled"),
                furnace.getUpdateTag(registries).getAllKeys());
        assertEquals(tag, furnace.getUpdateTag(registries));
        tag.putInt("Heat", Integer.MAX_VALUE);
        tag.putInt("Perditio", -1);
        tag.putInt("Cooldown", -10);
        tag.putInt("CycleDuration", -3);
        furnace.loadAdditional(tag, registries);
        assertEquals(500, furnace.heat());
        assertEquals(0, furnace.perditio());
        assertEquals(0, furnace.cooldown());
        assertEquals(0, furnace.cycleDuration());
    }

    @Test
    void inputRequiresServerAssemblyAndFitsOneItemWithoutConsumingTheCallerStack() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace();
        ItemStack input = new ItemStack(Items.DIRT, 8);
        assertFalse(furnace.insertInput(input));
        furnace.loadAdditional(reserves(true, 500), registries);
        Level client = mock(Level.class);
        when(client.isClientSide()).thenReturn(true);
        furnace.setLevel(client);
        assertFalse(furnace.insertInput(input));
        furnace.setLevel(level);
        try (var index = mockStatic(AspectIndexAccess.class)) {
            index.when(() -> AspectIndexAccess.of(input)).thenReturn(AspectList.EMPTY.add(order, 251));
            assertFalse(furnace.insertInput(input));
            index.when(() -> AspectIndexAccess.of(input)).thenReturn(AspectList.EMPTY.add(order, 250));
            assertTrue(furnace.insertInput(input));
            assertFalse(furnace.insertInput(input));
        }
        assertEquals(8, input.getCount());
        assertEquals(1, furnace.input().getCount());
        assertEquals(250, furnace.inputCost());
    }

    @Test
    void cycleConsumesCachedInputAndMatchingPrimalReserves() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace();
        furnace.loadAdditional(reserves(true, 500), registries);
        ItemStack input = new ItemStack(Items.DIRT);
        try (var index = mockStatic(AspectIndexAccess.class);
                var structure = mockStatic(AdvancedAlchemicalFurnaceStructure.class)) {
            index.when(() -> AspectIndexAccess.of(input)).thenReturn(AspectList.EMPTY.add(order, 2));
            assertTrue(furnace.insertInput(input));
            structure
                    .when(() -> AdvancedAlchemicalFurnaceStructure.isLoaded(level, BlockPos.ZERO))
                    .thenReturn(true);
            structure
                    .when(() -> AdvancedAlchemicalFurnaceStructure.isFormed(eq(level), eq(BlockPos.ZERO), any()))
                    .thenReturn(true);
            BlockEntityAdvancedAlchemicalFurnace.serverTick(level, BlockPos.ZERO, furnace.getBlockState(), furnace);
            index.verify(() -> AspectIndexAccess.of(input), times(1));
        }
        assertTrue(furnace.input().isEmpty());
        assertEquals(2, furnace.aspects().amountOf(order));
        assertEquals(496, furnace.heat());
        assertEquals(498, furnace.aqua());
        assertEquals(498, furnace.perditio());
        assertEquals(5, furnace.cooldown());
        assertEquals(5, furnace.cycleDuration());
    }

    @Test
    void capacityStopsProcessingWithoutConsumingReservesOrInput() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace();
        CompoundTag tag = reserves(true, 500);
        TTNbt.store(tag, "Aspects", AspectList.CODEC, registries, AspectList.EMPTY.add(order, 499));
        furnace.loadAdditional(tag, registries);
        ItemStack input = new ItemStack(Items.DIRT);
        try (var index = mockStatic(AspectIndexAccess.class);
                var structure = mockStatic(AdvancedAlchemicalFurnaceStructure.class)) {
            index.when(() -> AspectIndexAccess.of(input)).thenReturn(AspectList.EMPTY.add(order, 2));
            assertTrue(furnace.insertInput(input));
            structure
                    .when(() -> AdvancedAlchemicalFurnaceStructure.isLoaded(level, BlockPos.ZERO))
                    .thenReturn(true);
            structure
                    .when(() -> AdvancedAlchemicalFurnaceStructure.isFormed(eq(level), eq(BlockPos.ZERO), any()))
                    .thenReturn(true);
            BlockEntityAdvancedAlchemicalFurnace.serverTick(level, BlockPos.ZERO, furnace.getBlockState(), furnace);
        }
        assertEquals(499, furnace.aspects().totalAmount());
        assertEquals(1, furnace.input().getCount());
        assertEquals(500, furnace.heat());
        assertEquals(500, furnace.aqua());
        assertEquals(500, furnace.perditio());
        assertEquals(0, furnace.cooldown());
    }

    @Test
    void rechargingUsesRelaysBeforeNodesAndTheExactRemainingHeadroom() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace();
        furnace.loadAdditional(reserves(true, 490), registries);
        when(level.getGameTime()).thenReturn(5L);
        try (var relay = mockStatic(VisRelayNetwork.class);
                var structure = mockStatic(AdvancedAlchemicalFurnaceStructure.class)) {
            structure
                    .when(() -> AdvancedAlchemicalFurnaceStructure.isLoaded(level, BlockPos.ZERO))
                    .thenReturn(true);
            structure
                    .when(() -> AdvancedAlchemicalFurnaceStructure.isFormed(eq(level), eq(BlockPos.ZERO), any()))
                    .thenReturn(true);
            relay.when(() -> VisRelayNetwork.drainEverySourceNear(eq(level), eq(BlockPos.ZERO), any(), anyInt()))
                    .thenReturn(3);
            relay.when(() -> VisRelayNetwork.drainNodesNear(eq(level), eq(BlockPos.ZERO), any(), anyInt()))
                    .thenReturn(2);
            BlockEntityAdvancedAlchemicalFurnace.serverTick(level, BlockPos.ZERO, furnace.getBlockState(), furnace);
            relay.verify(() -> VisRelayNetwork.drainEverySourceNear(level, BlockPos.ZERO, TTAspects.IGNIS, 11));
            relay.verify(() -> VisRelayNetwork.drainNodesNear(level, BlockPos.ZERO, TTAspects.IGNIS, 8));
            relay.verify(() -> VisRelayNetwork.drainNodesNear(level, BlockPos.ZERO, TTAspects.PERDITIO, 7));
            relay.verify(() -> VisRelayNetwork.drainNodesNear(level, BlockPos.ZERO, TTAspects.AQUA, 7));
        }
        assertEquals(494, furnace.heat());
        assertEquals(495, furnace.perditio());
        assertEquals(495, furnace.aqua());
    }

    @Test
    void nozzlesKeepStableIdentityAndOnlyExtractThroughTheirAssembledMouth() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace();
        CompoundTag tag = reserves(true, 500);
        TTNbt.store(tag, "Aspects", AspectList.CODEC, registries, AspectList.EMPTY.add(order, 9));
        furnace.loadAdditional(tag, registries);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            AdvancedFurnaceNozzle nozzle = furnace.nozzle(side);
            assertSame(nozzle, furnace.nozzle(side));
            assertTrue(nozzle.releasesOn(side));
            assertFalse(nozzle.receivesOn(side));
            assertEquals(0, nozzle.insert(order, 3, side, false));
            assertEquals(0, nozzle.roomFor(order, side));
            assertEquals(0, nozzle.extract(order, 3, side.getOpposite(), true));
            assertEquals(9, nozzle.extract(order, 20, side, true));
            assertEquals(0, nozzle.extract(order, -3, side, true));
        }
        assertEquals(9, furnace.aspects().totalAmount());
        assertThrows(IllegalArgumentException.class, () -> furnace.nozzle(Direction.UP));
        assertEquals(4, furnace.nozzle(Direction.EAST).extract(order, 4, Direction.EAST, false));
        assertEquals(5, furnace.aspects().totalAmount());
        furnace.loadAdditional(reserves(false, 500), registries);
        assertFalse(furnace.nozzle(Direction.EAST).connectsOn(Direction.EAST));
    }

    @Test
    void unloadedStructureDoesNotAdvanceOrDrainAndAssemblyChangesRefreshCapabilities() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace();
        CompoundTag tag = reserves(false, 500);
        tag.putInt("Cooldown", 7);
        furnace.loadAdditional(tag, registries);
        try (var relay = mockStatic(VisRelayNetwork.class);
                var structure = mockStatic(AdvancedAlchemicalFurnaceStructure.class)) {
            BlockEntityAdvancedAlchemicalFurnace.serverTick(level, BlockPos.ZERO, furnace.getBlockState(), furnace);
            assertEquals(7, furnace.cooldown());
            relay.verifyNoInteractions();
            structure.verify(() -> AdvancedAlchemicalFurnaceStructure.isFormed(any(), any(), any()), never());
            structure
                    .when(() -> AdvancedAlchemicalFurnaceStructure.isLoaded(level, BlockPos.ZERO))
                    .thenReturn(true);
            structure
                    .when(() -> AdvancedAlchemicalFurnaceStructure.isFormed(eq(level), eq(BlockPos.ZERO), any()))
                    .thenReturn(true);
            BlockEntityAdvancedAlchemicalFurnace.serverTick(level, BlockPos.ZERO, furnace.getBlockState(), furnace);
            BlockEntityAdvancedAlchemicalFurnace.serverTick(level, BlockPos.ZERO, furnace.getBlockState(), furnace);
            structure.verify(() -> AdvancedAlchemicalFurnaceStructure.refreshNozzles(level, BlockPos.ZERO), times(1));
        }
        assertTrue(furnace.isAssembled());
        assertEquals(5, furnace.cooldown());
    }

    @Test
    void unloadingDoesNotPolluteButActualRemovalReleasesStockOnce() {
        BlockEntityAdvancedAlchemicalFurnace furnace = furnace();
        CompoundTag tag = reserves(true, 500);
        TTNbt.store(tag, "Aspects", AspectList.CODEC, registries, AspectList.EMPTY.add(order, 9));
        furnace.loadAdditional(tag, registries);
        try (var aura = mockStatic(AuraHelper.class)) {
            furnace.setRemoved();
            aura.verifyNoInteractions();
            assertEquals(9, furnace.aspects().totalAmount());
            furnace.dropContents(BlockPos.ZERO);
            furnace.dropContents(BlockPos.ZERO);
            aura.verify(() -> AuraHelper.polluteAura(level, BlockPos.ZERO, 9, true), times(1));
            assertTrue(furnace.aspects().isEmpty());
        }
    }
}
