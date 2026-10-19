package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.FloatyLineRenderer;
import com.leclowndu93150.thaumaturge.client.effect.OccludingEffectRenderer;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistCleric;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.phys.Vec3;

public final class CultistClericRenderer
        extends HumanoidMobRenderer<EntityCultistCleric, HumanoidModel<EntityCultistCleric>> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/entity/cultist.png");
    private static final float SHADOW = 0.5F;
    private static final int LINE_COLOR = ARGB32.color(255, 30, 10, 42);
    private static final float LINE_SPEED = -0.025F;
    private static final float LINE_WIDTH = 0.14F;

    public CultistClericRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(TTModelLayers.CULTIST)), SHADOW);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public void render(
            EntityCultistCleric entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light) {
        float age = entity.tickCount + partialTicks;
        boolean ritualist = entity.isRitualist();
        float bob = ritualist ? CultistClericRenderMath.bob(age, entity.getId()) : 0.0F;
        float lineStartY = CultistClericRenderMath.lineStartY(entity.getBbHeight(), bob);
        Vec3 position = entity.getPosition(partialTicks);
        poseStack.pushPose();
        poseStack.translate(0.0F, bob, 0.0F);
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, light);
        poseStack.popPose();
        if (ritualist) {
            Vec3 focus = entity.ritualFocus();
            Vec3 fromFocusToHead = CultistClericRenderMath.fromFocusToHead(position, focus, lineStartY);
            float time = FloatyLineRenderer.time(entity.level().getGameTime(), partialTicks);
            OccludingEffectRenderer.enqueueBeam(
                    position,
                    focus,
                    fromFocusToHead,
                    time,
                    LINE_COLOR,
                    LINE_SPEED,
                    CultistClericRenderMath.fade(age),
                    LINE_WIDTH);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCultistCleric entity) {
        return TEXTURE;
    }
}
