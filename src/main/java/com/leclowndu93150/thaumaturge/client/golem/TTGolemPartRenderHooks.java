package com.leclowndu93150.thaumaturge.client.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemPartRenderHook;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemPartRenderView;
import com.leclowndu93150.thaumaturge.api.client.golems.RegisterGolemPartRenderHooksEvent;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.client.render.ItemRenderHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTGolemPartRenderHooks {
    private static final ResourceLocation CLAWS_MESH = TTIds.rl("models/mesh/golem_arms_claws.ttmesh");
    private static final ResourceLocation BREAKERS_MESH = TTIds.rl("models/mesh/golem_arms_breakers.ttmesh");
    private static final ResourceLocation DARTS_MESH = TTIds.rl("models/mesh/golem_arms_darter.ttmesh");
    private static final ResourceLocation WHEEL_MESH = TTIds.rl("models/mesh/golem_legs_wheel.ttmesh");
    private static final ResourceLocation HAULER_MESH = TTIds.rl("models/mesh/golem_hauler.ttmesh");
    private static final ResourceLocation PAULDRONS_MESH = TTIds.rl("models/mesh/golem_armor_pauldrons.ttmesh");

    private TTGolemPartRenderHooks() {}

    static float grinderRotation(float ageInTicks, float swingProgress) {
        float swingLeft = 1.0F - swingProgress;
        return 2.5F * ageInTicks + 720.0F * (1.0F - swingLeft * swingLeft * swingLeft);
    }

    @SubscribeEvent
    public static void onRegisterHooks(RegisterGolemPartRenderHooksEvent event) {
        event.register(CLAWS_MESH, new ClawJaws());
        event.register(BREAKERS_MESH, new GrinderSpin());
        event.register(DARTS_MESH, new LauncherAim());
        event.register(WHEEL_MESH, new WheelRoll());
        event.register(HAULER_MESH, new BasketCargo());
        event.register(PAULDRONS_MESH, new ShoulderMirror());
    }

    private static void rotateAboutX(PoseStack pose, float pivotY, float pivotZ, float degrees) {
        pose.translate(0.0F, pivotY, pivotZ);
        pose.mulPose(Axis.XP.rotationDegrees(degrees));
        pose.translate(0.0F, -pivotY, -pivotZ);
    }

    private static final class ClawJaws implements GolemPartRenderHook {
        private static final float MAX_OPEN_DEGREES = 34.0F;

        @Override
        public void posePiece(String piece, GolemPartRenderView golem, PoseStack pose, GolemPartModel.LimbSide side) {
            float direction = "claw001".equals(piece) ? 1.0F : "claw002".equals(piece) ? -1.0F : 0.0F;
            if (direction == 0.0F) {
                return;
            }
            float progress = golem.swingProgress();
            if (progress <= 0.0F) {
                return;
            }
            float squaredSwing = progress * progress;
            float open = MAX_OPEN_DEGREES * Mth.sin(squaredSwing * Mth.PI);
            rotateAboutX(pose, 0.01F, 0.0F, direction * open);
        }
    }

    private static final class GrinderSpin implements GolemPartRenderHook {
        @Override
        public void posePiece(String piece, GolemPartRenderView golem, PoseStack pose, GolemPartModel.LimbSide side) {
            if (!"grinder".equals(piece)) {
                return;
            }
            float spin = grinderRotation(golem.ageInTicks(), golem.swingProgress());
            float angle = side == GolemPartModel.LimbSide.LEFT ? -(spin + 25.0F) : spin;
            pose.mulPose(Axis.XP.rotationDegrees(angle));
        }
    }

    private static final class LauncherAim implements GolemPartRenderHook {
        @Override
        public float aimArmPitch(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigPitch) {
            return golem.fighting() ? -Mth.HALF_PI + golem.headPitch() * Mth.DEG_TO_RAD + rigPitch * 0.12F : rigPitch;
        }

        @Override
        public float aimArmYaw(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigYaw) {
            return golem.fighting() ? rigYaw * 0.12F : rigYaw;
        }

        @Override
        public float aimArmRoll(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigRoll) {
            return golem.fighting() ? rigRoll * 0.12F : rigRoll;
        }
    }

    private static final class WheelRoll implements GolemPartRenderHook {
        @Override
        public void posePiece(String piece, GolemPartRenderView golem, PoseStack pose, GolemPartModel.LimbSide side) {
            if ("wheel".equals(piece)) {
                pose.mulPose(Axis.XP.rotationDegrees(-golem.wheelRotation()));
            }
        }
    }

    private static final class BasketCargo implements GolemPartRenderHook {
        @Override
        public void decoratePiece(
                String piece,
                GolemPartRenderView golem,
                PoseStack pose,
                MultiBufferSource buffers,
                GolemPartModel.LimbSide side) {
            if (!"basket".equals(piece) || !golem.hasSecondHandItem()) {
                return;
            }
            pose.pushPose();
            if (golem.secondHandItemIsBlock()) {
                pose.translate(0.0F, 0.15F, 0.38F);
                pose.scale(0.5F, 0.5F, 0.5F);
            } else {
                pose.translate(0.0F, 0.07F, 0.38F);
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                pose.scale(0.6F, 0.6F, 0.6F);
            }
            ItemRenderHelper.render(
                    golem.secondHandItem(),
                    ItemDisplayContext.FIXED,
                    pose,
                    buffers,
                    golem.lightCoords(),
                    OverlayTexture.NO_OVERLAY,
                    0);
            pose.popPose();
        }
    }

    private static final class ShoulderMirror implements GolemPartRenderHook {
        @Override
        public void posePiece(String piece, GolemPartRenderView golem, PoseStack pose, GolemPartModel.LimbSide side) {
            if (side == GolemPartModel.LimbSide.LEFT) {
                pose.mulPose(Axis.YP.rotationDegrees(180.0F));
            }
        }
    }
}
