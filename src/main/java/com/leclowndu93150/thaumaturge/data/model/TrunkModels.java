package com.leclowndu93150.thaumaturge.data.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.content.world.tree.TrunkCorner;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class TrunkModels {
    private static final int FULL = 16;
    private static final int HALF = 8;
    private static final String SIDE = "#side";
    private static final String END = "#end";

    private TrunkModels() {}

    public static void register(
            Consumer<BlockStateGenerator> blockStateOutput,
            BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput,
            Block log,
            Block wood,
            ResourceLocation wideSide,
            ResourceLocation wideEnd) {
        TextureMapping logMapping = TextureMapping.logColumn(log);
        ResourceLocation vertical = ModelTemplates.CUBE_COLUMN.create(log, logMapping, modelOutput);
        ResourceLocation horizontal = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(log, logMapping, modelOutput);
        Map<TrunkCorner, ResourceLocation> cornerModels = new EnumMap<>(TrunkCorner.class);
        for (TrunkCorner corner : TrunkCorner.values()) {
            if (corner == TrunkCorner.NONE) {
                continue;
            }
            ResourceLocation model = ModelLocationUtils.getModelLocation(log, "_trunk_" + corner.getSerializedName());
            modelOutput.accept(
                    model, () -> cornerModelJson(corner, wideSide, wideEnd, logMapping.get(TextureSlot.SIDE)));
            cornerModels.put(corner, model);
        }
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(log)
                .with(PropertyDispatch.properties(BlockStateProperties.AXIS, TrunkCorner.PROPERTY)
                        .generate((axis, corner) -> switch (axis) {
                            case Y -> variant(corner == TrunkCorner.NONE ? vertical : cornerModels.get(corner));
                            case Z -> variant(horizontal).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
                            case X ->
                                variant(horizontal)
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
                        })));

        TextureMapping woodMapping = logMapping.copyAndUpdate(TextureSlot.END, logMapping.get(TextureSlot.SIDE));
        ResourceLocation woodModel = ModelTemplates.CUBE_COLUMN.create(wood, woodMapping, modelOutput);
        blockStateOutput.accept(MultiVariantGenerator.multiVariant(wood)
                .with(PropertyDispatch.property(BlockStateProperties.AXIS)
                        .select(Direction.Axis.Y, variant(woodModel))
                        .select(
                                Direction.Axis.Z,
                                variant(woodModel).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
                        .select(
                                Direction.Axis.X,
                                variant(woodModel)
                                        .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                        .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))));
    }

    private static Variant variant(ResourceLocation model) {
        return Variant.variant().with(VariantProperties.MODEL, model);
    }

    private static JsonObject cornerModelJson(
            TrunkCorner corner, ResourceLocation wideSide, ResourceLocation wideEnd, ResourceLocation particle) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:block/block");
        JsonObject textures = new JsonObject();
        textures.addProperty("particle", particle.toString());
        textures.addProperty("side", wideSide.toString());
        textures.addProperty("end", wideEnd.toString());
        root.add("textures", textures);
        JsonObject element = new JsonObject();
        element.add("from", triple(0, 0, 0));
        element.add("to", triple(FULL, FULL, FULL));
        int x = corner.offsetX();
        int z = corner.offsetZ();
        JsonObject faces = new JsonObject();
        faces.add("north", face(SIDE, (1 - x) * HALF, 0, HALF, FULL, Direction.NORTH));
        faces.add("south", face(SIDE, x * HALF, 0, HALF, FULL, Direction.SOUTH));
        faces.add("west", face(SIDE, z * HALF, 0, HALF, FULL, Direction.WEST));
        faces.add("east", face(SIDE, (1 - z) * HALF, 0, HALF, FULL, Direction.EAST));
        faces.add("up", face(END, x * HALF, z * HALF, HALF, HALF, Direction.UP));
        faces.add("down", face(END, x * HALF, (1 - z) * HALF, HALF, HALF, Direction.DOWN));
        element.add("faces", faces);
        JsonArray elements = new JsonArray();
        elements.add(element);
        root.add("elements", elements);
        return root;
    }

    private static JsonObject face(String texture, int u, int v, int width, int height, Direction cull) {
        JsonObject face = new JsonObject();
        JsonArray uv = new JsonArray();
        uv.add(u);
        uv.add(v);
        uv.add(u + width);
        uv.add(v + height);
        face.add("uv", uv);
        face.addProperty("texture", texture);
        face.addProperty("cullface", cull.getSerializedName());
        return face;
    }

    private static JsonArray triple(int x, int y, int z) {
        JsonArray values = new JsonArray();
        values.add(x);
        values.add(y);
        values.add(z);
        return values;
    }
}
