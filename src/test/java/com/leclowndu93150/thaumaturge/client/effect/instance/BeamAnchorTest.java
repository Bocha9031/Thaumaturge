package com.leclowndu93150.thaumaturge.client.effect.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class BeamAnchorTest {
    private static final float PARTIAL_TICK = 0.5F;

    @Test
    void castPointUsesTheCorrectMainHandSideAndViewOrigin() {
        LivingEntity caster = caster(HumanoidArm.RIGHT, false, InteractionHand.MAIN_HAND);

        Vec3 point = BeamAnchor.castPoint(caster, PARTIAL_TICK);

        assertEquals(9.68, point.x, 1.0E-6);
        assertEquals(64.9, point.y, 1.0E-6);
        assertEquals(-1.55, point.z, 1.0E-6);
    }

    @Test
    void offhandUseMovesTheAnchorToTheOppositeSide() {
        LivingEntity caster = caster(HumanoidArm.LEFT, true, InteractionHand.OFF_HAND);

        Vec3 point = BeamAnchor.castPoint(caster, PARTIAL_TICK);

        assertEquals(9.68, point.x, 1.0E-6);
        assertEquals(-1.55, point.z, 1.0E-6);
    }

    private static LivingEntity caster(HumanoidArm mainArm, boolean usingItem, InteractionHand usedHand) {
        LivingEntity caster = mock(LivingEntity.class);
        when(caster.getPosition(PARTIAL_TICK)).thenReturn(new Vec3(10.0, 64.0, -2.0));
        when(caster.getViewYRot(PARTIAL_TICK)).thenReturn(0.0F);
        when(caster.getViewVector(PARTIAL_TICK)).thenReturn(new Vec3(0.0, 0.0, 1.0));
        when(caster.getBbHeight()).thenReturn(1.8F);
        when(caster.getMainArm()).thenReturn(mainArm);
        when(caster.isUsingItem()).thenReturn(usingItem);
        when(caster.getUsedItemHand()).thenReturn(usedHand);
        return caster;
    }
}
