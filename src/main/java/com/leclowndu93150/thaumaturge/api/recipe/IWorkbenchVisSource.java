package com.leclowndu93150.thaumaturge.api.recipe;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

/**
 * An external supplier of primal vis toward an arcane craft. Registered sources are consulted by
 * the workbench payment planner after the wand in the wand slot and before the loaded crystals, so
 * that an addon (for example a networked storage system near the workbench) can pay a primal's
 * share of a craft instead of a crystal.
 *
 * <p>Supplies are made in the caller's transaction. A caller may use a child transaction to test
 * availability and close it uncommitted to restore tentative changes.
 * Sources are only consulted on the server, when the craft has an {@link ArcaneWorkbenchContext}.
 *
 * <p>Register sources through {@link RegisterWorkbenchVisSourcesEvent}.
 *
 * @since 1.0.0
 */
@FunctionalInterface
public interface IWorkbenchVisSource {
    /**
     * Supplies up to {@code need} centivis of the given aspect toward a craft.
     *
     * @param context   where and for whom the craft runs
     * @param player    the crafting player
     * @param workbench the workbench inventory
     * @param aspect    the primal aspect required
     * @param need      the centivis still required for this aspect
     * @param transaction the transaction that owns resource changes
     * @return the centivis supplied, never more than {@code need}; larger or negative values are
     *         clamped by the caller
     */
    int supply(
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneWorkbench workbench,
            Holder<IAspect> aspect,
            int need,
            TransactionContext transaction);
}
