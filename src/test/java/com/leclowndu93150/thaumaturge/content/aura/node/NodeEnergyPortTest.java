package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class NodeEnergyPortTest {
    private double originalCost;

    @BeforeEach
    void setUp() {
        ModConfigSpec.DoubleValue cost = ThaumaturgeCommonConfig.ENERGIZED_NODE_VIS_PER_POINT;
        originalCost = cost.get();
        cost.set(6.0D);
    }

    @AfterEach
    void tearDown() {
        ThaumaturgeCommonConfig.ENERGIZED_NODE_VIS_PER_POINT.set(originalCost);
    }

    private BlockEntityNode node(Holder<IAspect> aspect, int capacity, int held, NodeType type) {
        BlockState state = TTBlocks.NODE.get().defaultBlockState();
        BlockEntityNode node = new BlockEntityNode(BlockPos.ZERO, state);
        node.aspectsBase = AspectList.ofEntries(List.of(new AspectInstance(aspect, capacity)));
        node.aspects = held == 0 ? AspectList.EMPTY : AspectList.ofEntries(List.of(new AspectInstance(aspect, held)));
        node.setNodeType(type);
        node.energized = true;
        return node;
    }

    @Test
    void refillRunsOnlyEveryTwentyTicks() {
        ServerLevel level = mock(ServerLevel.class);
        when(level.getGameTime()).thenReturn(19L, 20L, 21L, 40L);

        assertFalse(NodeEnergy.refillDue(level));
        assertTrue(NodeEnergy.refillDue(level));
        assertFalse(NodeEnergy.refillDue(level));
        assertTrue(NodeEnergy.refillDue(level));
    }

    @Test
    void normalNodesUseVisAndRefillAtMostTheConfiguredPointBatch() {
        Holder<IAspect> aspect = Holder.direct(mock(IAspect.class));
        BlockEntityNode node = node(aspect, 40, 0, NodeType.NORMAL);
        ServerLevel level = mock(ServerLevel.class);
        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 6.0F, false))
                    .thenReturn(6.0F);

            assertTrue(NodeEnergy.refill(node, level, BlockPos.ZERO));

            assertEquals(2, node.aspects.amountOf(aspect));
            aura.verify(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 6.0F, false), times(2));
            aura.verify(() -> AuraHelper.drainFlux(level, BlockPos.ZERO, 6.0F, false), never());
        }
    }

    @Test
    void taintedNodesUseFluxAndNeverExceedTheirRemainingCapacity() {
        Holder<IAspect> aspect = Holder.direct(mock(IAspect.class));
        BlockEntityNode node = node(aspect, 40, 39, NodeType.TAINTED);
        ServerLevel level = mock(ServerLevel.class);
        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.drainFlux(level, BlockPos.ZERO, 6.0F, false))
                    .thenReturn(6.0F);

            assertTrue(NodeEnergy.refill(node, level, BlockPos.ZERO));

            assertEquals(40, node.aspects.amountOf(aspect));
            aura.verify(() -> AuraHelper.drainFlux(level, BlockPos.ZERO, 6.0F, false));
            aura.verify(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 6.0F, false), never());
        }
    }

    @Test
    void partialAuraPaymentIsRefundedAndStopsThatAspectsRefill() {
        Holder<IAspect> aspect = Holder.direct(mock(IAspect.class));
        BlockEntityNode node = node(aspect, 40, 0, NodeType.NORMAL);
        ServerLevel level = mock(ServerLevel.class);
        try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
            aura.when(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 6.0F, false))
                    .thenReturn(2.0F);

            assertFalse(NodeEnergy.refill(node, level, BlockPos.ZERO));

            assertEquals(0, node.aspects.amountOf(aspect));
            aura.verify(() -> AuraHelper.addVis(level, BlockPos.ZERO, 2.0F));
            aura.verify(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 6.0F, false), times(1));
        }
    }

    @Test
    void zeroConfiguredCostStillRestoresOnePointPerDivisorAttemptAndServerTickDelegates() {
        ModConfigSpec.DoubleValue cost = ThaumaturgeCommonConfig.ENERGIZED_NODE_VIS_PER_POINT;
        try {
            cost.set(0.0D);
            MappedRegistry<IAspect> aspects = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
            Registry.register(aspects, TTAspects.IGNIS, mock(IAspect.class));
            Holder<IAspect> aspect = aspects.getHolderOrThrow(TTAspects.IGNIS);
            BlockEntityNode node = node(aspect, 20, 0, NodeType.NORMAL);
            ServerLevel level = mock(ServerLevel.class);
            node.setLevel(level);
            when(level.getGameTime()).thenReturn(20L);
            try (MockedStatic<AuraHelper> aura = mockStatic(AuraHelper.class)) {
                aura.when(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 0.0F, false))
                        .thenReturn(0.0F);

                node.serverTick(level, BlockPos.ZERO);

                assertEquals(1, node.aspects.amountOf(aspect));
                verify(level).sendBlockUpdated(BlockPos.ZERO, node.getBlockState(), node.getBlockState(), 3);
                aura.verify(() -> AuraHelper.drainVis(level, BlockPos.ZERO, 0.0F, false));
            }
        } finally {
            cost.set(originalCost);
        }
    }
}
