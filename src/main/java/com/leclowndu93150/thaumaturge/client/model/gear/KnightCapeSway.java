package com.leclowndu93150.thaumaturge.client.model.gear;

import com.leclowndu93150.thaumaturge.content.entity.cloak.CloakTracker;
import com.leclowndu93150.thaumaturge.content.entity.cloak.CloakWearer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

record KnightCapeSway(float flap, float lean, float sideLean) {
    private static final float FLAP_PER_BLOCK = 10.0F;
    private static final float FLAP_MIN = -6.0F;
    private static final float FLAP_MAX = 32.0F;
    private static final float LEAN_PER_BLOCK = 100.0F;
    private static final float LEAN_MAX = 150.0F;
    private static final float SIDE_LEAN_LIMIT = 20.0F;
    private static final float STEP_FREQUENCY = 6.0F;
    private static final float STEP_FLAP = 32.0F;
    private static final float LEAN_SHARE = 0.5F;
    private static final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);
    private static final KnightCapeSway STILL = new KnightCapeSway(0.0F, 0.0F, 0.0F);

    static KnightCapeSway sample(LivingEntity entity, float partialTick) {
        if (entity instanceof Player player) return fromPlayer(player, partialTick);
        if (entity instanceof CloakWearer wearer) return fromCloak(entity, wearer.cloak(), partialTick);
        return STILL;
    }

    static KnightCapeSway fromPlayer(Player player, float partialTick) {
        double dx =
                Mth.lerp(partialTick, player.xCloakO, player.xCloak) - Mth.lerp(partialTick, player.xo, player.getX());
        double dy =
                Mth.lerp(partialTick, player.yCloakO, player.yCloak) - Mth.lerp(partialTick, player.yo, player.getY());
        double dz =
                Mth.lerp(partialTick, player.zCloakO, player.zCloak) - Mth.lerp(partialTick, player.zo, player.getZ());
        float step = Mth.sin(Mth.lerp(partialTick, player.walkDistO, player.walkDist) * STEP_FREQUENCY)
                * STEP_FLAP
                * Mth.lerp(partialTick, player.oBob, player.bob);
        float fallFlyingTicks = player.getFallFlyingTicks() + partialTick;
        float fallFlyingScale = Mth.clamp(fallFlyingTicks * fallFlyingTicks / 100.0F, 0.0F, 1.0F);
        return fromDisplacement(
                dx, dy, dz, Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot), step, 1.0F - fallFlyingScale);
    }

    static KnightCapeSway fromCloak(LivingEntity entity, CloakTracker cloak, float partialTick) {
        double dx = cloak.x(partialTick) - Mth.lerp(partialTick, entity.xo, entity.getX());
        double dy = cloak.y(partialTick) - Mth.lerp(partialTick, entity.yo, entity.getY());
        double dz = cloak.z(partialTick) - Mth.lerp(partialTick, entity.zo, entity.getZ());
        float step = Mth.sin(cloak.walkDistance(partialTick) * STEP_FREQUENCY) * STEP_FLAP * cloak.bob(partialTick);
        return fromDisplacement(dx, dy, dz, Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot), step);
    }

    static KnightCapeSway fromDisplacement(double dx, double dy, double dz, float bodyYaw, float step) {
        return fromDisplacement(dx, dy, dz, bodyYaw, step, 1.0F);
    }

    private static KnightCapeSway fromDisplacement(
            double dx, double dy, double dz, float bodyYaw, float step, float forwardLeanScale) {
        float yaw = bodyYaw * Mth.DEG_TO_RAD;
        double backX = Mth.sin(yaw);
        double backZ = -Mth.cos(yaw);
        float flap = Mth.clamp((float) dy * FLAP_PER_BLOCK, FLAP_MIN, FLAP_MAX) + step;
        float lean = Mth.clamp((float) (dx * backX + dz * backZ) * LEAN_PER_BLOCK * forwardLeanScale, 0.0F, LEAN_MAX);
        float sideLean =
                Mth.clamp((float) (dx * backZ - dz * backX) * LEAN_PER_BLOCK, -SIDE_LEAN_LIMIT, SIDE_LEAN_LIMIT);
        return new KnightCapeSway(flap, lean, sideLean);
    }

    void swing(ModelPart cape) {
        float pitch = (lean * LEAN_SHARE + flap) * DEGREES_TO_RADIANS;
        float roll = sideLean * LEAN_SHARE * DEGREES_TO_RADIANS;
        Quaternionf sway = new Quaternionf().rotationX(pitch).rotateZ(roll).rotateY(-roll);
        Vector3f euler = new Matrix3f()
                .rotationZYX(cape.zRot, cape.yRot, cape.xRot)
                .rotate(sway)
                .getEulerAnglesZYX(new Vector3f());
        cape.setRotation(euler.x, euler.y, euler.z);
    }
}
