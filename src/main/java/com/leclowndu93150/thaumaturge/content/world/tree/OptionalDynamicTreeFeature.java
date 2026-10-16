package com.leclowndu93150.thaumaturge.content.world.tree;

import com.leclowndu93150.thaumaturge.compat.dynamictrees.DynamicTreesPlacement;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.neoforged.fml.ModList;

public final class OptionalDynamicTreeFeature extends Feature<TreeConfiguration> {
    public OptionalDynamicTreeFeature() {
        super(TreeConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<TreeConfiguration> context) {
        return ModList.get().isLoaded("dynamictrees")
                ? DynamicTreesPlacement.place(context)
                : Feature.TREE.place(context);
    }
}
