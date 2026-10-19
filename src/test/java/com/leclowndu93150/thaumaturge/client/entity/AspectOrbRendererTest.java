package com.leclowndu93150.thaumaturge.client.entity;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.aspect.ParticleTextures;
import org.junit.jupiter.api.Test;

final class AspectOrbRendererTest {
    @Test
    void selectsAspectBlendModeAndFallsBackToAdditiveForUnusualValues() {
        assertSame(TTRenderTypes.fxTranslucent(ParticleTextures.ORB_GLOW), AspectOrbRenderer.renderTypeForBlend(771));
        assertSame(
                TTRenderTypes.fxAlphaAdditiveBlurred(ParticleTextures.ORB_GLOW),
                AspectOrbRenderer.renderTypeForBlend(1));
        assertSame(
                TTRenderTypes.fxAlphaAdditiveBlurred(ParticleTextures.ORB_GLOW),
                AspectOrbRenderer.renderTypeForBlend(0));
    }
}
