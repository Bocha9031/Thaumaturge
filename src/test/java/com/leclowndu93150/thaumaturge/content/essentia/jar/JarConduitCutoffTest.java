package com.leclowndu93150.thaumaturge.content.essentia.jar;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTCapabilities;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"removal", "unchecked"})
class JarConduitCutoffTest {
    private Level level;
    private Holder<IAspect> order;
    private Holder<IAspect> fire;

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, TTAspects.ORDO, mock(IAspect.class));
        Registry.register(registry, TTAspects.IGNIS, mock(IAspect.class));
        order = registry.getHolderOrThrow(TTAspects.ORDO);
        fire = registry.getHolderOrThrow(TTAspects.IGNIS);
        RegistryAccess registries = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
        level = mock(Level.class);
        when(level.registryAccess()).thenReturn(registries);
    }

    private BlockEntityJar jar() {
        BlockEntityJar jar =
                spy(new BlockEntityJar(BlockPos.ZERO, TTBlocks.JAR_NORMAL.get().defaultBlockState()));
        jar.setLevel(level);
        return jar;
    }

    private BlockEntityJarVoid voidJar() {
        BlockEntityJarVoid jar = spy(
                new BlockEntityJarVoid(BlockPos.ZERO, TTBlocks.JAR_VOID.get().defaultBlockState()));
        jar.setLevel(level);
        return jar;
    }

    private BlockEntityAlembic alembic() {
        BlockEntityAlembic alembic =
                spy(new BlockEntityAlembic(BlockPos.ZERO, TTBlocks.ALEMBIC.get().defaultBlockState()));
        alembic.setLevel(level);
        return alembic;
    }

    private void assertNoCommittedSideEffects(AbstractSyncedBlockEntity entity) {
        verify(entity, never()).setChanged();
        verify(entity, never()).syncToClient();
        verify(level, never()).sendBlockUpdated(any(), any(), any(), anyInt());
    }

    @Test
    void normalJarSimulationRespectsLabelsContentsAndCapacity() {
        BlockEntityJar jar = jar();
        jar.setAspectFilter(TTAspects.ORDO);
        jar.setStorageContents(TTAspects.ORDO, BlockEntityJar.CAPACITY - 2);
        long revision = jar.storage(Direction.UP).contentRevision();
        clearInvocations(jar, level);

        assertEquals(0, jar.insert(fire, 10, Direction.UP, true));
        assertEquals(0, jar.roomFor(fire, Direction.UP));
        assertEquals(2, jar.insert(order, 10, Direction.UP, true));
        assertEquals(2, jar.roomFor(order, Direction.UP));
        assertEquals(BlockEntityJar.CAPACITY - 2, jar.amount());
        assertEquals(revision, jar.storage(Direction.UP).contentRevision());
        assertNoCommittedSideEffects(jar);
        assertEquals(2, jar.insert(order, 10, Direction.UP, false));
        assertEquals(BlockEntityJar.CAPACITY, jar.amount());
        assertEquals(0, jar.insert(order, 1, Direction.UP, true));
        assertEquals(0, jar.roomFor(order, Direction.UP));
        assertSame(EssentiaPull.NONE, jar.pull(null));
    }

    @Test
    void emptyLabelledJarRejectsAnotherAspectWithoutChangingItsIntent() {
        BlockEntityJar jar = jar();
        jar.setAspectFilter(TTAspects.ORDO);
        clearInvocations(jar, level);
        assertEquals(0, jar.insert(fire, 1, Direction.UP, true));
        assertEquals(0, jar.roomFor(fire, Direction.UP));
        assertEquals(BlockEntityJar.CAPACITY, jar.insert(order, Integer.MAX_VALUE, Direction.UP, true));
        assertEquals(TTAspects.ORDO, jar.aspectFilterKey());
        assertNull(jar.aspectKey());
        assertEquals(0, jar.amount());
        assertNoCommittedSideEffects(jar);
    }

    @Test
    void occupiedUnlabelledJarRejectsAnotherAspectInSimulation() {
        BlockEntityJar jar = jar();
        jar.setStorageContents(TTAspects.ORDO, 5);
        clearInvocations(jar, level);
        assertEquals(0, jar.insert(fire, 1, Direction.UP, true));
        assertEquals(0, jar.roomFor(fire, Direction.UP));
        assertEquals(TTAspects.ORDO, jar.aspectKey());
        assertEquals(5, jar.amount());
        assertNoCommittedSideEffects(jar);
    }

    @Test
    void jarExtractionSimulationIsAllOrNothingAndHasNoSideEffects() {
        BlockEntityJar jar = jar();
        jar.setStorageContents(TTAspects.ORDO, 5);
        long revision = jar.storage(Direction.UP).contentRevision();
        clearInvocations(jar, level);
        assertEquals(0, jar.extract(order, 6, Direction.UP, true));
        assertEquals(0, jar.extract(order, 6, Direction.UP, false));
        assertEquals(0, jar.extract(fire, 1, Direction.UP, true));
        assertEquals(5, jar.extract(order, 5, Direction.UP, true));
        assertEquals(5, jar.amount());
        assertEquals(revision, jar.storage(Direction.UP).contentRevision());
        assertNoCommittedSideEffects(jar);
        assertEquals(5, jar.extract(order, 5, Direction.UP, false));
        assertEquals(0, jar.amount());
    }

    @Test
    void jarAllowsWholeDeviceQueriesButTransfersOnlyThroughItsTop() {
        BlockEntityJar jar = jar();
        jar.setStorageContents(TTAspects.ORDO, 5);
        clearInvocations(jar, level);
        assertSame(order, jar.heldAspect(null));
        assertEquals(5, jar.heldAmount(null));
        assertEquals(EssentiaPull.of(order, 32), jar.pull(null));
        for (Direction face : Direction.values()) {
            assertEquals(face == Direction.UP, jar.connectsOn(face));
            if (face != Direction.UP) {
                assertEquals(0, jar.insert(order, 1, face, true));
                assertEquals(0, jar.extract(order, 1, face, true));
                assertEquals(0, jar.roomFor(order, face));
            }
        }
        assertEquals(0, jar.insert(order, 1, null, true));
        assertEquals(0, jar.extract(order, 1, null, true));
        assertEquals(0, jar.roomFor(order, null));
        assertNoCommittedSideEffects(jar);
    }

    @Test
    void jarRejectsUnregisteredAspectsAndNonPositiveSimulationRequests() {
        BlockEntityJar jar = jar();
        Holder<IAspect> unregistered = Holder.direct(mock(IAspect.class));
        clearInvocations(jar, level);
        assertEquals(0, jar.insert(unregistered, 1, Direction.UP, true));
        assertEquals(0, jar.roomFor(unregistered, Direction.UP));
        assertEquals(0, jar.extract(unregistered, 1, Direction.UP, true));
        assertEquals(0, jar.insert(order, -1, Direction.UP, true));
        assertEquals(0, jar.extract(order, 0, Direction.UP, true));
        assertNoCommittedSideEffects(jar);
    }

    @Test
    void labelledVoidJarSimulatesOverflowWithoutMutatingOrVoiding() {
        BlockEntityJarVoid jar = voidJar();
        jar.setAspectFilter(TTAspects.ORDO);
        jar.setStorageContents(TTAspects.ORDO, BlockEntityJar.CAPACITY);
        long revision = jar.storage(Direction.UP).contentRevision();
        clearInvocations(jar, level);
        assertEquals(12, jar.insert(order, 12, Direction.UP, true));
        assertEquals(Integer.MAX_VALUE, jar.roomFor(order, Direction.UP));
        assertEquals(0, jar.insert(fire, 12, Direction.UP, true));
        assertEquals(0, jar.roomFor(fire, Direction.UP));
        assertEquals(BlockEntityJar.CAPACITY, jar.amount());
        assertEquals(revision, jar.storage(Direction.UP).contentRevision());
        verify(jar, never()).onStorageVoided(anyInt());
        assertNoCommittedSideEffects(jar);
        assertEquals(12, jar.insert(order, 12, Direction.UP, false));
        assertEquals(BlockEntityJar.CAPACITY, jar.amount());
        assertEquals(EssentiaPull.of(order, 32), jar.pull(null));
        assertEquals(48, jar.pullThreshold());
    }

    @Test
    void alembicSimulationIsExtractionOnlyAndAllOrNothing() {
        BlockEntityAlembic alembic = alembic();
        alembic.setStorageContents(TTAspects.ORDO, 5);
        long revision = alembic.storage(Direction.UP).contentRevision();
        clearInvocations(alembic, level);
        assertEquals(0, alembic.insert(order, 1, Direction.UP, true));
        assertEquals(0, alembic.roomFor(order, Direction.UP));
        assertEquals(0, alembic.extract(order, 6, Direction.UP, true));
        assertEquals(0, alembic.extract(order, 6, Direction.UP, false));
        assertEquals(0, alembic.extract(fire, 1, Direction.UP, true));
        assertEquals(5, alembic.extract(order, 5, Direction.UP, true));
        assertSame(order, alembic.heldAspect(null));
        assertEquals(5, alembic.heldAmount(null));
        assertSame(EssentiaPull.NONE, alembic.pull(null));
        assertEquals(revision, alembic.storage(Direction.UP).contentRevision());
        assertNoCommittedSideEffects(alembic);
        assertEquals(5, alembic.extract(order, 5, Direction.UP, false));
        assertEquals(0, alembic.amount());
    }

    @Test
    void alembicExtractionSimulationRespectsItsSpoutAndClosedBottom() {
        BlockEntityAlembic alembic = alembic();
        alembic.setStorageContents(TTAspects.ORDO, 5);
        alembic.aimSpout(Direction.NORTH);
        clearInvocations(alembic, level);
        assertEquals(0, alembic.extract(order, 1, Direction.NORTH, true));
        assertEquals(0, alembic.extract(order, 1, Direction.DOWN, true));
        assertEquals(1, alembic.extract(order, 1, Direction.UP, true));
        assertEquals(0, alembic.extract(Holder.direct(mock(IAspect.class)), 1, Direction.UP, true));
        assertNoCommittedSideEffects(alembic);
    }

    private <T> ICapabilityProvider<BlockEntity, Direction, T> provider(
            RegisterCapabilitiesEvent event, BlockCapability<T, Direction> capability, BlockEntityType<?> type) {
        return mockingDetails(event).getInvocations().stream()
                .filter(call -> call.getMethod().getName().equals("registerBlockEntity"))
                .filter(call -> call.getArgument(0) == capability && call.getArgument(1) == type)
                .map(call -> (ICapabilityProvider<BlockEntity, Direction, T>) call.getArgument(2))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void nativeRegistrationExposesDirectConduitAndLegacyAdapterForAllThreeVessels() {
        RegisterCapabilitiesEvent event = mock(RegisterCapabilitiesEvent.class);
        TTCapabilities.onRegisterCapabilities(event);
        BlockEntityJar jar = jar();
        BlockEntityJarVoid voidJar = voidJar();
        BlockEntityAlembic alembic = alembic();
        List<BlockEntity> vessels = List.of(jar, voidJar, alembic);
        List<BlockEntityType<?>> types =
                List.of(TTBlockEntities.JAR.get(), TTBlockEntities.JAR_VOID.get(), TTBlockEntities.ALEMBIC.get());
        for (int index = 0; index < vessels.size(); index++) {
            BlockEntity vessel = vessels.get(index);
            ICapabilityProvider<BlockEntity, Direction, IEssentiaConduit> direct =
                    provider(event, EssentiaCapabilities.CONDUIT, types.get(index));
            ICapabilityProvider<BlockEntity, Direction, IEssentiaTransport> old =
                    provider(event, EssentiaCapabilities.TRANSPORT, types.get(index));
            assertSame(vessel, direct.getCapability(vessel, Direction.UP));
            IEssentiaTransport legacy = old.getCapability(vessel, Direction.UP);
            assertNotNull(legacy);
            assertNotSame(vessel, legacy);
            assertEquals(0, legacy.addEssentia(fire, 1, Direction.DOWN, true));
            assertEquals(0, legacy.takeEssentia(order, 1, Direction.DOWN, true));
            assertSame(vessel, direct.getCapability(vessel, null));
            assertNotNull(old.getCapability(vessel, null));
        }
        IEssentiaConduit direct = provider(event, EssentiaCapabilities.CONDUIT, TTBlockEntities.JAR.get())
                .getCapability(jar, Direction.UP);
        IEssentiaTransport legacy = provider(event, EssentiaCapabilities.TRANSPORT, TTBlockEntities.JAR.get())
                .getCapability(jar, Direction.UP);
        jar.setAspectFilter(TTAspects.ORDO);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO, Direction.UP))
                .thenReturn(direct);
        when(level.getCapability(EssentiaCapabilities.TRANSPORT, BlockPos.ZERO, Direction.UP))
                .thenReturn(legacy);
        assertSame(jar, EssentiaAccess.conduit(level, BlockPos.ZERO, Direction.UP));
        assertSame(legacy, EssentiaAccess.transport(level, BlockPos.ZERO, Direction.UP));
        assertEquals(0, legacy.addEssentia(fire, 1, Direction.UP, true));
        assertEquals(BlockEntityJar.CAPACITY, legacy.spaceFor(order, Direction.UP));
        assertEquals(BlockEntityJar.CAPACITY, legacy.addEssentia(order, Integer.MAX_VALUE, Direction.UP, true));
        assertEquals(0, jar.amount());
    }
}
