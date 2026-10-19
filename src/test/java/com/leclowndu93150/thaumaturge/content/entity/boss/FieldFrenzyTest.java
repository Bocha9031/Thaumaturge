package com.leclowndu93150.thaumaturge.content.entity.boss;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

final class FieldFrenzyTest {
    @Test
    void spentFrenzyDoesNotBecomeAvailableAgainAfterSaveAndLoad() {
        FieldFrenzy beforeSave = new FieldFrenzy(null, null);
        beforeSave.ignite();

        CompoundTag saved = new CompoundTag();
        beforeSave.addAdditionalSaveData(saved);

        FieldFrenzy afterLoad = new FieldFrenzy(null, null);
        afterLoad.readAdditionalSaveData(saved);
        afterLoad.ignite();

        assertTrue(saved.getBoolean("frenzy_spent"));
        assertFalse(afterLoad.active());
    }
}
