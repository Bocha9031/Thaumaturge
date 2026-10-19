package com.leclowndu93150.thaumaturge.compat.jei.category;

import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.ResearchConstruct;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public record ResearchConstructRecipe(
        ResourceLocation entryId, int stageIndex, IResearchEntry entry, ResearchConstruct construct) {
    public Map<String, Integer> materialCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String cell : construct.cells()) {
            if (!cell.isEmpty()) {
                counts.merge(cell, 1, Integer::sum);
            }
        }
        return counts;
    }
}
