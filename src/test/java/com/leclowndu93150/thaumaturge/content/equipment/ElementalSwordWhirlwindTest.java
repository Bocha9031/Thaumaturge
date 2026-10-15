package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ElementalSwordWhirlwindTest {
    @Test
    void descentIsSlowedAndUsesTheEntityGravity() {
        Level level = level(false);
        LivingEntity user = user(new Vec3(1.0, -0.6, 3.0));
        user.fallDistance = 12.0F;
        when(user.getGravity()).thenReturn(0.1);

        TTItems.ELEMENTAL_SWORD.get().onUseTick(level, user, ItemStack.EMPTY, 71999);

        verify(user).setDeltaMovement(eq(1.0), doubleThat(value -> Math.abs(value + 0.4) < 1.0E-9), eq(3.0));
        assertEquals(10.0F, user.fallDistance, 1.0E-5F);
    }

    @Test
    void upwardOvershootUsesSourceCap() {
        Level level = level(false);
        LivingEntity user = user(new Vec3(0.0, 0.6, 0.0));
        when(user.getGravity()).thenReturn(0.1);

        TTItems.ELEMENTAL_SWORD.get().onUseTick(level, user, ItemStack.EMPTY, 71999);

        verify(user).setDeltaMovement(0.0, 0.2, 0.0);
    }

    @Test
    void zeroGravityDoesNotAddDefaultLift() {
        Level level = level(false);
        LivingEntity user = user(Vec3.ZERO);
        when(user.getGravity()).thenReturn(0.0);

        TTItems.ELEMENTAL_SWORD.get().onUseTick(level, user, ItemStack.EMPTY, 71999);

        verify(user).setDeltaMovement(0.0, 0.0, 0.0);
    }

    @Test
    void periodicWearRunsOnServerAndNotClient() {
        ItemStack stack = mock(ItemStack.class);
        LivingEntity user = user(Vec3.ZERO);
        when(user.getUsedItemHand()).thenReturn(InteractionHand.MAIN_HAND);

        try (MockedStatic<ClientEffects> clientEffects = mockStatic(ClientEffects.class)) {
            TTItems.ELEMENTAL_SWORD.get().onUseTick(level(true), user, stack, 72000);
            verify(stack, never()).hurtAndBreak(anyInt(), any(LivingEntity.class), any(EquipmentSlot.class));
        }

        TTItems.ELEMENTAL_SWORD.get().onUseTick(level(false), user, stack, 72000);
        verify(stack).hurtAndBreak(1, user, EquipmentSlot.MAINHAND);
    }

    private static LivingEntity user(Vec3 movement) {
        LivingEntity user = mock(LivingEntity.class);
        when(user.getDeltaMovement()).thenReturn(movement);
        when(user.getBoundingBox()).thenReturn(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
        return user;
    }

    private static Level level(boolean clientSide) {
        Level level = mock(Level.class);
        RandomSource random = mock(RandomSource.class);
        when(level.isClientSide()).thenReturn(clientSide);
        when(level.getRandom()).thenReturn(random);
        when(level.getEntities(any(Entity.class), any(AABB.class))).thenReturn(List.of());
        return level;
    }
}
