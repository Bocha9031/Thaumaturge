package com.leclowndu93150.thaumaturge.client.golem;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class GolemGhostRenderTypeTest {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/pig/pig.png");
    private static final int GHOST_TINT = 0x26FFFFFF;

    @Test
    void ghostGeometryTestsDepthButDoesNotWriteIt() {
        assertSame(
                TTRenderTypes.entityTranslucentNoDepthWrite(TEXTURE),
                GolemRenderer.renderType(TEXTURE, false, GHOST_TINT));
    }

    @Test
    void xrayGeometryStillIgnoresDepthAndOpaqueGeometryKeepsCutoutRendering() {
        assertSame(
                TTRenderTypes.entityTranslucentNoDepth(TEXTURE), GolemRenderer.renderType(TEXTURE, true, GHOST_TINT));
        assertSame(RenderType.entityCutout(TEXTURE), GolemRenderer.renderType(TEXTURE, false, -1));
    }
}
