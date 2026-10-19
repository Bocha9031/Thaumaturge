package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.entity.TaintSporeModel;
import com.leclowndu93150.thaumaturge.content.taint.entity.EntityTaintSpore;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Translucent, slowly swelling Taint Spore renderer. */
public final class TaintSporeRenderer extends MobRenderer<EntityTaintSpore, TaintSporeModel<EntityTaintSpore>> {
    static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/taint_spore.png");
    private static final float SHADOW = 0.25F;
    private static final float MIN_SCALE = 0.01F;
    private static final float BASE_SCALE = 0.3F;
    private static final float SCALE_PER_SIZE = 0.075F;
    private static final float BREATH_DEPTH = 0.06F;
    private static final float BREATH_PERIOD_TICKS = 70.0F;

    public TaintSporeRenderer(EntityRendererProvider.Context context) {
        super(context, new TaintSporeModel<>(context.bakeLayer(TTModelLayers.TAINT_SPORE)), SHADOW);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityTaintSpore entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EntityTaintSpore entity, PoseStack poseStack, float partialTick) {
        float age = entity.tickCount + partialTick;
        float size = Math.max(MIN_SCALE, BASE_SCALE + entity.getDisplaySize(partialTick) * SCALE_PER_SIZE);
        float breath = BREATH_DEPTH * Mth.sin(age * Mth.TWO_PI / BREATH_PERIOD_TICKS);
        float across = Math.max(MIN_SCALE, size * (1.0F + breath));
        float tall = Math.max(MIN_SCALE, size * (1.0F - breath));
        poseStack.scale(across, tall, across);
    }
}
