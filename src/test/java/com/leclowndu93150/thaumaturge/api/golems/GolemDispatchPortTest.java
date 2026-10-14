package com.leclowndu93150.thaumaturge.api.golems;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock("GolemDispatch binding")
class GolemDispatchPortTest {
    private static final DispatchBindings BINDINGS = new DispatchBindings();

    private Level level;
    private AtomicLong gameTime;
    private ApiBinding<?> binding;
    private Field valueField;
    private Object savedBinding;

    @BeforeEach
    void isolateDispatchBinding() throws ReflectiveOperationException {
        Field bindingField = GolemDispatch.class.getDeclaredField("BINDING");
        bindingField.setAccessible(true);
        binding = (ApiBinding<?>) bindingField.get(null);
        valueField = ApiBinding.class.getDeclaredField("value");
        valueField.setAccessible(true);
        savedBinding = valueField.get(binding);
        valueField.set(binding, null);
        GolemDispatch.bind(BINDINGS);

        level = mock(Level.class);
        gameTime = new AtomicLong(20L);
        when(level.isClientSide()).thenReturn(false);
        when(level.getGameTime()).thenAnswer(invocation -> gameTime.get());
        GolemDispatch.supplyQueue(level).clear();
    }

    @AfterEach
    void restoreDispatchBinding() throws ReflectiveOperationException {
        valueField.set(binding, savedBinding);
    }

    @Test
    void foldsOnlyOrdersWithMatchingDestinationComponentsCountAndBatch() {
        BlockPos destination = new BlockPos(4, 5, 6);
        ItemStack original = namedStone(2, "first");

        SupplyOrder first = GolemDispatch.orderSupply(level, destination, Direction.UP, original)
                .orElseThrow();
        original.setCount(1);

        SupplyOrder duplicate = GolemDispatch.orderSupply(level, destination, Direction.UP, namedStone(2, "first"))
                .orElseThrow();
        SupplyOrder separateBatch = GolemDispatch.orderSupply(
                        level, destination, Direction.UP, namedStone(2, "first"), 1)
                .orElseThrow();
        SupplyOrder separateComponents = GolemDispatch.orderSupply(
                        level, destination, Direction.UP, namedStone(2, "second"))
                .orElseThrow();
        SupplyOrder separateCount = GolemDispatch.orderSupply(level, destination, Direction.UP, namedStone(3, "first"))
                .orElseThrow();

        assertSame(first, duplicate);
        assertTrue(first instanceof ProvisionRequest);
        assertSame(GolemDispatch.supplyQueue(level), GolemHelper.getProvisionRequests(level));
        assertEquals(2, first.wanted().getCount());
        assertEquals(4, GolemDispatch.supplyQueue(level).size());
        assertFalse(first.equals(separateBatch));
        assertFalse(first.equals(separateComponents));
        assertFalse(first.equals(separateCount));
    }

    @Test
    void assignmentAndReleaseKeepTaskPointersAndExtendExpiry() {
        SupplyOrder order = GolemDispatch.orderSupply(level, BlockPos.ZERO, Direction.NORTH, new ItemStack(Items.STONE))
                .orElseThrow();
        Task previousTask = Task.atBlock(null, BlockPos.ZERO);
        Task task = Task.atBlock(null, new BlockPos(1, 0, 0));

        assertEquals(220L, order.expiresAt());
        gameTime.set(50L);
        order.assign(previousTask);
        assertSame(order, previousTask.servedOrder());
        order.assign(task);
        assertSame(order, previousTask.servedOrder());
        assertSame(order, task.servedOrder());
        assertSame(task, order.servingTask());
        assertEquals(2450L, order.expiresAt());

        gameTime.set(80L);
        order.release();
        assertNull(task.servedOrder());
        assertNull(order.servingTask());
        assertEquals(2480L, order.expiresAt());
    }

    @Test
    void clientOrdersAreNotQueued() {
        when(level.isClientSide()).thenReturn(true);

        Optional<SupplyOrder> result =
                GolemDispatch.orderSupply(level, BlockPos.ZERO, Direction.UP, new ItemStack(Items.STONE));

        assertTrue(result.isEmpty());
        assertTrue(GolemDispatch.supplyQueue(level).isEmpty());
    }

    @Test
    void queueDropsOldestOrderWhenItExceedsLimit() {
        for (int index = 0; index <= 1000; index++) {
            GolemDispatch.orderSupply(level, new BlockPos(index, 0, 0), Direction.UP, new ItemStack(Items.STONE));
        }

        List<SupplyOrder> queue = GolemDispatch.supplyQueue(level);
        assertEquals(1000, queue.size());
        assertFalse(queue.stream().anyMatch(order -> new BlockPos(0, 0, 0).equals(order.destinationBlock())));
        assertTrue(queue.stream().anyMatch(order -> new BlockPos(1000, 0, 0).equals(order.destinationBlock())));
    }

    private static ItemStack namedStone(int count, String name) {
        ItemStack stack = new ItemStack(Items.STONE, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static final class DispatchBindings implements GolemDispatch.Bindings {
        private final Map<Level, List<SupplyOrder>> queues = new IdentityHashMap<>();

        @Override
        public Optional<SealType> sealType(ResourceLocation id) {
            return Optional.empty();
        }

        @Override
        public ISealEntity sealAt(Level level, SealPos pos) {
            return null;
        }

        @Override
        public void postTask(Level level, Task task) {}

        @Override
        public List<SupplyOrder> supplyQueue(Level level) {
            return queues.computeIfAbsent(level, ignored -> new ArrayList<>());
        }
    }
}
