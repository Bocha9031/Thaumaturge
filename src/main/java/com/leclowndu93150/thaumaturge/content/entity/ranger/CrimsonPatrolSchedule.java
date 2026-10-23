package com.leclowndu93150.thaumaturge.content.entity.ranger;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class CrimsonPatrolSchedule extends SavedData {
    private static final long UNSCHEDULED = 0L;

    public static final Codec<CrimsonPatrolSchedule> CODEC = RecordCodecBuilder
            .create(builder -> builder.group(Codec.LONG.fieldOf("next_attempt").forGetter(schedule -> schedule.nextAttempt)).apply(builder, CrimsonPatrolSchedule::new));
    public static final SavedDataType<CrimsonPatrolSchedule> TYPE = new SavedDataType<>(TTIds.rl("crimson_patrols"), CrimsonPatrolSchedule::new, CODEC, DataFixTypes.LEVEL);

    private long nextAttempt;

    public CrimsonPatrolSchedule() {
        this(UNSCHEDULED);
    }

    private CrimsonPatrolSchedule(long nextAttempt) {
        this.nextAttempt = nextAttempt;
    }

    public static CrimsonPatrolSchedule get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isScheduled() {
        return nextAttempt != UNSCHEDULED;
    }

    public boolean isDue(long gameTime) {
        return gameTime >= nextAttempt;
    }

    public void schedule(long gameTime) {
        nextAttempt = gameTime;
        setDirty();
    }
}
