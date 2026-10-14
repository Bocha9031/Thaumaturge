package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.mockito.Mockito.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import org.junit.jupiter.api.Test;

class GolemPressLavaCutoffTest {
    @Test
    void lavaUsesCutoffHeightWindingNormalizedUvsAndFullBrightness() {
        PoseStack.Pose pose = new PoseStack().last();
        VertexConsumer buffer = mock(VertexConsumer.class, RETURNS_SELF);
        GolemBuilderRenderer.lavaQuad(pose, buffer);
        var vertices = inOrder(buffer);
        vertices.verify(buffer).addVertex(pose, -0.3125F, 0.75F, 1.3125F);
        vertices.verify(buffer).setUv(1.0F, 1.0F);
        vertices.verify(buffer).addVertex(pose, 0.3125F, 0.75F, 1.3125F);
        vertices.verify(buffer).setUv(0.0F, 1.0F);
        vertices.verify(buffer).addVertex(pose, 0.3125F, 0.75F, 0.6875F);
        vertices.verify(buffer).setUv(0.0F, 0.0F);
        vertices.verify(buffer).addVertex(pose, -0.3125F, 0.75F, 0.6875F);
        vertices.verify(buffer).setUv(1.0F, 0.0F);
        verify(buffer, times(4)).setLight(LightTexture.FULL_BRIGHT);
        verify(buffer, times(4)).setNormal(pose, 0.0F, 1.0F, 0.0F);
        verify(buffer, times(4)).setColor(-1);
    }
}
