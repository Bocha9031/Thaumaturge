package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.client.golems.RegisterGolemPartRenderHooksEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class GolemPartRenderHookRegistration {
    private GolemPartRenderHookRegistration() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            RegisterGolemPartRenderHooksEvent registration = new RegisterGolemPartRenderHooksEvent();
            ModLoader.postEvent(registration);
            GolemPartRenderHooks.install(registration.hooks());
        });
    }
}
