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
class PlayerResearchCompatibilityTest {
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
    void allNewQueriesReachTheMatchingLegacyMethodWithUnchangedArguments() {
        IPlayerKnowledge legacy = mock(IPlayerKnowledge.class, CALLS_REAL_METHODS);
        Set<ResourceLocation> entries = Set.of(ENTRY);
        when(legacy.researchStatus(ENTRY)).thenReturn(ResearchStatus.IN_PROGRESS);
        when(legacy.isResearchKnown(ENTRY)).thenReturn(true);
        when(legacy.isResearchComplete(ENTRY)).thenReturn(true);
        when(legacy.isResearchKnown(ENTRY, 4)).thenReturn(true);
        when(legacy.researchStage(ENTRY)).thenReturn(7);
        when(legacy.researchList()).thenReturn(entries);
        when(legacy.hasResearchFlag(ENTRY, ResearchFlag.POPUP)).thenReturn(true);
        when(legacy.knowledge(KnowledgeType.OBSERVATION, CATEGORY)).thenReturn(3);
        when(legacy.rawKnowledge(KnowledgeType.OBSERVATION, CATEGORY)).thenReturn(49);
        PlayerResearch research = legacy;
        assertEquals(ResearchStatus.IN_PROGRESS, research.status(ENTRY));
        assertTrue(research.knows(ENTRY));
        assertTrue(research.hasCompleted(ENTRY));
        assertTrue(research.hasReachedStage(ENTRY, 4));
        assertFalse(research.hasReachedStage(ENTRY, 5));
        assertEquals(7, research.stageOf(ENTRY));
        assertSame(entries, research.knownEntries());
        assertTrue(research.hasFlag(ENTRY, ResearchFlag.POPUP));
        assertEquals(3, research.knowledgeLevel(KnowledgeType.OBSERVATION, CATEGORY));
        assertEquals(49, research.knowledgePoints(KnowledgeType.OBSERVATION, CATEGORY));
        verify(legacy).isResearchKnown(ENTRY, 4);
        verify(legacy).isResearchKnown(ENTRY, 5);
    }

    @Test
    void allNewMutationsAndSyncReachTheMatchingLegacyMethod() {
        IPlayerKnowledge legacy = mock(IPlayerKnowledge.class, CALLS_REAL_METHODS);
        when(legacy.addResearch(ENTRY)).thenReturn(true);
        when(legacy.removeResearch(ENTRY)).thenReturn(true);
        when(legacy.setResearchFlag(ENTRY, ResearchFlag.PAGE)).thenReturn(true);
        when(legacy.addKnowledge(KnowledgeType.THEORY, CATEGORY, -17)).thenReturn(true);
        ServerPlayer owner = mock(ServerPlayer.class);
        PlayerResearch research = legacy;
        assertTrue(research.learn(ENTRY));
        assertFalse(research.setStage(ENTRY, 9));
        assertTrue(research.forget(ENTRY));
        assertTrue(research.addFlag(ENTRY, ResearchFlag.PAGE));
        assertFalse(research.removeFlag(ENTRY, ResearchFlag.RESEARCH));
        assertTrue(research.addKnowledgePoints(KnowledgeType.THEORY, CATEGORY, -17));
        research.queueSync(owner);
        research.wipe();
        verify(legacy).setResearchStage(ENTRY, 9);
        verify(legacy).clearResearchFlag(ENTRY, ResearchFlag.RESEARCH);
        verify(legacy).addKnowledge(KnowledgeType.THEORY, CATEGORY, -17);
        verify(legacy).sync(owner);
        verify(legacy).clear();
    }

    @Test
    void legacyViewForwardsEveryQueryMutationAndSyncToPureNewResearch() {
        PlayerResearch research = mock(PlayerResearch.class);
        Set<ResourceLocation> entries = Set.of(ENTRY);
        when(research.status(ENTRY)).thenReturn(ResearchStatus.COMPLETE);
        when(research.hasCompleted(ENTRY)).thenReturn(true);
        when(research.knows(ENTRY)).thenReturn(true);
        when(research.hasReachedStage(ENTRY, 4)).thenReturn(true);
        when(research.stageOf(ENTRY)).thenReturn(7);
        when(research.knownEntries()).thenReturn(entries);
        when(research.learn(ENTRY)).thenReturn(true);
        when(research.forget(ENTRY)).thenReturn(true);
        when(research.addFlag(ENTRY, ResearchFlag.PAGE)).thenReturn(true);
        when(research.hasFlag(ENTRY, ResearchFlag.POPUP)).thenReturn(true);
        when(research.knowledgeLevel(KnowledgeType.OBSERVATION, CATEGORY)).thenReturn(3);
        when(research.knowledgePoints(KnowledgeType.OBSERVATION, CATEGORY)).thenReturn(49);
        when(research.addKnowledgePoints(KnowledgeType.THEORY, CATEGORY, -17)).thenReturn(true);
        IPlayerKnowledge view = IPlayerKnowledge.viewOf(research);
        assertEquals(ResearchStatus.COMPLETE, view.researchStatus(ENTRY));
        assertTrue(view.isResearchComplete(ENTRY));
        assertTrue(view.isResearchKnown(ENTRY));
        assertTrue(view.isResearchKnown(ENTRY, 4));
        assertEquals(7, view.researchStage(ENTRY));
        assertSame(entries, view.researchList());
        assertTrue(view.addResearch(ENTRY));
        assertFalse(view.setResearchStage(ENTRY, 9));
        assertTrue(view.removeResearch(ENTRY));
        assertTrue(view.setResearchFlag(ENTRY, ResearchFlag.PAGE));
        assertFalse(view.clearResearchFlag(ENTRY, ResearchFlag.RESEARCH));
        assertTrue(view.hasResearchFlag(ENTRY, ResearchFlag.POPUP));
        assertEquals(3, view.knowledge(KnowledgeType.OBSERVATION, CATEGORY));
        assertEquals(49, view.rawKnowledge(KnowledgeType.OBSERVATION, CATEGORY));
        assertTrue(view.addKnowledge(KnowledgeType.THEORY, CATEGORY, -17));
        ServerPlayer owner = mock(ServerPlayer.class);
        view.sync(owner);
        view.clear();
        verify(research).setStage(ENTRY, 9);
        verify(research).removeFlag(ENTRY, ResearchFlag.RESEARCH);
        verify(research).addKnowledgePoints(KnowledgeType.THEORY, CATEGORY, -17);
        verify(research).queueSync(owner);
        verify(research).wipe();
    }

    @Test
    void legacyViewsReuseOldRecordsAndCompareNewRecordsByUnderlyingIdentity() {
        IPlayerKnowledge oldRecord = mock(IPlayerKnowledge.class);
        assertSame(oldRecord, IPlayerKnowledge.viewOf(oldRecord));
        PlayerResearch research = mock(PlayerResearch.class);
        IPlayerKnowledge first = IPlayerKnowledge.viewOf(research);
        IPlayerKnowledge second = IPlayerKnowledge.viewOf(research);
        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(System.identityHashCode(research), first.hashCode());
        assertEquals(first.hashCode(), second.hashCode());
        assertSame(first, IPlayerKnowledge.viewOf(first));
        assertNotEquals(first, IPlayerKnowledge.viewOf(mock(PlayerResearch.class)));
        assertNotEquals(first, research);
    }

    @Test
    void newBindingFailsBeforeInstallationRejectsNullAndCannotBeReplacedThroughEitherFacade() {
        Player player = mock(Player.class);
        assertThrows(IllegalStateException.class, () -> PlayerResearchAccess.of(player));
        assertThrows(IllegalStateException.class, () -> KnowledgeAccess.of(player));
        assertThrows(NullPointerException.class, () -> PlayerResearchAccess.bind(null));
        assertThrows(IllegalStateException.class, () -> PlayerResearchAccess.of(player));
        PlayerResearch research = mock(PlayerResearch.class);
        PlayerResearchAccess.bind(actualPlayer -> {
            assertSame(player, actualPlayer);
            return research;
        });
        assertSame(research, PlayerResearchAccess.of(player));
        assertSame(research, PlayerResearchAccess.of(player));
        assertEquals(IPlayerKnowledge.viewOf(research), KnowledgeAccess.of(player));
        assertThrows(IllegalStateException.class, () -> PlayerResearchAccess.bind(ignored -> research));
        assertThrows(IllegalStateException.class, () -> KnowledgeAccess.bind(ignored -> mock(IPlayerKnowledge.class)));
    }

    @Test
    void legacyBindingInstallsTheOnlyNewFacadeBindingAndReturnsItsOriginalRecord() {
        Player player = mock(Player.class);
        IPlayerKnowledge record = mock(IPlayerKnowledge.class);
        KnowledgeAccess.bind(actualPlayer -> {
            assertSame(player, actualPlayer);
            return record;
        });
        assertSame(record, PlayerResearchAccess.of(player));
        assertSame(record, KnowledgeAccess.of(player));
        assertThrows(IllegalStateException.class, () -> KnowledgeAccess.bind(ignored -> record));
        assertThrows(IllegalStateException.class, () -> PlayerResearchAccess.bind(ignored -> record));
    }

    @Test
    void currentNativeBackendSupportsNewReadsWritesLiveViewsAndQueuedSyncWithoutMigration() {
        PlayerKnowledge record = new PlayerKnowledge();
        PlayerResearchAccess.bind(player -> record);
        Player player = mock(Player.class);
        PlayerResearch research = PlayerResearchAccess.of(player);
        assertSame(record, research);
        assertSame(record, KnowledgeAccess.of(player));
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
