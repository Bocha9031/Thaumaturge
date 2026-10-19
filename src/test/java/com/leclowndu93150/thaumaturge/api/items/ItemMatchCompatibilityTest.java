package com.leclowndu93150.thaumaturge.api.items;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.junit.jupiter.api.Test;

class ItemMatchCompatibilityTest {
    @Test
    void legacySharedFiltersReturnRelaxedCopiesInsteadOfMutatingGlobalConstants() throws Exception {
        Class<?> filterType = Class.forName(InvHelper.class.getName() + "$InvFilter");
        Field strictField = filterType.getField("STRICT");
        Object strict = strictField.get(null);
        Method relax = filterType.getMethod("setRelaxedComponents");
        Field relaxedComponents = filterType.getDeclaredField("relaxedComponents");
        relaxedComponents.setAccessible(true);
        Object relaxedStrict = relax.invoke(strict);

        assertNotSame(strict, relaxedStrict);
        assertFalse(relaxedComponents.getBoolean(strict));
        assertTrue(relaxedComponents.getBoolean(relaxedStrict));
        assertFalse(legacyMatches(filterType, strict, plainIron(), namedIron()));
        assertTrue(legacyMatches(filterType, relaxedStrict, plainIron(), namedIron()));
        assertSame(relaxedStrict, relax.invoke(relaxedStrict));

        Field baseTagsField = InvHelper.class.getField("BASE_TAGS");
        Object baseTags = baseTagsField.get(null);
        Object relaxedBaseTags = relax.invoke(baseTags);
        assertNotSame(baseTags, relaxedBaseTags);
        assertFalse(relaxedComponents.getBoolean(baseTags));
        assertTrue(relaxedComponents.getBoolean(relaxedBaseTags));
    }

    @Test
    void exactMatchIgnoresCountButRespectsItemComponentsAndDamage() {
        ItemStack first = new ItemStack(Items.IRON_SWORD, 1);
        first.setDamageValue(3);
        ItemStack same = first.copyWithCount(4);
        ItemStack differentDamage = first.copy();
        differentDamage.setDamageValue(4);

        assertTrue(ItemMatch.EXACT.test(first, same));
        assertFalse(ItemMatch.EXACT.test(first, differentDamage));
        assertTrue(ItemMatch.EXACT.ignoringDamage(true).test(first, differentDamage));
        assertFalse(ItemMatch.EXACT.test(first, new ItemStack(Items.DIAMOND_SWORD)));
    }

    @Test
    void componentSubsetIsDirectionalAndAnyComponentsCanIgnoreExtras() {
        ItemStack plain = new ItemStack(Items.IRON_SWORD);
        ItemStack named = plain.copy();
        named.set(DataComponents.CUSTOM_NAME, Component.literal("named"));

        ItemMatch subset = ItemMatch.EXACT.withComponentSubset(true);
        assertTrue(subset.test(plain, named));
        assertFalse(subset.test(named, plain));
        assertTrue(ItemMatch.EXACT.ignoringComponents(true).test(named, plain));
    }

    @Test
    void stackFilterPreservesWhitelistBlacklistAndLimits() {
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        ItemStack gold = new ItemStack(Items.GOLD_INGOT);
        StackFilter whitelist = new StackFilter(List.of(iron, ItemStack.EMPTY), List.of(16), false, ItemMatch.EXACT);
        StackFilter blacklist = StackFilter.of(List.of(iron), true, ItemMatch.EXACT);

        assertTrue(whitelist.accepts(iron));
        assertFalse(whitelist.accepts(gold));
        assertFalse(whitelist.accepts(ItemStack.EMPTY));
        assertTrue(blacklist.accepts(gold));
        assertFalse(blacklist.accepts(iron));
        assertEquals(new InvHelper.FilterMatch(iron, 16), whitelist.firstAcceptedWithLimit(List.of(gold, iron)));
    }

    @Test
    void handlerQueriesAndExtractionUseTheNativeHandlerWithoutMutatingOnSimulation() {
        ItemStackHandler handler = new ItemStackHandler(2);
        handler.setStackInSlot(0, new ItemStack(Items.IRON_INGOT, 3));
        handler.setStackInSlot(1, new ItemStack(Items.GOLD_INGOT, 2));
        ItemStack wanted = new ItemStack(Items.IRON_INGOT, 2);

        assertEquals(3, InvHelper.count(handler, wanted, ItemMatch.EXACT));
        assertEquals(
                2, InvHelper.extract(handler, wanted, ItemMatch.EXACT, true).getCount());
        assertEquals(3, handler.getStackInSlot(0).getCount());
        assertEquals(
                2, InvHelper.extract(handler, wanted, ItemMatch.EXACT, false).getCount());
        assertEquals(1, handler.getStackInSlot(0).getCount());
        assertTrue(StackFilter.of(List.of(wanted), false, ItemMatch.EXACT)
                .firstAccepted(handler, true)
                .isEmpty());
    }

    @Test
    void takeFromPlayerChecksBeforeMutatingAndConsumesOnlyTheRequestedCount() {
        Player player = mock(Player.class);
        Inventory inventory = new Inventory(player);
        when(player.getInventory()).thenReturn(inventory);
        inventory.setItem(0, new ItemStack(Items.IRON_INGOT, 2));
        inventory.setItem(1, new ItemStack(Items.IRON_INGOT, 3));

        assertFalse(InvHelper.takeFromPlayer(player, new ItemStack(Items.GOLD_INGOT, 1), false));
        assertEquals(2, inventory.getItem(0).getCount());
        assertEquals(3, inventory.getItem(1).getCount());
        assertTrue(InvHelper.takeFromPlayer(player, new ItemStack(Items.IRON_INGOT, 4), false));
        assertTrue(inventory.getItem(0).isEmpty());
        assertEquals(1, inventory.getItem(1).getCount());
    }

    private static boolean legacyMatches(Class<?> filterType, Object filter, ItemStack first, ItemStack second)
            throws Exception {
        Method compare = InvHelper.class.getMethod("areItemStacksEqual", ItemStack.class, ItemStack.class, filterType);
        return (boolean) compare.invoke(null, first, second, filter);
    }

    private static ItemStack plainIron() {
        return new ItemStack(Items.IRON_INGOT);
    }

    private static ItemStack namedIron() {
        ItemStack stack = plainIron();
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("named"));
        return stack;
    }
}
