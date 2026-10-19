package com.leclowndu93150.thaumaturge.content.equipment.bauble;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;

public final class AmuletVisItem extends Item {
    private final int rechargeInterval;

    public AmuletVisItem(Properties properties, int rechargeInterval) {
        super(properties);
        this.rechargeInterval = rechargeInterval;
    }

    public void wornTick(ItemStack stack, LivingEntity wearer) {
        if (!(wearer instanceof Player player)
                || player.level().isClientSide()
                || player.tickCount % rechargeInterval != 0) {
            return;
        }
        List<RechargeTarget> targets = new ArrayList<>();
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            collectRechargeTarget(player.getInventory().getItem(slot), targets);
        }
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            collectWandTarget(player.getInventory().getItem(slot), targets);
        }
        ItemStack offhand = player.getOffhandItem();
        collectRechargeTarget(offhand, targets);
        collectWandTarget(offhand, targets);
        if (ModList.get().isLoaded(TTIds.CURIOS)) {
            for (ItemStack curio : ThaumaturgeCuriosCompat.equippedCurios(player)) {
                collectRechargeTarget(curio, targets);
            }
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                collectRechargeTarget(player.getItemBySlot(slot), targets);
            }
        }
        targets = orderTargets(targets);
        for (RechargeTarget target : targets) {
            if (target.recharge(player)) {
                return;
            }
        }
    }

    static void collectRechargeTarget(ItemStack candidate, List<RechargeTarget> targets) {
        if (RechargeAccess.profile(candidate) != null && RechargeAccess.roomFor(candidate) > 0) {
            targets.add(new RechargeTarget(candidate, RechargeAccess.fillFraction(candidate), null));
        }
    }

    private static void collectWandTarget(ItemStack candidate, List<RechargeTarget> targets) {
        if (!(candidate.getItem() instanceof ItemWand)) {
            return;
        }
        int max = WandVisHelper.getMaxVis(candidate);
        ResourceKey<IAspect> lowest = null;
        int lowestAmount = Integer.MAX_VALUE;
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            int amount = WandVisHelper.getVis(candidate, primal);
            if (amount < max && amount < lowestAmount) {
                lowestAmount = amount;
                lowest = primal;
            }
        }
        if (lowest != null) {
            targets.add(new RechargeTarget(candidate, (float) lowestAmount / max, lowest));
        }
    }

    static List<RechargeTarget> orderTargets(List<RechargeTarget> targets) {
        return targets.stream()
                .sorted(Comparator.comparingDouble(RechargeTarget::fill))
                .toList();
    }

    record RechargeTarget(ItemStack stack, float fill, ResourceKey<IAspect> wandAspect) {
        boolean recharge(Player player) {
            if (wandAspect == null) {
                return RechargeAccess.drawFromAura(player.level(), stack, player.blockPosition(), player, 1) > 0;
            }
            if (AuraHelper.drainVis(player.level(), player.blockPosition(), 1.0F, false) <= 0.0F) {
                return false;
            }
            WandVisHelper.addVis(stack, wandAspect, 1, true);
            return true;
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thaumaturge.amulet_vis.text").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
