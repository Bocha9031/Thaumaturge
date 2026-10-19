package com.leclowndu93150.thaumaturge.content.infusion;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class InfusionCraftJobPlanningTest {
    @Test
    void recipeWithoutAJobComponentPlanDoesNotStartJobConstruction() {
        InfusionJobRecipe recipe = mock(InfusionJobRecipe.class);
        InfusionInput input = new InfusionInput(ItemStack.EMPTY, List.of());
        MatrixEnvironment environment = new MatrixEnvironment(List.of(), 10, 1.0F, 0.0F);

        when(recipe.jobComponents(input)).thenReturn(null);

        assertNull(BlockEntityInfusionMatrix.createCraftJob(
                recipe, input, environment, mock(Player.class), mock(ServerLevel.class)));
        verify(recipe, never()).jobResult(input, null, null);
    }
}
