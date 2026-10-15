package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class InfusionDestructiveSwingMemoryTest {
    @AfterEach
    void clearRememberedSwings() {
        InfusionEnchantmentEvents.resetSession();
    }

    @Test
    void nullFaceClickForgetsPreviousDestructiveSwing() throws ReflectiveOperationException {
        InfusionEnchantmentEvents.resetSession();
        UUID playerId = UUID.randomUUID();
        BlockPos pos = new BlockPos(3, 4, 5);
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        ItemStack held = mock(ItemStack.class);
        BlockState state = mock(BlockState.class);
        when(level.isClientSide()).thenReturn(false);
        when(level.getBlockState(pos)).thenReturn(state);
        when(player.level()).thenReturn(level);
        when(player.getUUID()).thenReturn(playerId);
        when(player.isShiftKeyDown()).thenReturn(false);
        when(player.getMainHandItem()).thenReturn(held);
        when(held.isCorrectToolForDrops(state)).thenReturn(true);

        try (MockedStatic<InfusionEnchantmentHelper> enchantments = mockStatic(InfusionEnchantmentHelper.class)) {
            enchantments
                    .when(() -> InfusionEnchantmentHelper.has(held, InfusionEnchantment.DESTRUCTIVE))
                    .thenReturn(true);

            PlayerInteractEvent.LeftClickBlock validClick =
                    new PlayerInteractEvent.LeftClickBlock(player, pos, Direction.NORTH, Action.START);
            InfusionEnchantmentEvents.onLeftClickBlock(validClick);
            assertTrue(destructiveTargets().containsKey(playerId));

            PlayerInteractEvent.LeftClickBlock nullFaceClick =
                    new PlayerInteractEvent.LeftClickBlock(player, pos, null, Action.START);
            InfusionEnchantmentEvents.onLeftClickBlock(nullFaceClick);

            assertFalse(destructiveTargets().containsKey(playerId));
        }
    }

    private static Map<?, ?> destructiveTargets() throws ReflectiveOperationException {
        Field targetsField = InfusionEnchantmentEvents.class.getDeclaredField("DESTRUCTIVE_TARGETS");
        targetsField.setAccessible(true);
        Object targets = targetsField.get(null);
        return (Map<?, ?>) targets;
    }
}
