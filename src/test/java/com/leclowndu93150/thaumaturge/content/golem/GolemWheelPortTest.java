package com.leclowndu93150.thaumaturge.content.golem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

final class GolemWheelPortTest {
    private static final double DEGREES_PER_BLOCK = 360.0D / (Math.PI * 0.5D);

    @Test
    void forwardAndBackwardTravelRollInOppositeDirections() {
        assertEquals(DEGREES_PER_BLOCK * 0.5D, GolemWheel.spun(0.0F, 0.0D, 0.5D, 0.0F), 0.0001D);
        assertEquals(-DEGREES_PER_BLOCK * 0.5D, GolemWheel.spun(0.0F, 0.0D, -0.5D, 0.0F), 0.0001D);
    }

    @Test
    void lateralTravelDoesNotRollAndFacingDefinesForward() {
        assertEquals(0.0F, GolemWheel.spun(0.0F, 1.0D, 0.0D, 0.0F), 0.0001F);
        assertEquals(90.0F, GolemWheel.spun(0.0F, -Math.PI / 8.0D, 0.0D, 90.0F), 0.0001F);
    }

    @Test
    void rotationWrapsForLargePositiveAndNegativeChanges() {
        assertEquals((float) (170.0D + DEGREES_PER_BLOCK - 360.0D), GolemWheel.spun(170.0F, 0.0D, 1.0D, 0.0F), 0.0001F);
        assertEquals(
                (float) (-170.0D - DEGREES_PER_BLOCK + 360.0D), GolemWheel.spun(-170.0F, 0.0D, -1.0D, 0.0F), 0.0001F);
        assertEquals(33.0F, GolemWheel.spun(33.0F, 0.0D, Math.PI * 5.0D, 0.0F), 0.0001F);
    }

    @Test
    void entityCallerUsesPreviousHorizontalPositionAndBodyYaw() throws ReflectiveOperationException {
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);
        when(golem.getX()).thenReturn(4.0D);
        when(golem.getZ()).thenReturn(7.5D);
        when(golem.getYRot()).thenReturn(90.0F);
        golem.xo = 4.0D;
        golem.zo = 7.0D;
        golem.yBodyRot = 0.0F;
        golem.xOld = 40.0D;
        golem.yOld = 100.0D;
        golem.zOld = 40.0D;

        Method updateWheelRotation = EntityThaumaturgeGolem.class.getDeclaredMethod("updateWheelRotation");
        updateWheelRotation.setAccessible(true);
        updateWheelRotation.invoke(golem);

        assertEquals(DEGREES_PER_BLOCK * 0.5D, golem.wheelRotation, 0.0001D);
    }
}
