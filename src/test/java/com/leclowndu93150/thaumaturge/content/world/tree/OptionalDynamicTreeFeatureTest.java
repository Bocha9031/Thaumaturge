package com.leclowndu93150.thaumaturge.content.world.tree;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.neoforged.fml.ModList;
import org.junit.jupiter.api.Test;

class OptionalDynamicTreeFeatureTest {
    @Test
    void absentDynamicTreesUsesTheNativeGeneratorWithoutLoadingOptionalClasses() {
        WorldGenLevel level = mock(WorldGenLevel.class);
        when(level.getMinBuildHeight()).thenReturn(-64);
        when(level.getMaxBuildHeight()).thenReturn(320);
        when(level.getBlockState(any())).thenReturn(Blocks.STONE.defaultBlockState());
        when(level.isStateAtPosition(any(), any())).thenAnswer(invocation -> {
            Predicate<BlockState> predicate = invocation.getArgument(1);
            return predicate.test(Blocks.STONE.defaultBlockState());
        });
        StraightTrunkPlacer trunk = spy(new StraightTrunkPlacer(8, 0, 0));
        TreeConfiguration config = new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(Blocks.OAK_LOG),
                        trunk,
                        BlockStateProvider.simple(Blocks.OAK_LEAVES),
                        new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.ZERO, 3),
                        new TwoLayersFeatureSize(1, 0, 1))
                .build();
        ModList mods = mock(ModList.class);
        try (var modList = mockStatic(ModList.class)) {
            modList.when(ModList::get).thenReturn(mods);
            assertFalse(new OptionalDynamicTreeFeature()
                    .place(new FeaturePlaceContext<>(
                            Optional.empty(),
                            level,
                            mock(ChunkGenerator.class),
                            RandomSource.create(42L),
                            new BlockPos(0, 64, 0),
                            config)));
            verify(mods).isLoaded("dynamictrees");
            verify(trunk).getTreeHeight(any());
            verify(level, never()).setBlock(any(), any(), anyInt());
        }
    }
}
