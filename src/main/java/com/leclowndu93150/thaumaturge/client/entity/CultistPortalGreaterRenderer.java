package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistPortalGreater;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class CultistPortalGreaterRenderer extends EntityRenderer<EntityCultistPortalGreater> {
    private static final float PORTAL_HEIGHT = 3.2F;
    private static final float PORTAL_WIDTH = 2.6F;
    private static final float SHADOW = 0.15F;

    public CultistPortalGreaterRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = SHADOW;
    }

    @Override
    public void render(
            EntityCultistPortalGreater entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        CultistPortalRenderer.queuePortal(
                entity.getPosition(partialTicks),
                true,
                entity.tickCount + partialTicks,
                entity.hurtTime,
                entity.pulse,
                entity.getHealth() / entity.getMaxHealth(),
                entity.getBbHeight() / 2.0F,
                entity.tickCount + partialTicks,
                partialTicks,
                PORTAL_HEIGHT,
                PORTAL_WIDTH);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityCultistPortalGreater entity) {
        return TTIds.rl("textures/misc/cultist_portal.png");
    }
}
