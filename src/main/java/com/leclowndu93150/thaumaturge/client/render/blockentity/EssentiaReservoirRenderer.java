package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.content.essentia.reservoir.BlockEntityEssentiaReservoir;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/** Renders the reservoir's aspect-cycling mixed essentia volume. */
public final class EssentiaReservoirRenderer implements BlockEntityRenderer<BlockEntityEssentiaReservoir> {
    private static final int FLUID_ALPHA = 0xE6;
    private static final float CYCLE_TICKS = 20.0F;
    private static final float FLUID_LIFT = 2.0F / 16.0F;
    private static final float FLUID_WIDEN = 1.25F;
    private static final int MIN_BLOCK_LIGHT = 12;

    public EssentiaReservoirRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityEssentiaReservoir reservoir,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        int amount = reservoir.getStoredAmount();
        if (amount <= 0) return;

        // JarRenderer's fluid prism is widened and lifted to the reservoir's 3/16..13/16 bounds.
        poseStack.pushPose();
        poseStack.translate(0.5F, FLUID_LIFT, 0.5F);
        poseStack.scale(FLUID_WIDEN, 1.0F, FLUID_WIDEN);
        poseStack.translate(-0.5F, 0.0F, -0.5F);
        int color = reservoir.getLevel() == null
                ? -1
                : fluidColor(reservoir.contents(), reservoir.getLevel().getGameTime(), partialTick);
        JarRenderer.submitFluid(
                amount,
                BlockEntityEssentiaReservoir.CAPACITY,
                color,
                fluidLight(light),
                JarRenderer.ANIMATED_GLOW_MATERIAL.sprite(),
                poseStack,
                buffers);
        poseStack.popPose();
    }

    static int fluidColor(AspectList contents, long gameTime, float partialTick) {
        List<AspectInstance> entries = contents.entries();
        if (entries.isEmpty()) {
            return -1;
        }
        float cycle = (gameTime + partialTick) / CYCLE_TICKS;
        int step = Mth.floor(cycle);
        int index = Math.floorMod(step, entries.size());
        int from = entries.get(Math.floorMod(index - 1, entries.size()))
                .aspect()
                .value()
                .color();
        int to = entries.get(index).aspect().value().color();
        return FastColor.ARGB32.color(FLUID_ALPHA, FastColor.ARGB32.lerp(cycle - step, from, to));
    }

    static int fluidLight(int light) {
        return LightTexture.pack(Math.max(LightTexture.block(light), MIN_BLOCK_LIGHT), LightTexture.sky(light));
    }
}
