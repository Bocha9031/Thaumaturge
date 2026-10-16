package com.leclowndu93150.thaumaturge.content.aura.relay;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayHelper;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneWorkbench;
import com.leclowndu93150.thaumaturge.api.recipe.IWorkbenchVisSource;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

public final class VisRelayWorkbenchSource implements IWorkbenchVisSource {
    @Override
    public int supply(
            ArcaneWorkbenchContext context,
            ServerPlayer player,
            IArcaneWorkbench workbench,
            Holder<IAspect> aspect,
            int need,
            TransactionContext transaction) {
        return VisRelayHelper.drainCentivis(
                context.level(),
                context.blockPosition().orElseGet(player::blockPosition),
                aspect.unwrapKey().orElseThrow(),
                need,
                transaction);
    }
}
