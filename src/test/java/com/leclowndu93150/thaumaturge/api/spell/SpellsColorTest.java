package com.leclowndu93150.thaumaturge.api.spell;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import org.junit.jupiter.api.Test;

class SpellsColorTest {
    @Test
    void effectPartsUseTheirAspectColorEvenWhenAspectIsFixed() {
        assertTrue(Spells.usesAspectColor(SpellPartKind.EFFECT, false));
    }

    @Test
    void fixedAspectNonEffectsKeepTheirPartColor() {
        assertFalse(Spells.usesAspectColor(SpellPartKind.DELIVERY, false));
    }

    @Test
    void selectableNonEffectsUseTheirAspectColor() {
        assertTrue(Spells.usesAspectColor(SpellPartKind.MODIFIER, true));
    }
}
