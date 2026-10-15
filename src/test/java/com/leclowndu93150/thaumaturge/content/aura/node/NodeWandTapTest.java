package com.leclowndu93150.thaumaturge.content.aura.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class NodeWandTapTest {
    @Test
    void tapWaitsForTheFiveTickCadence() {
        assertFalse(NodeWandTap.tap(
                new BlockEntityNode(BlockPos.ZERO, TTBlocks.NODE.get().defaultBlockState()),
                mock(ServerLevel.class),
                mock(Player.class),
                ItemStack.EMPTY,
                4));
    }

    @Test
    void onlyCanonicalPrimalsAreTappableAndTheirOrderIsStable() {
        ResourceKey<IAspect> addonKey =
                ResourceKey.create(IAspect.REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("test", "addon_primal"));
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        for (ResourceKey<IAspect> key : TTAspects.PRIMALS) {
            Registry.register(registry, key, mock(IAspect.class));
        }
        IAspect addon = mock(IAspect.class);
        when(addon.isPrimal()).thenReturn(true);
        Registry.register(registry, addonKey, addon);
        RegistryAccess access = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
        Holder<IAspect> aer = registry.getHolderOrThrow(TTAspects.AER);
        Holder<IAspect> terra = registry.getHolderOrThrow(TTAspects.TERRA);
        Holder<IAspect> addonPrimal = registry.getHolderOrThrow(addonKey);

        BlockEntityNode node =
                new BlockEntityNode(BlockPos.ZERO, TTBlocks.NODE.get().defaultBlockState());
        node.aspects = AspectList.ofEntries(
                List.of(new AspectInstance(addonPrimal, 1), new AspectInstance(terra, 1), new AspectInstance(aer, 1)));
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        when(level.registryAccess()).thenReturn(access);
        when(level.getRandom()).thenReturn(random);
        when(random.nextInt(2)).thenReturn(0);
        node.setLevel(level);
        Player player = mock(Player.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        PlayerResearch research = mock(PlayerResearch.class);
        when(research.hasCompleted(any(ResourceLocation.class))).thenReturn(false);
        ItemStack wand = new ItemStack(Items.STICK);

        try (MockedStatic<PlayerResearchAccess> researchAccess =
                        org.mockito.Mockito.mockStatic(PlayerResearchAccess.class);
                MockedStatic<WandVisHelper> wandVis = org.mockito.Mockito.mockStatic(WandVisHelper.class)) {
            researchAccess.when(() -> PlayerResearchAccess.of(player)).thenReturn(research);
            wandVis.when(() -> WandVisHelper.getParts(wand)).thenReturn(WandParts.starter());
            wandVis.when(() -> WandVisHelper.getMaxVis(wand)).thenReturn(10_000);

            assertTrue(NodeWandTap.tap(node, level, player, wand, 5));

            wandVis.verify(() -> WandVisHelper.addVis(wand, TTAspects.AER, 1, true));
            assertEquals(0, node.aspects.amountOf(aer));
            assertEquals(1, node.aspects.amountOf(terra));
            assertEquals(1, node.aspects.amountOf(addonPrimal));
        }
    }

    @Test
    void preserveResearchLeavesOneVisInStock() {
        MappedRegistry<IAspect> registry = new MappedRegistry<>(IAspect.REGISTRY_KEY, Lifecycle.stable());
        for (ResourceKey<IAspect> key : TTAspects.PRIMALS) {
            Registry.register(registry, key, mock(IAspect.class));
        }
        RegistryAccess access = new RegistryAccess.ImmutableRegistryAccess(List.of(registry)).freeze();
        Holder<IAspect> ignis = registry.getHolderOrThrow(TTAspects.IGNIS);
        Holder<IAspect> terra = registry.getHolderOrThrow(TTAspects.TERRA);
        BlockEntityNode node =
                new BlockEntityNode(BlockPos.ZERO, TTBlocks.NODE.get().defaultBlockState());
        node.aspects = AspectList.ofEntries(List.of(new AspectInstance(terra, 1), new AspectInstance(ignis, 2)));
        ServerLevel level = mock(ServerLevel.class);
        RandomSource random = mock(RandomSource.class);
        when(level.registryAccess()).thenReturn(access);
        when(level.getRandom()).thenReturn(random);
        when(random.nextInt(1)).thenReturn(0);
        node.setLevel(level);
        Player player = mock(Player.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        PlayerResearch research = mock(PlayerResearch.class);
        when(research.hasCompleted(any(ResourceLocation.class)))
                .thenAnswer(call -> call.getArgument(0, ResourceLocation.class).equals(TTIds.rl("node_preserve")));
        ItemStack wand = new ItemStack(Items.STICK);
        WandParts nonStarter = new WandParts(TTWandParts.CAP_GOLD.get(), TTWandParts.ROD_GREATWOOD.get(), false);

        try (MockedStatic<PlayerResearchAccess> researchAccess =
                        org.mockito.Mockito.mockStatic(PlayerResearchAccess.class);
                MockedStatic<WandVisHelper> wandVis = org.mockito.Mockito.mockStatic(WandVisHelper.class)) {
            researchAccess.when(() -> PlayerResearchAccess.of(player)).thenReturn(research);
            wandVis.when(() -> WandVisHelper.getParts(wand)).thenReturn(nonStarter);
            wandVis.when(() -> WandVisHelper.getMaxVis(wand)).thenReturn(10_000);

            assertTrue(NodeWandTap.tap(node, level, player, wand, 5));

            wandVis.verify(() -> WandVisHelper.addVis(wand, TTAspects.IGNIS, 1, true));
            assertEquals(1, node.aspects.amountOf(ignis));
            assertEquals(1, node.aspects.amountOf(terra));
        }
    }
}
