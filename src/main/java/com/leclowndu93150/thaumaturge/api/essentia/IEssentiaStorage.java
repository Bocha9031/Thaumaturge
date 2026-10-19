package com.leclowndu93150.thaumaturge.api.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import net.minecraft.core.Holder;

/**
 * A side-bound, server-authoritative enumerable essentia storage view.
 *
 * <p>Implementations expose this contract through {@link EssentiaCapabilities#STORAGE}. The
 * capability context selects the side, so operations on a returned view require no additional
 * direction argument. Querying with a {@code null} side does not expose this capability. A view
 * may be read-only by returning zero from both transfer methods.
 *
 * <p>The simulate-based transfer methods are immediate compatibility conveniences and operate
 * outside any caller-owned transaction. Prefer the transaction overloads when a transfer must
 * participate in a larger atomic operation. A transaction can expose tentative contents to its
 * caller, but does not advance {@link #contentRevision()} or notify the host until the root scope
 * commits.
 *
 * <p>All methods are server-thread contracts. Capability instances follow NeoForge block
 * capability lifetime rules and become invalid when their owning block entity is removed or its
 * capabilities are invalidated; consumers must not retain a view after its invalidation listener
 * fires.
 *
 * @since 1.0.0
 */
public interface IEssentiaStorage {
    /**
     * Returns an immutable snapshot of every currently stored aspect. The snapshot must not expose
     * mutable implementation state and remains safe to retain after the storage changes.
     *
     * @return all visible contents, never {@code null}
     */
    AspectList contents();

    /**
     * Returns the stored amount of one aspect.
     *
     * @param aspect the aspect queried
     * @return its amount, or zero when absent
     */
    default int amount(Holder<IAspect> aspect) {
        return contents().amountOf(aspect);
    }

    /**
     * Inserts up to {@code amount} of an aspect.
     *
     * @param aspect the aspect supplied
     * @param amount the maximum amount to insert
     * @param simulate whether to report the result without changing storage
     * @throws IllegalStateException if called while a transaction is already open on this thread;
     *     use the transaction overload instead
     * @return the amount accepted or that would be accepted, between zero and {@code amount}
     */
    int insert(Holder<IAspect> aspect, int amount, boolean simulate);

    /**
     * Inserts up to {@code amount} as part of a caller-owned transaction. The change is visible
     * immediately to the current server-thread scope, but is rolled back if that scope or any
     * enclosing scope aborts.
     *
     * <p>The default is deliberately fail-closed: legacy providers that only implement the
     * simulate-based methods cannot safely participate in an atomic craft. Implementations that
     * own rollback-capable storage should override this method.
     *
     * @param aspect the aspect supplied
     * @param amount the maximum amount to insert
     * @param transaction the open transaction that owns the change
     * @return the amount accepted, between zero and {@code amount}
     */
    default int insert(Holder<IAspect> aspect, int amount, TransactionContext transaction) {
        return 0;
    }

    /**
     * Extracts up to {@code amount} of an aspect.
     *
     * @param aspect the aspect requested
     * @param amount the maximum amount to extract
     * @param simulate whether to report the result without changing storage
     * @throws IllegalStateException if called while a transaction is already open on this thread;
     *     use the transaction overload instead
     * @return the amount extracted or that would be extracted, between zero and {@code amount}
     */
    int extract(Holder<IAspect> aspect, int amount, boolean simulate);

    /**
     * Extracts up to {@code amount} as part of a caller-owned transaction. The change is visible
     * immediately to the current server-thread scope, but is rolled back if that scope or any
     * enclosing scope aborts.
     *
     * <p>The default is deliberately fail-closed: legacy providers that only implement the
     * simulate-based methods cannot safely participate in an atomic craft. Implementations that
     * own rollback-capable storage should override this method.
     *
     * @param aspect the aspect requested
     * @param amount the maximum amount to extract
     * @param transaction the open transaction that owns the change
     * @return the amount extracted, between zero and {@code amount}
     */
    default int extract(Holder<IAspect> aspect, int amount, TransactionContext transaction) {
        return 0;
    }

    /**
     * Returns a monotonically increasing revision for committed content changes.
     *
     * <p>Server-side consumers may cache {@link #contents()} while this value is unchanged.
     * Revisions are scoped to the lifetime of this capability instance; they need not survive
     * chunk unloads or server restarts. Every committed mutation that changes {@link #contents()},
     * including mutations performed outside this capability view, must advance it. Simulations,
     * loading persisted state, client synchronization and non-content configuration changes do not
     * advance it.
     *
     * @return the current content revision
     */
    long contentRevision();
}
