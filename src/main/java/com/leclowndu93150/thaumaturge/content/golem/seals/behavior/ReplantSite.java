package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

final class ReplantSite {
    private static final long DEFAULT_POS = 0L;
    private static final byte DEFAULT_FACE = 0;

    static final Codec<ReplantSite> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.LONG.optionalFieldOf("pos", DEFAULT_POS).forGetter(site -> site.pos.asLong()),
                    Codec.BYTE.optionalFieldOf("face", DEFAULT_FACE).forGetter(site ->
                            (byte) site.face.get3DDataValue()),
                    Codec.BOOL.optionalFieldOf("tilled", false).forGetter(site -> site.tilled),
                    ItemStack.OPTIONAL_CODEC
                            .optionalFieldOf("seed", ItemStack.EMPTY)
                            .forGetter(site -> site.seed))
            .apply(
                    instance,
                    (pos, face, tilled, seed) ->
                            new ReplantSite(BlockPos.of(pos), Direction.from3DDataValue(face), seed, tilled)));

    static final Codec<ReplantSite> LEGACY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.either(Codec.LONG, BlockPos.CODEC)
                            .xmap(either -> either.map(value -> value, BlockPos::asLong), Either::left)
                            .optionalFieldOf("taskloc", DEFAULT_POS)
                            .forGetter(site -> site.pos.asLong()),
                    Codec.BYTE.optionalFieldOf("taskface", DEFAULT_FACE).forGetter(site ->
                            (byte) site.face.get3DDataValue()),
                    Codec.BOOL.optionalFieldOf("farmland", false).forGetter(site -> site.tilled),
                    ItemStack.OPTIONAL_CODEC
                            .optionalFieldOf("seed", ItemStack.EMPTY)
                            .forGetter(site -> site.seed))
            .apply(
                    instance,
                    (pos, face, tilled, seed) ->
                            new ReplantSite(BlockPos.of(pos), Direction.from3DDataValue(face), seed, tilled)));

    private final BlockPos pos;
    private final Direction face;
    private final ItemStack seed;
    private final boolean tilled;
    private int taskId;

    ReplantSite(BlockPos pos, Direction face, ItemStack seed, boolean tilled) {
        this.pos = pos;
        this.face = face;
        this.seed = seed;
        this.tilled = tilled;
    }

    BlockPos pos() {
        return pos;
    }

    Direction face() {
        return face;
    }

    ItemStack seed() {
        return seed;
    }

    boolean tilled() {
        return tilled;
    }

    int taskId() {
        return taskId;
    }

    void assign(int taskId) {
        this.taskId = taskId;
    }
}
