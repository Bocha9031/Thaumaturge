package com.leclowndu93150.thaumaturge.api.transaction;

/**
 * A server-thread transaction shared by resource participants. Participants must journal their
 * state before mutating it and defer external effects until root commit.
 *
 * <p>This is the native 1.21.1 transaction contract; that NeoForge version has no transfer
 * transaction API. A simulation is an ordinary transaction closed without committing.
 */
public sealed interface TransactionContext permits Transaction {}
