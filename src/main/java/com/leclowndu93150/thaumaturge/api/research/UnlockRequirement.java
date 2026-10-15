package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * An extra requirement a player must meet before a research entry can be unlocked.
 * Requirements run on either logical side when checking unlocks, auto-completing siblings,
 * updating login unlocks, or displaying research. They should only read state and be cheap.
 *
 * @since 1.1.0
 */
@FunctionalInterface
public interface UnlockRequirement {
    /** Tests whether this player meets the requirement. */
    boolean isMet(Player player, PlayerResearch research);

    /** Returns a requirement met once the player completes the given research entry. */
    static UnlockRequirement completed(ResourceLocation entry) {
        return (player, research) -> research.hasCompleted(entry);
    }

    /** Combines this requirement with another; the other is skipped when this one fails. */
    default UnlockRequirement and(UnlockRequirement other) {
        return (player, research) -> isMet(player, research) && other.isMet(player, research);
    }
}
