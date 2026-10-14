package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** Static facade for seal lookup, task posting, and golem supply orders. */
public final class GolemDispatch {
    private static final int SUPPLY_QUEUE_LIMIT = 1000;
    private static final int NO_BATCH = 0;
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("GolemDispatch");

    private GolemDispatch() {}

    public static void bind(Bindings bindings) {
        BINDING.bind(bindings);
    }

    public static Optional<SealType> sealType(ResourceLocation id) {
        return BINDING.get().sealType(id);
    }

    public static ItemStack sealItem(ResourceLocation id) {
        return sealType(id).map(type -> new ItemStack(type.placer())).orElse(ItemStack.EMPTY);
    }

    public static Optional<ISealEntity> sealAt(Level level, SealPos pos) {
        return Optional.ofNullable(BINDING.get().sealAt(level, pos));
    }

    public static void postTask(Level level, Task task) {
        if (!level.isClientSide()) {
            BINDING.get().postTask(level, task);
        }
    }

    public static Optional<SupplyOrder> orderSupply(Level level, ISealEntity seal, ItemStack stack) {
        return enqueue(level, SupplyOrder.toSeal(seal), stack, NO_BATCH);
    }

    public static Optional<SupplyOrder> orderSupply(Level level, BlockPos pos, Direction face, ItemStack stack) {
        return orderSupply(level, pos, face, stack, NO_BATCH);
    }

    public static Optional<SupplyOrder> orderSupply(
            Level level, BlockPos pos, Direction face, ItemStack stack, int batch) {
        return enqueue(level, SupplyOrder.toBlock(pos, face), stack, batch);
    }

    public static Optional<SupplyOrder> orderSupply(Level level, Entity entity, ItemStack stack) {
        return orderSupply(level, entity, stack, NO_BATCH);
    }

    public static Optional<SupplyOrder> orderSupply(Level level, Entity entity, ItemStack stack, int batch) {
        return enqueue(level, SupplyOrder.toEntity(entity), stack, batch);
    }

    public static List<SupplyOrder> supplyQueue(Level level) {
        return BINDING.get().supplyQueue(level);
    }

    @SuppressWarnings("removal")
    private static Optional<SupplyOrder> enqueue(
            Level level, SupplyOrder.Destination destination, ItemStack stack, int batch) {
        if (level.isClientSide()) {
            return Optional.empty();
        }
        SupplyOrder order = new ProvisionRequest(level, stack, destination, batch);
        List<SupplyOrder> queue = supplyQueue(level);
        for (SupplyOrder queued : queue) {
            if (queued.equals(order)) {
                return Optional.of(queued);
            }
        }
        int overflow = queue.size() - SUPPLY_QUEUE_LIMIT + 1;
        for (int index = 0; index < overflow && !queue.isEmpty(); index++) {
            queue.remove(0);
        }
        queue.add(order);
        return Optional.of(order);
    }

    public interface Bindings {
        Optional<SealType> sealType(ResourceLocation id);

        @Nullable
        ISealEntity sealAt(Level level, SealPos pos);

        void postTask(Level level, Task task);

        List<SupplyOrder> supplyQueue(Level level);
    }
}
