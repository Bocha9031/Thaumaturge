package com.leclowndu93150.thaumaturge.api.client.golems;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Posted once during client setup so addons can bind hooks to mesh locations. A hook applies to every
 * part model whose {@code objModel()} is that mesh id. Each mesh accepts one hook. Client only.
 */
public final class RegisterGolemPartRenderHooksEvent extends Event implements IModBusEvent {
    private final Map<ResourceLocation, GolemPartRenderHook> hooks = new LinkedHashMap<>();

    /** Registers a hook for a mesh location. Duplicate mesh registrations are rejected. */
    public void register(ResourceLocation mesh, GolemPartRenderHook hook) {
        Objects.requireNonNull(mesh, "mesh");
        Objects.requireNonNull(hook, "hook");
        if (hooks.putIfAbsent(mesh, hook) != null) {
            throw new IllegalArgumentException("Golem part mesh " + mesh + " already has a render hook");
        }
    }

    /** Returns an immutable snapshot of all registrations. */
    public Map<ResourceLocation, GolemPartRenderHook> hooks() {
        return Map.copyOf(hooks);
    }
}
