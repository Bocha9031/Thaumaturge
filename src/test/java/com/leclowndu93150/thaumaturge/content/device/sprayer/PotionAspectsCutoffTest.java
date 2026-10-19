package com.leclowndu93150.thaumaturge.content.device.sprayer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.mixin.world.item.alchemy.PotionBrewingAccessor;
import com.leclowndu93150.thaumaturge.mixin.world.item.alchemy.PotionBrewingMixAccessor;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PotionAspectsCutoffTest {
    private ServerLevel level;
    private Holder<IAspect> aqua;
    private Holder<IAspect> magic;
    private Holder<IAspect> alkimia;
    private Holder<IAspect> compound;
    private final List<Holder<IAspect>> primals = new ArrayList<>();

    @BeforeEach
    void setUp() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        Registry.register(registry, TTAspects.AQUA, mock(IAspect.class));
        Registry.register(registry, TTAspects.PRAECANTATIO, mock(IAspect.class));
        Registry.register(registry, TTAspects.ALKIMIA, mock(IAspect.class));
        aqua = registry.getHolderOrThrow(TTAspects.AQUA);
        magic = registry.getHolderOrThrow(TTAspects.PRAECANTATIO);
        alkimia = registry.getHolderOrThrow(TTAspects.ALKIMIA);
        compound = magic;
        for (int i = 0; i < 11; i++) {
            IAspect value = mock(IAspect.class);
            when(value.isPrimal()).thenReturn(true);
            var key = ResourceKey.create(
                    IAspect.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("thaumaturge", "test_primal_" + i));
            Registry.register(registry, key, value);
            primals.add(registry.getHolderOrThrow(key));
        }
        level = mock(ServerLevel.class);
        when(level.registryAccess()).thenReturn(new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze());
    }

    private ItemStack potion(Holder<Potion> potion) {
        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
        return stack;
    }

    private PotionBrewingMixAccessor mix(Holder<Potion> from, Holder<Potion> to, Item... ingredients) {
        var mix = mock(PotionBrewingMixAccessor.class);
        doReturn(from).when(mix).thaumaturge$getFrom();
        doReturn(to).when(mix).thaumaturge$getTo();
        when(mix.thaumaturge$getIngredient()).thenReturn(Ingredient.of(ingredients));
        return mix;
    }

    private void brewing(List<?> mixes) {
        PotionBrewing brewing = mock(PotionBrewing.class, withSettings().extraInterfaces(PotionBrewingAccessor.class));
        doReturn(mixes).when((PotionBrewingAccessor) brewing).thaumaturge$getPotionMixes();
        when(level.potionBrewing()).thenReturn(brewing);
    }

    @Test
    void waterAndUnknownContentsUseFiveEachFallbackWhenNoBrewingRouteExists() {
        brewing(List.of());
        AspectList water = PotionAspects.of(level, potion(Potions.WATER));
        assertEquals(2, water.size());
        assertEquals(5, water.amountOf(magic));
        assertEquals(5, water.amountOf(alkimia));
        assertEquals(0, water.amountOf(aqua));
        verify(level).potionBrewing();
        AspectList fallback = PotionAspects.of(level, new ItemStack(Items.POTION));
        assertEquals(2, fallback.size());
        assertEquals(5, fallback.amountOf(magic));
        assertEquals(5, fallback.amountOf(alkimia));
    }

    @Test
    void brewingRouteTotalsUniqueReagentsThenAppliesIntegerReduction() {
        brewing(List.of(
                mix(Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Items.REDSTONE),
                mix(Potions.AWKWARD, Potions.SWIFTNESS, Items.SUGAR),
                mix(Potions.WATER, Potions.AWKWARD, Items.NETHER_WART)));
        try (var index = mockStatic(AspectIndexAccess.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY.add(aqua, 3));
            AspectList recipe = PotionAspects.of(level, potion(Potions.LONG_SWIFTNESS));
            assertEquals(4, recipe.amountOf(aqua));
            assertEquals(4, recipe.amountOf(alkimia));
            index.verify(() -> AspectIndexAccess.of(any(ItemStack.class)), times(3));
        }
    }

    @Test
    void routeCollectsEveryBrewingBranchAndDeduplicatesRepeatedIngredients() {
        brewing(List.of(
                mix(Potions.AWKWARD, Potions.SWIFTNESS, Items.REDSTONE, Items.SUGAR),
                mix(Potions.WATER, Potions.SWIFTNESS, Items.GLOWSTONE_DUST),
                mix(Potions.WATER, Potions.AWKWARD, Items.SUGAR, Items.REDSTONE)));
        try (var index = mockStatic(AspectIndexAccess.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(AspectList.EMPTY.add(aqua, 3));
            AspectList recipe = PotionAspects.of(level, potion(Potions.SWIFTNESS));
            assertEquals(4, recipe.amountOf(aqua));
            assertEquals(4, recipe.amountOf(alkimia));
            index.verify(() -> AspectIndexAccess.of(any(ItemStack.class)), times(3));
        }
    }

    @Test
    void recipeCapDropsEarliestLowestAmountsAndBrewingCyclesTerminate() {
        brewing(List.of(mix(Potions.SWIFTNESS, Potions.SWIFTNESS, Items.SUGAR)));
        AspectList all = AspectList.EMPTY.add(compound, 3);
        for (Holder<IAspect> primal : primals) all = all.add(primal, 3);
        final AspectList indexed = all;
        try (var index = mockStatic(AspectIndexAccess.class)) {
            index.when(() -> AspectIndexAccess.of(any(ItemStack.class))).thenReturn(indexed);
            AspectList recipe = PotionAspects.of(level, potion(Potions.SWIFTNESS));
            assertEquals(10, recipe.size());
            assertEquals(0, recipe.amountOf(compound));
            assertEquals(2, recipe.amountOf(alkimia));
            assertEquals(0, recipe.amountOf(primals.get(0)));
            assertEquals(0, recipe.amountOf(primals.get(1)));
            assertEquals(2, recipe.amountOf(primals.get(2)));
            index.verify(() -> AspectIndexAccess.of(any(ItemStack.class)), times(1));
        }
    }
}
