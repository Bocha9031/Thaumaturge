package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftCost;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftCostEvent;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneWorkbench;
import com.leclowndu93150.thaumaturge.api.recipe.IWorkbenchAuraSource;
import com.leclowndu93150.thaumaturge.api.recipe.IWorkbenchVisSource;
import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTWorkbenchSources;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

public final class WorkbenchPayment {

    private WorkbenchPayment() {}

    public static Plan plan(
            IArcaneRecipe recipe, IArcaneWorkbench inventory, Player player, ArcaneWorkbenchContext context) {
        return plan(recipe, inventory, player, context, null);
    }

    public static Plan plan(
            IArcaneRecipe recipe,
            IArcaneWorkbench inventory,
            Player player,
            ArcaneWorkbenchContext context,
            @Nullable TransactionContext transaction) {
        if (transaction != null) {
            return planInternal(recipe, inventory, player, context, transaction);
        }
        try (Transaction root = Transaction.openRoot()) {
            return planInternal(recipe, inventory, player, context, root);
        }
    }

    public static Plan plan(IArcaneRecipe recipe, IArcaneWorkbench inventory, Player player) {
        return planInternalOutsideTransaction(recipe, inventory, player, null);
    }

    private static Plan planInternalOutsideTransaction(
            IArcaneRecipe recipe, IArcaneWorkbench inventory, Player player, @Nullable ArcaneWorkbenchContext context) {
        try (Transaction transaction = Transaction.openRoot()) {
            return planInternal(recipe, inventory, player, context, transaction);
        }
    }

    private static Plan planInternal(
            IArcaneRecipe recipe,
            IArcaneWorkbench inventory,
            Player player,
            @Nullable ArcaneWorkbenchContext context,
            TransactionContext transaction) {
        ItemStack wand = inventory.wandStack();
        boolean hasWand = wand.getItem() instanceof ItemWand;

        Map<ResourceKey<IAspect>, Integer> wandCentivis = new LinkedHashMap<>();
        Map<ResourceKey<IAspect>, Integer> sourceCentivis = new LinkedHashMap<>();
        AspectList crystalNeeds = calculateCrystalNeeds(
                recipe, inventory, player, context, hasWand, wand, wandCentivis, sourceCentivis, transaction);

        boolean fullWand = hasWand && crystalNeeds.isEmpty() && sourceCentivis.isEmpty();
        float modifier;
        if (fullWand) {
            modifier = averageCraftModifier(wand, player);
        } else if (!crystalNeeds.isEmpty()) {
            modifier = WandEconomy.CRAFT_AURA_SURCHARGE * gearModifier(player);
        } else {
            modifier = gearModifier(player);
        }
        int auraVis = recipe.visCost() <= 0 ? 0 : Math.max(1, Mth.ceil(recipe.visCost() * modifier));

        Plan plan = new Plan(
                fullWand, wandCentivis, sourceCentivis, crystalNeeds, auraVis, hasCrystals(inventory, crystalNeeds));
        return applyCostEvent(recipe, inventory, player, plan);
    }

    private static AspectList calculateCrystalNeeds(
            IArcaneRecipe recipe,
            IArcaneWorkbench inventory,
            Player player,
            @Nullable ArcaneWorkbenchContext context,
            boolean hasWand,
            ItemStack wand,
            Map<ResourceKey<IAspect>, Integer> wandCentivis,
            Map<ResourceKey<IAspect>, Integer> sourceCentivis,
            TransactionContext transaction) {
        AspectList crystalNeeds = AspectList.EMPTY;
        for (AspectInstance entry : recipe.crystalCost().entries()) {
            ResourceKey<IAspect> primal = entry.aspect().getKey();
            int centivis = entry.amount() * WandEconomy.CRYSTAL_SUBSTITUTE_VIS * WandEconomy.CENTIVIS_PER_VIS;
            Map<ResourceKey<IAspect>, Integer> single = new LinkedHashMap<>();
            single.put(primal, centivis);
            if (hasWand && WandVisHelper.payExact(wand, single, true)) {
                wandCentivis.put(primal, centivis);
            } else if (supplyFromSources(context, player, inventory, entry.aspect(), centivis, transaction)
                    >= centivis) {
                sourceCentivis.put(primal, centivis);
            } else {
                crystalNeeds = crystalNeeds.add(entry.aspect(), entry.amount());
            }
        }
        return crystalNeeds;
    }

    private static Plan applyCostEvent(IArcaneRecipe recipe, IArcaneWorkbench inventory, Player player, Plan plan) {
        boolean affordable = plan.crystalsSatisfied();
        ArcaneCraftCost initial = new ArcaneCraftCost(
                plan.fullWand(), plan.wandCentivis(), plan.crystalsToConsume(), plan.auraVis(), affordable);
        ArcaneCraftCostEvent event = new ArcaneCraftCostEvent(recipe, inventory, player, initial);
        NeoForge.EVENT_BUS.post(event);
        ArcaneCraftCost result = event.getCost();
        if (result == initial) {
            return plan;
        }
        return new Plan(
                result.paidFromWand(),
                result.wandCentivis(),
                plan.sourceCentivis(),
                result.crystalsNeeded(),
                result.auraVis(),
                result.affordable() && hasCrystals(inventory, result.crystalsNeeded()));
    }

    public static ArcaneCraftCost cost(
            IArcaneRecipe recipe, IArcaneWorkbench workbench, Player player, @Nullable ArcaneWorkbenchContext context) {
        try (Transaction transaction = Transaction.openRoot()) {
            return planInternal(recipe, workbench, player, context, transaction).cost();
        }
    }

    public static @Nullable ItemStack paidWand(Plan plan, ItemStack wand) {
        ItemStack paid = wand.copy();
        if (plan.wandCentivis().isEmpty()) {
            return paid;
        }
        return WandVisHelper.payExact(paid, plan.wandCentivis(), false) ? paid : null;
    }

    public static boolean canCraft(Plan plan, @Nullable BlockEntityArcaneWorkbench tile) {
        if (!plan.crystalsSatisfied()) {
            return false;
        }
        return plan.auraVis() <= 0 || (tile != null && tile.auraVis >= plan.auraVis());
    }

    static boolean pay(
            Plan plan,
            ServerPlayer player,
            IArcaneWorkbench inventory,
            ArcaneWorkbenchContext context,
            TransactionContext transaction) {
        return paySources(plan, context, player, inventory, transaction)
                && payAura(plan, context, player, inventory, transaction);
    }

    public static boolean paySources(
            Plan plan,
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneWorkbench inventory,
            TransactionContext transaction) {
        if (!plan.crystalsSatisfied()) return false;
        if (!plan.wandCentivis().isEmpty() && !WandVisHelper.payExact(inventory.wandStack(), plan.wandCentivis(), true))
            return false;

        for (Map.Entry<ResourceKey<IAspect>, Integer> entry :
                plan.sourceCentivis().entrySet()) {
            Holder<IAspect> aspect = Aspects.resolve(player.level(), entry.getKey());
            if (aspect == null) return false;
            int remaining = entry.getValue();
            for (IWorkbenchVisSource source : TTWorkbenchSources.visSources()) {
                if (remaining <= 0) break;
                int supplied = clampSupply(
                        source.supply(context, player, inventory, aspect, remaining, transaction), remaining);
                remaining -= supplied;
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    public static boolean payAura(
            Plan plan,
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneWorkbench inventory,
            TransactionContext transaction) {
        if (plan.auraVis() <= 0) {
            return true;
        }
        BlockEntityArcaneWorkbench tile = placedWorkbench(context);
        if (tile != null) {
            return tile.spendAura(plan.auraVis(), transaction);
        }

        int remainingAura = plan.auraVis();
        for (IWorkbenchAuraSource source : TTWorkbenchSources.auraSources()) {
            if (remainingAura <= 0) break;
            int supplied =
                    clampSupply(source.supply(context, player, inventory, remainingAura, transaction), remainingAura);
            remainingAura -= supplied;
        }
        return remainingAura == 0;
    }

    private static @Nullable BlockEntityArcaneWorkbench placedWorkbench(ArcaneWorkbenchContext context) {
        if (context.kind() != ArcaneWorkbenchContext.Kind.PLACED) {
            return null;
        }
        return context.blockPosition()
                .map(context.level()::getBlockEntity)
                .filter(BlockEntityArcaneWorkbench.class::isInstance)
                .map(BlockEntityArcaneWorkbench.class::cast)
                .orElse(null);
    }

    private static int supplyFromSources(
            @Nullable ArcaneWorkbenchContext context,
            Player player,
            IArcaneWorkbench inventory,
            Holder<IAspect> aspect,
            int need,
            TransactionContext transaction) {
        if (context == null || !(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }
        int supplied = 0;
        try (Transaction probe = Transaction.open(transaction)) {
            for (IWorkbenchVisSource source : TTWorkbenchSources.visSources()) {
                if (supplied >= need) {
                    break;
                }
                supplied += clampSupply(
                        source.supply(context, serverPlayer, inventory, aspect, need - supplied, probe),
                        need - supplied);
            }
        }
        return supplied;
    }

    private static int clampSupply(int supplied, int need) {
        return Math.max(0, Math.min(need, supplied));
    }

    public static int crudeCost(IArcaneRecipe recipe) {
        return Mth.ceil(recipe.visCost() * WandEconomy.CRAFT_AURA_SURCHARGE);
    }

    private static float averageCraftModifier(ItemStack wand, Player player) {
        float total = 0.0F;
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            total += WandVisHelper.getConsumptionModifier(wand, player, primal, true);
        }
        return total / WandEconomy.PRIMAL_COUNT;
    }

    private static float gearModifier(Player player) {
        return Math.max(1.0F - CasterManager.getTotalVisDiscount(player), WandEconomy.MIN_CONSUMPTION_MODIFIER);
    }

    private static boolean hasCrystals(IArcaneWorkbench inventory, AspectList needs) {
        for (AspectInstance entry : needs.entries()) {
            int found = 0;
            for (AspectInstance crystalEntry : inventory.availableCrystals().entries()) {
                Holder<IAspect> holder = crystalEntry.aspect();
                if (holder != null
                        && holder.value().tag().equals(entry.aspect().value().tag())) {
                    found += crystalEntry.amount();
                }
            }
            if (found < entry.amount()) {
                return false;
            }
        }
        return true;
    }

    public record Plan(
            boolean fullWand,
            Map<ResourceKey<IAspect>, Integer> wandCentivis,
            Map<ResourceKey<IAspect>, Integer> sourceCentivis,
            AspectList crystalsToConsume,
            int auraVis,
            boolean crystalsSatisfied) {

        public ArcaneCraftCost cost() {
            return new ArcaneCraftCost(fullWand, wandCentivis, crystalsToConsume, auraVis, crystalsSatisfied);
        }
    }
}
