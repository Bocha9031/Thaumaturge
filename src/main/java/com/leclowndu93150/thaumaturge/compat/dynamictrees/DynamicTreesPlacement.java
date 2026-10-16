package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import com.dtteam.dynamictrees.api.worldgen.LevelContext;
import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.tree.TreeHelper;
import com.dtteam.dynamictrees.tree.species.Species;
import com.dtteam.dynamictrees.worldgen.DynamicTreeGenerationContext;
import com.dtteam.dynamictrees.worldgen.JoCode;
import com.dtteam.dynamictrees.worldgen.JoCodeRegistry;
import com.dtteam.dynamictrees.worldgen.feature.DynamicTreeFeature;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

/** Generates dynamic trees at the positions selected by Thaumaturge's normal placed features. */
public final class DynamicTreesPlacement {
    private DynamicTreesPlacement() {}

    public static boolean place(FeaturePlaceContext<TreeConfiguration> context) {
        if (!DTConfigs.SERVER.worldGen.get()) return Feature.TREE.place(context);
        Block log = context.config()
                .trunkProvider
                .getState(context.random(), context.origin())
                .getBlock();
        boolean greatwood = log == TTBlocks.LOG_GREATWOOD.get();
        String speciesName;
        if (greatwood) {
            speciesName = "greatwood";
        } else if (log == TTBlocks.LOG_SILVERWOOD.get()) {
            speciesName = "silverwood";
        } else if (log == Blocks.OAK_LOG) {
            speciesName = "oak";
        } else {
            return Feature.TREE.place(context);
        }
        ResourceLocation speciesId = log == Blocks.OAK_LOG
                ? ResourceLocation.fromNamespaceAndPath("dynamictrees", speciesName)
                : TTIds.rl(speciesName);
        Species species = Species.REGISTRY.get(speciesId);
        if (!species.isValid()) return Feature.TREE.place(context);

        WorldGenLevel level = context.level();
        BlockPos root = context.origin().below();
        if (!species.isAcceptableSoilForWorldgen(level, root, level.getBlockState(root))) return false;
        BlockPos.MutableBlockPos stem = context.origin().mutable();
        for (int height = 0; height < species.getSignalEnergy(); height++, stem.move(Direction.UP)) {
            if (level.isOutsideBuildHeight(stem) || !DynamicTreeFeature.validTreePos(level, stem)) return false;
        }
        int radius = greatwood ? 8 : 6;
        JoCode template = JoCodeRegistry.getRandomCode(species.getRegistryName(), radius, context.random());
        if (template == null) return false;
        // JoCodes keep mutable rotation state. Each placement owns its copy during parallel generation.
        JoCode code = new JoCode(template.toString()).setCareful(true);
        Direction facing = Direction.from2DDataValue(context.random().nextInt(4));
        code.generate(new DynamicTreeGenerationContext(
                LevelContext.create(level),
                species,
                context.origin(),
                root.mutable(),
                level.getBiome(root),
                facing,
                radius,
                true));
        return TreeHelper.getBranchOpt(level.getBlockState(context.origin()))
                .filter(branch -> branch.getFamily() == species.getFamily())
                .isPresent();
    }
}
