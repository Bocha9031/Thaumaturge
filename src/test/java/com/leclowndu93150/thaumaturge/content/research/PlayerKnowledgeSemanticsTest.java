package com.leclowndu93150.thaumaturge.content.research;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.ResearchFlag;
import com.leclowndu93150.thaumaturge.api.capability.ResearchStatus;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

final class PlayerKnowledgeSemanticsTest {
    private static final ResourceLocation ENTRY = TTIds.rl("backend_semantics");

    @Test
    void stageAndCompletionAreIndependentAndForgettingClearsAllEntryState() {
        PlayerKnowledge backend = new PlayerKnowledge();
        PlayerResearch research = backend;

        assertEquals(ResearchStatus.UNKNOWN, research.status(ENTRY));
        assertEquals(-1, research.stageOf(ENTRY));
        assertFalse(research.hasReachedStage(ENTRY, 0));

        assertTrue(research.learn(ENTRY));
        assertEquals(ResearchStatus.IN_PROGRESS, research.status(ENTRY));
        assertEquals(0, research.stageOf(ENTRY));
        assertTrue(research.hasReachedStage(ENTRY, 0));
        assertFalse(research.hasReachedStage(ENTRY, 1));

        assertTrue(research.setStage(ENTRY, 2));
        assertEquals(ResearchStatus.IN_PROGRESS, research.status(ENTRY));
        assertTrue(research.hasReachedStage(ENTRY, 2));
        assertFalse(research.hasReachedStage(ENTRY, 3));

        assertTrue(backend.markComplete(ENTRY));
        assertEquals(ResearchStatus.COMPLETE, research.status(ENTRY));
        assertTrue(research.hasReachedStage(ENTRY, 2));
        assertFalse(backend.markComplete(TTIds.rl("unknown")));

        assertTrue(research.addFlag(ENTRY, ResearchFlag.PAGE));
        assertTrue(research.forget(ENTRY));
        assertEquals(ResearchStatus.UNKNOWN, research.status(ENTRY));
        assertEquals(-1, research.stageOf(ENTRY));
        assertFalse(research.hasFlag(ENTRY, ResearchFlag.PAGE));
        assertFalse(backend.hasCompleted(ENTRY));
    }
}
