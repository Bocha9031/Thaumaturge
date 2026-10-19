package com.leclowndu93150.thaumaturge.content.equipment.bauble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.leclowndu93150.thaumaturge.api.items.ChargeDisplay;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class AmuletVisItemTest {
    @Test
    void ordersEligibleRechargeTargetsByFillRatio() {
        ItemStack lessFilled = rechargeableStack(100, 20);
        ItemStack moreFilled = rechargeableStack(80, 40);
        ItemStack full = rechargeableStack(10, 10);
        ItemStack ineligible = new ItemStack(Items.STICK);
        List<AmuletVisItem.RechargeTarget> targets = new ArrayList<>();

        AmuletVisItem.collectRechargeTarget(moreFilled, targets);
        AmuletVisItem.collectRechargeTarget(full, targets);
        AmuletVisItem.collectRechargeTarget(ineligible, targets);
        AmuletVisItem.collectRechargeTarget(lessFilled, targets);

        List<AmuletVisItem.RechargeTarget> ordered = AmuletVisItem.orderTargets(targets);
        assertEquals(2, ordered.size());
        assertSame(lessFilled, ordered.get(0).stack());
        assertSame(moreFilled, ordered.get(1).stack());
    }

    private static ItemStack rechargeableStack(int capacity, int stored) {
        ItemStack stack = new ItemStack(Items.STICK);
        stack.set(TTDataComponents.RECHARGEABLE.get(), new ChargeProfile(capacity, ChargeDisplay.ALWAYS));
        stack.set(TTDataComponents.CHARGE.get(), stored);
        return stack;
    }
}
