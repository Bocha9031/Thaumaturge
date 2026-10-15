package com.leclowndu93150.thaumaturge.api.research;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearch;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Lets addons add extra requirements for unlocking a research entry.
 *
 * <p>Every registered requirement for an entry must pass. Register during common setup, such as
 * inside {@code enqueueWork} in {@code FMLCommonSetupEvent}. Conditions registered through the
 * deprecated {@link #register} remain available as requirements backed by the legacy knowledge
 * view.
 *
 * <pre>{@code
 * @SubscribeEvent
 * public static void onCommonSetup(FMLCommonSetupEvent event) {
 *     event.enqueueWork(() -> ResearchUnlockConditions.require(
 *             ResourceLocation.fromNamespaceAndPath("thaumaturge_tinkerer", "some_entry"),
 *             UnlockRequirement.completed(ResourceLocation.fromNamespaceAndPath("thaumaturge_tinkerer", "prerequisite")),
 *             Component.translatable("tooltip.thaumaturge_tinkerer.requires_prerequisite")
 *     ));
 * }
 * }</pre>
 */
public final class ResearchUnlockConditions {
    /** @deprecated implement {@link UnlockRequirement} and register it with {@link #require} */
    @Deprecated(since = "1.1.0", forRemoval = true)
    @FunctionalInterface
    public interface Condition {
        boolean test(Player player, IPlayerKnowledge knowledge);

        @Deprecated(since = "1.1.0", forRemoval = true)
        static Condition requiresComplete(ResourceLocation research) {
            return (player, knowledge) -> knowledge.isResearchComplete(research);
        }

        @Deprecated(since = "1.1.0", forRemoval = true)
        default Condition and(Condition other) {
            return (player, knowledge) -> test(player, knowledge) && other.test(player, knowledge);
        }
    }

    private record Registered(
            UnlockRequirement requirement, @Nullable Component lockedMessage) {}

    private static final Map<ResourceLocation, List<Registered>> CONDITIONS = new ConcurrentHashMap<>();

    private ResearchUnlockConditions() {}

    public static void require(
            ResourceLocation entry, UnlockRequirement requirement, @Nullable Component lockedMessage) {
        Objects.requireNonNull(entry);
        Objects.requireNonNull(requirement);
        CONDITIONS
                .computeIfAbsent(entry, id -> new CopyOnWriteArrayList<>())
                .add(new Registered(requirement, lockedMessage));
    }

    /** @deprecated use {@link #require} */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void register(ResourceLocation entry, Condition condition, @Nullable Component lockedMessage) {
        Objects.requireNonNull(condition);
        require(entry, (player, research) -> condition.test(player, IPlayerKnowledge.viewOf(research)), lockedMessage);
    }

    public static boolean passes(Player player, PlayerResearch research, ResourceLocation entry) {
        List<Registered> requirements = CONDITIONS.get(entry);
        if (requirements == null) {
            return true;
        }
        for (Registered registered : requirements) {
            if (!registered.requirement().isMet(player, research)) {
                return false;
            }
        }
        return true;
    }

    /** @deprecated use {@link #passes(Player, PlayerResearch, ResourceLocation)} */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static boolean passes(Player player, IPlayerKnowledge knowledge, ResourceLocation entry) {
        return passes(player, (PlayerResearch) knowledge, entry);
    }

    public static List<Component> lockedMessages(Player player, PlayerResearch research, ResourceLocation entry) {
        List<Registered> requirements = CONDITIONS.get(entry);
        if (requirements == null) {
            return List.of();
        }
        List<Component> messages = new ArrayList<>();
        for (Registered registered : requirements) {
            if (registered.lockedMessage() != null && !registered.requirement().isMet(player, research)) {
                messages.add(registered.lockedMessage());
            }
        }
        return messages;
    }

    /** @deprecated use {@link #lockedMessages(Player, PlayerResearch, ResourceLocation)} */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static List<Component> lockedMessages(Player player, IPlayerKnowledge knowledge, ResourceLocation entry) {
        return lockedMessages(player, (PlayerResearch) knowledge, entry);
    }
}
