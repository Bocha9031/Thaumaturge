package com.leclowndu93150.thaumaturge.content.essentia.flow;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EssentiaIntakeCutoffTest {
    private ServerLevel level;
    private EssentiaIntakeHost host;
    private IEssentiaConduit supplier;
    private Holder<IAspect> aspect;
    private EssentiaIntake intake;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, TTAspects.HERBA, mock(IAspect.class));
        aspect = registry.getHolderOrThrow(TTAspects.HERBA);
        level = mock(ServerLevel.class);
        when(level.registryAccess()).thenReturn(new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze());
        host = mock(EssentiaIntakeHost.class, CALLS_REAL_METHODS);
        when(host.getLevel()).thenReturn(level);
        when(host.getBlockPos()).thenReturn(BlockPos.ZERO);
        when(host.intakeFace()).thenReturn(Direction.EAST);
        when(host.wantsEssentia()).thenReturn(true);
        supplier = mock(IEssentiaConduit.class);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.east(), Direction.WEST))
                .thenReturn(supplier);
        when(supplier.releasesOn(Direction.WEST)).thenReturn(true);
        when(supplier.pull(Direction.WEST)).thenReturn(EssentiaPull.NONE);
        when(supplier.extract(aspect, 1, Direction.WEST, false)).thenReturn(1);
        intake = new EssentiaIntake(host, TTAspects.HERBA, 128, 5);
    }

    @Test
    void firstDrawUsesModernConduitAndThenWaitsExactlyFiveCalls() {
        assertTrue(intake.pullOne());
        verify(level).getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO.east(), Direction.WEST);
        for (int i = 0; i < 4; i++) assertFalse(intake.pullOne());
        verify(supplier, times(1)).extract(aspect, 1, Direction.WEST, false);
        assertTrue(intake.pullOne());
        verify(supplier, times(2)).extract(aspect, 1, Direction.WEST, false);
    }

    @Test
    void onlyConfiguredSideReceivesAndPullUsesFixedStrengthUntilDemandIsSatisfied() {
        assertTrue(intake.connectsOn(Direction.EAST));
        assertTrue(intake.receivesOn(Direction.EAST));
        assertFalse(intake.connectsOn(Direction.WEST));
        assertFalse(intake.releasesOn(Direction.EAST));
        assertSame(aspect, intake.pull(Direction.EAST).aspect());
        assertEquals(128, intake.pull(Direction.EAST).strength());
        assertSame(EssentiaPull.NONE, intake.pull(null));
        assertEquals(128, intake.pull(Direction.EAST).strength());
        when(host.wantsEssentia()).thenReturn(false);
        assertSame(EssentiaPull.NONE, intake.pull(Direction.EAST));
    }

    @Test
    void queriesAndExternalSimulationNeverStoreOrExtractEssentia() {
        assertNull(intake.heldAspect(null));
        assertEquals(0, intake.heldAmount(null));
        assertEquals(0, intake.pullThreshold());
        assertEquals(0, intake.insert(aspect, 1, Direction.EAST, true));
        assertEquals(0, intake.insert(aspect, 1, Direction.EAST, false));
        assertEquals(0, intake.extract(aspect, 1, Direction.EAST, true));
        assertEquals(0, intake.extract(aspect, 1, Direction.EAST, false));
        verifyNoInteractions(supplier);
    }

    @Test
    void equalOrStrongerSupplierPullAndRefusedOutputPreventExtraction() {
        when(supplier.pull(Direction.WEST)).thenReturn(EssentiaPull.any(128));
        assertFalse(intake.pullOne());
        verify(supplier, never()).extract(any(), anyInt(), any(), anyBoolean());
        when(supplier.pull(Direction.WEST)).thenReturn(EssentiaPull.NONE);
        when(supplier.releasesOn(Direction.WEST)).thenReturn(false);
        for (int i = 0; i < 5; i++) assertFalse(intake.pullOne());
        verify(supplier, never()).extract(any(), anyInt(), any(), anyBoolean());
    }

    @Test
    void noDemandOrRefusedExtractionNeverReportsSuccess() {
        when(host.wantsEssentia()).thenReturn(false);
        assertFalse(intake.pullOne());
        verify(supplier, never()).extract(any(), anyInt(), any(), anyBoolean());
        when(host.wantsEssentia()).thenReturn(true);
        when(supplier.extract(aspect, 1, Direction.WEST, false)).thenReturn(0);
        for (int i = 0; i < 5; i++) assertFalse(intake.pullOne());
    }

    @Test
    void missingRegistryAspectOrLevelNeverQueriesAProvider() {
        EssentiaIntake unknown = new EssentiaIntake(host, TTAspects.VITIUM, 128, 5);
        assertFalse(unknown.pullOne());
        when(host.getLevel()).thenReturn(null);
        assertFalse(intake.pullOne());
        verify(level, never()).getCapability(any(), any(BlockPos.class), any(Direction.class));
    }
}
