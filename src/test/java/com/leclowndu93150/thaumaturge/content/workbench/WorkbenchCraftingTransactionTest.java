package com.leclowndu93150.thaumaturge.content.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneWorkbench;
import com.leclowndu93150.thaumaturge.api.recipe.IWorkbenchAuraSource;
import com.leclowndu93150.thaumaturge.api.transaction.SnapshotJournal;
import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingRecipe;
import com.leclowndu93150.thaumaturge.registry.TTRecipeTypes;
import com.leclowndu93150.thaumaturge.registry.TTWorkbenchSources;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.MockedStatic;

class WorkbenchCraftingTransactionTest {
    @Test
    @ResourceLock("TTWorkbenchSources")
    void craftingRunnerRollsBackPaidAuraWhenStoreRejectsAndCallerCommits() throws ReflectiveOperationException {
        TransactionalBalance balance = new TransactionalBalance(10);
        IWorkbenchAuraSource source = (context, player, workbench, need, transaction) -> {
            balance.drain(need, transaction);
            return need;
        };
        withAuraSources(List.of(source), () -> {
            ServerLevel level = mock(ServerLevel.class);
            MinecraftServer server = mock(MinecraftServer.class);
            RecipeManager recipeManager = mock(RecipeManager.class);
            ServerPlayer player = mock(ServerPlayer.class);
            UUID playerId = UUID.randomUUID();
            when(level.getServer()).thenReturn(server);
            when(server.isSameThread()).thenReturn(true);
            when(level.getRecipeManager()).thenReturn(recipeManager);
            when(player.serverLevel()).thenReturn(level);
            when(player.getUUID()).thenReturn(playerId);

            ArcaneCraftingRecipe recipe = mock(ArcaneCraftingRecipe.class);
            List<ItemStack> inputStacks = new ArrayList<>();
            for (int slot = 0; slot < InventoryArcaneWorkbench.SIZE; slot++) {
                inputStacks.add(ItemStack.EMPTY);
            }
            inputStacks.set(0, new ItemStack(Items.STONE));
            inputStacks.set(InventoryArcaneWorkbench.WAND_SLOT, new ItemStack(Items.STICK));
            ArcaneCraftingInput input =
                    ArcaneCraftingInput.of(3, 3, inputStacks).withPlayer(player);
            when(recipe.matches(input, level)).thenReturn(true);
            when(recipe.doesPassGate(player)).thenReturn(true);
            when(recipe.assemble(eq(input), any())).thenReturn(new ItemStack(Items.DIAMOND));
            when(recipe.getRemainingItems(input)).thenReturn(NonNullList.withSize(9, ItemStack.EMPTY));
            when(recipe.visCost()).thenReturn(0);
            when(recipe.crystalCost()).thenReturn(AspectList.EMPTY);
            when(recipe.getIngredients()).thenReturn(NonNullList.create());
            RecipeHolder<ArcaneCraftingRecipe> holder =
                    new RecipeHolder<>(ResourceLocation.withDefaultNamespace("transaction_test"), recipe);
            when(recipeManager.getAllRecipesFor(TTRecipeTypes.ARCANE.get())).thenReturn(List.of(holder));

            ArcaneWorkbenchContext context = new ArcaneWorkbenchContext(
                    level, UUID.randomUUID(), null, null, playerId, ArcaneWorkbenchContext.Kind.VIRTUAL);
            InventoryArcaneWorkbench inventory = new InventoryArcaneWorkbench();
            inventory.setItem(0, new ItemStack(Items.DIRT));
            inventory.setItem(InventoryArcaneWorkbench.WAND_SLOT, new ItemStack(Items.STICK));
            List<ItemStack> overflow = new ArrayList<>();
            WorkbenchCraftingStore store = new WorkbenchCraftingStore(inventory, null, 0, 0, 3, 3, overflow::add);
            WorkbenchPayment.Plan plan = auraPlan(5);

            try (MockedStatic<WorkbenchPayment> payment = mockStatic(WorkbenchPayment.class, CALLS_REAL_METHODS)) {
                payment.when(() -> WorkbenchPayment.plan(
                                eq(recipe), eq(input), eq(player), eq(context), any(TransactionContext.class)))
                        .thenReturn(plan);
                try (Transaction outer = Transaction.openRoot()) {
                    ArcaneCraftingTransaction.Result result =
                            new ArcaneCraftingTransactions().craft(context, player, input, store, outer);
                    assertEquals(ArcaneCraftingTransaction.Failure.INGREDIENTS_CHANGED, result.failure());
                    outer.commit();
                }
            }
            assertEquals(10, balance.value);
            assertEquals(Items.DIRT, inventory.getItem(0).getItem());
            assertTrue(overflow.isEmpty());
        });
    }

    @Test
    @ResourceLock("TTWorkbenchSources")
    void partialAuraProviderFailureRollsBackEarlierProvider() throws ReflectiveOperationException {
        TransactionalBalance balance = new TransactionalBalance(10);
        IWorkbenchAuraSource drainingSource = (context, player, workbench, need, transaction) -> {
            int drained = Math.min(need, 3);
            balance.drain(drained, transaction);
            return drained;
        };
        IWorkbenchAuraSource refusingSource = (context, player, workbench, need, transaction) -> 0;
        withAuraSources(List.of(drainingSource, refusingSource), () -> {
            try (Transaction transaction = Transaction.openRoot()) {
                assertFalse(WorkbenchPayment.pay(
                        auraPlan(5),
                        mock(ServerPlayer.class),
                        mock(IArcaneWorkbench.class),
                        mock(ArcaneWorkbenchContext.class),
                        transaction));
            }
            assertEquals(10, balance.value);
        });
    }

    @Test
    @ResourceLock("TTWorkbenchSources")
    void successfulPaymentRollsBackWhenTheRealStoreRefuses() throws ReflectiveOperationException {
        TransactionalBalance balance = new TransactionalBalance(10);
        IWorkbenchAuraSource source = (context, player, workbench, need, transaction) -> {
            balance.drain(need, transaction);
            return need;
        };
        withAuraSources(List.of(source), () -> {
            InventoryArcaneWorkbench inventory = new InventoryArcaneWorkbench();
            inventory.setItem(0, new ItemStack(Items.STONE));
            WorkbenchCraftingStore store = store(inventory, new ArrayList<>());
            IArcaneCraftingStore.Consumption stale = new IArcaneCraftingStore.Consumption(
                    List.of(new ItemStack(Items.DIRT)), List.of(ItemStack.EMPTY), AspectList.EMPTY, ItemStack.EMPTY);

            try (Transaction transaction = Transaction.openRoot()) {
                assertTrue(WorkbenchPayment.pay(
                        auraPlan(5),
                        mock(ServerPlayer.class),
                        inventory,
                        mock(ArcaneWorkbenchContext.class),
                        transaction));
                assertFalse(store.consume(stale, transaction));
            }

            assertEquals(10, balance.value);
            assertEquals(Items.STONE, inventory.getItem(0).getItem());
        });
    }

    @Test
    void abortRestoresGridAndWandWithoutPublishingOverflowOrChanges() {
        TrackedInventory inventory = new TrackedInventory();
        inventory.setItem(0, new ItemStack(Items.STONE, 2));
        inventory.setItem(InventoryArcaneWorkbench.WAND_SLOT, new ItemStack(Items.STICK));
        inventory.dirtyCalls = 0;
        List<ItemStack> overflow = new ArrayList<>();
        List<Integer> changes = new ArrayList<>();
        inventory.addChangedListener(() -> changes.add(1));
        WorkbenchCraftingStore store = store(inventory, overflow);

        try (Transaction transaction = Transaction.openRoot()) {
            assertTrue(store.consume(consumption(), transaction));
            assertEquals(1, inventory.getItem(0).getCount());
            assertEquals(Items.DIAMOND, inventory.wandStack().getItem());
            assertTrue(overflow.isEmpty());
            assertTrue(changes.isEmpty());
        }

        assertEquals(2, inventory.getItem(0).getCount());
        assertEquals(Items.STICK, inventory.wandStack().getItem());
        assertTrue(overflow.isEmpty());
        assertTrue(changes.isEmpty());
        assertEquals(0, inventory.dirtyCalls);
    }

    @Test
    void nestedCommitPublishesOnlyAfterRootCommitAndChangedStoreRefusesWithoutMutation() {
        TrackedInventory inventory = new TrackedInventory();
        inventory.setItem(0, new ItemStack(Items.STONE, 2));
        inventory.setItem(InventoryArcaneWorkbench.WAND_SLOT, new ItemStack(Items.STICK));
        inventory.dirtyCalls = 0;
        List<ItemStack> overflow = new ArrayList<>();
        List<Integer> changes = new ArrayList<>();
        inventory.addChangedListener(() -> changes.add(1));
        WorkbenchCraftingStore store = store(inventory, overflow);

        try (Transaction outer = Transaction.openRoot()) {
            try (Transaction child = Transaction.open(outer)) {
                assertTrue(store.consume(consumption(), child));
                child.commit();
            }
            assertTrue(overflow.isEmpty());
            assertTrue(changes.isEmpty());
            outer.commit();
        }
        assertEquals(1, changes.size());
        assertEquals(1, inventory.dirtyCalls);
        assertEquals(1, overflow.size());
        assertEquals(Items.DIRT, overflow.getFirst().getItem());

        IArcaneCraftingStore.Consumption stale = new IArcaneCraftingStore.Consumption(
                List.of(new ItemStack(Items.STONE, 9)),
                List.of(ItemStack.EMPTY),
                AspectList.EMPTY,
                inventory.wandStack());
        try (Transaction transaction = Transaction.openRoot()) {
            assertFalse(store.consume(stale, transaction));
            transaction.commit();
        }
        assertEquals(1, inventory.getItem(0).getCount());
        assertEquals(1, changes.size());
        assertEquals(1, inventory.dirtyCalls);
        assertEquals(1, overflow.size());
    }

    private static WorkbenchCraftingStore store(InventoryArcaneWorkbench inventory, List<ItemStack> overflow) {
        return new WorkbenchCraftingStore(inventory, null, 0, 0, 1, 1, overflow::add);
    }

    private static IArcaneCraftingStore.Consumption consumption() {
        return new IArcaneCraftingStore.Consumption(
                List.of(new ItemStack(Items.STONE, 2)),
                List.of(new ItemStack(Items.DIRT)),
                AspectList.EMPTY,
                new ItemStack(Items.DIAMOND));
    }

    private static WorkbenchPayment.Plan auraPlan(int auraVis) {
        return new WorkbenchPayment.Plan(false, Map.of(), Map.of(), AspectList.EMPTY, auraVis, true);
    }

    private static void withAuraSources(List<IWorkbenchAuraSource> sources, ThrowingRunnable action)
            throws ReflectiveOperationException {
        Field field = TTWorkbenchSources.class.getDeclaredField("AURA_SOURCES");
        field.setAccessible(true);
        List<?> registered = (List<?>) field.get(null);
        List<IWorkbenchAuraSource> original = List.copyOf(TTWorkbenchSources.auraSources());
        try {
            registered.clear();
            TTWorkbenchSources.registerAuraSources(sources);
            action.run();
        } finally {
            registered.clear();
            TTWorkbenchSources.registerAuraSources(original);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws ReflectiveOperationException;
    }

    private static final class TransactionalBalance {
        private int value;
        private final SnapshotJournal<Integer> journal = new SnapshotJournal<>() {
            @Override
            protected Integer createSnapshot() {
                return value;
            }

            @Override
            protected void revertToSnapshot(Integer snapshot) {
                value = snapshot;
            }
        };

        private TransactionalBalance(int value) {
            this.value = value;
        }

        private void drain(int amount, TransactionContext transaction) {
            journal.updateSnapshots(transaction);
            value -= amount;
        }
    }

    private static final class TrackedInventory extends InventoryArcaneWorkbench {
        private int dirtyCalls;

        @Override
        public void setChanged() {
            dirtyCalls++;
            super.setChanged();
        }
    }
}
