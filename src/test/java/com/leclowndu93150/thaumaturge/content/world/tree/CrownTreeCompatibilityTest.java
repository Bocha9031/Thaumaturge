package com.leclowndu93150.thaumaturge.content.world.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownRule;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownShape;
import com.leclowndu93150.thaumaturge.content.world.tree.crown.CrownTrunkPlacer;
import com.leclowndu93150.thaumaturge.data.model.TrunkModels;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Test;

final class CrownTreeCompatibilityTest {
    private record PlacedLog(BlockPos position, BlockState state) {}

    @Test
    void crownRulesKeepSourceRoundingAndSpaceContracts() {
        assertEquals(16_777_217, CrownRule.OPEN_AIR.clusterCoordinate(16_777_217, 0.25));
        assertEquals(16_777_218, CrownRule.REPLACEABLE.clusterCoordinate(16_777_217, 0.25));
        assertEquals(-16_777_217, CrownRule.OPEN_AIR.clusterCoordinate(-16_777_217, -0.25));
        assertEquals(-16_777_218, CrownRule.REPLACEABLE.clusterCoordinate(-16_777_217, -0.25));
        assertEquals(-1, CrownRule.OPEN_AIR.lineCoordinate(-4, 9, 3, 9, true));
        assertFalse(CrownRule.OPEN_AIR.isOpen(Blocks.BIRCH_LEAVES.defaultBlockState(), Blocks.OAK_LEAVES));
        assertTrue(CrownRule.REPLACEABLE.isOpen(Blocks.SHORT_GRASS.defaultBlockState(), Blocks.OAK_LEAVES));
    }

    @Test
    void crownRejectsObstructionAndBuildHeightAndMarksTheFourTrunkCorners() {
        BlockPos origin = new BlockPos(0, 64, 0);
        BlockGreatwoodLog greatwoodLog = TTBlocks.LOG_GREATWOOD.get();
        CrownShape shape = new CrownShape(2, 0.618, 0.38, 1.2, true);
        CrownTrunkPlacer placer = new CrownTrunkPlacer(12, 0, 0, shape, CrownRule.OPEN_AIR);
        TreeConfiguration config = treeConfiguration(placer, greatwoodLog);

        WorldGenLevel obstructed = treeLevel(origin, origin.above(3));
        List<PlacedLog> obstructedLogs = new ArrayList<>();
        assertTrue(placer.placeTrunk(
                        obstructed,
                        (pos, state) -> obstructedLogs.add(new PlacedLog(pos.immutable(), state)),
                        RandomSource.create(91L),
                        12,
                        origin,
                        config)
                .isEmpty());
        assertTrue(obstructedLogs.isEmpty());

        WorldGenLevel open = treeLevel(origin, null);
        when(open.isOutsideBuildHeight(any(BlockPos.class)))
                .thenAnswer(invocation -> ((BlockPos) invocation.getArgument(0)).getY() >= 320);
        assertTrue(placer.isFree(open, origin));
        assertFalse(placer.isFree(open, new BlockPos(0, 320, 0)));

        List<PlacedLog> generatedLogs = new ArrayList<>();
        List<FoliagePlacer.FoliageAttachment> attachments = placer.placeTrunk(
                open,
                (pos, state) -> generatedLogs.add(new PlacedLog(pos.immutable(), state)),
                RandomSource.create(91L),
                12,
                origin,
                config);
        assertFalse(attachments.isEmpty());
        assertEquals(TrunkCorner.NORTH_WEST, findLog(generatedLogs, origin).getValue(TrunkCorner.PROPERTY));
        assertEquals(
                TrunkCorner.NORTH_EAST, findLog(generatedLogs, origin.east()).getValue(TrunkCorner.PROPERTY));
        assertEquals(
                TrunkCorner.SOUTH_WEST, findLog(generatedLogs, origin.south()).getValue(TrunkCorner.PROPERTY));
        assertEquals(
                TrunkCorner.SOUTH_EAST,
                findLog(generatedLogs, origin.east().south()).getValue(TrunkCorner.PROPERTY));
    }

    @Test
    void modelGenerationCoversEveryAxisCornerStateAndWideTextureReference() {
        Map<ResourceLocation, Supplier<JsonElement>> models = new HashMap<>();
        List<BlockStateGenerator> blockStates = new ArrayList<>();
        ResourceLocation wideSide = ResourceLocation.fromNamespaceAndPath("thaumaturge", "block/log_greatwood_wide");
        ResourceLocation wideEnd = ResourceLocation.fromNamespaceAndPath("thaumaturge", "block/log_greatwood_top_wide");

        TrunkModels.register(
                blockStates::add,
                models::put,
                TTBlocks.LOG_GREATWOOD.get(),
                TTBlocks.WOOD_GREATWOOD.get(),
                wideSide,
                wideEnd);

        assertEquals(2, blockStates.size());
        assertEquals(
                15,
                blockStates
                        .get(0)
                        .get()
                        .getAsJsonObject()
                        .getAsJsonObject("variants")
                        .size());
        for (TrunkCorner corner : List.of(
                TrunkCorner.NORTH_WEST, TrunkCorner.NORTH_EAST, TrunkCorner.SOUTH_WEST, TrunkCorner.SOUTH_EAST)) {
            ResourceLocation modelId = ResourceLocation.fromNamespaceAndPath(
                    "thaumaturge", "block/log_greatwood_trunk_" + corner.getSerializedName());
            Supplier<JsonElement> modelSupplier = models.get(modelId);
            assertNotNull(modelSupplier);
            JsonObject textures = modelSupplier.get().getAsJsonObject().getAsJsonObject("textures");
            assertEquals(wideSide.toString(), textures.get("side").getAsString());
            assertEquals(wideEnd.toString(), textures.get("end").getAsString());
        }
    }

    private static TreeConfiguration treeConfiguration(CrownTrunkPlacer placer, Block log) {
        return new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(log),
                        placer,
                        BlockStateProvider.simple(Blocks.OAK_LEAVES),
                        new BlobFoliagePlacer(ConstantInt.ZERO, ConstantInt.ZERO, 0),
                        new TwoLayersFeatureSize(1, 0, 0))
                .ignoreVines()
                .build();
    }

    private static WorldGenLevel treeLevel(BlockPos origin, BlockPos obstruction) {
        WorldGenLevel level = mock(WorldGenLevel.class);
        when(level.getBlockState(any(BlockPos.class))).thenAnswer(invocation -> {
            BlockPos pos = invocation.getArgument(0);
            if (obstruction != null && obstruction.equals(pos)) {
                return Blocks.STONE.defaultBlockState();
            }
            if (pos.getY() == origin.getY() - 1
                    && pos.getX() >= origin.getX()
                    && pos.getX() < origin.getX() + 2
                    && pos.getZ() >= origin.getZ()
                    && pos.getZ() < origin.getZ() + 2) {
                return Blocks.FARMLAND.defaultBlockState();
            }
            return Blocks.AIR.defaultBlockState();
        });
        when(level.getFluidState(any(BlockPos.class))).thenReturn(Fluids.EMPTY.defaultFluidState());
        when(level.isOutsideBuildHeight(any(BlockPos.class))).thenReturn(false);
        when(level.isStateAtPosition(any(BlockPos.class), org.mockito.ArgumentMatchers.<Predicate<BlockState>>any()))
                .thenAnswer(invocation -> {
                    BlockPos pos = invocation.getArgument(0);
                    Predicate<BlockState> predicate = invocation.getArgument(1);
                    return predicate.test(stateAt(origin, obstruction, pos));
                });
        return level;
    }

    private static BlockState stateAt(BlockPos origin, BlockPos obstruction, BlockPos pos) {
        if (obstruction != null && obstruction.equals(pos)) {
            return Blocks.STONE.defaultBlockState();
        }
        if (pos.getY() == origin.getY() - 1
                && pos.getX() >= origin.getX()
                && pos.getX() < origin.getX() + 2
                && pos.getZ() >= origin.getZ()
                && pos.getZ() < origin.getZ() + 2) {
            return Blocks.FARMLAND.defaultBlockState();
        }
        return Blocks.AIR.defaultBlockState();
    }

    private static BlockState findLog(List<PlacedLog> placedLogs, BlockPos target) {
        for (PlacedLog log : placedLogs) {
            if (log.position().equals(target)) {
                return log.state();
            }
        }
        throw new AssertionError("Missing generated log at " + target);
    }
}
