package com.leclowndu93150.thaumaturge.content.spell.carrier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.spell.world.SpellTargeting;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class SpellProjectilePortTest {
    @Test
    void blockHitReflectsVelocityDamagesItOnceAndPlaysNeutralBounceSound() throws Exception {
        ServerLevel level = mock(ServerLevel.class);
        SpellProjectile projectile = new SpellProjectile(TTEntities.FOCUS_PROJECTILE.get(), level);
        projectile.setDeltaMovement(new Vec3(1.0D, 2.0D, 3.0D));
        setInt(projectile, "bounces", 2);
        Vec3 impact = new Vec3(3.0D, 4.0D, 5.0D);
        BlockHitResult hit = new BlockHitResult(impact, Direction.EAST, BlockPos.ZERO, false);

        projectile.onHit(hit);

        assertEquals(new Vec3(-0.9D, 1.53D, 2.7D), projectile.getDeltaMovement());
        assertEquals(new Vec3(3.05D, 4.0D, 5.0D), projectile.position());
        assertEquals(1, getInt(projectile, "bounces"));
        assertTrue(isHurtMarked(projectile));
        verify(level).playSound(null, 3.05D, 4.0D, 5.0D, SoundEvents.SLIME_JUMP_SMALL, SoundSource.NEUTRAL, 0.4F, 1.3F);
    }

    @Test
    void tooSlowBounceIsSpentInsteadOfRepeatedForever() throws Exception {
        ServerLevel level = mock(ServerLevel.class);
        SpellProjectile projectile = new SpellProjectile(TTEntities.FOCUS_PROJECTILE.get(), level);
        projectile.setDeltaMovement(new Vec3(0.01D, 0.0D, 0.0D));
        setInt(projectile, "bounces", 2);
        Method bounce = SpellProjectile.class.getDeclaredMethod("bounce", BlockHitResult.class);
        bounce.setAccessible(true);

        assertFalse((boolean)
                bounce.invoke(projectile, new BlockHitResult(Vec3.ZERO, Direction.EAST, BlockPos.ZERO, false)));
        assertEquals(0, getInt(projectile, "bounces"));
        assertEquals(Vec3.ZERO, projectile.position());
    }

    @Test
    void serverExpiryDiscardsBeforeTheBaseTick() {
        ServerLevel level = mock(ServerLevel.class);
        SpellProjectile projectile = new SpellProjectile(TTEntities.FOCUS_PROJECTILE.get(), level);
        projectile.tickCount = 1201;

        projectile.tick();

        assertEquals(1201, projectile.tickCount);
        assertTrue(projectile.isRemoved());
    }

    @Test
    void homingReacquiresWhenTheCurrentQuarryLosesLineOfSight() throws Exception {
        ServerLevel level = mock(ServerLevel.class);
        SpellProjectile projectile = new SpellProjectile(TTEntities.FOCUS_PROJECTILE.get(), level);
        LivingEntity owner = mock(LivingEntity.class);
        LivingEntity blocked = mock(LivingEntity.class);
        LivingEntity replacement = mock(LivingEntity.class);
        when(blocked.isAlive()).thenReturn(true);
        when(replacement.isAlive()).thenReturn(true);
        when(blocked.getBoundingBox()).thenReturn(new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D));
        when(replacement.getBoundingBox()).thenReturn(new AABB(9.0D, 0.0D, -1.0D, 11.0D, 2.0D, 1.0D));
        when(replacement.getX()).thenReturn(10.0D);
        when(replacement.getBbHeight()).thenReturn(2.0F);
        projectile.setPos(0.0D, 0.0D, 0.0D);
        projectile.setDeltaMovement(new Vec3(0.0D, 0.0D, 2.0D));
        projectile.tickCount = 5;
        setEntity(projectile, "quarry", blocked);
        Method steer = SpellProjectile.class.getDeclaredMethod("steer", ServerLevel.class, LivingEntity.class);
        steer.setAccessible(true);

        try (MockedStatic<SpellTargeting> targeting = org.mockito.Mockito.mockStatic(SpellTargeting.class)) {
            targeting
                    .when(() -> SpellTargeting.canSee(level, projectile.position(), blocked, projectile))
                    .thenReturn(false);
            targeting
                    .when(() -> SpellTargeting.nearest(
                            eq(level),
                            any(Vec3.class),
                            anyDouble(),
                            org.mockito.ArgumentMatchers.<Predicate<LivingEntity>>any()))
                    .thenReturn(Optional.of(replacement));

            steer.invoke(projectile, level, owner);

            assertEquals(replacement, getEntity(projectile, "quarry"));
            assertEquals(2.0D, projectile.getDeltaMovement().length(), 1.0E-9D);
            assertTrue(projectile.getDeltaMovement().x > 0.0D);
            assertTrue(projectile.getDeltaMovement().z > 0.0D);
            assertTrue(isHurtMarked(projectile));
            targeting.verify(() -> SpellTargeting.canSee(level, projectile.position(), blocked, projectile));
        }
    }

    private static boolean isHurtMarked(SpellProjectile projectile) throws Exception {
        Field field = net.minecraft.world.entity.Entity.class.getDeclaredField("hurtMarked");
        field.setAccessible(true);
        return field.getBoolean(projectile);
    }

    private static void setInt(SpellProjectile projectile, String fieldName, int value) throws Exception {
        Field field = SpellProjectile.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setInt(projectile, value);
    }

    private static int getInt(SpellProjectile projectile, String fieldName) throws Exception {
        Field field = SpellProjectile.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(projectile);
    }

    private static void setEntity(SpellProjectile projectile, String fieldName, LivingEntity entity) throws Exception {
        Field field = SpellProjectile.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(projectile, entity);
    }

    private static LivingEntity getEntity(SpellProjectile projectile, String fieldName) throws Exception {
        Field field = SpellProjectile.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (LivingEntity) field.get(projectile);
    }
}
