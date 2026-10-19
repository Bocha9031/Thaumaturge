package com.leclowndu93150.thaumaturge.content.infusion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class InfusionCraftJobTest {
    @Test
    void missingIngredientDebtUsesOriginalCostEvenAfterRequiredEssentiaWasDrained() {
        Holder<IAspect> requiredAspect = Holder.direct(mock(IAspect.class));
        AspectList originalCost = AspectList.of(new AspectInstance(requiredAspect, 4));
        InfusionCraftJob job = new InfusionCraftJob(
                List.of(),
                AspectList.EMPTY,
                new ItemStack(Items.DIAMOND),
                new ItemStack(Items.IRON_INGOT),
                0,
                Optional.empty(),
                originalCost);

        job.oweOneMore(RandomSource.create(0L));

        assertEquals(1, job.essentia().amountOf(requiredAspect));
        assertEquals(originalCost, job.cost());
    }

    @Test
    void legacyJobWithoutOriginalCostDoesNotInventEssentiaDebt() {
        InfusionCraftJob job = new InfusionCraftJob(
                List.of(),
                AspectList.EMPTY,
                new ItemStack(Items.DIAMOND),
                new ItemStack(Items.IRON_INGOT),
                0,
                Optional.empty());

        job.oweOneMore(RandomSource.create(0L));

        assertEquals(AspectList.EMPTY, job.essentia());
    }
}
