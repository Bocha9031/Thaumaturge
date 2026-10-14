package com.leclowndu93150.thaumaturge.client.render.blockentity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.junit.jupiter.api.Test;

class AdvancedFurnacePanelCutoffTest {
    @Test
    void windowPanelUsesCutoffFaceLiftFillHeightAndNormalizedUvs() {
        PoseStack.Pose pose = new PoseStack().last();
        VertexConsumer buffer = mock(VertexConsumer.class, RETURNS_SELF);
        var plane = AdvancedAlchemicalFurnaceRenderer.windowPlane(-0.375F, 0.006F, 0.5F);

        AdvancedAlchemicalFurnaceRenderer.writePanel(buffer, pose, plane, 0.0F, 12);

        verify(buffer, times(2)).addVertex(pose, 1.881F, 1.4375F, -0.375F);
        verify(buffer, times(2)).addVertex(pose, 1.881F, 1.4375F, -0.625F);
        verify(buffer, times(2)).addVertex(pose, 1.881F, 1.1875F, -0.625F);
        verify(buffer, times(2)).addVertex(pose, 1.881F, 1.1875F, -0.375F);
        verify(buffer, times(2)).setUv(0.0F, 1.0F);
        verify(buffer, times(2)).setUv(1.0F, 1.0F);
        verify(buffer, times(2)).setUv(1.0F, 0.0F);
        verify(buffer, times(2)).setUv(0.0F, 0.0F);
        verify(buffer, times(8)).setLight(12);
        verify(buffer, times(8)).setColor(-1);
    }

    @Test
    void ventPanelKeepsItsOwnCutoffPositionAndShortFill() {
        PoseStack.Pose pose = new PoseStack().last();
        VertexConsumer buffer = mock(VertexConsumer.class, RETURNS_SELF);
        var plane = AdvancedAlchemicalFurnaceRenderer.ventPlane(0.006F, 0.5F);

        AdvancedAlchemicalFurnaceRenderer.writePanel(buffer, pose, plane, 0.0F, 15);

        verify(buffer, times(2)).addVertex(pose, 1.9435F, 0.5625F, 0.625F);
        verify(buffer, times(2)).addVertex(pose, 1.9435F, 0.5625F, 0.375F);
        verify(buffer, times(2)).addVertex(pose, 1.9435F, 0.4375F, 0.375F);
        verify(buffer, times(2)).addVertex(pose, 1.9435F, 0.4375F, 0.625F);
        verify(buffer, times(8)).setLight(15);
    }
}
