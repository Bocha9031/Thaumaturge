package com.leclowndu93150.thaumaturge.content.device;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.junit.jupiter.api.Test;

class EverfullUrnCutoffTest {
    private static final int OWN_CELL = 37;

    @Test
    void preservesExistingTankAndHandlerDataWhileRejectingExternalFluidInsertion() {
        HolderLookup.Provider registries = net.minecraft.core.RegistryAccess.EMPTY;
        var urn = new BlockEntityEverfullUrn(
                BlockPos.ZERO, TTBlocks.EVERFULL_URN.get().defaultBlockState());
        FluidStack water = new FluidStack(Fluids.WATER, 500);

        assertEquals(0, urn.getTank().fill(water, IFluidHandler.FluidAction.EXECUTE));
        assertEquals(0, urn.waterAmount());

        FluidTank oldTank = new FluidTank(BlockEntityEverfullUrn.CAPACITY);
        oldTank.fill(water, IFluidHandler.FluidAction.EXECUTE);
        CompoundTag oldSave = new CompoundTag();
        oldSave.put("Tank", oldTank.writeToNBT(registries, new CompoundTag()));
        oldSave.putIntArray("Handlers", new int[] {0, OWN_CELL, 74});

        urn.loadAdditional(oldSave, registries);

        assertEquals(500, urn.waterAmount());
        CompoundTag newSave = new CompoundTag();
        urn.saveAdditional(newSave, registries);
        assertEquals(oldSave.getCompound("Tank"), newSave.getCompound("Tank"));
        assertArrayEquals(new int[] {0, 74}, newSave.getIntArray("Handlers"));
    }
}
