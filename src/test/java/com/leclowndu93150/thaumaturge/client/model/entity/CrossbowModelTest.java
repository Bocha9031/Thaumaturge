package com.leclowndu93150.thaumaturge.client.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

final class CrossbowModelTest {
    @Test
    void bakedModelUsesTheFullResolutionTextureAndScaledGeometryRoot() {
        ModelPart root = CrossbowModel.createLayer().bakeRoot();
        CrossbowModel model = new CrossbowModel(root);

        assertSame(root, model.root());
        assertEquals(0.5F, root.xScale);
        assertEquals(0.5F, root.yScale);
        assertEquals(0.5F, root.zScale);
        assertNotNull(root.getChild("stand").getChild("leg_front_right").getChild("foot"));
        assertNotNull(root.getChild("aim").getChild("bow_left").getChild("limb").getChild("limb_tip"));
    }
}
