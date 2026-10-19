package com.leclowndu93150.thaumaturge.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.client.particle.support.BoltLineShape;
import com.leclowndu93150.thaumaturge.content.particle.BoltParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.StabilizerRuneParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.VisSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;

final class ParticleBehaviorPortTest {
    @Test
    void visSparkleAndWispyMoteRemoveAtTheirExactArrivalDistances() throws Exception {
        ClientLevel level = mock(ClientLevel.class);
        VisSparkleParticle sparkle = construct(
                VisSparkleParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    VisSparkleParticleOptions.class,
                    ParticleSheet.class
                },
                level,
                0.0,
                0.0,
                0.0,
                new VisSparkleParticleOptions(0.2, 0.0, 0.0, 0),
                null);
        invokeUpdate(sparkle);
        assertFalse(sparkle.isAlive());

        Entity target = mock(Entity.class);
        when(level.getEntity(8)).thenReturn(target);
        when(target.isAlive()).thenReturn(true);
        when(target.position()).thenReturn(new Vec3(0.25, 0.0, 0.0));
        WispyMoteParticle mote = construct(
                WispyMoteParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    double.class,
                    WispyMoteParticleOptions.class,
                    ParticleSheet.class
                },
                level,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                new WispyMoteParticleOptions(0, 40, 0.0F, 8),
                null);
        invokeUpdate(mote);
        assertFalse(mote.isAlive());
    }

    @Test
    void visSparkleKeepsHomingSpeedBoundedAndShrinksWhileNearTarget() throws Exception {
        VisSparkleParticle sparkle = construct(
                VisSparkleParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    VisSparkleParticleOptions.class,
                    ParticleSheet.class
                },
                mock(ClientLevel.class),
                0.0,
                0.0,
                0.0,
                new VisSparkleParticleOptions(10.0, 0.0, 0.0, 0),
                null);

        invokeUpdate(sparkle);
        assertTrue(Math.abs(particleVelocity(sparkle, "xd")) <= 0.1);
        assertTrue(Math.abs(particleVelocity(sparkle, "yd")) <= 0.1);
        assertTrue(Math.abs(particleVelocity(sparkle, "zd")) <= 0.1);

        sparkle.setPos(9.0, 0.0, 0.0);
        invokeUpdate(sparkle);
        Field proximityScale = VisSparkleParticle.class.getDeclaredField("proximityScale");
        proximityScale.setAccessible(true);
        assertEquals(0.95F, proximityScale.getFloat(sparkle), 1.0E-6F);

        Field age = Particle.class.getDeclaredField("age");
        age.setAccessible(true);
        age.setInt(sparkle, 10);
        invokeUpdate(sparkle);
        assertEquals(0.95F * 0.95F, proximityScale.getFloat(sparkle), 1.0E-6F);
    }

    @Test
    void wispFollowsOwnerPositionDeltaAndIgnoresRemovedOwner() throws Exception {
        ClientLevel level = mock(ClientLevel.class);
        when(level.getRandom()).thenReturn(RandomSource.create(12L));
        Entity owner = mock(Entity.class);
        when(level.getEntity(12)).thenReturn(owner);
        when(owner.getX()).thenReturn(20.0);
        when(owner.getZ()).thenReturn(30.0);
        when(owner.getDeltaMovement()).thenReturn(new Vec3(0.25, 0.0, 0.5));
        owner.xo = 18.0;
        owner.zo = 27.0;
        WispParticle wisp = construct(
                WispParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    WispParticleOptions.class,
                    ParticleSheet.class
                },
                level,
                5.0,
                4.0,
                6.0,
                new WispParticleOptions(12),
                null);
        wisp.setParticleSpeed(0.0, 0.0, 0.0);

        wisp.tick();
        assertEquals(new Vec3(7.0, 4.0, 9.0), wisp.getPos());

        when(owner.isRemoved()).thenReturn(true);
        wisp.setPos(7.0, 4.0, 9.0);
        wisp.tick();
        assertEquals(new Vec3(7.0, 4.0, 9.0), wisp.getPos());
    }

    @Test
    void seekerUsesResolvedTargetForInitialLifetimeAndSkipsRemovedTargets() throws Exception {
        ClientLevel level = mock(ClientLevel.class);
        Entity target = mock(Entity.class);
        when(level.getEntity(23)).thenReturn(target);
        when(target.getX()).thenReturn(2.0);
        when(target.getEyeY()).thenReturn(0.0);
        when(target.getZ()).thenReturn(0.0);
        BoreSparkleParticleOptions farFallback = new BoreSparkleParticleOptions(23, 100.0, 0.0, 0.0, 1.0F, 1.0F, 1.0F);

        Particle resolved = construct(
                BoreSparkleParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    BoreSparkleParticleOptions.class,
                    ParticleSheet.class
                },
                level,
                0.0,
                0.0,
                0.0,
                farFallback,
                null);
        assertTrue(resolved.getLifetime() >= 10 && resolved.getLifetime() < 30);

        when(target.isRemoved()).thenReturn(true);
        Particle removed = construct(
                BoreSparkleParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    BoreSparkleParticleOptions.class,
                    ParticleSheet.class
                },
                level,
                0.0,
                0.0,
                0.0,
                farFallback,
                null);
        assertTrue(removed.getLifetime() >= 500);
    }

    @Test
    void ventCullingUsesGraphicsRangeAndSkipsUninitializedCamera() {
        Camera camera = mock(Camera.class);
        when(camera.isInitialized()).thenReturn(false);
        assertFalse(VentParticle.beyondSpawnRange(1000.0, 0.0, 0.0, camera, true));
        verify(camera, never()).getPosition();

        when(camera.isInitialized()).thenReturn(true);
        when(camera.getPosition()).thenReturn(Vec3.ZERO);
        assertFalse(VentParticle.beyondSpawnRange(25.0, 0.0, 0.0, camera, true));
        assertTrue(VentParticle.beyondSpawnRange(25.01, 0.0, 0.0, camera, true));
        assertFalse(VentParticle.beyondSpawnRange(50.0, 0.0, 0.0, camera, false));
        assertTrue(VentParticle.beyondSpawnRange(50.01, 0.0, 0.0, camera, false));
    }

    @Test
    void ventClampsZeroScaleAndStillFinishesGrowing() throws Exception {
        VentParticle vent = construct(
                VentParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    VentParticleOptions.class,
                    ParticleSheet.class
                },
                mock(ClientLevel.class),
                0.0,
                0.0,
                0.0,
                new VentParticleOptions(0.0, 0.0, 0.0, 0xFFFFFFFF, 0.0F, false),
                null);

        Field fullSize = VentParticle.class.getDeclaredField("fullSize");
        fullSize.setAccessible(true);
        assertEquals(0.05F, fullSize.getFloat(vent), 1.0E-6F);
        for (int tick = 0; tick < 80 && vent.isAlive(); tick++) {
            invokeUpdate(vent);
        }
        assertFalse(vent.isAlive());
    }

    @Test
    void nitorCorePulseReturnsToBaselineWithSmoothMidpoint() {
        assertEquals(1.0F, NitorCoreParticle.pulseScale(0, 10, 1.12F), 1.0E-6F);
        assertEquals(1.12F, NitorCoreParticle.pulseScale(5, 10, 1.12F), 1.0E-6F);
        assertEquals(1.0F, NitorCoreParticle.pulseScale(10, 10, 1.12F), 1.0E-6F);
        assertEquals(1.0F, NitorCoreParticle.pulseScale(20, 10, 1.12F), 1.0E-6F);
    }

    @Test
    void smokeSpiralUsesOnlyItsFourActiveAtlasFrames() {
        assertEquals(0, SmokeSpiralParticle.frameAt(0.0F));
        assertEquals(1, SmokeSpiralParticle.frameAt(0.25F));
        assertEquals(2, SmokeSpiralParticle.frameAt(0.5F));
        assertEquals(3, SmokeSpiralParticle.frameAt(0.75F));
        assertEquals(3, SmokeSpiralParticle.frameAt(1.0F));
        assertEquals(0, SmokeSpiralParticle.frameAt(-1.0F));
    }

    @Test
    void taintSplosionUsesFiniteLaunchAndLifetimeAtRandomBounds() {
        assertEquals(new Vec3(0.0, 0.1, 0.0), TaintSplosionParticle.launchVelocity(0.0, 0.0, 0.0, 0.0, 0.0));

        Vec3 velocity = TaintSplosionParticle.launchVelocity(1.0, 0.0, 0.0, 0.5, 0.5);
        assertEquals(0.2892, velocity.x, 1.0E-12);
        assertEquals(0.1, velocity.y, 1.0E-12);
        assertEquals(0.0, velocity.z, 1.0E-12);
        assertEquals(660, TaintSplosionParticle.lifetime(0.0));
        assertEquals(66, TaintSplosionParticle.lifetime(1.0));
    }

    @Test
    void fluxGooDropletLifetimeIsSkewedTowardShorterParticles() {
        assertEquals(50, FluxGooDropletParticle.skewedLifetime(0.0F));
        assertEquals(84, FluxGooDropletParticle.skewedLifetime(0.5F));
        assertEquals(600, FluxGooDropletParticle.skewedLifetime(1.0F));
    }

    @Test
    void stabilizerRuneClampsNonpositiveLifetimesAndStartsWithItsFadedAlpha() throws Exception {
        ClientLevel level = mock(ClientLevel.class);
        for (int life : new int[] {0, -1}) {
            StabilizerRuneParticle particle = construct(
                    StabilizerRuneParticle.class,
                    new Class<?>[] {
                        ClientLevel.class,
                        double.class,
                        double.class,
                        double.class,
                        double.class,
                        double.class,
                        double.class,
                        StabilizerRuneParticleOptions.class,
                        ParticleSheet.class
                    },
                    level,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    new StabilizerRuneParticleOptions(life),
                    null);
            assertEquals(1, particle.getLifetime());

            Field alpha = Particle.class.getDeclaredField("alpha");
            alpha.setAccessible(true);
            assertEquals(0.3F, alpha.getFloat(particle), 1.0E-6F);
        }
    }

    @Test
    void boltInterpolatesWorldCoordinatesBeforeCameraOffsetAndFloatConversion() throws Exception {
        double originX = 16_777_216.0;
        double originY = 64.0;
        double originZ = -16_777_216.0;
        BoltParticle bolt = construct(
                BoltParticle.class,
                new Class<?>[] {
                    ClientLevel.class,
                    double.class,
                    double.class,
                    double.class,
                    BoltParticleOptions.class,
                    ParticleSheet.class
                },
                mock(ClientLevel.class),
                originX,
                originY,
                originZ,
                new BoltParticleOptions(originX + 10.0, originY + 1.0, originZ, 1.0F, 1.0F, 1.0F, 0.6F),
                null);
        Field shapeField = BoltParticle.class.getDeclaredField("shape");
        shapeField.setAccessible(true);
        BoltLineShape shape = (BoltLineShape) shapeField.get(bolt);

        Camera camera = mock(Camera.class);
        when(camera.getPosition()).thenReturn(new Vec3(originX, originY, originZ));
        when(camera.rotation()).thenReturn(new Quaternionf());
        List<Vec3> vertices = new ArrayList<>();
        VertexConsumer buffer = mock(VertexConsumer.class, invocation -> {
            if (invocation.getMethod().getName().equals("addVertex") && invocation.getArguments().length == 3) {
                Object[] arguments = invocation.getArguments();
                vertices.add(new Vec3(
                        ((Float) arguments[0]).floatValue(),
                        ((Float) arguments[1]).floatValue(),
                        ((Float) arguments[2]).floatValue()));
            }
            return invocation.getMock();
        });
        float partialTick = 0.5F;
        bolt.render(buffer, camera, partialTick);

        double amplitude = partialTick / 10.0F;
        double expectedX =
                Mth.lerp(0.25, shape.coordinate(0, 0, amplitude), shape.coordinate(1, 0, amplitude)) - originX;
        double expectedY =
                Mth.lerp(0.25, shape.coordinate(0, 1, amplitude), shape.coordinate(1, 1, amplitude)) - originY;
        double expectedZ =
                Mth.lerp(0.25, shape.coordinate(0, 2, amplitude), shape.coordinate(1, 2, amplitude)) - originZ;
        Vec3 firstVertex = vertices.get(0);
        assertEquals(expectedX, firstVertex.x - 0.1F, 1.0E-5);
        assertEquals(expectedY, firstVertex.y + 0.1F, 1.0E-5);
        assertEquals(expectedZ, firstVertex.z, 1.0E-5);
    }

    private static void invokeUpdate(Object particle) throws Exception {
        Method update = particle.getClass().getDeclaredMethod("update");
        update.setAccessible(true);
        update.invoke(particle);
    }

    private static double particleVelocity(Particle particle, String name) throws Exception {
        Field velocity = Particle.class.getDeclaredField(name);
        velocity.setAccessible(true);
        return velocity.getDouble(particle);
    }

    private static <T> T construct(Class<T> type, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        Constructor<T> constructor = type.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return constructor.newInstance(arguments);
    }
}
