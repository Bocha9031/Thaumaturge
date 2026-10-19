package com.leclowndu93150.thaumaturge.content.equipment.hover;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.IHoverGear;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.mixin.server.network.ServerGamePacketListenerImplAccessor;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForgeMod;

public final class HoverManager {
    public static final int FUEL_INTERVAL_TICKS = 360;
    private static final ResourceLocation FLIGHT_MODIFIER_ID = TTIds.rl("hover_flight");
    private static final double FLIGHT_MODIFIER_VALUE = 1.0;
    private static final int HUM_INTERVAL_TICKS = 24;
    private static final float TOGGLE_VOLUME = 0.33F;
    private static final float HUM_VOLUME = 0.05F;
    private static final float HUM_PITCH_SPREAD = 0.05F;
    private static final double IDLE_FALL_DAMPING = 0.75;

    private HoverManager() {}

    public static boolean isHovering(Player player) {
        return player.getData(TTAttachments.HOVERING);
    }

    public static ItemStack wornHoverGear(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof IHoverGear) {
            return chest;
        }
        if (ModList.get().isLoaded(TTIds.CURIOS)) {
            return ThaumaturgeCuriosCompat.findCurio(player, stack -> stack.getItem() instanceof IHoverGear);
        }
        return ItemStack.EMPTY;
    }

    public static boolean isWearingHoverGear(Player player) {
        return !wornHoverGear(player).isEmpty();
    }

    public static void toggle(ServerPlayer player) {
        ItemStack worn = wornHoverGear(player);
        if (!(worn.getItem() instanceof IHoverGear gear)) {
            return;
        }
        boolean hovering = !isHovering(player);
        if (hovering && gear.getHoverFuel(worn) <= 0) {
            return;
        }
        setHovering(player, hovering);
        playToggleSound(player, hovering);
    }

    public static void tick(ServerPlayer player, ItemStack stack, IHoverGear gear) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (!isHovering(player)) {
            player.fallDistance *= IDLE_FALL_DAMPING;
            return;
        }

        player.resetFallDistance();
        ((ServerGamePacketListenerImplAccessor) player.connection).setAboveGroundTickCount(0);
        keepAloft(player);
        burnFuel(player, stack, gear);
        if (gear.getHoverFuel(stack) <= 0) {
            setHovering(player, false);
            playToggleSound(player, false);
            return;
        }
        if (player.tickCount % HUM_INTERVAL_TICKS == 0) {
            play(
                    player,
                    TTSounds.JACOBS.get(),
                    HUM_VOLUME,
                    1.0F + player.getRandom().nextFloat() * HUM_PITCH_SPREAD);
        }
    }

    public static void setHovering(ServerPlayer player, boolean hovering) {
        player.setData(TTAttachments.HOVERING, hovering);
        if (hovering) {
            keepAloft(player);
        } else {
            land(player);
        }
    }

    private static void burnFuel(ServerPlayer player, ItemStack stack, IHoverGear gear) {
        ChargeTick tick = advanceCharge(player.getData(TTAttachments.HOVER_CHARGE));
        if (tick.consumeFuel()) {
            gear.consumeHoverFuel(stack);
        }
        player.setData(TTAttachments.HOVER_CHARGE, tick.elapsedTicks());
    }

    static ChargeTick advanceCharge(int elapsedTicks) {
        int nextElapsed = elapsedTicks + 1;
        return nextElapsed >= FUEL_INTERVAL_TICKS ? new ChargeTick(0, true) : new ChargeTick(nextElapsed, false);
    }

    record ChargeTick(int elapsedTicks, boolean consumeFuel) {}

    private static void keepAloft(ServerPlayer player) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight != null && !flight.hasModifier(FLIGHT_MODIFIER_ID)) {
            flight.addTransientModifier(new AttributeModifier(
                    FLIGHT_MODIFIER_ID, FLIGHT_MODIFIER_VALUE, AttributeModifier.Operation.ADD_VALUE));
        }
        Abilities abilities = player.getAbilities();
        if (!abilities.flying && !player.onGround()) {
            abilities.flying = true;
            player.onUpdateAbilities();
        }
    }

    private static void land(ServerPlayer player) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight != null) {
            flight.removeModifier(FLIGHT_MODIFIER_ID);
        }
        Abilities abilities = player.getAbilities();
        if (abilities.flying && !player.getAbilities().mayfly) {
            abilities.flying = false;
            player.onUpdateAbilities();
        }
    }

    private static void playToggleSound(ServerPlayer player, boolean hovering) {
        play(player, hovering ? TTSounds.HHON.get() : TTSounds.HHOFF.get(), TOGGLE_VOLUME, 1.0F);
    }

    private static void play(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.level()
                .playSound(
                        null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }
}
