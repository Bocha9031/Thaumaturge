package com.leclowndu93150.thaumaturge.content.world.taint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gson.JsonElement;
import com.leclowndu93150.thaumaturge.content.world.objects.SurfaceBiomeSearch;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.junit.jupiter.api.Test;

class TaintBiomeWorldgenTest {
    @Test
    void disabledLandmarksSkipBiomeSourceAndEntityPlacementLookup() {
        TaintBiomeConfig config = new TaintBiomeConfig(
                net.minecraft.world.level.block.Blocks.CRYING_OBSIDIAN, 0, UniformInt.of(0, 0), 0, 0, 32, false, 4, 48);
        WorldGenLevel level = mock(WorldGenLevel.class);
        FeaturePlaceContext<TaintBiomeConfig> context = new FeaturePlaceContext<>(
                Optional.empty(),
                level,
                mock(ChunkGenerator.class),
                RandomSource.create(17),
                new BlockPos(-16, 64, 32),
                config);

        assertFalse(new TaintBiomeFeature(TaintBiomeConfig.CODEC).place(context));

        verify(level, never()).getLevel();
    }

    @Test
    void taintBiomeConfigCodecPreservesNonDefaultDatapackValues() {
        TaintBiomeConfig config = new TaintBiomeConfig(
                net.minecraft.world.level.block.Blocks.CRYING_OBSIDIAN,
                7,
                UniformInt.of(2, 5),
                3,
                17,
                64,
                false,
                9,
                120);

        JsonElement encoded =
                TaintBiomeConfig.CODEC.encodeStart(JsonOps.INSTANCE, config).getOrThrow();
        TaintBiomeConfig decoded =
                TaintBiomeConfig.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(config.crust(), decoded.crust());
        assertEquals(7, decoded.maxCrustBlobs());
        assertEquals(2, decoded.crustRadius().getMinValue());
        assertEquals(5, decoded.crustRadius().getMaxValue());
        assertEquals(3, decoded.grassFibreAttempts());
        assertEquals(17, decoded.generalFibreAttempts());
        assertEquals(64, decoded.groundSearchDepth());
        assertFalse(decoded.landmarks());
        assertEquals(9, decoded.landmarkRadiusChunks());
        assertEquals(120, decoded.landmarkAttempts());
    }

    @Test
    void surfaceSearchChoosesFirstEligibleSurfaceQuartCenterInNegativeChunk() {
        PlacementContext context = mock(PlacementContext.class);
        WorldGenLevel level = mock(WorldGenLevel.class);
        ChunkGenerator generator = mock(ChunkGenerator.class);
        PlacedFeature feature = mock(PlacedFeature.class);
        Holder<Biome> biome = Holder.direct(mock(Biome.class));
        net.minecraft.world.level.biome.BiomeGenerationSettings settings =
                mock(net.minecraft.world.level.biome.BiomeGenerationSettings.class);
        AtomicInteger checked = new AtomicInteger();
        BlockPos expected = new BlockPos(-26, 73, -26);

        when(context.topFeature()).thenReturn(Optional.of(feature));
        when(context.getLevel()).thenReturn(level);
        when(context.generator()).thenReturn(generator);
        when(context.getHeight(Heightmap.Types.WORLD_SURFACE_WG, expected.getX(), expected.getZ()))
                .thenReturn(expected.getY());
        when(level.getBiome(any(BlockPos.class))).thenReturn(biome);
        when(generator.getBiomeGenerationSettings(biome)).thenReturn(settings);
        when(settings.hasFeature(feature)).thenAnswer(call -> checked.incrementAndGet() == 6);

        PlacementModifier search = SurfaceBiomeSearch.INSTANCE;
        List<BlockPos> selected = search.getPositions(context, RandomSource.create(29), new BlockPos(-17, 0, -17))
                .toList();

        assertEquals(List.of(expected), selected);
        assertEquals(6, checked.get());
    }

    @Test
    void surfaceSearchPreservesOriginWithoutFeatureAndSkipsWhenNoSurfaceBiomeMatches() {
        PlacementContext missingFeatureContext = mock(PlacementContext.class);
        BlockPos origin = new BlockPos(17, 64, -17);
        when(missingFeatureContext.topFeature()).thenReturn(Optional.empty());
        assertEquals(
                List.of(origin),
                SurfaceBiomeSearch.INSTANCE
                        .getPositions(missingFeatureContext, RandomSource.create(3), origin)
                        .toList());

        PlacementContext context = mock(PlacementContext.class);
        WorldGenLevel level = mock(WorldGenLevel.class);
        ChunkGenerator generator = mock(ChunkGenerator.class);
        PlacedFeature feature = mock(PlacedFeature.class);
        Holder<Biome> biome = Holder.direct(mock(Biome.class));
        net.minecraft.world.level.biome.BiomeGenerationSettings settings =
                mock(net.minecraft.world.level.biome.BiomeGenerationSettings.class);
        when(context.topFeature()).thenReturn(Optional.of(feature));
        when(context.getLevel()).thenReturn(level);
        when(context.generator()).thenReturn(generator);
        when(context.getHeight(any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(80);
        when(level.getBiome(any(BlockPos.class))).thenReturn(biome);
        when(generator.getBiomeGenerationSettings(biome)).thenReturn(settings);
        when(settings.hasFeature(feature)).thenReturn(false);

        assertTrue(SurfaceBiomeSearch.INSTANCE
                .getPositions(context, RandomSource.create(4), origin)
                .toList()
                .isEmpty());
    }
}
