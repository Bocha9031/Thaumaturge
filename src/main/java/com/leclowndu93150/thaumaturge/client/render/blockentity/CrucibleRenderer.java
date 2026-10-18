package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.content.crucible.BlockEntityCrucible;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public final class CrucibleRenderer implements BlockEntityRenderer<BlockEntityCrucible> {

    public CrucibleRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(
            BlockEntityCrucible crucible,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffers,
            int light,
            int overlay) {
        FluidStack fluid = crucible.getTank().getFluid();
        if (fluid.isEmpty()) return;
        float height = crucible.getFluidHeight();

        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ext.getStillTexture(fluid));
        int color = crucible.getLevel() instanceof ClientLevel level
                ? ext.getTintColor(fluid.getFluid().defaultFluidState(), level, crucible.getBlockPos())
                : ext.getTintColor(fluid);

        float recolor = (float) crucible.getAspects().totalAmount() / BlockEntityCrucible.MAX_ASPECT;
        if (recolor > 0.0F) {
            recolor = 0.5F + recolor / 2F;
        }
        if (recolor > 1F) recolor = 1F;
        float r = Mth.lerp(recolor, ARGB32.red(color) / 255.0F, 0.25F);
        float g = Mth.lerp(recolor, ARGB32.green(color) / 255.0F, 0.0F);
        float b = Mth.lerp(recolor, ARGB32.blue(color) / 255.0F, 0.75F);
        color = ARGB32.colorFromFloat(1F, r, g, b);

        float min = 0 / 16F;
        float max = 16 / 16F;
        VertexConsumer wrapped = sprite.wrap(buffers.getBuffer(Sheets.translucentCullBlockSheet()));
        PoseStack.Pose pose = poseStack.last();
        JarRenderer.addVertex(wrapped, pose, min, height, min, min, min, color, light, 0, 1, 0);
        JarRenderer.addVertex(wrapped, pose, min, height, max, min, max, color, light, 0, 1, 0);
        JarRenderer.addVertex(wrapped, pose, max, height, max, max, max, color, light, 0, 1, 0);
        JarRenderer.addVertex(wrapped, pose, max, height, min, max, min, color, light, 0, 1, 0);
    }
}
