package com.leclowndu93150.thaumaturge.compat.jei.category;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.research.ResearchConstruct;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ResearchConstructRecipeTest {
    @Test
    void materialCountsMatchRepeatedStructureCells() {
        ResearchConstruct construct = new ResearchConstruct(
                3,
                1,
                2,
                List.of("#c:glass_blocks", "#minecraft:wooden_slabs", "", "#c:glass_blocks", "#c:glass_blocks", ""),
                AspectList.EMPTY);
        ResearchConstructRecipe recipe =
                new ResearchConstructRecipe(ResourceLocation.withDefaultNamespace("test"), 0, null, construct);

        assertEquals(Map.of("#c:glass_blocks", 3, "#minecraft:wooden_slabs", 1), recipe.materialCounts());
    }
}
