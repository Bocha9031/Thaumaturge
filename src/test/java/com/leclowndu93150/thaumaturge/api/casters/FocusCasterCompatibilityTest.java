package com.leclowndu93150.thaumaturge.api.casters;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.content.spell.casting.CasterHands;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.content.workbench.SlotWorkbenchWand;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;

@SuppressWarnings("removal")
class FocusCasterCompatibilityTest {
    private record Payment(ItemStack stack, Player player, float cost, boolean crafting, boolean simulate) {}

    private static class State {
        ItemStack focus = ItemStack.EMPTY;
        final BlockState block = Blocks.STONE.defaultBlockState();
        ItemStack factoredStack;
        Player factoredPlayer;
        boolean factoredCrafting;
        Payment payment;

        float factor(ItemStack stack, Player player, boolean crafting) {
            factoredStack = stack;
            factoredPlayer = player;
            factoredCrafting = crafting;
            return 0.6F;
        }

        boolean pay(ItemStack stack, Player player, float cost, boolean crafting, boolean simulate) {
            payment = new Payment(stack, player, cost, crafting, simulate);
            return simulate;
        }
    }

    private static final class LegacyCaster extends State implements ICaster {
        @Override
        public ItemStack getFocusStack(ItemStack stack) {
            return focus;
        }

        @Override
        public void setFocus(ItemStack stack, ItemStack focus) {
            this.focus = focus;
        }

        @Override
        public BlockState getPickedBlock(ItemStack stack) {
            return block;
        }

        @Override
        public float getConsumptionModifier(ItemStack stack, Player player, boolean crafting) {
            return factor(stack, player, crafting);
        }

        @Override
        public boolean consumeVis(ItemStack stack, Player player, float amount, boolean crafting, boolean simulate) {
            return pay(stack, player, amount, crafting, simulate);
        }
    }

    private static final class ModernCaster extends State implements ICaster {
        @Override
        public ItemStack socketedFocus(ItemStack stack) {
            return focus;
        }

        @Override
        public void socketFocus(ItemStack stack, ItemStack focus) {
            this.focus = focus;
        }

        @Override
        public BlockState rememberedBlock(ItemStack stack) {
            return block;
        }

        @Override
        public float visCostFactor(ItemStack stack, Player player, boolean crafting) {
            return factor(stack, player, crafting);
        }

        @Override
        public boolean drawVis(ItemStack stack, Player player, float cost, boolean crafting, boolean simulate) {
            return pay(stack, player, cost, crafting, simulate);
        }
    }

    @Test
    void legacyAddonImplementationsAnswerEveryNewMethod() {
        LegacyCaster legacy = new LegacyCaster();
        FocusCaster caster = legacy;
        ItemStack stack = new ItemStack(Items.STICK);
        ItemStack focus = new ItemStack(Items.STONE);
        Player player = mock(Player.class);
        caster.socketFocus(stack, focus);
        assertSame(focus, caster.socketedFocus(stack));
        assertSame(legacy.block, caster.rememberedBlock(stack));
        assertEquals(0.6F, caster.visCostFactor(stack, null, true));
        assertSame(stack, legacy.factoredStack);
        assertNull(legacy.factoredPlayer);
        assertTrue(legacy.factoredCrafting);
        assertTrue(caster.drawVis(stack, player, 7.5F, false, true));
        assertEquals(new Payment(stack, player, 7.5F, false, true), legacy.payment);
        assertFalse(caster.drawVis(stack, player, 2.0F, true, false));
        assertEquals(new Payment(stack, player, 2.0F, true, false), legacy.payment);
    }

    @Test
    void newImplementationsStillAnswerEveryLegacyMethod() {
        ModernCaster modern = new ModernCaster();
        ICaster caster = modern;
        ItemStack stack = new ItemStack(Items.STICK);
        ItemStack focus = new ItemStack(Items.STONE);
        Player player = mock(Player.class);
        caster.setFocus(stack, focus);
        assertSame(focus, caster.getFocusStack(stack));
        assertSame(modern.block, caster.getPickedBlock(stack));
        assertEquals(0.6F, caster.getConsumptionModifier(stack, player, false));
        assertSame(stack, modern.factoredStack);
        assertSame(player, modern.factoredPlayer);
        assertFalse(modern.factoredCrafting);
        assertTrue(caster.consumeVis(stack, player, 7.5F, false, true));
        assertEquals(new Payment(stack, player, 7.5F, false, true), modern.payment);
        assertFalse(caster.consumeVis(stack, player, 2.0F, true, false));
        assertEquals(new Payment(stack, player, 2.0F, true, false), modern.payment);
    }

    private static ItemStack wandStack() {
        WandCap cap = new WandCap(1.0F, Map.of(), 1, TTIds.rl("test_cap"));
        return ItemWand.create(TTItems.WAND.get(), cap, TTWandParts.ROD_WOOD.get(), false);
    }

    @Test
    void wandCopiesFocusOnBothInsertionAndReadAndClearsItsComponent() {
        ItemStack stack = wandStack();
        ItemWand wand = (ItemWand) stack.getItem();
        ItemStack focus = new ItemStack(Items.STONE, 3);
        wand.socketFocus(stack, focus);
        focus.setCount(1);
        ItemStack read = wand.socketedFocus(stack);
        assertEquals(3, read.getCount());
        read.setCount(2);
        assertEquals(3, wand.getFocusStack(stack).getCount());
        wand.setFocus(stack, ItemStack.EMPTY);
        assertTrue(wand.socketedFocus(stack).isEmpty());
        assertFalse(stack.has(TTDataComponents.SOCKETED_FOCUS.get()));
        wand.socketFocus(stack, null);
        assertTrue(wand.socketedFocus(stack).isEmpty());
    }

    @Test
    void wandNonPositiveCostsSucceedWithoutTouchingPlayerOrStorage() {
        ItemWand wand = mock(ItemWand.class, CALLS_REAL_METHODS);
        ItemStack stack = mock(ItemStack.class);
        Player player = mock(Player.class);
        assertTrue(wand.drawVis(stack, player, 0.0F, true, false));
        assertTrue(wand.consumeVis(stack, player, -5.0F, false, true));
        verifyNoInteractions(stack, player);
    }

    @Test
    void wandPaymentFailsWithoutPartialDrainAndSimulationPreservesDiscountedStock() {
        ItemStack stack = wandStack();
        ItemWand wand = (ItemWand) stack.getItem();
        Player player = mock(Player.class);
        Map<ResourceKey<IAspect>, Integer> pools = new LinkedHashMap<>();
        TTAspects.PRIMALS.forEach(aspect -> pools.put(aspect, 100));
        ResourceKey<IAspect> last = TTAspects.PRIMALS.get(TTAspects.PRIMALS.size() - 1);
        pools.put(last, 9);
        WandVis insufficient = new WandVis(pools);
        WandVisHelper.setAllVis(stack, insufficient);
        try (var discounts = mockStatic(CasterManager.class)) {
            discounts.when(() -> CasterManager.getTotalVisDiscount(player)).thenReturn(0.5F);
            assertFalse(wand.drawVis(stack, player, 1.2F, false, false));
            assertEquals(insufficient, WandVisHelper.getAllVis(stack));
            pools.put(last, 100);
            WandVis funded = new WandVis(pools);
            WandVisHelper.setAllVis(stack, funded);
            assertTrue(wand.drawVis(stack, player, 1.2F, true, true));
            assertEquals(funded, WandVisHelper.getAllVis(stack));
            assertTrue(wand.consumeVis(stack, player, 1.2F, true, false));
            TTAspects.PRIMALS.forEach(aspect -> assertEquals(90, WandVisHelper.getVis(stack, aspect)));
        }
    }

    @Test
    void pureFocusCasterAddonItemsAreRecognizedInHandsAndWorkbench() {
        Item addon = mock(Item.class, withSettings().extraInterfaces(FocusCaster.class));
        assertFalse(addon instanceof ICaster);
        ItemStack stack = mock(ItemStack.class);
        when(stack.getItem()).thenReturn(addon);
        LivingEntity holder = mock(LivingEntity.class);
        when(holder.getMainHandItem()).thenReturn(new ItemStack(Items.STONE));
        when(holder.getOffhandItem()).thenReturn(stack);
        assertSame(stack, CasterHands.held(holder));
        assertTrue(SlotWorkbenchWand.isUsableWand(stack));
    }
}
