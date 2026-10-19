package com.leclowndu93150.thaumaturge.content.entity.construct;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

class TurretAmmoCompatibilityTest {
    private final EntityTurretCrossbow turret =
            new EntityTurretCrossbow(TTEntities.TURRET_CROSSBOW.get(), mock(Level.class));

    @Test
    void acceptsAllVanillaArrowVariants() {
        assertTrue(turret.isValidAmmo(new ItemStack(Items.ARROW)));
        assertTrue(turret.isValidAmmo(new ItemStack(Items.TIPPED_ARROW)));
        assertTrue(turret.isValidAmmo(new ItemStack(Items.SPECTRAL_ARROW)));
    }

    @Test
    void rejectsNonArrowAndEmptyStacks() {
        assertFalse(turret.isValidAmmo(new ItemStack(Items.STICK)));
        assertFalse(turret.isValidAmmo(ItemStack.EMPTY));
    }
}
