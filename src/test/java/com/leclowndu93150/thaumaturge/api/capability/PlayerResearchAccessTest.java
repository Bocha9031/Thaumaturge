package com.leclowndu93150.thaumaturge.api.capability;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import java.lang.reflect.Field;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock("PlayerResearchAccess binding")
class PlayerResearchAccessTest {
    private static final ResourceLocation ENTRY = TTIds.rl("test_research");
    private static final ResourceKey<IResearchCategory> CATEGORY =
            ResourceKey.create(IResearchCategory.REGISTRY_KEY, TTIds.rl("test_category"));

    private ApiBinding<?> binding;
    private Field valueField;
    private Object savedBinding;

    @BeforeEach
    void isolateBinding() throws ReflectiveOperationException {
        Field bindingField = PlayerResearchAccess.class.getDeclaredField("BINDING");
        bindingField.setAccessible(true);
        binding = (ApiBinding<?>) bindingField.get(null);
        valueField = ApiBinding.class.getDeclaredField("value");
        valueField.setAccessible(true);
        savedBinding = valueField.get(binding);
        valueField.set(binding, null);
    }

    @AfterEach
    void restoreBinding() throws ReflectiveOperationException {
        valueField.set(binding, savedBinding);
    }

    @Test
    void bindingRequiresImplementationAndCanOnlyBeInstalledOnce() {
        Player player = mock(Player.class);
        assertThrows(IllegalStateException.class, () -> PlayerResearchAccess.of(player));
        assertThrows(NullPointerException.class, () -> PlayerResearchAccess.bind(null));
        assertThrows(IllegalStateException.class, () -> PlayerResearchAccess.of(player));

        PlayerResearch research = mock(PlayerResearch.class);
        PlayerResearchAccess.bind(actualPlayer -> {
            assertSame(player, actualPlayer);
            return research;
        });
        assertSame(research, PlayerResearchAccess.of(player));
        assertSame(research, PlayerResearchAccess.of(player));
        assertThrows(IllegalStateException.class, () -> PlayerResearchAccess.bind(ignored -> research));
    }

    @Test
    void nativeBackendSupportsReadsWritesAndQueuedSync() {
        PlayerKnowledge record = new PlayerKnowledge();
        PlayerResearchAccess.bind(player -> record);
        PlayerResearch research = PlayerResearchAccess.of(mock(Player.class));
        assertSame(record, research);

        Set<ResourceLocation> known = research.knownEntries();
        assertTrue(research.learn(ENTRY));
        assertTrue(known.contains(ENTRY));
        assertThrows(UnsupportedOperationException.class, () -> known.add(TTIds.rl("other")));
        assertEquals(ResearchStatus.IN_PROGRESS, research.status(ENTRY));
        assertTrue(research.setStage(ENTRY, 4));
        assertTrue(research.hasReachedStage(ENTRY, 4));
        assertFalse(research.hasReachedStage(ENTRY, 5));
        assertFalse(research.hasCompleted(ENTRY));
        assertTrue(record.markComplete(ENTRY));
        assertTrue(research.hasCompleted(ENTRY));
        assertEquals(ResearchStatus.COMPLETE, research.status(ENTRY));
        assertTrue(research.addFlag(ENTRY, ResearchFlag.POPUP));
        assertTrue(research.hasFlag(ENTRY, ResearchFlag.POPUP));
        assertTrue(research.removeFlag(ENTRY, ResearchFlag.POPUP));
        assertTrue(research.addKnowledgePoints(KnowledgeType.THEORY, CATEGORY, 65));
        assertEquals(2, research.knowledgeLevel(KnowledgeType.THEORY, CATEGORY));
        assertEquals(65, research.knowledgePoints(KnowledgeType.THEORY, CATEGORY));
        assertTrue(research.addKnowledgePoints(KnowledgeType.THEORY, CATEGORY, -100));
        assertEquals(0, research.knowledgePoints(KnowledgeType.THEORY, CATEGORY));
        research.queueSync(mock(ServerPlayer.class));
        assertTrue(record.takeSyncPending());
        assertFalse(record.takeSyncPending());
        assertTrue(research.forget(ENTRY));
        assertFalse(known.contains(ENTRY));
        assertTrue(research.learn(ENTRY));
        research.wipe();
        assertTrue(known.isEmpty());
        assertEquals(ResearchStatus.UNKNOWN, research.status(ENTRY));
    }
}
