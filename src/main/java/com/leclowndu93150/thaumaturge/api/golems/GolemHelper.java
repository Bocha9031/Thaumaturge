package com.leclowndu93150.thaumaturge.api.golems;

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

/** @deprecated use {@link GolemDispatch}; this class is removed in 2.0.0. */
@Deprecated(since = "1.1.0", forRemoval = true)
public final class GolemHelper {
    private GolemHelper() {}

    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void bind(Bindings bindings) {
        GolemDispatch.bind(new LegacyDispatchBindings(bindings));
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public static Optional<SealType> sealType(ResourceLocation id) {
        return GolemDispatch.sealType(id);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public static ItemStack getSealStack(ResourceLocation id) {
        return GolemDispatch.sealItem(id);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public static @Nullable ISealEntity getSealEntity(Level level, @Nullable SealPos pos) {
        return pos == null ? null : GolemDispatch.sealAt(level, pos).orElse(null);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void addGolemTask(Level level, Task task) {
        GolemDispatch.postTask(level, task);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void requestProvisioning(Level level, ISealEntity seal, ItemStack stack) {
        GolemDispatch.orderSupply(level, seal, stack);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void requestProvisioning(Level level, BlockPos pos, Direction side, ItemStack stack) {
        GolemDispatch.orderSupply(level, pos, side, stack);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void requestProvisioning(Level level, Entity entity, ItemStack stack) {
        GolemDispatch.orderSupply(level, entity, stack);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void requestProvisioning(Level level, BlockPos pos, Direction side, ItemStack stack, int batch) {
        GolemDispatch.orderSupply(level, pos, side, stack, batch);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void requestProvisioning(Level level, Entity entity, ItemStack stack, int batch) {
        GolemDispatch.orderSupply(level, entity, stack, batch);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    @SuppressWarnings("unchecked")
    public static List<ProvisionRequest> getProvisionRequests(Level level) {
        return (List<ProvisionRequest>) (List<?>) GolemDispatch.supplyQueue(level);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public interface Bindings {
        Optional<SealType> sealType(ResourceLocation id);

        @Nullable
        ISealEntity getSealEntity(Level level, @Nullable SealPos pos);

        void addGolemTask(Level level, Task task);

        List<ProvisionRequest> getProvisionRequests(Level level);
    }
}
