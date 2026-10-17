package com.leclowndu93150.thaumaturge.api.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

final class ResearchUnlockConditionsPortTest {
    @Test
    void requirementsShareOrderedPassAndMessageStorage() {
        ResourceLocation entry = ResourceLocation.parse("thaumaturge:requirements_port_test");
        ResourceLocation prerequisite = ResourceLocation.parse("thaumaturge:prerequisite_port_test");
        Player player = mock(Player.class);
        PlayerResearch research = mock(PlayerResearch.class);
        Component customMessage = Component.literal("custom");
        Component prerequisiteMessage = Component.literal("prerequisite");
        when(research.hasCompleted(prerequisite)).thenReturn(false);

        ResearchUnlockConditions.require(entry, (checkedPlayer, checkedResearch) -> false, customMessage);
        ResearchUnlockConditions.require(entry, UnlockRequirement.completed(prerequisite), prerequisiteMessage);

        assertFalse(ResearchUnlockConditions.passes(player, research, entry));
        assertEquals(
                List.of(customMessage, prerequisiteMessage),
                ResearchUnlockConditions.lockedMessages(player, research, entry));
    }

    @Test
    void requirementsShortCircuitInRegistrationOrder() {
        ResourceLocation entry = ResourceLocation.parse("thaumaturge:short_circuit_port_test");
        Player player = mock(Player.class);
        PlayerResearch research = mock(PlayerResearch.class);
        UnlockRequirement later = (checkedPlayer, checkedResearch) -> {
            throw new AssertionError("Later requirement must not run after a failure");
        };
        ResearchUnlockConditions.require(entry, (checkedPlayer, checkedResearch) -> false, null);
        ResearchUnlockConditions.require(entry, later, null);

        assertFalse(ResearchUnlockConditions.passes(player, research, entry));
    }

    @Test
    void completedAndCompositionUsePlayerResearchAndShortCircuit() {
        ResourceLocation prerequisite = ResourceLocation.parse("thaumaturge:completed_port_test");
        Player player = mock(Player.class);
        PlayerResearch research = mock(PlayerResearch.class);
        UnlockRequirement later = (checkedPlayer, checkedResearch) -> {
            throw new AssertionError("Second requirement must not run after a failure");
        };
        when(research.hasCompleted(prerequisite)).thenReturn(false);

        assertFalse(UnlockRequirement.completed(prerequisite).and(later).isMet(player, research));
        verify(research).hasCompleted(prerequisite);
    }

    @Test
    void registrationRejectsNullEntryAndRequirement() {
        ResourceLocation entry = ResourceLocation.parse("thaumaturge:null_validation_port_test");
        UnlockRequirement requirement = (player, research) -> true;

        assertThrows(NullPointerException.class, () -> ResearchUnlockConditions.require(null, requirement, null));
        assertThrows(NullPointerException.class, () -> ResearchUnlockConditions.require(entry, null, null));
        assertThrows(NullPointerException.class, () -> ResearchUnlockConditions.require(entry, null, null));
    }
}
