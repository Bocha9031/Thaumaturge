package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import java.util.List;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

public final class OreScanColors {
    public static final int DEFAULT = 0xC4C4C4;

    private static final int C_IRON = 0xD8B49C;
    private static final int C_COAL = 0x1C1A1E;
    private static final int C_REDSTONE = 0xF0140E;
    private static final int C_GOLD = 0xFFD23A;
    private static final int C_LAPIS = 0x1E3CC0;
    private static final int C_DIAMOND = 0x8CF2F4;
    private static final int C_EMERALD = 0x22DC5A;
    private static final int C_QUARTZ = 0xF2E8DA;
    private static final int C_COPPER = 0xE89A62;
    private static final int C_AMBER = 0xF0A01E;
    private static final int C_CINNABAR = 0x8A0E14;

    private record Entry(TagKey<Block> ore, int color) {}

    private static final List<Entry> ENTRIES = List.of(
            new Entry(Tags.Blocks.ORES_IRON, C_IRON),
            new Entry(Tags.Blocks.ORES_COAL, C_COAL),
            new Entry(Tags.Blocks.ORES_REDSTONE, C_REDSTONE),
            new Entry(Tags.Blocks.ORES_GOLD, C_GOLD),
            new Entry(Tags.Blocks.ORES_LAPIS, C_LAPIS),
            new Entry(Tags.Blocks.ORES_DIAMOND, C_DIAMOND),
            new Entry(Tags.Blocks.ORES_EMERALD, C_EMERALD),
            new Entry(Tags.Blocks.ORES_QUARTZ, C_QUARTZ),
            new Entry(Tags.Blocks.ORES_COPPER, C_COPPER),
            new Entry(TTBlockTags.ORES_AMBER, C_AMBER),
            new Entry(TTBlockTags.ORES_CINNABAR, C_CINNABAR));

    private OreScanColors() {}

    public static int of(BlockState state) {
        for (Entry entry : ENTRIES) {
            if (state.is(entry.ore())) {
                return entry.color();
            }
        }
        return DEFAULT;
    }
}
