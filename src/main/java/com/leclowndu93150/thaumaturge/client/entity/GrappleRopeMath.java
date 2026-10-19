package com.leclowndu93150.thaumaturge.client.entity;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

final class GrappleRopeMath {
    private static final int MIN_SEGMENTS = 2;
    private static final float SAMPLES_PER_BLOCK = 5.0F;
    private static final double WAVE_BLOCKS_PER_AMPLITUDE = 0.9;
    private static final double WAVE_LENGTH_X = 1.3;
    private static final double WAVE_LENGTH_Y = 1.75;
    private static final double WAVE_LENGTH_Z = 2.2;
    private static final double WAVE_DRIFT = 0.35;
    private static final double HOOK_RAMP = 8.0;
    private static final double THROWER_FADE_POWER = 1.5;

    private GrappleRopeMath() {}

    static List<Vec3> points(Vec3 hookToThrower, float amplitude, float ageInTicks) {
        double span = hookToThrower.length();
        int samples = Math.max(MIN_SEGMENTS + 1, (int) Math.ceil(span * SAMPLES_PER_BLOCK));
        double wave = amplitude * WAVE_BLOCKS_PER_AMPLITUDE;
        double drift = ageInTicks * WAVE_DRIFT;
        List<Vec3> points = new ArrayList<>(samples + 1);
        for (int i = 0; i <= samples; i++) {
            double along = i / (double) samples;
            double distance = along * span;
            double envelope = wave * Math.min(1.0, along * HOOK_RAMP) * Math.pow(1.0 - along, THROWER_FADE_POWER);
            double x = envelope * Math.sin(distance / WAVE_LENGTH_X * Mth.TWO_PI - drift);
            double y = envelope * Math.sin(distance / WAVE_LENGTH_Y * Mth.TWO_PI - drift);
            double z = envelope * Math.sin(distance / WAVE_LENGTH_Z * Mth.TWO_PI - drift);
            points.add(new Vec3(hookToThrower.x * along + x, hookToThrower.y * along + y, hookToThrower.z * along + z));
        }
        return points;
    }
}
