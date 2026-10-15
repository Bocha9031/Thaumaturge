package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

final class NodeWandTap {
    private static final ResourceLocation RESEARCH_TAPPER_ONE = TTIds.rl("node_tapper_1");
    private static final ResourceLocation RESEARCH_TAPPER_TWO = TTIds.rl("node_tapper_2");
    private static final ResourceLocation RESEARCH_PRESERVE = TTIds.rl("node_preserve");
    private static final int TAP_INTERVAL = 5;
    private static final int BASE_STRENGTH = 1;
    private static final int PRESERVE_MINIMUM_STOCK = 2;

    private NodeWandTap() {}

    static boolean tap(BlockEntityNode node, ServerLevel level, Player player, ItemStack wand, int remainingUse) {
        if (remainingUse % TAP_INTERVAL != 0) {
            return false;
        }
        PlayerResearch research = PlayerResearchAccess.of(player);
        int strength = BASE_STRENGTH
                + (research.hasCompleted(RESEARCH_TAPPER_ONE) ? 1 : 0)
                + (research.hasCompleted(RESEARCH_TAPPER_TWO) ? 1 : 0);
        WandParts parts = WandVisHelper.getParts(wand);
        boolean starter = parts.cap() == TTWandParts.CAP_IRON.get() && parts.rod() == TTWandParts.ROD_WOOD.get();
        boolean preserve = !player.isShiftKeyDown() && !starter && research.hasCompleted(RESEARCH_PRESERVE);
        int minimumStock = preserve ? PRESERVE_MINIMUM_STOCK : 1;
        int wandMax = WandVisHelper.getMaxVis(wand);
        List<Holder<IAspect>> tappable = new ArrayList<>();
        for (ResourceKey<IAspect> key : TTAspects.PRIMALS) {
            Holder<IAspect> primal = Aspects.resolve(level, key);
            if (primal != null
                    && node.getAspects().amountOf(primal) >= minimumStock
                    && WandVisHelper.getVis(wand, key) < wandMax) {
                tappable.add(primal);
            }
        }
        if (tappable.isEmpty()) {
            return false;
        }
        Holder<IAspect> chosen = tappable.get(level.getRandom().nextInt(tappable.size()));
        int stock = node.getAspects().amountOf(chosen);
        int offered = Math.min(strength, preserve ? stock - 1 : stock);
        ResourceKey<IAspect> key = chosen.unwrapKey().orElseThrow();
        int accepted = offered - WandVisHelper.addVis(wand, key, offered, true);
        if (accepted <= 0) {
            return false;
        }
        node.aspects = node.aspects.reduce(chosen, accepted);
        node.recordDrain(player.getUUID(), chosen.value().color());
        return true;
    }
}
