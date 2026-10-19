package com.leclowndu93150.thaumaturge.content.entity.ai;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class GhastLikeMoveControl extends MoveControl {
    private static final double APPROACH_SCALE = 0.1;

    private final Mob flyer;
    private int floatDuration;

    public GhastLikeMoveControl(Mob flyer) {
        super(flyer);
        this.flyer = flyer;
    }

    public void stop() {
        this.operation = MoveControl.Operation.WAIT;
    }

    @Override
    public void tick() {
        if (this.operation == MoveControl.Operation.MOVE_TO) {
            if (this.floatDuration-- <= 0) {
                this.floatDuration += this.flyer.getRandom().nextInt(5) + 2;
                Vec3 delta = new Vec3(
                        this.wantedX - this.flyer.getX(),
                        this.wantedY - this.flyer.getY(),
                        this.wantedZ - this.flyer.getZ());
                double distance = delta.length();
                delta = delta.normalize();
                if (canReach(delta, Mth.ceil(distance))) {
                    this.flyer.setDeltaMovement(this.flyer.getDeltaMovement().add(delta.scale(APPROACH_SCALE)));
                } else {
                    this.operation = MoveControl.Operation.WAIT;
                }
            }
        }
    }

    private boolean canReach(Vec3 direction, int steps) {
        AABB box = this.flyer.getBoundingBox();
        for (int step = 1; step < steps; step++) {
            box = box.move(direction);
            if (!this.flyer.level().noCollision(this.flyer, box)) {
                return false;
            }
        }
        return true;
    }
}
