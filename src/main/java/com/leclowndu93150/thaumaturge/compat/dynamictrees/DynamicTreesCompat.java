package com.leclowndu93150.thaumaturge.compat.dynamictrees;

import com.dtteam.dynamictrees.event.RegistryEvent;
import com.dtteam.dynamictrees.registry.NeoForgeRegistryHandler;
import com.dtteam.dynamictrees.systems.genfeature.GenFeature;
import com.leclowndu93150.thaumaturge.TTIds;
import net.neoforged.bus.api.IEventBus;

public final class DynamicTreesCompat {
    private DynamicTreesCompat() {}

    public static void init(IEventBus modBus) {
        NeoForgeRegistryHandler.setup(TTIds.MODID, modBus);
        modBus.addListener(DynamicTreesCompat::onGenFeatureRegistry);
    }

    public static void onGenFeatureRegistry(RegistryEvent<GenFeature> event) {
        if (event.isEntryOfType(GenFeature.class)) {
            event.getRegistry().register(new ShimmerleafGenFeature(TTIds.DT_SHIMMERLEAF_FEATURE));
            event.getRegistry().register(new TreeFootprintGenFeature(TTIds.DT_TREE_FOOTPRINT_FEATURE));
        }
    }
}
