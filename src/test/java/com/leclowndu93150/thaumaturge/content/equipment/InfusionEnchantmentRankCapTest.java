package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

class InfusionEnchantmentRankCapTest {
    @Test
    void arcingUsesDeclaredMaximumForReach() {
        Player attacker = mock(Player.class);
        ServerLevel level = mock(ServerLevel.class);
        LivingEntity target = mock(LivingEntity.class);
        ItemStack weapon = stackWithLevel(InfusionEnchantment.ARCING, Integer.MAX_VALUE);
        AABB targetBounds = new AABB(1.0, 2.0, 3.0, 2.0, 4.0, 5.0);
        when(attacker.level()).thenReturn(level);
        when(attacker.getMainHandItem()).thenReturn(weapon);
        when(level.isClientSide()).thenReturn(false);
        when(target.isAlive()).thenReturn(true);
        when(target.getBoundingBox()).thenReturn(targetBounds);
        when(level.getEntitiesOfClass(eq(Mob.class), any(AABB.class), any())).thenReturn(List.of());
        AttackEntityEvent event = mock(AttackEntityEvent.class);
        when(event.getEntity()).thenReturn(attacker);
        when(event.getTarget()).thenReturn(target);

        InfusionEnchantmentEvents.onAttack(event);

        verify(level).getEntitiesOfClass(eq(Mob.class), eq(targetBounds.inflate(5.5, 3.0, 5.5)), any());
    }

    @Test
    void arcingHitsNearestValidTargetsFirst() {
        Player attacker = mock(Player.class);
        ServerLevel level = mock(ServerLevel.class);
        LivingEntity target = mock(LivingEntity.class);
        Mob farther = mock(Mob.class);
        Mob nearer = mock(Mob.class);
        DamageSource damageSource = mock(DamageSource.class);
        DamageSources damageSources = mock(DamageSources.class);
        RandomSource random = mock(RandomSource.class);
        ItemStack weapon = stackWithLevel(InfusionEnchantment.ARCING, 1);
        AABB targetBounds = new AABB(-1.0, -1.0, -1.0, 1.0, 1.0, 1.0);
        when(attacker.level()).thenReturn(level);
        when(attacker.getMainHandItem()).thenReturn(weapon);
        when(attacker.getRootVehicle()).thenReturn(attacker);
        when(attacker.getAttributeValue(Attributes.ATTACK_DAMAGE)).thenReturn(10.0);
        when(level.isClientSide()).thenReturn(false);
        when(level.getRandom()).thenReturn(random);
        when(level.damageSources()).thenReturn(damageSources);
        when(damageSources.playerAttack(attacker)).thenReturn(damageSource);
        when(target.isAlive()).thenReturn(true);
        when(target.getId()).thenReturn(99);
        when(target.getBoundingBox()).thenReturn(targetBounds);
        when(target.getX()).thenReturn(0.0);
        when(target.getY()).thenReturn(0.0);
        when(target.getZ()).thenReturn(0.0);
        when(farther.getId()).thenReturn(2);
        when(farther.isAlive()).thenReturn(true);
        when(farther.getRootVehicle()).thenReturn(farther);
        when(farther.getX()).thenReturn(8.0);
        when(farther.getY()).thenReturn(0.0);
        when(farther.getZ()).thenReturn(0.0);
        when(nearer.getId()).thenReturn(1);
        when(nearer.isAlive()).thenReturn(true);
        when(nearer.getRootVehicle()).thenReturn(nearer);
        when(nearer.getX()).thenReturn(1.0);
        when(nearer.getY()).thenReturn(0.0);
        when(nearer.getZ()).thenReturn(0.0);
        when(farther.distanceToSqr(target)).thenReturn(64.0);
        when(nearer.distanceToSqr(target)).thenReturn(1.0);
        when(farther.hurt(damageSource, 5.0F)).thenReturn(true);
        when(nearer.hurt(damageSource, 5.0F)).thenReturn(true);
        when(level.getEntitiesOfClass(eq(Mob.class), any(AABB.class), any())).thenReturn(List.of(farther, nearer));
        AttackEntityEvent event = mock(AttackEntityEvent.class);
        when(event.getEntity()).thenReturn(attacker);
        when(event.getTarget()).thenReturn(target);

        try (MockedStatic<EnchantmentHelper> enchantments = mockStatic(EnchantmentHelper.class);
                MockedStatic<Effects> effects = mockStatic(Effects.class)) {
            InfusionEnchantmentEvents.onAttack(event);

            verify(nearer).hurt(damageSource, 5.0F);
            verify(farther, never()).hurt(any(DamageSource.class), anyFloat());
        }
    }

    @Test
    void soundingPassesDeclaredMaximumToScanner() {
        ServerPlayer player = mock(ServerPlayer.class);
        ServerLevel level = mock(ServerLevel.class);
        ItemStack tool = stackWithLevel(InfusionEnchantment.SOUNDING, Integer.MAX_VALUE);
        BlockPos pos = new BlockPos(2, 3, 4);
        when(player.isShiftKeyDown()).thenReturn(true);
        when(player.getItemInHand(InteractionHand.MAIN_HAND)).thenReturn(tool);
        when(level.isClientSide()).thenReturn(false);
        when(level.getRandom()).thenReturn(mock(RandomSource.class));
        PlayerInteractEvent.RightClickBlock event = mock(PlayerInteractEvent.RightClickBlock.class);
        when(event.getEntity()).thenReturn(player);
        when(event.getLevel()).thenReturn(level);
        when(event.getHand()).thenReturn(InteractionHand.MAIN_HAND);
        when(event.getPos()).thenReturn(pos);

        try (MockedStatic<SoundingScan> scanner = mockStatic(SoundingScan.class)) {
            InfusionEnchantmentEvents.onRightClickBlock(event);

            scanner.verify(() -> SoundingScan.perform(level, player, pos, InfusionEnchantment.SOUNDING.maxLevel()));
            verify(event).setCanceled(true);
            verify(event).setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    @Test
    void maximumRefiningRankDoesNotOverflowItsChance() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextDouble()).thenReturn(0.5);
        ServerLevel level = mock(ServerLevel.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        when(level.getRandom()).thenReturn(random);
        BlockState state = mock(BlockState.class);
        Player player = mock(Player.class);
        ItemStack tool = stackWithLevel(InfusionEnchantment.REFINING, Integer.MAX_VALUE);
        when(tool.isCorrectToolForDrops(state)).thenReturn(true);
        ItemEntity drop = mock(ItemEntity.class);
        ItemStack originalDrop = new ItemStack(Items.RAW_IRON);
        when(drop.getItem()).thenReturn(originalDrop);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        when(player.getMainHandItem()).thenReturn(tool);
        BlockDropsEvent event = blockDrops(level, state, player, new ArrayList<>(List.of(drop)));

        try (MockedStatic<RefiningResults> refiningResults = mockStatic(RefiningResults.class)) {
            refiningResults.when(() -> RefiningResults.clusterFor(state)).thenReturn(Items.IRON_INGOT);

            InfusionEnchantmentEvents.onBlockDrops(event);
        }

        verify(drop).setItem(argThat(stack -> stack.is(Items.IRON_INGOT) && stack.getCount() == 1));
    }

    @Test
    void refiningAppliesToMatchingDropsWithoutASecondToolGate() {
        RandomSource random = mock(RandomSource.class);
        when(random.nextDouble()).thenReturn(0.1);
        ServerLevel level = mock(ServerLevel.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        when(level.getRandom()).thenReturn(random);
        BlockState state = mock(BlockState.class);
        Player player = mock(Player.class);
        ItemStack tool = stackWithLevel(InfusionEnchantment.REFINING, 1);
        when(tool.isCorrectToolForDrops(state)).thenReturn(false);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        when(player.getMainHandItem()).thenReturn(tool);
        ItemEntity drop = mock(ItemEntity.class);
        when(drop.getItem()).thenReturn(new ItemStack(Items.RAW_IRON));
        BlockDropsEvent event = blockDrops(level, state, player, new ArrayList<>(List.of(drop)));

        try (MockedStatic<RefiningResults> refiningResults = mockStatic(RefiningResults.class)) {
            refiningResults.when(() -> RefiningResults.clusterFor(state)).thenReturn(Items.IRON_INGOT);

            InfusionEnchantmentEvents.onBlockDrops(event);
        }

        verify(drop).setItem(argThat(stack -> stack.is(Items.IRON_INGOT) && stack.getCount() == 1));
    }

    @Test
    void essenceUsesDeclaredMaximumForGeneratedCrystals() {
        RandomSource random = mock(RandomSource.class);
        AtomicInteger calls = new AtomicInteger();
        when(random.nextInt(anyInt())).thenAnswer(invocation -> {
            int call = calls.getAndIncrement();
            return call == 0 || call % 2 == 1 ? 0 : 1;
        });
        ServerLevel level = mock(ServerLevel.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
        when(level.isClientSide()).thenReturn(false);
        when(level.getRandom()).thenReturn(random);
        Player killer = mock(Player.class);
        LivingEntity victim = mock(LivingEntity.class);
        ItemStack weapon = stackWithLevel(InfusionEnchantment.ESSENCE, Integer.MAX_VALUE);
        DamageSource damageSource = mock(DamageSource.class);
        when(killer.level()).thenReturn(level);
        when(killer.getMainHandItem()).thenReturn(weapon);
        when(damageSource.getEntity()).thenReturn(killer);
        LivingDropsEvent event = mock(LivingDropsEvent.class);
        when(event.getSource()).thenReturn(damageSource);
        when(event.getEntity()).thenReturn(victim);
        List<ItemEntity> drops = new ArrayList<>();
        when(event.getDrops()).thenReturn(drops);

        List<AspectInstance> entries = new ArrayList<>();
        for (int index = 0; index < 6; index++) {
            Holder<IAspect> holder = Holder.direct(mock(IAspect.class));
            entries.add(new AspectInstance(holder, 1));
        }
        AspectList aspects = AspectList.ofEntries(entries);

        try (MockedStatic<EntityAspects> entityAspects = mockStatic(EntityAspects.class);
                MockedStatic<EssentiaCrystalFactory> crystals = mockStatic(EssentiaCrystalFactory.class);
                MockedConstruction<ItemEntity> generated = mockConstruction(ItemEntity.class)) {
            entityAspects.when(() -> EntityAspects.of(victim)).thenReturn(aspects);
            for (AspectInstance entry : entries) {
                crystals.when(() -> EssentiaCrystalFactory.of(entry.aspect()))
                        .thenReturn(new ItemStack(Items.AMETHYST_SHARD));
            }

            InfusionEnchantmentEvents.onLivingDrops(event);

            assertEquals(
                    InfusionEnchantment.ESSENCE.maxLevel(),
                    generated.constructed().size());
        }
    }

    @Test
    void essenceSkipDoesNotDiscardUnselectedAspects() {
        RandomSource random = mock(RandomSource.class);
        AtomicInteger calls = new AtomicInteger();
        when(random.nextInt(anyInt())).thenAnswer(invocation -> switch (calls.getAndIncrement()) {
            case 0, 1, 2, 3, 4, 6 -> 0;
            default -> 1;
        });
        ServerLevel level = mock(ServerLevel.class);
        when(level.isClientSide()).thenReturn(false);
        when(level.getRandom()).thenReturn(random);
        Player killer = mock(Player.class);
        LivingEntity victim = mock(LivingEntity.class);
        ItemStack weapon = stackWithLevel(InfusionEnchantment.ESSENCE, InfusionEnchantment.ESSENCE.maxLevel());
        DamageSource damageSource = mock(DamageSource.class);
        when(killer.level()).thenReturn(level);
        when(killer.getMainHandItem()).thenReturn(weapon);
        when(victim.level()).thenReturn(level);
        when(damageSource.getEntity()).thenReturn(killer);
        LivingDropsEvent event = mock(LivingDropsEvent.class);
        when(event.getSource()).thenReturn(damageSource);
        when(event.getEntity()).thenReturn(victim);
        List<ItemEntity> drops = new ArrayList<>();
        when(event.getDrops()).thenReturn(drops);

        AspectInstance aspect = new AspectInstance(Holder.direct(mock(IAspect.class)), 3);
        AspectList aspects = AspectList.ofEntries(List.of(aspect));
        try (MockedStatic<EntityAspects> entityAspects = mockStatic(EntityAspects.class);
                MockedStatic<EssentiaCrystalFactory> crystals = mockStatic(EssentiaCrystalFactory.class);
                MockedConstruction<ItemEntity> generated = mockConstruction(ItemEntity.class)) {
            entityAspects.when(() -> EntityAspects.of(victim)).thenReturn(aspects);
            crystals.when(() -> EssentiaCrystalFactory.of(aspect.aspect()))
                    .thenReturn(new ItemStack(Items.AMETHYST_SHARD));

            InfusionEnchantmentEvents.onLivingDrops(event);

            assertEquals(3, generated.constructed().size());
        }
    }

    private static ItemStack stackWithLevel(InfusionEnchantment enchantment, int level) {
        ItemStack stack = new ItemStack(Items.IRON_PICKAXE);
        Map<InfusionEnchantment, Integer> levels = new EnumMap<>(InfusionEnchantment.class);
        levels.put(enchantment, level);
        stack.set(TTDataComponents.INFUSION_ENCHANTMENTS.get(), new InfusionEnchantments(levels));
        return stack;
    }

    private static BlockDropsEvent blockDrops(
            ServerLevel level, BlockState state, Player breaker, List<ItemEntity> drops) {
        BlockDropsEvent event = mock(BlockDropsEvent.class);
        when(event.getLevel()).thenReturn(level);
        when(event.getState()).thenReturn(state);
        when(event.getPos()).thenReturn(BlockPos.ZERO);
        when(event.getBreaker()).thenReturn(breaker);
        when(event.getDrops()).thenReturn(drops);
        return event;
    }
}
