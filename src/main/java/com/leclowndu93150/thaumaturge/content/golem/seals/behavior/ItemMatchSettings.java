package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.items.ItemMatch;

public final class ItemMatchSettings {
    public static final SealSetting MATCH_DAMAGE =
            new SealSetting("match_damage", "gui.thaumaturge.seal.setting.meta", true).readingAlso("pmeta");
    public static final SealSetting MATCH_COMPONENTS =
            new SealSetting("match_components", "gui.thaumaturge.seal.setting.nbt", true).readingAlso("pnbt");
    public static final SealSetting MATCH_TAGS =
            new SealSetting("match_tags", "gui.thaumaturge.seal.setting.ore", false).readingAlso("pore");
    public static final SealSetting MATCH_MOD =
            new SealSetting("match_mod", "gui.thaumaturge.seal.setting.mod", false).readingAlso("pmod");
    public static final SealSetting[] ALL = {MATCH_DAMAGE, MATCH_COMPONENTS, MATCH_TAGS, MATCH_MOD};

    private ItemMatchSettings() {}

    public static ItemMatch of(ISealEntity seal) {
        return new ItemMatch(
                !seal.setting(MATCH_DAMAGE),
                !seal.setting(MATCH_COMPONENTS),
                false,
                seal.setting(MATCH_TAGS),
                seal.setting(MATCH_MOD));
    }
}
