package com.leclowndu93150.thaumaturge.content.infusion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.recipe.InfusionCraftingTransaction.Failure;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.junit.jupiter.api.Test;

class InfusionCraftingInspectionTest {
    @Test
    void matchedRecipeWithoutJobPlanIsReportedAsInvalid() {
        InfusionJobRecipe recipe = mock(InfusionJobRecipe.class);
        InfusionInput input = new InfusionInput(ItemStack.EMPTY, List.of());
        when(recipe.jobComponents(input)).thenReturn(null);
        InfusionRecipeMatcher.Match match = new InfusionRecipeMatcher.Match(
                new RecipeHolder<>(ResourceLocation.fromNamespaceAndPath("thaumaturge", "broken"), recipe), false);

        var inspection = InfusionCraftingTransactions.inspectMatch(null, null, null, input, match);

        assertEquals(Failure.INVALID_RECIPE, inspection.failure());
        assertFalse(inspection.successful());
    }
}
