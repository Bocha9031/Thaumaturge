package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.content.entity.ai.HoldStillGoal;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class PechStateTest {
    @Test
    void nativeConstructionAndWeaponChangesKeepOneCombatGoal() {
        TestPech pech = new TestPech(mock(ServerLevel.class));
        assertEquals(EntityPech.LOOT_SLOTS, pech.loot.size());
        assertFalse(pech.isDomesticated());
        assertEquals(1, pech.combatGoalCount());

        pech.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        pech.refreshCombatGoal();
        assertEquals(1, pech.combatGoalCount());
        assertTrue(pech.hasRangedGoal());

        pech.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
        pech.refreshCombatGoal();
        assertEquals(1, pech.combatGoalCount());
        assertFalse(pech.hasRangedGoal());
    }

    @Test
    void tradeHoldResumesAfterDamageAndOnlyMenuCloseReleasesIt() {
        TestPech pech = new TestPech(mock(ServerLevel.class));
        pech.setDomesticated(true);
        pech.atTradeTable = true;
        pech.setOnGround(true);
        Goal holdGoal = pech.goalSelector.getAvailableGoals().stream()
                .map(wrapped -> wrapped.getGoal())
                .filter(HoldStillGoal.class::isInstance)
                .findFirst()
                .orElseThrow();

        pech.hurtTime = 1;
        assertFalse(holdGoal.canUse());
        pech.hurtTime = 0;
        assertTrue(holdGoal.canUse());
        holdGoal.stop();
        assertTrue(pech.atTradeTable);
    }

    private static RegistryAccess registries() {
        return new RegistryAccess.ImmutableRegistryAccess(
                        BuiltInRegistries.REGISTRY.stream().toList())
                .freeze();
    }

    private static final class TestPech extends EntityPech {
        private TestPech(ServerLevel level) {
            super(TTEntities.PECH.get(), level);
        }

        private long combatGoalCount() {
            return this.goalSelector.getAvailableGoals().stream()
                    .filter(goal ->
                            goal.getGoal() instanceof MeleeAttackGoal || goal.getGoal() instanceof RangedAttackGoal)
                    .count();
        }

        private boolean hasRangedGoal() {
            return this.goalSelector.getAvailableGoals().stream()
                    .anyMatch(goal -> goal.getGoal() instanceof RangedAttackGoal);
        }
    }

    @Test
    void loadoutUsesSourceWeightedBoundaries() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextInt(100))
                .thenReturn(0, 9, 10, 14, 15, 39, 40, 44, 45, 49, 50, 54, 55, 59, 60, 64, 65, 69, 70, 99);

        assertSame(TTItems.PECH_WAND.get(), PechLoadout.roll(random));
        assertSame(TTItems.PECH_WAND.get(), PechLoadout.roll(random));
        assertSame(Items.STONE_SWORD, PechLoadout.roll(random));
        assertSame(Items.STONE_SWORD, PechLoadout.roll(random));
        assertSame(Items.BOW, PechLoadout.roll(random));
        assertSame(Items.BOW, PechLoadout.roll(random));
        assertSame(Items.STONE_AXE, PechLoadout.roll(random));
        assertSame(Items.STONE_AXE, PechLoadout.roll(random));
        assertSame(Items.IRON_SWORD, PechLoadout.roll(random));
        assertSame(Items.IRON_SWORD, PechLoadout.roll(random));
        assertSame(Items.IRON_AXE, PechLoadout.roll(random));
        assertSame(Items.IRON_AXE, PechLoadout.roll(random));
        assertSame(Items.FISHING_ROD, PechLoadout.roll(random));
        assertSame(Items.FISHING_ROD, PechLoadout.roll(random));
        assertSame(Items.STONE_PICKAXE, PechLoadout.roll(random));
        assertSame(Items.STONE_PICKAXE, PechLoadout.roll(random));
        assertSame(Items.IRON_PICKAXE, PechLoadout.roll(random));
        assertSame(Items.IRON_PICKAXE, PechLoadout.roll(random));
        assertSame(Items.AIR, PechLoadout.roll(random));
        assertSame(Items.AIR, PechLoadout.roll(random));
        verify(random, times(20)).nextInt(100);
    }

    @Test
    void hoardMergesThenUsesAnEmptySlotAndReturnsOverflow() {
        List<ItemStack> hoard = PechHoard.empty();
        hoard.set(0, new ItemStack(Items.STONE, 60));

        assertTrue(PechHoard.canStore(hoard, new ItemStack(Items.STONE, 8)));
        assertTrue(PechHoard.store(hoard, new ItemStack(Items.STONE, 8)).isEmpty());
        assertEquals(64, hoard.get(0).getCount());
        assertEquals(4, hoard.get(1).getCount());

        List<ItemStack> full = PechHoard.empty();
        for (int index = 0; index < full.size(); index++) {
            full.set(index, new ItemStack(Items.DIRT, 64));
        }
        assertFalse(PechHoard.canStore(full, new ItemStack(Items.STONE)));
        ItemStack remainder = PechHoard.store(full, new ItemStack(Items.STONE, 3));
        assertEquals(3, remainder.getCount());
    }

    @Test
    void saveWritesNewHoardShapeAndReadsItBack() {
        RegistryAccess registries = registries();
        List<ItemStack> original = PechHoard.empty();
        original.set(2, new ItemStack(Items.GOLD_INGOT, 7));
        CompoundTag saved = new CompoundTag();

        PechHoard.save(saved, original, registries);
        assertTrue(saved.contains("hoard"));
        assertFalse(saved.contains("Items"));
        CompoundTag entry = saved.getList("hoard", Tag.TAG_COMPOUND).getCompound(0);
        assertEquals(2, entry.getByte("Slot"));
        assertEquals("minecraft:gold_ingot", entry.getString("id"));
        assertFalse(entry.contains("slot"));
        assertFalse(entry.contains("stack"));

        List<ItemStack> restored = PechHoard.load(saved, registries);
        assertEquals(7, restored.get(2).getCount());
        assertTrue(restored.get(0).isEmpty());
    }

    @Test
    void loadAcceptsSourceFlatItemStackEntriesUnderHoard() {
        RegistryAccess registries = registries();
        NonNullList<ItemStack> contents = PechHoard.empty();
        contents.set(3, new ItemStack(Items.EMERALD, 6));
        CompoundTag nativeInventory = new CompoundTag();
        ContainerHelper.saveAllItems(nativeInventory, contents, registries);
        CompoundTag source = new CompoundTag();
        source.put("hoard", nativeInventory.getList("Items", Tag.TAG_COMPOUND));

        List<ItemStack> restored = PechHoard.load(source, registries);

        assertEquals(6, restored.get(3).getCount());
        assertTrue(restored.get(0).isEmpty());
    }

    @Test
    void loadAcceptsLegacyItemsSaveShape() {
        RegistryAccess registries = registries();
        CompoundTag legacy = new CompoundTag();
        NonNullList<ItemStack> oldHoard = PechHoard.empty();
        oldHoard.set(4, new ItemStack(Items.EMERALD, 3));
        ContainerHelper.saveAllItems(legacy, oldHoard, registries);

        List<ItemStack> restored = PechHoard.load(legacy, registries);
        assertEquals(3, restored.get(4).getCount());
    }

    @Test
    void savedStateWritesNewKeysAndRoundTrips() {
        RegistryAccess registries = registries();
        List<ItemStack> hoard = PechHoard.empty();
        hoard.set(1, new ItemStack(Items.GOLD_INGOT, 2));
        CompoundTag saved = new CompoundTag();

        PechSavedState.save(saved, registries, hoard, true, 1234, EntityPech.TYPE_STALKER);

        assertTrue(saved.contains("hoard"));
        assertTrue(saved.contains("is_tamed"));
        assertTrue(saved.contains("rage_ticks"));
        assertTrue(saved.contains("variant"));
        PechSavedState.State restored = PechSavedState.load(saved, registries);
        assertTrue(restored.domesticated());
        assertEquals(1234, restored.rageTicks());
        assertEquals(EntityPech.TYPE_STALKER, restored.variant());
        assertEquals(2, restored.loot().get(1).getCount());
    }

    @Test
    void savedStateReadsLegacyEntityKeysAndItems() {
        RegistryAccess registries = registries();
        CompoundTag legacy = new CompoundTag();
        legacy.putBoolean("Tamed", true);
        legacy.putShort("Anger", (short) 320);
        legacy.putByte("PechType", (byte) EntityPech.TYPE_MAGE);
        NonNullList<ItemStack> oldHoard = PechHoard.empty();
        oldHoard.set(5, new ItemStack(Items.EMERALD, 4));
        ContainerHelper.saveAllItems(legacy, oldHoard, registries);

        PechSavedState.State restored = PechSavedState.load(legacy, registries);
        assertTrue(restored.domesticated());
        assertEquals(320, restored.rageTicks());
        assertEquals(EntityPech.TYPE_MAGE, restored.variant());
        assertEquals(4, restored.loot().get(5).getCount());
    }
}
