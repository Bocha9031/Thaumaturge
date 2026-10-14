package com.leclowndu93150.thaumaturge.api.research;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

final class ResearchParentProgressionTest {
    private static final ResourceLocation PARENT = ResourceLocation.parse("thaumaturge:parent");

    @Test
    void unqualifiedParentRequiresCompletion() {
        PlayerResearch research = mock(PlayerResearch.class);
        ResearchParent parent = ResearchParent.of(PARENT);

        when(research.hasCompleted(PARENT)).thenReturn(false);
        when(research.hasReachedStage(PARENT, 0)).thenReturn(true);
        assertFalse(parent.isSatisfiedBy(research));

        when(research.hasCompleted(PARENT)).thenReturn(true);
        assertTrue(parent.isSatisfiedBy(research));
    }

    @Test
    void stageQualifiedParentUsesOneBasedStageAndCompletionAlsoSatisfiesIt() {
        PlayerResearch research = mock(PlayerResearch.class);
        ResearchParent parent = new ResearchParent(PARENT, 2, false);

        when(research.hasCompleted(PARENT)).thenReturn(false);
        when(research.hasReachedStage(PARENT, 1)).thenReturn(false);
        assertFalse(parent.isSatisfiedBy(research));

        when(research.hasReachedStage(PARENT, 1)).thenReturn(true);
        assertTrue(parent.isSatisfiedBy(research));

        when(research.hasReachedStage(PARENT, 1)).thenReturn(false);
        when(research.hasCompleted(PARENT)).thenReturn(true);
        assertTrue(parent.isSatisfiedBy(research));
    }
}
