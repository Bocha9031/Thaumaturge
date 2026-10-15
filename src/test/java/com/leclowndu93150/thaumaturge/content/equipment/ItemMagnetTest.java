package com.leclowndu93150.thaumaturge.content.equipment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class ItemMagnetTest {
    private static Tool.Rule ruleFor(Tool tool, TagKey<Block> tag) {
        return tool.rules().stream()
                .filter(rule -> rule.blocks().unwrapKey().filter(tag::equals).isPresent())
                .findFirst()
                .orElseThrow();
    }

    @Test
    void pullsAlongNormalizedOffsetAddsLiftAndClampsEachAxis() {
        Vec3 velocity = new Vec3(0.1, 0.0, 0.0);
        Vec3 position = new Vec3(3.0, 4.0, 0.0);

        Vec3 result = ItemMagnet.pull(velocity, position, Vec3.ZERO);

        assertEquals(-0.08, result.x, 1.0E-12);
        assertEquals(-0.14, result.y, 1.0E-12);
        assertEquals(0.0, result.z, 1.0E-12);
        Vec3 capped = ItemMagnet.pull(new Vec3(-0.25, -0.25, 0.0), new Vec3(1.0, 0.0, 0.0), Vec3.ZERO);
        assertEquals(-0.25, capped.x, 1.0E-12);
        assertEquals(-0.15, capped.y, 1.0E-12);
    }

    @Test
    void zeroDistanceReturnsVelocityWithoutNaN() {
        Vec3 velocity = new Vec3(0.1, -0.2, 0.25);

        Vec3 result = ItemMagnet.pull(velocity, new Vec3(2.0, 3.0, 4.0), new Vec3(2.0, 3.0, 4.0));

        assertEquals(velocity, result);
    }

    @Test
    void registeredElementalAxeRetainsNativeToolMaterialProperties() {
        ElementalAxeItem axe = TTItems.ELEMENTAL_AXE.get();
        ItemStack stack = new ItemStack(axe);
        Tool tool = stack.get(DataComponents.TOOL);

        assertNotNull(tool);
        Tool.Rule axeRule = ruleFor(tool, BlockTags.MINEABLE_WITH_AXE);
        assertEquals(9.0F, axeRule.speed().orElseThrow());
        assertTrue(axeRule.correctForDrops().orElseThrow());
        Tool.Rule restriction = ruleFor(tool, BlockTags.INCORRECT_FOR_DIAMOND_TOOL);
        assertFalse(restriction.correctForDrops().orElseThrow());
        assertTrue(restriction.speed().isEmpty());
        assertEquals(1500, stack.getMaxDamage());
        assertEquals(1, stack.getMaxStackSize());
        assertEquals(18, axe.getEnchantmentValue());
        assertTrue(axe.canDisableShield(stack, ItemStack.EMPTY, null, null));
    }

    @Test
    void registeredElementalAxeUsesItsThaumiumRepairIngredient() {
        ElementalAxeItem axe = TTItems.ELEMENTAL_AXE.get();
        ItemStack stack = new ItemStack(axe);
        Ingredient thaumiumIngot = Ingredient.of(TTItems.INGOT_THAUMIUM.get());

        try (MockedStatic<Ingredient> ingredients = mockStatic(Ingredient.class, CALLS_REAL_METHODS)) {
            ingredients.when(() -> Ingredient.of(TTItemTags.INGOTS_THAUMIUM)).thenReturn(thaumiumIngot);

            assertTrue(axe.isValidRepairItem(stack, new ItemStack(TTItems.INGOT_THAUMIUM.get())));
            assertFalse(axe.isValidRepairItem(stack, new ItemStack(Blocks.DIRT.asItem())));
        }
    }

    @Test
    void registeredAxeSuccessfulHitDamagesItByTwo() {
        ElementalAxeItem axe = TTItems.ELEMENTAL_AXE.get();
        ItemStack stack = new ItemStack(axe);
        ServerLevel level = mock(ServerLevel.class);
        Player attacker = mock(Player.class);
        LivingEntity target = mock(LivingEntity.class);
        when(attacker.level()).thenReturn(level);
        when(attacker.hasInfiniteMaterials()).thenReturn(false);

        assertTrue(stack.hurtEnemy(target, attacker));
        stack.postHurtEnemy(target, attacker);

        assertEquals(2, stack.getDamageValue());
    }
}
