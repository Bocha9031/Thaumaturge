package com.leclowndu93150.thaumaturge.client.effect.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.util.List;
import net.minecraft.client.Minecraft;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class BoltInstanceTest {
    @Test
    void pathExtendsPastTheTargetSoTubeMeshCanRenderItsFinalSegment() {
        try (MockedStatic<Minecraft> minecraft = mockStatic(Minecraft.class)) {
            minecraft.when(Minecraft::getInstance).thenReturn(mock(Minecraft.class));
            BoltInstance bolt = new BoltInstance(0.0, 0.0, 0.0, 2.0, 0.0, 0.0, 0xFFFFFF, 1.0F);

            List<BoltInstance.PathStep> path = bolt.computePath(0.0F);

            assertTrue(path.size() >= 4);
            BoltInstance.PathStep previous = path.get(path.size() - 3);
            BoltInstance.PathStep target = path.get(path.size() - 2);
            BoltInstance.PathStep terminal = path.get(path.size() - 1);
            assertEquals(2.0, target.x());
            assertEquals(0.0, target.y());
            assertEquals(0.0, target.z());
            assertEquals(target.x() * 2.0 - previous.x(), terminal.x(), 1.0E-9);
            assertEquals(target.y() * 2.0 - previous.y(), terminal.y(), 1.0E-9);
            assertEquals(target.z() * 2.0 - previous.z(), terminal.z(), 1.0E-9);
            assertEquals(target.width(), terminal.width());
        }
    }
}
