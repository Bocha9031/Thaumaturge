package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.dtteam.dynamictrees.systems.genfeature.GenFeatureConfiguration;
import com.dtteam.dynamictrees.systems.genfeature.context.PostGenerationContext;
import com.leclowndu93150.thaumaturge.content.world.tree.silverwood.ScatteredFlowersDecorator;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ShimmerleafGenFeature extends GenFeature {
    public ShimmerleafGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {}

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        LevelAccessor level = context.level();
        BlockState flower = TTBlocks.PLANT_SHIMMERLEAF.get().defaultBlockState();
        ScatteredFlowersDecorator.scatter(context.pos().above(), context.random(), level::isEmptyBlock, pos -> ScatteredFlowersDecorator.isFlowerSoil(level.getBlockState(pos.below())),
                pos -> level.setBlock(pos, flower, Block.UPDATE_CLIENTS));
        return true;
    }
}
