package com.leclowndu93150.thaumaturge.content.entity.eldritch;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class LeadingAim {
    private static final double CURRENT_LEAD_TICKS = 1.0;
    private static final double AIM_HEIGHT_FRACTION = 0.5;

    private LeadingAim() {}

    public static Vec3 at(Entity shooter, Entity target) {
        return at(shooter, target, CURRENT_LEAD_TICKS);
    }

    public static Vec3 at(Entity shooter, Entity target, double leadTicks) {
        return direction(
                shooter.position(), target.position(), target.getBbHeight(), target.getDeltaMovement(), leadTicks);
    }

    static Vec3 direction(
            Vec3 shooterPosition, Vec3 targetPosition, double targetHeight, Vec3 targetMotion, double leadTicks) {
        Vec3 aimPoint =
                targetPosition.add(0.0, targetHeight * AIM_HEIGHT_FRACTION, 0.0).add(targetMotion.scale(leadTicks));
        return aimPoint.subtract(shooterPosition);
    }
}
