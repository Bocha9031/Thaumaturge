package com.leclowndu93150.thaumaturge.client.render.crystal;

import com.leclowndu93150.thaumaturge.client.model.mesh.TTMesh;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshPart;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalFaceTransforms;
import com.leclowndu93150.thaumaturge.content.world.crystal.CrystalShards;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;

public final class CrystalBakedModel implements DynamicBlockStateModel {
    private static final Direction[] FACES = Direction.values();
    private static final int TINT_INDEX = 0;
    private static final int MATERIAL_FLAGS = 0;
    private static final boolean AMBIENT_OCCLUSION = true;

    private final BlockStateModelPart[][] shards;
    private final Material.Baked material;

    public CrystalBakedModel(TTMesh mesh, Material.Baked material) {
        this.material = material;
        this.shards = new BlockStateModelPart[FACES.length][CrystalShards.COUNT];
        for (Direction face : FACES) {
            for (int shard = 0; shard < CrystalShards.COUNT; shard++) {
                shards[face.ordinal()][shard] = bakeShard(mesh.parts().get(shard), face);
            }
        }
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        int growth = state.hasProperty(BlockCrystal.SIZE) ? state.getValue(BlockCrystal.SIZE) : 0;
        long seed = CrystalShards.seed(state, pos);
        boolean any = false;
        for (Direction face : FACES) {
            if (!CrystalShards.supports(level, pos, face)) {
                continue;
            }
            any = true;
            int[] order = CrystalShards.order(face, seed);
            for (int i = 0; i <= growth; i++) {
                parts.add(shards[face.ordinal()][order[i]]);
            }
        }
        if (!any) {
            parts.add(shards[Direction.DOWN.ordinal()][CrystalShards.unsupported(seed)]);
        }
    }

    private BlockStateModelPart bakeShard(TTMeshPart part, Direction face) {
        List<BakedQuad> quads = new ArrayList<>();
        CrystalQuadBaker.bakePart(part, material, TINT_INDEX, CrystalFaceTransforms.forFace(face), quads);
        QuadCollection.Builder builder = new QuadCollection.Builder();
        for (BakedQuad quad : quads) {
            builder.addUnculledFace(quad);
        }
        return new SimpleModelWrapper(builder.build(), AMBIENT_OCCLUSION, material);
    }

    @Override
    public Material.Baked particleMaterial() {
        return material;
    }

    @Override
    public int materialFlags() {
        return MATERIAL_FLAGS;
    }
}
