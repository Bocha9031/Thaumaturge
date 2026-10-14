package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/** @deprecated use {@link SupplyOrder}, placed through {@link GolemDispatch}. */
@Deprecated(since = "1.1.0", forRemoval = true)
public final class ProvisionRequest extends SupplyOrder {
    private int legacyId;

    ProvisionRequest(Level level, ItemStack stack, Destination destination, int batch) {
        super(level, stack, destination, batch);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public long getTimeout() {
        return expiresAt();
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void extendTimeout() {
        prolong();
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public int getId() {
        return legacyId;
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setId(int value) {
        legacyId = value;
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setBatch(int value) {
        rebatch(value);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setUI(int batch) {
        rebatch(batch);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public @Nullable ISealEntity getSeal() {
        return requestingSeal();
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public @Nullable Entity getEntity() {
        return recipient();
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public ItemStack getStack() {
        return wanted();
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public @Nullable BlockPos getPos() {
        return destinationBlock();
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setPos(@Nullable BlockPos newPos) {
        moveBlock(newPos);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public @Nullable Direction getSide() {
        return destinationFace();
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setSide(@Nullable Direction newSide) {
        moveFace(newSide);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public @Nullable Task getLinkedTask() {
        return servingTask();
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void link(Task task) {
        assign(task);
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void unlink() {
        release();
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setLinkedTask(@Nullable Task task) {
        pointServingTask(task);
        prolong();
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public boolean isInvalid() {
        return isSpent();
    }

    @Deprecated(since = "1.1.0", forRemoval = true)
    public void setInvalid(boolean invalid) {
        if (invalid) {
            markSpent();
        } else {
            clearSpent();
        }
    }
}
