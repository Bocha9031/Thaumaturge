package com.leclowndu93150.thaumaturge.client.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class FloatyLineRendererTest {
    @Test
    void ribbonsStayFiniteAndAttachToBothEndpointsInEveryDirection() {
        for (Vec3 end : List.of(new Vec3(5, 0, 0), new Vec3(0, 5, 0), new Vec3(0, -5, 0), new Vec3(-3, 2, 4))) {
            List<Vec3> vertices = vertices(end, 1.0F);
            assertTrue(vertices.size() >= 16);
            assertEquals(0.0, vertices.get(0).add(vertices.get(1)).scale(0.5).length(), 0.00001);
            Vec3 lastCenter =
                    vertices.get(vertices.size() - 2).add(vertices.getLast()).scale(0.5);
            assertEquals(0.0, lastCenter.distanceTo(end), 0.00001);
            assertTrue(vertices.stream()
                    .allMatch(v -> Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z)));
        }
    }

    @Test
    void partialFlowStopsAtRequestedProgressAndEmptyFlowDrawsNothing() {
        assertTrue(vertices(Vec3.ZERO, 1.0F).isEmpty());
        assertTrue(vertices(new Vec3(0, 0, 5), 0.0F).isEmpty());
        List<Vec3> vertices = vertices(new Vec3(0, 0, 5), 0.5F);
        Vec3 lastCenter =
                vertices.get(vertices.size() - 2).add(vertices.getLast()).scale(0.5);
        assertEquals(2.5, lastCenter.z, 0.00001);
    }

    private static List<Vec3> vertices(Vec3 end, float progress) {
        List<Vec3> vertices = new ArrayList<>();
        VertexConsumer buffer = mock(VertexConsumer.class, RETURNS_SELF);
        doAnswer(call -> {
                    vertices.add(new Vec3(
                            call.getArgument(1, Float.class),
                            call.getArgument(2, Float.class),
                            call.getArgument(3, Float.class)));
                    return buffer;
                })
                .when(buffer)
                .addVertex(any(PoseStack.Pose.class), anyFloat(), anyFloat(), anyFloat());
        FloatyLineRenderer.write(new PoseStack().last(), buffer, end, 14.5F, 0xAAEEFF, -0.02F, progress, 0.15F);
        return vertices;
    }
}
