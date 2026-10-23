package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.effect.rendertype.TTFXRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.PortalStrip;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistPortalLesser;
import com.leclowndu93150.thaumaturge.content.entity.boss.EntityCultistPortalGreater;
import com.leclowndu93150.thaumaturge.content.entity.portal.CultistPortal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class CultistPortalRenderer<T extends LivingEntity & CultistPortal> extends EntityRenderer<T, CultistPortalRenderer.State> {
    private static final Identifier TEXTURE = TTIds.rl("textures/misc/cultist_portal.png");
    private static final float LESSER_SHADOW_RADIUS = 0.0F;
    private static final float LESSER_SCALE_Y = 1.4F;
    private static final float LESSER_SCALE_FACTOR = 1.25F;
    private static final float GREATER_SHADOW_RADIUS = 0.1F;
    private static final float GREATER_SCALE_Y = 1.5F;
    private static final float GREATER_SCALE_FACTOR = 1.3F;
    private static final int FRAME_COUNT = 32;
    private static final float FULL_GROWTH_TICKS = 50.0F;
    private static final float HURT_DEGREES_PER_TICK = 72.0F;
    private static final float HURT_SHRINK_DIVISOR = 4.0F;
    private static final float HURT_GROWTH = 6.0F;
    private static final float PULSE_DEGREES_PER_TICK = 36.0F;
    private static final float PULSE_GROW_DIVISOR = 4.0F;
    private static final float PULSE_GROWTH = 12.0F;
    private static final float DAMAGE_DIVISOR = 3.0F;
    private static final float SHIMMER_HEIGHT_PERIOD_BASE = 5.0F;
    private static final float SHIMMER_HEIGHT_PERIOD_RATE = 12.0F;
    private static final float SHIMMER_WIDTH_PERIOD_BASE = 6.0F;
    private static final float SHIMMER_WIDTH_PERIOD_RATE = 15.0F;
    private static final float SHIMMER_HEIGHT_DIVISOR = 4.0F;
    private static final float SHIMMER_WIDTH_DIVISOR = 3.0F;
    private static final float HALF_TURN_DEGREES = 180.0F;

    private final float baseScaleY;
    private final float scaleFactor;

    private CultistPortalRenderer(EntityRendererProvider.Context context, float shadowRadius, float baseScaleY, float scaleFactor) {
        super(context);
        this.shadowRadius = shadowRadius;
        this.baseScaleY = baseScaleY;
        this.scaleFactor = scaleFactor;
    }

    public static CultistPortalRenderer<EntityCultistPortalLesser> lesser(EntityRendererProvider.Context context) {
        return new CultistPortalRenderer<>(context, LESSER_SHADOW_RADIUS, LESSER_SCALE_Y, LESSER_SCALE_FACTOR);
    }

    public static CultistPortalRenderer<EntityCultistPortalGreater> greater(EntityRendererProvider.Context context) {
        return new CultistPortalRenderer<>(context, GREATER_SHADOW_RADIUS, GREATER_SCALE_Y, GREATER_SCALE_FACTOR);
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.active = entity.isPortalOpen();
        state.activeCounter = entity.portalAge() + partialTicks;
        state.hurtTime = entity.hurtTime;
        state.pulse = entity.pulseTicks();
        state.healthFraction = entity.getHealth() / entity.getMaxHealth();
        state.halfHeight = entity.getBbHeight() / 2.0F;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, poseStack, collector, camera);
        if (!state.active) {
            return;
        }
        float baseWidth = Math.min(FULL_GROWTH_TICKS, state.activeCounter) / FULL_GROWTH_TICKS * scaleFactor;
        PortalContribution total = hurtContribution(state).plus(pulseContribution(state)).plus(shimmerContribution(state));
        float sx = baseWidth + total.widthDelta();
        float sy = baseScaleY + total.heightDelta();
        int frame = FRAME_COUNT - 1 - Math.floorMod(Mth.floor(state.activeCounter), FRAME_COUNT);
        int tint = ARGB.white(total.alphaFactor());
        poseStack.pushPose();
        poseStack.translate(0.0F, state.halfHeight, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(HALF_TURN_DEGREES - camera.yRot));
        collector.submitCustomGeometry(poseStack, TTFXRenderTypes.translucent(TEXTURE), (pose, buffer) -> PortalStrip.write(pose, buffer, sx, sy, frame, FRAME_COUNT, tint));
        poseStack.popPose();
    }

    private PortalContribution hurtContribution(State state) {
        if (state.hurtTime <= 0) {
            return PortalContribution.NONE;
        }
        float wobble = PortalWave.sineByDegrees(state.hurtTime, HURT_DEGREES_PER_TICK);
        return new PortalContribution(-wobble / HURT_SHRINK_DIVISOR, growthToWidth(HURT_GROWTH * wobble), 1.0F);
    }

    private PortalContribution pulseContribution(State state) {
        if (state.pulse <= 0) {
            return PortalContribution.NONE;
        }
        float beat = PortalWave.sineByDegrees(state.pulse, PULSE_DEGREES_PER_TICK);
        return new PortalContribution(beat / PULSE_GROW_DIVISOR, growthToWidth(PULSE_GROWTH * beat), 1.0F);
    }

    private static PortalContribution shimmerContribution(State state) {
        float amplitude = (1.0F - state.healthFraction) / DAMAGE_DIVISOR;
        float heightPeriod = PortalWave.period(SHIMMER_HEIGHT_PERIOD_BASE, SHIMMER_HEIGHT_PERIOD_RATE, amplitude);
        float widthPeriod = PortalWave.period(SHIMMER_WIDTH_PERIOD_BASE, SHIMMER_WIDTH_PERIOD_RATE, amplitude);
        float heightBob = PortalWave.offsetSine(state.activeCounter, heightPeriod, amplitude);
        float widthBob = PortalWave.offsetSine(state.activeCounter, widthPeriod, amplitude);
        float alpha = Mth.clamp(1.0F - heightBob, 0.0F, 1.0F);
        return new PortalContribution(-heightBob / SHIMMER_HEIGHT_DIVISOR, -widthBob / SHIMMER_WIDTH_DIVISOR, alpha);
    }

    private float growthToWidth(float growthTicks) {
        return growthTicks / FULL_GROWTH_TICKS * scaleFactor;
    }

    public static final class State extends EntityRenderState {
        public boolean active;
        public float activeCounter;
        public int hurtTime;
        public int pulse;
        public float healthFraction;
        public float halfHeight;
    }
}
