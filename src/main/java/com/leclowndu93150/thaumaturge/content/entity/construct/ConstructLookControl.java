package com.leclowndu93150.thaumaturge.content.entity.construct;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;

final class ConstructLookControl extends LookControl {
    ConstructLookControl(Mob mob) {
        super(mob);
    }

    @Override
    public void setLookAt(double x, double y, double z, float yawSpeed, float pitchSpeed) {
        super.setLookAt(x, y, z, Math.min(yawSpeed, mob.getHeadRotSpeed()), Math.min(pitchSpeed, mob.getMaxHeadXRot()));
    }
}
