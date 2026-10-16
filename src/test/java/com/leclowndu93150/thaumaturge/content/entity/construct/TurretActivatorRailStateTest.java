package com.leclowndu93150.thaumaturge.content.entity.construct;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.Test;

class TurretActivatorRailStateTest {
    @Test
    void poweredRailDepartureRestoresAi() throws ReflectiveOperationException {
        Level level = mock(Level.class);
        EntityTurretCrossbow turret = turret(level);
        BlockPos position = turret.blockPosition();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState poweredRail =
                TTBlocks.ACTIVATOR_RAIL.get().defaultBlockState().setValue(BlockStateProperties.POWERED, true);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(air);
        when(level.getBlockState(position)).thenReturn(poweredRail);

        followActivatorRail(turret);

        assertTrue(turret.isNoAi());

        when(level.getBlockState(position)).thenReturn(air);
        followActivatorRail(turret);

        assertFalse(turret.isNoAi());
    }

    @Test
    void unpoweredRailEnablesAi() throws ReflectiveOperationException {
        Level level = mock(Level.class);
        EntityTurretCrossbow turret = turret(level);
        BlockPos position = turret.blockPosition();
        BlockState unpoweredRail =
                TTBlocks.ACTIVATOR_RAIL.get().defaultBlockState().setValue(BlockStateProperties.POWERED, false);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());
        when(level.getBlockState(position)).thenReturn(unpoweredRail);
        turret.setNoAi(true);

        followActivatorRail(turret);

        assertFalse(turret.isNoAi());
    }

    @Test
    void savedRailSwitchRestoresAiAfterLoadingAwayFromRail() throws ReflectiveOperationException {
        Level level = mock(Level.class);
        EntityTurretCrossbow original = turret(level);
        BlockPos position = original.blockPosition();
        BlockState poweredRail =
                TTBlocks.ACTIVATOR_RAIL.get().defaultBlockState().setValue(BlockStateProperties.POWERED, true);
        BlockState air = Blocks.AIR.defaultBlockState();
        when(level.getBlockState(any(BlockPos.class))).thenReturn(air);
        when(level.getBlockState(position)).thenReturn(poweredRail);

        followActivatorRail(original);
        CompoundTag saved = new CompoundTag();
        original.addAdditionalSaveData(saved);
        assertTrue(saved.getBoolean("rail_switched"));

        EntityTurretCrossbow loaded = turret(level);
        loaded.readAdditionalSaveData(saved);
        assertTrue(loaded.isNoAi());
        when(level.getBlockState(position)).thenReturn(air);
        followActivatorRail(loaded);

        assertFalse(loaded.isNoAi());
    }

    @Test
    void leavingRailDoesNotChangeAiWhenRailNeverDisabledIt() throws ReflectiveOperationException {
        Level level = mock(Level.class);
        EntityTurretCrossbow turret = turret(level);
        when(level.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());
        turret.setNoAi(true);

        followActivatorRail(turret);

        assertTrue(turret.isNoAi());
    }

    private static EntityTurretCrossbow turret(Level level) {
        return new EntityTurretCrossbow(TTEntities.TURRET_CROSSBOW.get(), level);
    }

    private static void followActivatorRail(EntityTurretCrossbow turret) throws ReflectiveOperationException {
        Method method = EntityTurretCrossbow.class.getDeclaredMethod("followActivatorRail");
        method.setAccessible(true);
        method.invoke(turret);
    }
}
