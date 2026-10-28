package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class BlockJarVoid extends AbstractBlockJar {
    public static final MapCodec<BlockJarVoid> CODEC = simpleCodec(BlockJarVoid::new);
    private static final Identifier SIDE_TEXTURE = TTIds.rl("block/jar_side_void");
    private static final Identifier TOP_TEXTURE = TTIds.rl("block/jar_top_void");

    public BlockJarVoid(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public Identifier jarSideTexture() {
        return SIDE_TEXTURE;
    }

    @Override
    public Identifier jarTopTexture() {
        return TOP_TEXTURE;
    }

    @Override
    protected MapCodec<BlockJarVoid> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<BlockEntityJarVoid> jarType() {
        return TTBlockEntities.JAR_VOID.get();
    }
}
