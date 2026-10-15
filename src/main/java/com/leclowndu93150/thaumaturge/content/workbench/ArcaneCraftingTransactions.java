package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction.Failure;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction.Result;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.api.transaction.RootCommitJournal;
import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

public final class ArcaneCraftingTransactions implements ArcaneCraftingTransaction.Bindings {

    @Override
    public Result preview(ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        return run(context, player, input, null, null, true, null);
    }

    @Override
    public ArcaneCraftingTransaction.Inspection inspect(
            ArcaneWorkbenchContext context, ServerPlayer player, IArcaneCraftingInput input) {
        Failure invalid = validate(context, player);
        if (invalid != Failure.NONE) {
            return ArcaneCraftingTransaction.Inspection.failure(invalid);
        }
        Match match = match(context.level(), player, input);
        if (match.holder() == null) {
            return ArcaneCraftingTransaction.Inspection.failure(match.failure());
        }
        IArcaneRecipe recipe = match.holder().value();
        ArcaneCraftingTransaction.Requirements requirements = new ArcaneCraftingTransaction.Requirements(
                recipe.visCost(), recipe.crystalCost(), recipe.getIngredients());
        return new ArcaneCraftingTransaction.Inspection(
                match.failure(),
                ResourceKey.create(Registries.RECIPE, match.holder().id()),
                recipe.assemble(input, context.level().registryAccess()),
                remainders(recipe, input),
                requirements,
                recipe.gateStatus(player));
    }

    @Override
    public Result craft(
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneCraftingInput input,
            IArcaneCraftingStore store,
            TransactionContext transaction) {
        return run(context, player, input, store, transaction, false, null);
    }

    static Result craftExpected(
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneCraftingInput input,
            IArcaneCraftingStore store,
            ItemStack expected) {
        return run(context, player, input, store, null, false, expected);
    }

    private static Result run(
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneCraftingInput input,
            @Nullable IArcaneCraftingStore store,
            @Nullable TransactionContext parent,
            boolean preview,
            @Nullable ItemStack expected) {
        try (Transaction transaction = Transaction.open(parent)) {
            Failure invalid = validate(context, player);
            if (invalid != Failure.NONE) {
                return Result.failure(invalid);
            }
            Match match = match(context.level(), player, input);
            if (match.failure() != Failure.NONE) {
                return Result.failure(match.failure());
            }
            IArcaneRecipe recipe = match.holder().value();
            ItemStack output = recipe.assemble(input, context.level().registryAccess());
            if (expected != null && !ItemStack.matches(output, expected)) {
                return Result.failure(Failure.INGREDIENTS_CHANGED);
            }
            List<ItemStack> remainders = remainders(recipe, input);
            BlockEntityArcaneWorkbench tile = placedWorkbench(context);
            if (tile != null) {
                tile.refreshAura();
            }
            WorkbenchPayment.Plan plan = WorkbenchPayment.plan(recipe, input, player, context, transaction);
            ItemStack wand = WorkbenchPayment.paidWand(plan, input.wandStack());
            if (wand == null || !plan.crystalsSatisfied()) {
                return new Result(Failure.PAYMENT_UNAVAILABLE, output, remainders, plan.cost());
            }
            if (!WorkbenchPayment.pay(plan, player, input, context, transaction)) {
                return new Result(Failure.PAYMENT_UNAVAILABLE, output, remainders, plan.cost());
            }
            if (store != null) {
                IArcaneCraftingStore.Consumption consumption =
                        new IArcaneCraftingStore.Consumption(grid(input), remainders, plan.crystalsToConsume(), wand);
                if (!store.consume(consumption, transaction)) {
                    return new Result(Failure.INGREDIENTS_CHANGED, output, remainders, plan.cost());
                }
            }
            if (!preview) {
                new RootCommitJournal(() -> ResearchProgressionEvents.recordCrafted(player, output.copy()))
                        .updateSnapshots(transaction);
                transaction.commit();
            }
            return new Result(Failure.NONE, output, remainders, plan.cost());
        }
    }

    private static Failure validate(ArcaneWorkbenchContext context, ServerPlayer player) {
        if (!context.level().getServer().isSameThread()) {
            return Failure.NOT_SERVER_THREAD;
        }
        if (context.level() != player.serverLevel() || !context.actingPlayer().equals(player.getUUID())) {
            return Failure.INVALID_CONTEXT;
        }
        return Failure.NONE;
    }

    private static Match match(ServerLevel level, ServerPlayer player, IArcaneCraftingInput input) {
        RecipeHolder<? extends IArcaneRecipe> locked = null;
        for (RecipeHolder<? extends IArcaneRecipe> holder :
                level.getRecipeManager().getAllRecipesFor(TTRecipeTypes.ARCANE.get())) {
            if (holder.value().matches(input, level)) {
                if (holder.value().doesPassGate(player)) {
                    return new Match(holder, Failure.NONE);
                }
                if (locked == null) {
                    locked = holder;
                }
            }
        }
        return locked == null ? new Match(null, Failure.NO_RECIPE) : new Match(locked, Failure.RESEARCH_LOCKED);
    }

    private static List<ItemStack> grid(IArcaneCraftingInput input) {
        List<ItemStack> grid = new ArrayList<>(input.width() * input.height());
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                grid.add(input.getItem(x, y).copy());
            }
        }
        return grid;
    }

    private static List<ItemStack> remainders(IArcaneRecipe recipe, IArcaneCraftingInput input) {
        List<ItemStack> all = recipe.getRemainingItems(input);
        int size = input.width() * input.height();
        List<ItemStack> remainders = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            remainders.add(i < all.size() ? all.get(i).copy() : ItemStack.EMPTY);
        }
        return remainders;
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

    private record Match(@Nullable RecipeHolder<? extends IArcaneRecipe> holder, Failure failure) {}
}
