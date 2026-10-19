package com.leclowndu93150.thaumaturge.content.equipment.bauble;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.MockedStatic;

class BaubleEventsTest {
    @Test
    void cancelsDeathBeforeApplyingUndyingEffects() {
        ServerPlayer player = mock(ServerPlayer.class);
        Level level = mock(Level.class);
        LivingDeathEvent event = mock(LivingDeathEvent.class);
        ItemStack charm = new ItemStack(Items.POPPY);
        ModList modList = mock(ModList.class);
        when(event.getEntity()).thenReturn(player);
        when(player.level()).thenReturn(level);
        when(modList.isLoaded(TTIds.CURIOS)).thenReturn(true);

        try (MockedStatic<ModList> mods = mockStatic(ModList.class);
                MockedStatic<ThaumaturgeCuriosCompat> curios = mockStatic(ThaumaturgeCuriosCompat.class)) {
            mods.when(ModList::get).thenReturn(modList);
            curios.when(() -> ThaumaturgeCuriosCompat.extractCurio(player, TTItems.CHARM_UNDYING.get()))
                    .thenReturn(charm);

            BaubleEvents.onDeath(event);

            InOrder order = inOrder(event, player);
            order.verify(event).setCanceled(true);
            order.verify(player).awardStat(Stats.ITEM_USED.get(Items.TOTEM_OF_UNDYING));
            order.verify(player).setHealth(1.0F);
        }
    }
}
