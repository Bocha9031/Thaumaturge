package com.leclowndu93150.thaumaturge.client.model.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.entity.cloak.CloakTracker;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class KnightCapeSwayTest {
    @Test
    void capeSwayUsesNativeAvatarDisplacementAndClamps() {
        KnightCapeSway sway = KnightCapeSway.fromDisplacement(1.0, 1.0, -2.0, 0.0F, 0.0F);

        assertEquals(10.0F, sway.flap(), 0.0001F);
        assertEquals(150.0F, sway.lean(), 0.0001F);
        assertEquals(-20.0F, sway.sideLean(), 0.0001F);
    }

    @Test
    void cultistTrackerInterpolatesFollowMovementAndStepBob() {
        Level level = mock(Level.class);
        LivingEntity entity = mock(LivingEntity.class);
        AtomicReference<Double> x = new AtomicReference<>(0.0);
        when(level.isClientSide()).thenReturn(true);
        when(entity.level()).thenReturn(level);
        when(entity.getX()).thenAnswer(call -> x.get());
        when(entity.getY()).thenReturn(0.0);
        when(entity.getZ()).thenReturn(0.0);
        when(entity.onGround()).thenReturn(true);
        when(entity.isDeadOrDying()).thenReturn(false);
        when(entity.isSwimming()).thenReturn(false);
        when(entity.isPassenger()).thenReturn(false);

        CloakTracker tracker = new CloakTracker();
        tracker.tick(entity);
        x.set(2.0);
        tracker.tick(entity);

        assertEquals(0.25, tracker.x(0.5F), 0.0001);
        assertEquals(0.6F, tracker.walkDistance(0.5F), 0.0001F);
        assertEquals(0.02F, tracker.bob(0.5F), 0.0001F);
        KnightCapeSway sway = KnightCapeSway.fromCloak(entity, tracker, 0.5F);
        assertEquals(Mth.sin(3.6F) * 0.64F, sway.flap(), 0.0001F);
    }

    @Test
    void playerCapeSamplingInterpolatesNativeCloakHistory() {
        Player player = mock(Player.class);
        player.xCloakO = 0.0;
        player.xCloak = 1.0;
        player.yCloakO = 0.0;
        player.yCloak = 1.0;
        player.zCloakO = 0.0;
        player.zCloak = 0.0;
        player.xo = 0.0;
        player.yo = 0.0;
        player.zo = 0.0;
        player.walkDistO = 0.0F;
        player.walkDist = 0.0F;
        player.oBob = 0.0F;
        player.bob = 0.0F;
        player.yBodyRotO = 0.0F;
        player.yBodyRot = 0.0F;
        when(player.getX()).thenReturn(0.0);
        when(player.getY()).thenReturn(0.0);
        when(player.getZ()).thenReturn(0.0);

        KnightCapeSway sway = KnightCapeSway.fromPlayer(player, 0.5F);

        assertEquals(5.0F, sway.flap(), 0.0001F);
        assertEquals(0.0F, sway.lean(), 0.0001F);
        assertEquals(-20.0F, sway.sideLean(), 0.0001F);
    }

    @Test
    void playerFallFlyingScaleAttenuatesLeanBeforeClamp() {
        Player moderate = playerWithCapeDisplacement(0.0, 0.0, -1.0);
        when(moderate.getFallFlyingTicks()).thenReturn(5);
        assertEquals(75.0F, KnightCapeSway.fromPlayer(moderate, 0.0F).lean(), 0.0001F);

        Player beyondClamp = playerWithCapeDisplacement(0.0, 0.0, -2.0);
        when(beyondClamp.getFallFlyingTicks()).thenReturn(5);
        assertEquals(150.0F, KnightCapeSway.fromPlayer(beyondClamp, 0.0F).lean(), 0.0001F);

        Player fullyFlying = playerWithCapeDisplacement(0.0, 0.0, -2.0);
        when(fullyFlying.getFallFlyingTicks()).thenReturn(10);
        assertEquals(0.0F, KnightCapeSway.fromPlayer(fullyFlying, 0.0F).lean(), 0.0001F);
    }

    @Test
    void capeRotationCanBeReappliedAfterNativePoseReset() {
        ModelPart root = KnightArmorModel.createChest().bakeRoot();
        ModelPart cape = root.getChild("body").getChild("chestplate").getChild("cape_upper");
        KnightCapeSway sway = new KnightCapeSway(12.0F, 30.0F, -16.0F);

        sway.swing(cape);
        float firstPitch = cape.xRot;
        float firstYaw = cape.yRot;
        float firstRoll = cape.zRot;
        assertNotEquals(0.0F, firstYaw, 0.0001F);
        assertNotEquals(0.0F, firstRoll, 0.0001F);
        root.getAllParts().forEach(ModelPart::resetPose);
        sway.swing(cape);

        assertEquals(firstPitch, cape.xRot, 0.0001F);
        assertEquals(firstYaw, cape.yRot, 0.0001F);
        assertEquals(firstRoll, cape.zRot, 0.0001F);
    }

    private static Player playerWithCapeDisplacement(double x, double y, double z) {
        Player player = mock(Player.class);
        player.xCloakO = x;
        player.xCloak = x;
        player.yCloakO = y;
        player.yCloak = y;
        player.zCloakO = z;
        player.zCloak = z;
        player.xo = 0.0;
        player.yo = 0.0;
        player.zo = 0.0;
        player.walkDistO = 0.0F;
        player.walkDist = 0.0F;
        player.oBob = 0.0F;
        player.bob = 0.0F;
        player.yBodyRotO = 0.0F;
        player.yBodyRot = 0.0F;
        when(player.getX()).thenReturn(0.0);
        when(player.getY()).thenReturn(0.0);
        when(player.getZ()).thenReturn(0.0);
        return player;
    }
}
