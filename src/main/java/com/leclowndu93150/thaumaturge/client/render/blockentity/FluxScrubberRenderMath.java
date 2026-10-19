package com.leclowndu93150.thaumaturge.client.render.blockentity;

final class FluxScrubberRenderMath {
    static final float PULSE_PERIOD = 140.0F;
    static final float PULSE_DEPTH = 0.055F;
    private static final long PHASE_RANGE = 1000L;

    private FluxScrubberRenderMath() {}

    static float bob(long gameTime, float partialTick, long blockSeed) {
        long phase = Math.floorMod(blockSeed, PHASE_RANGE);
        float cycle = (Math.floorMod(gameTime + phase, (long) PULSE_PERIOD) + partialTick) / PULSE_PERIOD;
        float triangle = 1.0F - Math.abs(2.0F * cycle - 1.0F);
        float eased = triangle * triangle * (3.0F - 2.0F * triangle);
        return eased * PULSE_DEPTH;
    }
}
