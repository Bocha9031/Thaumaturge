package com.leclowndu93150.thaumaturge.api.items;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Static accessor for the vis charge of rechargeable stacks.
 *
 * <p>A stack is rechargeable when it carries a {@link ChargeProfile} in the {@code thaumaturge:rechargeable} component. Its charge is
 * the {@code thaumaturge:charge} component. Every mutator changes the given stack in place. Draining the aura is server-authoritative:
 * call {@link #drawFromAura} on the logical server only.
 *
 * @since 1.0.0
 */
public final class RechargeAccess {
    /**
     * The value the legacy query methods return for a stack that is not rechargeable.
     *
     * @deprecated use {@link #isRechargeable(ItemStack)} instead
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static final int NOT_RECHARGEABLE = -1;

    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("RechargeAccess");
    private static final int NO_CHARGE = 0;

    private RechargeAccess() {}

    /**
     * Installs the implementation. Called once by Thaumaturge during mod construction; addons must not call it.
     *
     * @param impl the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings impl) {
        BINDING.bind(impl);
    }

    /**
     * @param stack the stack to inspect
     * @return the stack's charge profile, or null when the stack is not rechargeable
     */
    public static @Nullable ChargeProfile profile(ItemStack stack) {
        return stack.isEmpty() ? null : stack.get(BINDING.get().profile());
    }

    /**
     * @param stack the stack to inspect
     * @return whether the stack is rechargeable
     */
    public static boolean isRechargeable(ItemStack stack) {
        return profile(stack) != null;
    }

    /**
     * @param stack the stack to read
     * @return the stored vis; zero for a stack that was never charged or is not rechargeable
     * @apiNote Use {@link #isRechargeable(ItemStack)} to distinguish an empty store from a stack without one.
     * @since 1.1.0
     */
    public static int storedVis(ItemStack stack) {
        return isRechargeable(stack) ? storedCharge(stack) : NO_CHARGE;
    }

    /**
     * @param stack the stack to read
     * @return how much more vis the stack can take before reaching capacity; zero when full, overfull or not rechargeable
     * @since 1.1.0
     */
    public static int roomFor(ItemStack stack) {
        ChargeProfile profile = profile(stack);
        return profile == null ? NO_CHARGE : Math.max(NO_CHARGE, profile.capacity() - storedCharge(stack));
    }

    /**
     * @param stack the stack to read
     * @return the stored vis divided by capacity; may exceed one for overfull legacy data, zero when not rechargeable
     * @since 1.1.0
     */
    public static float fillFraction(ItemStack stack) {
        ChargeProfile profile = profile(stack);
        return profile == null ? NO_CHARGE : (float) storedCharge(stack) / profile.capacity();
    }

    /**
     * Tops a stack up from the aura at a position. Only whole vis is drained, and a player with aura-preservation research
     * will not drain a protected chunk.
     *
     * @param level the level holding the aura
     * @param stack the stack to charge, changed in place
     * @param pos the position whose chunk is drained
     * @param player the acting player, or null when no preservation check is needed
     * @param amount the maximum vis to add; non-positive amounts add nothing
     * @return the amount added
     * @since 1.1.0
     */
    public static int drawFromAura(Level level, ItemStack stack, BlockPos pos, @Nullable Player player, int amount) {
        int request = Math.min(amount, roomFor(stack));
        if (level.isClientSide() || request <= 0) {
            return NO_CHARGE;
        }
        if (player != null && AuraHelper.shouldPreserveAura(level, player, pos)) {
            return NO_CHARGE;
        }
        int available = (int) AuraHelper.drainVis(level, pos, request, true);
        if (available <= 0) {
            return NO_CHARGE;
        }
        int drained = Math.round(AuraHelper.drainVis(level, pos, available, false));
        if (drained > 0) {
            stack.set(BINDING.get().charge(), storedCharge(stack) + drained);
        }
        return Math.max(NO_CHARGE, drained);
    }

    /**
     * Adds charge without draining aura, limited to remaining capacity.
     *
     * @param stack the stack to charge, changed in place
     * @param amount the maximum vis to add; non-positive amounts add nothing
     * @return the amount added
     * @since 1.1.0
     */
    public static int addVis(ItemStack stack, int amount) {
        int added = Math.min(amount, roomFor(stack));
        if (added <= 0) {
            return NO_CHARGE;
        }
        stack.set(BINDING.get().charge(), storedCharge(stack) + added);
        return added;
    }

    /**
     * Pays a charge cost atomically.
     *
     * @param stack the stack to spend from
     * @param cost the required amount; negative values are rejected
     * @return true when the cost was paid, false with no mutation otherwise
     * @since 1.1.0
     */
    public static boolean spendVis(ItemStack stack, int cost) {
        if (cost < 0 || !isRechargeable(stack)) {
            return false;
        }
        int stored = storedCharge(stack);
        if (stored < cost) {
            return false;
        }
        if (cost > 0) {
            stack.set(BINDING.get().charge(), stored - cost);
        }
        return true;
    }

    /**
     * @deprecated use {@link #drawFromAura(Level, ItemStack, BlockPos, Player, int)}
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static float rechargeItem(Level level, ItemStack stack, BlockPos pos, @Nullable Player player, int amount) {
        return drawFromAura(level, stack, pos, player, amount);
    }

    /** @deprecated use {@link #addVis(ItemStack, int)} */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static float rechargeItemBlindly(ItemStack stack, @Nullable LivingEntity holder, int amount) {
        return addVis(stack, amount);
    }

    /** @deprecated use {@link #storedVis(ItemStack)} with {@link #isRechargeable(ItemStack)} */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static int getCharge(ItemStack stack) {
        return isRechargeable(stack) ? storedCharge(stack) : NOT_RECHARGEABLE;
    }

    /** @deprecated use {@link #fillFraction(ItemStack)} with {@link #isRechargeable(ItemStack)} */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static float getChargePercentage(ItemStack stack, @Nullable LivingEntity holder) {
        return isRechargeable(stack) ? fillFraction(stack) : NOT_RECHARGEABLE;
    }

    /** @deprecated use {@link #spendVis(ItemStack, int)} */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean consumeCharge(ItemStack stack, @Nullable LivingEntity holder, int amount) {
        return spendVis(stack, amount);
    }

    private static int storedCharge(ItemStack stack) {
        return stack.getOrDefault(BINDING.get().charge(), NO_CHARGE);
    }

    /**
     * The component types Thaumaturge registers behind this facade.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * @return the {@code thaumaturge:charge} component type
         */
        DataComponentType<Integer> charge();

        /**
         * @return the {@code thaumaturge:rechargeable} component type
         */
        DataComponentType<ChargeProfile> profile();
    }
}
