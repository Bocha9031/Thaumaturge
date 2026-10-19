package com.leclowndu93150.thaumaturge.content.entity.champion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.content.entity.champion.trait.WardedChampionTrait;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.junit.jupiter.api.Test;

class ChampionParityTest {
    @Test
    void championThresholdTracksDifficultySpookyStateAndWhitelistWeight() {
        assertEquals(0, ChampionEvents.rollModifier(true, Difficulty.NORMAL, false, 1, false));
        assertEquals(-2, ChampionEvents.rollModifier(true, Difficulty.EASY, false, 1, false));
        assertEquals(2, ChampionEvents.rollModifier(true, Difficulty.HARD, false, 1, false));
        assertEquals(2, ChampionEvents.rollModifier(true, Difficulty.NORMAL, true, 1, false));
        assertEquals(-2, ChampionEvents.rollModifier(false, Difficulty.NORMAL, false, 1, false));
        assertEquals(-1, ChampionEvents.rollModifier(false, Difficulty.NORMAL, true, 1, false));
        assertEquals(-1, ChampionEvents.rollModifier(false, Difficulty.NORMAL, false, 2, true));
    }

    @Test
    void wardRechargeClampsToTheFlooredHalfHealthCap() {
        LivingEntity mob = mock(LivingEntity.class);
        when(mob.getAttributeBaseValue(Attributes.MAX_HEALTH)).thenReturn(21.0);
        when(mob.getAbsorptionAmount()).thenReturn(9.75F);

        new WardedChampionTrait().tick(mob);

        verify(mob).setAbsorptionAmount(10.0F);
    }

    @Test
    void championRewardsRequireARealPlayerKiller() {
        LivingEntity victim = mock(LivingEntity.class);
        Player player = mock(Player.class);
        when(victim.getKillCredit()).thenReturn(player);
        assertSame(player, ChampionEvents.eligibleKiller(victim));

        when(victim.getKillCredit()).thenReturn(mock(FakePlayer.class));
        assertNull(ChampionEvents.eligibleKiller(victim));

        when(victim.getKillCredit()).thenReturn(null);
        assertNull(ChampionEvents.eligibleKiller(victim));
    }
}
