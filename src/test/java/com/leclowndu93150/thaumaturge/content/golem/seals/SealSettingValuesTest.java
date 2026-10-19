package com.leclowndu93150.thaumaturge.content.golem.seals;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealBehavior;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealSetting;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.api.items.ItemMatch;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.BreakBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.EmptyBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.GuardBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.HarvestBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.ItemMatchSettings;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.ProvideBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.StoreBehavior;
import com.leclowndu93150.thaumaturge.content.golem.seals.behavior.UseBehavior;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class SealSettingValuesTest {
    private static final List<SealSetting> RENAMED_SETTINGS = List.of(
            BreakBehavior.SILK_TOUCH,
            EmptyBehavior.CYCLE,
            EmptyBehavior.LEAVE_ONE,
            ProvideBehavior.SINGLE_ITEM,
            ProvideBehavior.LEAVE_ONE,
            StoreBehavior.ONLY_EXISTING,
            GuardBehavior.MONSTERS,
            GuardBehavior.ANIMALS,
            GuardBehavior.PLAYERS,
            HarvestBehavior.REPLANT,
            HarvestBehavior.REQUEST_SEEDS,
            ItemMatchSettings.MATCH_DAMAGE,
            ItemMatchSettings.MATCH_COMPONENTS,
            ItemMatchSettings.MATCH_TAGS,
            ItemMatchSettings.MATCH_MOD,
            UseBehavior.LEFT_CLICK,
            UseBehavior.INTO_AIR,
            UseBehavior.BARE_HANDED,
            UseBehavior.SNEAKING,
            UseBehavior.REQUEST_ITEMS);

    @Test
    void mapsSealMatchSettingsToTheImmutableItemMatchRules() {
        ISealEntity seal = mock(ISealEntity.class);
        when(seal.setting(ItemMatchSettings.MATCH_DAMAGE)).thenReturn(true);
        when(seal.setting(ItemMatchSettings.MATCH_COMPONENTS)).thenReturn(false);
        when(seal.setting(ItemMatchSettings.MATCH_TAGS)).thenReturn(true);
        when(seal.setting(ItemMatchSettings.MATCH_MOD)).thenReturn(false);

        assertEquals(new ItemMatch(false, true, false, true, false), ItemMatchSettings.of(seal));
    }

    @Test
    void readsFormerKeyButWritesCanonicalKey() {
        var codec = SealSettingValues.codec(sealType(BreakBehavior.SILK_TOUCH)).codec();
        SealSettingValues decoded = codec.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"psilk\":true}"))
                .result()
                .orElseThrow();

        assertTrue(decoded.get(BreakBehavior.SILK_TOUCH));

        var encoded = codec.encodeStart(JsonOps.INSTANCE, decoded)
                .result()
                .orElseThrow()
                .getAsJsonObject();
        assertTrue(encoded.get("silk_touch").getAsBoolean());
        assertFalse(encoded.has("psilk"));
    }

    @Test
    void canonicalValueTakesPrecedenceOverFormerKey() {
        var codec = SealSettingValues.codec(sealType(BreakBehavior.SILK_TOUCH)).codec();
        SealSettingValues decoded = codec.parse(
                        JsonOps.INSTANCE, JsonParser.parseString("{\"silk_touch\":false,\"psilk\":true}"))
                .result()
                .orElseThrow();

        assertFalse(decoded.get(BreakBehavior.SILK_TOUCH));
    }

    @Test
    void readsEveryRenamedSealSettingFromItsFormerKeyAndWritesTheCanonicalKey() {
        for (SealSetting setting : RENAMED_SETTINGS) {
            var codec = SealSettingValues.codec(sealType(setting)).codec();
            String formerKey = setting.formerKey();
            SealSettingValues decoded = codec.parse(
                            JsonOps.INSTANCE, JsonParser.parseString("{\"" + formerKey + "\":true}"))
                    .result()
                    .orElseThrow();

            assertTrue(decoded.get(setting), setting.key());

            var encoded = codec.encodeStart(JsonOps.INSTANCE, decoded)
                    .result()
                    .orElseThrow()
                    .getAsJsonObject();
            assertTrue(encoded.get(setting.key()).getAsBoolean(), setting.key());
            assertFalse(encoded.has(formerKey), formerKey);
        }
    }

    private static SealType sealType(SealSetting setting) {
        return SealType.builder(() -> mock(ISealBehavior.class))
                .settings(setting)
                .showSettings()
                .placer(() -> Items.STICK)
                .build();
    }
}
