package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.api.wands.render.WandRenderContext;
import com.leclowndu93150.thaumaturge.api.wands.render.WandRenderers;
import com.leclowndu93150.thaumaturge.client.casters.FocusColors;
import com.leclowndu93150.thaumaturge.client.render.BoxGeometry;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.client.render.TTRenderTypes;
import com.leclowndu93150.thaumaturge.content.casters.SocketedFocus;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class WandItemSpecialRenderer extends BlockEntityWithoutLevelRenderer {
    public record WandArg(WandCap cap, WandRod rod, boolean sceptre, boolean hasFocus, int focusColor) {}

    private static final ResourceLocation WAND_TEXTURE = TTIds.rl("textures/models/wand.png");
    private static final ResourceLocation SCRIPT_TEXTURE = TTIds.rl("textures/misc/script.png");

    private static final RenderType RUNES = TTRenderTypes.entityAdditiveEmissive(SCRIPT_TEXTURE);

    private static final float PX = 0.0625F;
    private static final int TEX_W = 32;
    private static final int TEX_H = 32;
    private static final int RUNE_LIGHT = 200;
    private static final float MODEL_LIFT = 0.5F;
    private static final float FOCUS_ALPHA = 0.95F;
    private static final int SCRIPT_GLYPHS = 16;
    private static final float STAFF_MODEL_SHIFT = 0.2F;
    private static final float FOCUS_STAFF_LIFT = -0.0475F;
    private static final float FOCUS_STAFF_SCALE_Y = 0.5525F;
    private static final float FOCUS_SCALE = 0.5F;
    private static final float FOCUS_TOP_PX = 6.0F;
    private static final float CAP_TOP_PX = 1.0F;
    private static final float CAP_STAFF_SCALE_Y = 1.1F;
    private static final float SCEPTRE_CAP_SCALE = 1.3F;

    public WandItemSpecialRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(
            ItemStack stack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        WandArg arg = extract(stack);
        poseStack.pushPose();
        poseStack.translate(0.5F, MODEL_LIFT, 0.5F);
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        WandRenderers.render(context(stack, arg, poseStack, buffers, light, overlay, displayContext, false));
        poseStack.popPose();
    }

    private record CapPlacement(float offsetY, float scaleXZ, float scaleY, float pivotPx) {}

    private static final float CAP_PIVOT_BOTTOM_PX = 20.0F;

    public static void submitParts(WandArg arg, PoseStack poseStack, MultiBufferSource buffers, int light) {
        submitParts(arg, poseStack, buffers, light, false);
    }

    /**
     * Submits the wand model. First-person hands use vanilla's cutout layer so Iris can route the solid rod and caps
     * through its dedicated hand pass.
     */
    public static void submitParts(
            WandArg arg, PoseStack poseStack, MultiBufferSource buffers, int light, boolean firstPersonHand) {
        submitParts(arg, poseStack, buffers, light, firstPersonHand, 0xFFFFFFFF, 0xFFFFFFFF);
    }

    public static void submitParts(
            WandArg arg,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            boolean firstPersonHand,
            int rodTint,
            int capTint) {
        boolean staff = arg.rod().staff();
        float ticks = clientTicks();

        poseStack.pushPose();
        if (staff) {
            poseStack.translate(0.0F, STAFF_MODEL_SHIFT, 0.0F);
        }
        submitRod(arg, poseStack, buffers, light, staff, ticks, firstPersonHand, rodTint);
        submitCaps(arg, poseStack, buffers, light, staff, firstPersonHand, capTint);
        if (arg.hasFocus()) {
            submitFocus(arg, poseStack, buffers, staff, ticks);
        }
        for (WandRuneLayout.Rune rune :
                WandRuneLayout.create(arg.sceptre(), arg.rod().runes(), staff, ticks)) {
            submitRune(poseStack.last(), buffers.getBuffer(RUNES), rune, ticks);
        }
        poseStack.popPose();
    }

    private static void submitRod(
            WandArg arg,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            boolean staff,
            float ticks,
            boolean firstPersonHand,
            int rodTint) {
        int rodLight = arg.rod().glow() ? (int) (200.0F + Mth.sin((int) ticks) * 5.0F + 5.0F) : light;
        RenderType rodType = firstPersonHand
                ? RenderType.entityCutoutNoCull(arg.rod().texture())
                : TTFlatRenderTypes.entityCutoutFlat(arg.rod().texture());
        poseStack.pushPose();
        if (staff) {
            poseStack.translate(0.0F, -0.1F, 0.0F);
            poseStack.scale(1.2F, 2.0F, 1.2F);
        }
        box(poseStack.last(), buffers.getBuffer(rodType), -1.0F, 1.0F, -1.0F, 2, 18, 2, 0, 8, rodTint, rodLight);
        poseStack.popPose();
    }

    private static List<CapPlacement> capLayout(boolean staff, boolean sceptre) {
        List<CapPlacement> layout = new ArrayList<>();
        if (sceptre) {
            layout.add(new CapPlacement(0.0F, SCEPTRE_CAP_SCALE, SCEPTRE_CAP_SCALE, 0.0F));
            layout.add(new CapPlacement(0.3F, 1.0F, 0.66F, 0.0F));
        } else {
            layout.add(new CapPlacement(0.0F, 1.0F, 1.0F, 0.0F));
        }
        if (staff) {
            layout.add(new CapPlacement(0.225F, 1.0F, 0.66F, 0.0F));
            layout.add(new CapPlacement(0.875F, 1.0F, 1.0F, CAP_PIVOT_BOTTOM_PX));
        } else {
            layout.add(new CapPlacement(0.0F, 1.0F, 1.0F, CAP_PIVOT_BOTTOM_PX));
        }
        return layout;
    }

    private static void submitCaps(
            WandArg arg,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            boolean staff,
            boolean firstPersonHand,
            int capTint) {
        RenderType capType = firstPersonHand
                ? RenderType.entityCutoutNoCull(arg.cap().texture())
                : TTFlatRenderTypes.entityCutoutFlat(arg.cap().texture());
        poseStack.pushPose();
        if (staff) {
            poseStack.scale(1.3F, CAP_STAFF_SCALE_Y, 1.3F);
        } else {
            poseStack.scale(1.2F, 1.0F, 1.2F);
        }
        for (CapPlacement placement : capLayout(staff, arg.sceptre())) {
            poseStack.pushPose();
            poseStack.translate(0.0F, placement.offsetY(), 0.0F);
            poseStack.scale(placement.scaleXZ(), placement.scaleY(), placement.scaleXZ());
            poseStack.translate(0.0F, placement.pivotPx() * PX, 0.0F);
            box(poseStack.last(), buffers.getBuffer(capType), -1.0F, -1.0F, -1.0F, 2, 2, 2, 0, 0, capTint, light);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static void submitFocus(
            WandArg arg, PoseStack poseStack, MultiBufferSource buffers, boolean staff, float ticks) {
        RenderType focusType = TTFlatRenderTypes.entityTranslucentFlat(WAND_TEXTURE);
        poseStack.pushPose();
        if (staff) {
            poseStack.translate(0.0F, FOCUS_STAFF_LIFT, 0.0F);
            poseStack.scale(0.525F, FOCUS_STAFF_SCALE_Y, 0.525F);
        } else {
            poseStack.scale(FOCUS_SCALE, FOCUS_SCALE, FOCUS_SCALE);
        }
        int tint = ARGB32.color((int) (FOCUS_ALPHA * 255.0F), arg.focusColor());
        int focusLight = (int) (195.0F + Mth.sin(ticks / 3.0F) * 10.0F + 10.0F);
        box(poseStack.last(), buffers.getBuffer(focusType), -3.0F, -6.0F, -3.0F, 6, 6, 6, 0, 0, tint, focusLight);
        poseStack.popPose();
    }

    public static float tipModelY(WandArg arg) {
        boolean staff = arg.rod().staff();
        if (arg.hasFocus()) {
            if (staff) {
                return STAFF_MODEL_SHIFT + FOCUS_STAFF_LIFT - FOCUS_TOP_PX * PX * FOCUS_STAFF_SCALE_Y;
            }
            return -FOCUS_TOP_PX * PX * FOCUS_SCALE;
        }
        if (staff) {
            return STAFF_MODEL_SHIFT - CAP_TOP_PX * PX * CAP_STAFF_SCALE_Y;
        }
        if (arg.sceptre()) {
            return -CAP_TOP_PX * PX * SCEPTRE_CAP_SCALE;
        }
        return -CAP_TOP_PX * PX;
    }

    private static float clientTicks() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null
                ? 0.0F
                : player.tickCount + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
    }

    private static final float[] RUNE_CORNER_X = {-1.0F, 1.0F, 1.0F, -1.0F};
    private static final float[] RUNE_CORNER_Y = {-1.0F, -1.0F, 1.0F, 1.0F};
    private static final float HALF_UV = 0.5F;
    private static final float RUNE_HALF_BASE = 1.0F;
    private static final float RUNE_SIZE_PULSE = 0.14F;
    private static final float RUNE_ALPHA_BASE = 0.62F;
    private static final float RUNE_ALPHA_PULSE = 0.3F;
    private static final float RUNE_GREEN_BASE = 0.66F;
    private static final float RUNE_GREEN_PULSE = 0.08F;

    private static void submitRune(PoseStack.Pose pose, VertexConsumer buffer, WandRuneLayout.Rune rune, float ticks) {
        float yaw = rune.yawDegrees() * Mth.DEG_TO_RAD;
        float outX = Mth.sin(yaw);
        float outZ = Mth.cos(yaw);
        float rightX = -outZ;
        float rightZ = outX;
        float pulse = WandRuneLayout.pulse(ticks, rune.slot());
        float swell = WandRuneLayout.swell(ticks, rune.slot());
        float half = rune.halfSize() * RUNE_HALF_BASE * (1.0F + swell * RUNE_SIZE_PULSE);
        float green = Mth.clamp(RUNE_GREEN_BASE + pulse * RUNE_GREEN_PULSE, 0.0F, 1.0F);
        float alpha = Mth.clamp(RUNE_ALPHA_BASE + pulse * RUNE_ALPHA_PULSE, 0.0F, 1.0F);
        int tint = ARGB32.colorFromFloat(alpha, 1.0F, green, 0.18F);
        float centerX = outX * rune.radius();
        float centerZ = outZ * rune.radius();
        float glyphU = rune.glyph() / (float) SCRIPT_GLYPHS;
        float glyphWidth = 1.0F / SCRIPT_GLYPHS;
        for (int corner = 0; corner < RUNE_CORNER_X.length; corner++) {
            float cornerX = RUNE_CORNER_X[corner];
            float cornerY = RUNE_CORNER_Y[corner];
            float x = centerX + rightX * cornerX * half;
            float y = rune.y() + cornerY * half;
            float z = centerZ + rightZ * cornerX * half;
            float u = glyphU + (cornerX + 1.0F) * HALF_UV * glyphWidth;
            float v = (cornerY + 1.0F) * HALF_UV;
            buffer.addVertex(pose.pose(), x, y, z)
                    .setColor(tint)
                    .setUv(u, v)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(RUNE_LIGHT)
                    .setNormal(pose, outX, 0.0F, outZ);
        }
    }

    private static void box(
            PoseStack.Pose pose,
            VertexConsumer buffer,
            float x,
            float y,
            float z,
            int dx,
            int dy,
            int dz,
            int u,
            int v,
            int tint,
            int light) {
        BoxGeometry.box(
                pose,
                buffer,
                x * PX,
                y * PX,
                z * PX,
                (x + dx) * PX,
                (y + dy) * PX,
                (z + dz) * PX,
                u,
                v,
                dx,
                dy,
                dz,
                TEX_W,
                TEX_H,
                tint,
                light,
                true);
    }

    public static WandArg extract(ItemStack stack) {
        WandParts parts = WandVisHelper.getParts(stack);
        ItemStack focusStack = ItemStack.EMPTY;
        SocketedFocus template = stack.get(TTDataComponents.SOCKETED_FOCUS.get());
        if (template != null) {
            focusStack = template.focus();
        }
        boolean hasFocus = FocusItems.isFocus(focusStack);
        int color = hasFocus ? FocusColors.of(focusStack) : 0xFFFFFF;
        return new WandArg(parts.cap(), parts.rod(), parts.sceptre(), hasFocus, color);
    }

    public static WandRenderContext context(
            ItemStack stack,
            WandArg arg,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay,
            @Nullable ItemDisplayContext displayContext,
            boolean firstPersonHand) {
        return new WandRenderContext(
                stack,
                arg.cap(),
                arg.rod(),
                arg.sceptre(),
                arg.hasFocus(),
                arg.focusColor(),
                poseStack,
                buffers,
                light,
                overlay,
                displayContext,
                firstPersonHand,
                (rodTint, capTint) -> submitParts(arg, poseStack, buffers, light, firstPersonHand, rodTint, capTint));
    }
}
