package com.leclowndu93150.thaumaturge.api.client.golems;

import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Animates named mesh pieces and the rig arms for a golem part model. Hooks are called on the render
 * thread for every golem part using the registered mesh, including golems rendered as items. They
 * should not retain the per-frame view. Client only.
 */
public interface GolemPartRenderHook {
    /** Adjusts the pose immediately before a named mesh piece is drawn, including x-ray passes. */
    default void posePiece(String piece, GolemPartRenderView golem, PoseStack pose, GolemPartModel.LimbSide side) {}

    /** Draws extra content after a mesh piece. This callback is skipped for x-ray passes. */
    default void decoratePiece(
            String piece,
            GolemPartRenderView golem,
            PoseStack pose,
            MultiBufferSource buffers,
            GolemPartModel.LimbSide side) {}

    /** Adjusts arm pitch in radians; hooks on all equipped parts are chained in part order. */
    default float aimArmPitch(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigPitch) {
        return rigPitch;
    }

    /** Adjusts arm yaw in radians; hooks on all equipped parts are chained in part order. */
    default float aimArmYaw(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigYaw) {
        return rigYaw;
    }

    /** Adjusts arm roll in radians; hooks on all equipped parts are chained in part order. */
    default float aimArmRoll(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigRoll) {
        return rigRoll;
    }
}
