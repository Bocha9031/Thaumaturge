package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.particle.GolemEmoteParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** Entity-event protocol and client-side presentation for golem emotes. */
public enum GolemEvent {
    TASK_CLAIMED(GolemEvent.HANDOFF_TASK_ID, Look.TASK),
    TASK_FAILED(GolemEvent.FAILED_ID, Look.FAILED),
    CONFUSED(GolemEvent.CONFUSED_ID, Look.CONFUSED),
    STAY(GolemEvent.STAY_ID, Look.STAY),
    FOLLOW(GolemEvent.FOLLOW_ID, Look.TASK),
    RANK_UP(GolemEvent.RANK_UP_ID, Look.HEARTS);

    public static final byte HANDOFF_TASK_ID = 5;

    private static final byte FAILED_ID = 101;
    private static final byte CONFUSED_ID = 102;
    private static final byte STAY_ID = 103;
    private static final byte FOLLOW_ID = 104;
    private static final byte RANK_UP_ID = 105;

    private static final int WHITE = 0xFFFFFF;
    private static final int CYAN = 0x19FFFF;
    private static final int YELLOW = 0xFFFF19;
    private static final float ICON_SCALE = 2.0F;
    private static final double ICON_LIFT = 0.1D;
    private static final double HEART_SCATTER = 0.01D;
    private static final double HEART_MAX_RISE = 0.02D;

    private final byte id;
    private final Look look;

    GolemEvent(byte id, Look look) {
        this.id = id;
        this.look = look;
    }

    public byte id() {
        return id;
    }

    public static @Nullable GolemEvent byId(byte id) {
        for (GolemEvent event : values()) {
            if (event.id == id) {
                return event;
            }
        }
        return null;
    }

    public static boolean emotesEnabled() {
        return ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get();
    }

    public void broadcast(Entity entity) {
        Level level = entity.level();
        if (!level.isClientSide() && emotesEnabled()) {
            level.broadcastEntityEvent(entity, id);
        }
    }

    public void play(Entity entity) {
        Level level = entity.level();
        double top = entity.getBoundingBox().maxY;
        switch (look) {
            case TASK -> icon(level, entity, top, GolemEmoteParticleOptions.ICON_TASK, WHITE, 6, 0.0D);
            case FAILED -> icon(level, entity, top, GolemEmoteParticleOptions.ICON_FAIL, CYAN, 10, 0.025D);
            case CONFUSED -> icon(level, entity, top, GolemEmoteParticleOptions.ICON_CONFUSED, WHITE, 10, 0.05D);
            case STAY -> icon(level, entity, top, GolemEmoteParticleOptions.ICON_STAY, YELLOW, 20, 0.01D);
            case HEARTS -> hearts(level, entity, top);
        }
    }

    private static void icon(Level level, Entity entity, double top, int icon, int color, int ticks, double rise) {
        GolemEmoteParticleOptions options = new GolemEmoteParticleOptions(color, icon, ticks, ICON_SCALE);
        level.addParticle(options, entity.getX(), top + ICON_LIFT, entity.getZ(), 0.0D, rise, 0.0D);
    }

    private static void hearts(Level level, Entity entity, double top) {
        RandomSource random = level.getRandom();
        for (int i = 0; i < 5; i++) {
            int ticks = 20 + random.nextInt(20);
            float scale = 0.3F + random.nextFloat() * 0.4F;
            GolemEmoteParticleOptions options =
                    new GolemEmoteParticleOptions(WHITE, GolemEmoteParticleOptions.ICON_HEART, ticks, scale);
            double vx = random.nextGaussian() * HEART_SCATTER;
            double vz = random.nextGaussian() * HEART_SCATTER;
            double vy = random.nextDouble() * HEART_MAX_RISE;
            level.addParticle(options, entity.getX(), top, entity.getZ(), vx, vy, vz);
        }
    }

    private enum Look {
        TASK,
        FAILED,
        CONFUSED,
        STAY,
        HEARTS
    }
}
