package com.leclowndu93150.thaumaturge.api.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;

final class ResearchUnlockConditionsPortTest {
    @Test
    void modernAndLegacyRequirementsShareOrderedPassAndMessageStorage() {
        ResourceLocation entry = ResourceLocation.parse("thaumaturge:modern_legacy_port_test");
        ResourceLocation prerequisite = ResourceLocation.parse("thaumaturge:legacy_prerequisite_port_test");
        Player player = mock(Player.class);
        PlayerResearch research = mock(PlayerResearch.class);
        Component modernMessage = Component.literal("modern");
        Component legacyMessage = Component.literal("legacy");
        when(research.hasCompleted(prerequisite)).thenReturn(false);

        ResearchUnlockConditions.require(entry, (checkedPlayer, checkedResearch) -> false, modernMessage);
        ResearchUnlockConditions.register(
                entry, ResearchUnlockConditions.Condition.requiresComplete(prerequisite), legacyMessage);

        assertFalse(ResearchUnlockConditions.passes(player, research, entry));
        assertEquals(
                List.of(modernMessage, legacyMessage),
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
        assertThrows(NullPointerException.class, () -> ResearchUnlockConditions.register(entry, null, null));
    }

    @Test
    void legacyOverloadsStillAcceptTheKnowledgeView() {
        ResourceLocation entry = ResourceLocation.parse("thaumaturge:legacy_view_port_test");
        ResourceLocation prerequisite = ResourceLocation.parse("thaumaturge:legacy_view_prerequisite_port_test");
        Player player = mock(Player.class);
        IPlayerKnowledge knowledge = mock(IPlayerKnowledge.class);
        when(knowledge.isResearchComplete(prerequisite)).thenReturn(true);
        ResearchUnlockConditions.register(
                entry, ResearchUnlockConditions.Condition.requiresComplete(prerequisite), null);

        assertTrue(ResearchUnlockConditions.passes(player, knowledge, entry));
        verify(knowledge).isResearchComplete(prerequisite);
        verify(knowledge, never()).hasCompleted(prerequisite);
    }
}
