package com.leclowndu93150.thaumaturge.api.aura;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import net.minecraft.resources.ResourceKey;

/**
 * A block that feeds primal vis into the vis relay network. Thaumaturge's energized aura nodes are
 * sources; an addon block becomes one by providing {@link VisRelayCapabilities#SOURCE} for its
 * block entity in {@code RegisterCapabilitiesEvent}.
 *
 * <p>A relay links to the nearest source within 8 blocks, or else to the nearest linked relay,
 * and checks the link again every 40 ticks. Relays and their consumers look the capability up
 * each time rather than keeping the instance, so a source may appear, disappear or change its
 * answers at any time. Every call happens on the server thread.
 *
 * <p>Drains run inside a local transaction. A source journals its state before changing it, so an
 * aborted transaction restores the source and a root commit publishes any sync or effects. The
 * relay chain flashes the drained aspect's colour after commit. A source must not drain the relay
 * network itself from inside {@link #drainCentivis}.
 *
 * @since 1.0.0
 */
public interface IVisRelaySource {
    /**
     * Whether relays may link to and drain this source now, for example because it is powered.
     *
     * @return true when the source is usable
     */
    boolean canSupply();

    /**
     * How much centivis of a primal the source could supply right now, without draining it.
     *
     * @param primal the primal aspect
     * @return the available centivis, zero or more
     */
    int availableCentivis(ResourceKey<IAspect> primal);

    /**
     * Drains up to {@code amount} centivis of a primal as part of a transaction.
     *
     * @param primal   the primal aspect
     * @param amount   the requested centivis, greater than zero
     * @param transaction the open transaction the drain belongs to
     * @return the centivis drained; values outside zero to {@code amount} are clamped by the caller
     */
    int drainCentivis(ResourceKey<IAspect> primal, int amount, TransactionContext transaction);
}
