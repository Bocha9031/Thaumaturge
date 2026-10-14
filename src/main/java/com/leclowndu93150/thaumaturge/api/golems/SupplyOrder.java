package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** A queued order for golems to bring an item stack to a seal, block face, or entity. */
public class SupplyOrder {
    private static final long UNCLAIMED_LIFETIME_TICKS = 200L;
    private static final long ASSIGNED_LIFETIME_TICKS = 2400L;

    private final Level level;
    private final ItemStack wanted;
    private Destination destination;
    private int batch;
    private @Nullable Task servingTask;
    private long expiresAt;
    private boolean spent;

    SupplyOrder(Level level, ItemStack wanted, Destination destination, int batch) {
        this.level = level;
        this.wanted = wanted.copy();
        this.destination = destination;
        this.batch = batch;
        this.expiresAt = level.getGameTime() + UNCLAIMED_LIFETIME_TICKS;
    }

    static Destination toSeal(ISealEntity seal) {
        return new Destination(seal, null, null, null);
    }

    static Destination toBlock(BlockPos pos, Direction face) {
        return new Destination(null, pos.immutable(), face, null);
    }

    static Destination toEntity(Entity entity) {
        return new Destination(null, null, null, entity);
    }

    public ItemStack wanted() {
        return wanted;
    }

    public @Nullable ISealEntity requestingSeal() {
        return destination.seal();
    }

    public @Nullable BlockPos destinationBlock() {
        return destination.pos();
    }

    public @Nullable Direction destinationFace() {
        return destination.face();
    }

    public @Nullable Entity recipient() {
        return destination.entity();
    }

    public int batch() {
        return batch;
    }

    public long expiresAt() {
        return expiresAt;
    }

    public @Nullable Task servingTask() {
        return servingTask;
    }

    public void assign(Task task) {
        servingTask = task;
        task.serveOrder(this);
        prolong();
    }

    public void release() {
        Task served = servingTask;
        if (served != null && served.servedOrder() == this) {
            served.serveOrder(null);
        }
        servingTask = null;
        prolong();
    }

    public boolean isSpent() {
        return spent;
    }

    public void markSpent() {
        spent = true;
    }

    void prolong() {
        expiresAt = level.getGameTime() + ASSIGNED_LIFETIME_TICKS;
    }

    void pointServingTask(@Nullable Task task) {
        servingTask = task;
    }

    void rebatch(int number) {
        batch = number;
    }

    void moveBlock(@Nullable BlockPos pos) {
        destination = new Destination(destination.seal(), pos, destination.face(), destination.entity());
    }

    void moveFace(@Nullable Direction face) {
        destination = new Destination(destination.seal(), destination.pos(), face, destination.entity());
    }

    void clearSpent() {
        spent = false;
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || other instanceof SupplyOrder order
                        && batch == order.batch
                        && wanted.getCount() == order.wanted.getCount()
                        && destination.sameAs(order.destination)
                        && ItemStack.isSameItemSameComponents(wanted, order.wanted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                destination.identityHash(),
                destination.face(),
                batch,
                wanted.getCount(),
                ItemStack.hashItemAndComponents(wanted));
    }

    record Destination(
            @Nullable ISealEntity seal,
            @Nullable BlockPos pos,
            @Nullable Direction face,
            @Nullable Entity entity) {
        boolean sameAs(Destination other) {
            if (seal != null || other.seal != null) {
                return seal != null && other.seal != null && seal.pos().equals(other.seal.pos());
            }
            if (entity != null || other.entity != null) {
                return entity == other.entity;
            }
            return face == other.face && Objects.equals(pos, other.pos);
        }

        int identityHash() {
            if (seal != null) {
                return seal.pos().hashCode();
            }
            if (entity != null) {
                return System.identityHashCode(entity);
            }
            return Objects.hashCode(pos);
        }
    }
}
