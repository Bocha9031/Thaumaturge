package com.leclowndu93150.thaumaturge.api.essentia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.Invocation;

@SuppressWarnings({"removal", "unchecked"})
class EssentiaConduitCompatibilityTest {
    private final Holder<IAspect> aspect = Holder.direct(mock(IAspect.class));

    @Test
    void absentPullHasNoAspectAndRejectsOffers() {
        EssentiaPull negative = new EssentiaPull(aspect, -4);
        assertEquals(EssentiaPull.NONE, negative);
        assertNull(negative.aspect());
        assertFalse(negative.isActive());
        assertFalse(negative.accepts(aspect));
        assertSame(EssentiaPull.NONE, EssentiaPull.of(aspect, 0));
        assertTrue(EssentiaPull.any(32).accepts(aspect));
        assertTrue(EssentiaPull.of(aspect, 64).accepts(aspect));
        assertFalse(EssentiaPull.of(aspect, 64).accepts(Holder.direct(mock(IAspect.class))));
    }

    @Test
    void legacyTransportDefaultsExposeConduitQueriesAndSimulation() {
        IEssentiaTransport transport = mock(IEssentiaTransport.class, CALLS_REAL_METHODS);
        when(transport.getEssentiaType(null)).thenReturn(aspect);
        when(transport.getEssentiaAmount(null)).thenReturn(5);
        when(transport.getSuctionType(null)).thenReturn(aspect);
        when(transport.getSuctionAmount(null)).thenReturn(64);
        when(transport.getMinimumSuction()).thenReturn(32);
        when(transport.isConnectable(Direction.UP)).thenReturn(true);
        when(transport.canInputFrom(Direction.UP)).thenReturn(true);
        when(transport.canOutputTo(Direction.UP)).thenReturn(true);
        when(transport.spaceFor(aspect, Direction.UP)).thenReturn(3);
        when(transport.getEssentiaType(Direction.UP)).thenReturn(aspect);
        when(transport.getEssentiaAmount(Direction.UP)).thenReturn(5);
        when(transport.addEssentia(aspect, 2, Direction.UP)).thenReturn(2);

        IEssentiaConduit conduit = transport;
        assertSame(aspect, conduit.heldAspect(null));
        assertEquals(5, conduit.heldAmount(null));
        assertEquals(EssentiaPull.of(aspect, 64), conduit.pull(null));
        assertEquals(32, conduit.pullThreshold());
        assertTrue(conduit.connectsOn(Direction.UP));
        assertTrue(conduit.receivesOn(Direction.UP));
        assertTrue(conduit.releasesOn(Direction.UP));
        assertEquals(3, conduit.roomFor(aspect, Direction.UP));
        assertEquals(3, conduit.insert(aspect, 10, Direction.UP, true));
        assertEquals(5, conduit.extract(aspect, 10, Direction.UP, true));
        verify(transport, never()).addEssentia(aspect, 10, Direction.UP);
        verify(transport, never()).takeEssentia(aspect, 10, Direction.UP);
        assertEquals(2, conduit.insert(aspect, 2, Direction.UP, false));
        verify(transport).addEssentia(aspect, 2, Direction.UP);
    }

    @Test
    void adapterKeepsExistingTransportAndNullIdentity() {
        IEssentiaTransport transport = mock(IEssentiaTransport.class);
        assertNull(ConduitAsTransport.view(null));
        assertSame(transport, ConduitAsTransport.view(transport));
    }

    @Test
    void adapterForwardsLegacyQueriesAndTransfersToSameConduit() {
        IEssentiaConduit conduit = mock(IEssentiaConduit.class);
        when(conduit.heldAspect(null)).thenReturn(aspect);
        when(conduit.heldAmount(null)).thenReturn(8);
        when(conduit.pull(null)).thenReturn(EssentiaPull.of(aspect, 64));
        when(conduit.pullThreshold()).thenReturn(32);
        when(conduit.connectsOn(Direction.UP)).thenReturn(true);
        when(conduit.receivesOn(Direction.UP)).thenReturn(true);
        when(conduit.releasesOn(Direction.UP)).thenReturn(true);
        when(conduit.roomFor(aspect, Direction.UP)).thenReturn(7);
        when(conduit.insert(aspect, 3, Direction.UP, true)).thenReturn(3);
        when(conduit.insert(aspect, 3, Direction.UP, false)).thenReturn(3);
        when(conduit.extract(aspect, 2, Direction.UP, true)).thenReturn(2);
        when(conduit.extract(aspect, 2, Direction.UP, false)).thenReturn(2);
        IEssentiaTransport transport = ConduitAsTransport.view(conduit);

        assertSame(aspect, transport.getEssentiaType(null));
        assertEquals(8, transport.getEssentiaAmount(null));
        assertSame(aspect, transport.getSuctionType(null));
        assertEquals(64, transport.getSuctionAmount(null));
        assertEquals(32, transport.getMinimumSuction());
        assertTrue(transport.isConnectable(Direction.UP));
        assertTrue(transport.canInputFrom(Direction.UP));
        assertTrue(transport.canOutputTo(Direction.UP));
        assertEquals(7, transport.spaceFor(aspect, Direction.UP));
        assertEquals(3, transport.addEssentia(aspect, 3, Direction.UP, true));
        assertEquals(3, transport.addEssentia(aspect, 3, Direction.UP));
        assertEquals(2, transport.takeEssentia(aspect, 2, Direction.UP, true));
        assertEquals(2, transport.takeEssentia(aspect, 2, Direction.UP));
        clearInvocations(conduit);
        transport.setSuction(aspect, 128);
        verifyNoInteractions(conduit);
    }

    @Test
    void lookupPrefersConduitAndFallsBackToLegacyTransport() {
        Level level = mock(Level.class);
        IEssentiaConduit conduit = mock(IEssentiaConduit.class);
        IEssentiaTransport legacy = mock(IEssentiaTransport.class);
        when(level.getCapability(EssentiaCapabilities.CONDUIT, BlockPos.ZERO, Direction.UP))
                .thenReturn(conduit);
        assertSame(conduit, EssentiaAccess.conduit(level, BlockPos.ZERO, Direction.UP));
        verify(level, never()).getCapability(EssentiaCapabilities.TRANSPORT, BlockPos.ZERO, Direction.UP);

        when(level.getCapability(EssentiaCapabilities.TRANSPORT, BlockPos.ZERO, null))
                .thenReturn(legacy);
        assertSame(legacy, EssentiaAccess.conduit(level, BlockPos.ZERO, null));
        assertFalse(EssentiaAccess.hasConduit(level, BlockPos.ZERO, Direction.DOWN));
    }

    @Test
    void blockEntityRegistrationPublishesConduitAndLegacyViewWithSameSides() {
        RegisterCapabilitiesEvent event = mock(RegisterCapabilitiesEvent.class);
        BlockEntityType<BlockEntity> type = mock(BlockEntityType.class);
        BlockEntity blockEntity = mock(BlockEntity.class);
        IEssentiaConduit conduit = mock(IEssentiaConduit.class);
        EssentiaCapabilities.registerConduit(event, type, (be, side) -> side == Direction.UP ? conduit : null);
        List<Invocation> calls = List.copyOf(mockingDetails(event).getInvocations());
        assertEquals(2, calls.size());
        assertSame(EssentiaCapabilities.CONDUIT, calls.get(0).getArgument(0));
        assertSame(EssentiaCapabilities.TRANSPORT, calls.get(1).getArgument(0));
        assertSame(type, calls.get(0).getArgument(1));
        assertSame(type, calls.get(1).getArgument(1));
        ICapabilityProvider<BlockEntity, Direction, IEssentiaConduit> direct =
                calls.get(0).getArgument(2);
        ICapabilityProvider<BlockEntity, Direction, IEssentiaTransport> legacy =
                calls.get(1).getArgument(2);
        assertSame(conduit, direct.getCapability(blockEntity, Direction.UP));
        assertNull(direct.getCapability(blockEntity, Direction.DOWN));
        assertNotNull(legacy.getCapability(blockEntity, Direction.UP));
        assertNull(legacy.getCapability(blockEntity, Direction.DOWN));
    }

    @Test
    void blockRegistrationPublishesBothViewsAndPreservesNullableSide() {
        RegisterCapabilitiesEvent event = mock(RegisterCapabilitiesEvent.class);
        Block block = mock(Block.class);
        IEssentiaConduit conduit = mock(IEssentiaConduit.class);
        EssentiaCapabilities.registerConduitBlock(
                event, (level, pos, state, be, side) -> side == null ? conduit : null, block);
        List<Invocation> calls = List.copyOf(mockingDetails(event).getInvocations());
        assertEquals(2, calls.size());
        assertSame(EssentiaCapabilities.CONDUIT, calls.get(0).getArgument(0));
        assertSame(EssentiaCapabilities.TRANSPORT, calls.get(1).getArgument(0));
        IBlockCapabilityProvider<IEssentiaConduit, Direction> direct =
                calls.get(0).getArgument(1);
        IBlockCapabilityProvider<IEssentiaTransport, Direction> legacy =
                calls.get(1).getArgument(1);
        assertSame(conduit, direct.getCapability(null, BlockPos.ZERO, null, null, null));
        assertNotNull(legacy.getCapability(null, BlockPos.ZERO, null, null, null));
        assertNull(legacy.getCapability(null, BlockPos.ZERO, null, null, Direction.DOWN));
    }

    @Test
    void transferHonorsSimulationAndReturnsRefusedRemainderToSource() {
        IEssentiaConduit source = mock(IEssentiaConduit.class);
        IEssentiaConduit destination = mock(IEssentiaConduit.class);
        when(source.releasesOn(Direction.UP)).thenReturn(true);
        when(destination.receivesOn(Direction.DOWN)).thenReturn(true);
        when(source.extract(aspect, 10, Direction.UP, true)).thenReturn(7);
        when(destination.insert(aspect, 7, Direction.DOWN, true)).thenReturn(5);
        assertEquals(5, EssentiaTransfer.transfer(source, Direction.UP, destination, Direction.DOWN, aspect, 10, true));
        verify(source, never()).extract(any(), anyInt(), any(), eq(false));
        verify(destination, never()).insert(any(), anyInt(), any(), eq(false));

        when(source.extract(aspect, 5, Direction.UP, false)).thenReturn(5);
        when(destination.insert(aspect, 5, Direction.DOWN, false)).thenReturn(3);
        assertEquals(
                3, EssentiaTransfer.transfer(source, Direction.UP, destination, Direction.DOWN, aspect, 10, false));
        verify(source).insert(aspect, 2, Direction.UP, false);
    }

    @Test
    void filterFacadeCopiesStacksAndRejectsRebinding() {
        ItemStack input = new ItemStack(TTItems.LABEL.get());
        ItemStack labelled = EssentiaAccess.withAspectFilter(input, TTAspects.ORDO);
        assertNull(EssentiaAccess.aspectFilter(input));
        assertEquals(TTAspects.ORDO, EssentiaAccess.aspectFilter(labelled));
        ItemStack cleared = EssentiaAccess.withAspectFilter(labelled, null);
        assertNull(EssentiaAccess.aspectFilter(cleared));
        assertEquals(TTAspects.ORDO, EssentiaAccess.aspectFilter(labelled));
        assertThrows(IllegalStateException.class, () -> EssentiaAccess.bind(TTDataComponents.ASPECT_FILTER));
    }
}
