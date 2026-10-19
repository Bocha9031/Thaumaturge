package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.OccludingEffectRenderer;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.content.entity.portal.CultistPortals;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.phys.Vec3;

public final class CultistPortalRenderer extends EntityRenderer<EntityCultistPortalLesser> {
    private static final ResourceLocation TEXTURE = TTIds.rl("textures/misc/cultist_portal.png");

    private static final int FRAMES = 32;
    private static final float FRAME_WIDTH = 1.0F / FRAMES;
    private static final float PORTAL_HEIGHT = 2.9F;
    private static final float PORTAL_WIDTH = 2.3F;
    private static final int LIGHT = 0x00F000DC;

    public CultistPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(
            EntityCultistPortalLesser entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
        queuePortal(
                entity.getPosition(partialTicks),
                entity.isActive(),
                entity.activeCounter + partialTicks,
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
    public ResourceLocation getTextureLocation(EntityCultistPortalLesser entity) {
        return TEXTURE;
    }

    static void queuePortal(
            Vec3 position,
            boolean active,
            float activeCounter,
            int hurtTime,
            int pulse,
            float healthFraction,
            float halfHeight,
            float time,
            float partialTicks,
            float height,
            float width) {
        if (!active) {
            return;
        }
        float damage = PortalAppearance.damage(healthFraction);
        float flinch = PortalAppearance.flinch(hurtTime, partialTicks);
        float beat = PortalAppearance.beat(pulse, partialTicks, CultistPortals.PULSE_TICKS);
        float scaleY = PortalAppearance.halfHeight(height, damage, time, flinch, beat);
        float scaleX = PortalAppearance.halfWidth(width, damage, time, flinch, beat)
                * PortalAppearance.openWidth(activeCounter);
        int frame = Math.floorMod((int) time, FRAMES);
        float u0 = frame * FRAME_WIDTH;
        float u1 = u0 + FRAME_WIDTH;
        int tint = ARGB32.colorFromFloat(PortalAppearance.opacity(damage, time), 1.0F, 1.0F, 1.0F);
        OccludingEffectRenderer.enqueuePortal(position.add(0.0, halfHeight, 0.0), scaleX, scaleY, u0, u1, tint, LIGHT);
    }
}
