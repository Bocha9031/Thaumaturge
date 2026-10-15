package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.api.client.golems.GolemPartRenderHook;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/** Immutable hook table keyed by mesh id, so separately created model values share one hook. */
public final class GolemPartRenderHooks {
    private static final GolemPartRenderHook UNHOOKED = new GolemPartRenderHook() {};
    private static volatile Map<ResourceLocation, GolemPartRenderHook> hooks = Map.of();

    private GolemPartRenderHooks() {}

    static void install(Map<ResourceLocation, GolemPartRenderHook> registeredHooks) {
        hooks = Map.copyOf(registeredHooks);
    }

    public static GolemPartRenderHook hookFor(GolemPartModel model) {
        return hooks.getOrDefault(model.objModel(), UNHOOKED);
    }
}
