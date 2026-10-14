package com.leclowndu93150.thaumaturge.api.capability;

import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

final class LegacyKnowledgeView implements IPlayerKnowledge {
    private final PlayerResearch research;

    LegacyKnowledgeView(PlayerResearch research) {
        this.research = research;
    }

    @Override
    public void clear() {
        research.wipe();
    }

    @Override
    public ResearchStatus researchStatus(ResourceLocation entry) {
        return research.status(entry);
    }

    @Override
    public boolean isResearchComplete(ResourceLocation entry) {
        return research.hasCompleted(entry);
    }

    @Override
    public boolean isResearchKnown(ResourceLocation entry) {
        return research.knows(entry);
    }

    @Override
    public boolean isResearchKnown(ResourceLocation entry, int minimumStage) {
        return research.hasReachedStage(entry, minimumStage);
    }

    @Override
    public int researchStage(ResourceLocation entry) {
        return research.stageOf(entry);
    }

    @Override
    public boolean addResearch(ResourceLocation entry) {
        return research.learn(entry);
    }

    @Override
    public boolean setResearchStage(ResourceLocation entry, int stage) {
        return research.setStage(entry, stage);
    }

    @Override
    public boolean removeResearch(ResourceLocation entry) {
        return research.forget(entry);
    }

    @Override
    public Set<ResourceLocation> researchList() {
        return research.knownEntries();
    }

    @Override
    public boolean setResearchFlag(ResourceLocation entry, ResearchFlag flag) {
        return research.addFlag(entry, flag);
    }

    @Override
    public boolean clearResearchFlag(ResourceLocation entry, ResearchFlag flag) {
        return research.removeFlag(entry, flag);
    }

    @Override
    public boolean hasResearchFlag(ResourceLocation entry, ResearchFlag flag) {
        return research.hasFlag(entry, flag);
    }

    @Override
    public boolean addKnowledge(KnowledgeType type, ResourceKey<IResearchCategory> category, int amount) {
        return research.addKnowledgePoints(type, category, amount);
    }

    @Override
    public int knowledge(KnowledgeType type, ResourceKey<IResearchCategory> category) {
        return research.knowledgeLevel(type, category);
    }

    @Override
    public int rawKnowledge(KnowledgeType type, ResourceKey<IResearchCategory> category) {
        return research.knowledgePoints(type, category);
    }

    @Override
    public void sync(ServerPlayer player) {
        research.queueSync(player);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LegacyKnowledgeView view && view.research == research;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(research);
    }
}
