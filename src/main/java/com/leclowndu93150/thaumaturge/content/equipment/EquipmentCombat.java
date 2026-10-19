package com.leclowndu93150.thaumaturge.content.equipment;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

final class EquipmentCombat {
    private EquipmentCombat() {}

    static boolean canAffect(Entity attacker, Entity target) {
        return !(attacker instanceof Player player && target instanceof Player victim) || player.canHarmPlayer(victim);
    }
}
