package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class GrappleGunItemTest {
    @Test
    void mainHandGrappleSpawnsForwardAndBesideDominantHand() {
        Player player = player(HumanoidArm.RIGHT);

        Vec3 spawn = GrappleGunItem.spawnPoint(player, InteractionHand.MAIN_HAND);

        assertTrue(spawn.distanceToSqr(new Vec3(1.6, 64.0, 3.9)) < 1.0E-10);
    }

    @Test
    void offHandGrappleSpawnsBesideOppositeHand() {
        Player player = player(HumanoidArm.RIGHT);

        Vec3 spawn = GrappleGunItem.spawnPoint(player, InteractionHand.OFF_HAND);

        assertTrue(spawn.distanceToSqr(new Vec3(2.4, 64.0, 3.9)) < 1.0E-10);
    }

    private static Player player(HumanoidArm mainArm) {
        Player player = mock(Player.class);
        when(player.getMainArm()).thenReturn(mainArm);
        when(player.getYRot()).thenReturn(0.0F);
        when(player.position()).thenReturn(new Vec3(2.0, 64.0, 3.0));
        return player;
    }
}
