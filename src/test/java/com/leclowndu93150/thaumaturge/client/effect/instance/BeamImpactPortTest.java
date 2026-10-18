package com.leclowndu93150.thaumaturge.client.effect.instance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.network.effect.ClientboundStreamEffectPayload;
import org.junit.jupiter.api.Test;

class BeamImpactPortTest {
    @Test
    void hitFlagIsEncodedWithoutChangingLegacyBeamPayloads() {
        ClientboundStreamEffectPayload hit = beamPayload(true);
        ClientboundStreamEffectPayload ordinary = beamPayload(false);

        assertTrue(hit.hasFlag(ClientboundStreamEffectPayload.FLAG_HIT));
        assertFalse(ordinary.hasFlag(ClientboundStreamEffectPayload.FLAG_HIT));
    }

    @Test
    void impactFlareKeepsShortBeamAliveUntilTheFlareExpires() {
        BeamInstance beam = beam(true);

        beam.tick();
        assertFalse(beam.isExpired());
        assertTrue(beam.hit());

        for (int i = 0; i < 4; i++) {
            beam.tick();
        }
        assertFalse(beam.isExpired());

        beam.tick();
        assertTrue(beam.isExpired());
        assertFalse(beam.hit());
    }

    @Test
    void ordinaryBeamLifetimeIsUnchanged() {
        BeamInstance beam = beam(false);

        beam.tick();
        assertFalse(beam.isExpired());
        beam.tick();
        assertTrue(beam.isExpired());
    }

    private static BeamInstance beam(boolean hit) {
        return new BeamInstance(0, 0, 0, 1, 0, 0, 0x80C0FF, 1, 0, 1.0F, false, -1, false, hit);
    }

    private static ClientboundStreamEffectPayload beamPayload(boolean hit) {
        return ClientboundStreamEffectPayload.beam(0, 0, 0, 1, 0, 0, 0x80C0FF, 1, 0, 1.0F, false, -1, false, hit);
    }
}
