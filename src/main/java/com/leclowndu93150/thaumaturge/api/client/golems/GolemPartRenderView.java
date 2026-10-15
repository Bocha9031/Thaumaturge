package com.leclowndu93150.thaumaturge.api.client.golems;

import net.minecraft.world.item.ItemStack;

/**
 * Read-only values for one frame of a golem render. Item previews use resting values: no swing,
 * combat, wheel rotation, or second-hand item. The view is temporary and must not be retained.
 * Client only.
 */
public interface GolemPartRenderView {
    /** Current attack swing progress, from zero to one. */
    float swingProgress();

    /** Golem age in ticks including the current partial tick. */
    float ageInTicks();

    /** Interpolated head pitch in degrees. */
    float headPitch();

    /** Whether the golem currently has a combat target. */
    boolean fighting();

    /** Wheel rotation in degrees. */
    float wheelRotation();

    /** Packed light coordinates for submitted geometry. */
    int lightCoords();

    /** ARGB outline colour, or zero when no native outline is active. */
    int outlineColor();

    /** Whether the golem has an item in its second hand. */
    boolean hasSecondHandItem();

    /** Whether the second-hand item is a block item. */
    boolean secondHandItemIsBlock();

    /** The second-hand stack. Hooks must not modify it. */
    ItemStack secondHandItem();
}
