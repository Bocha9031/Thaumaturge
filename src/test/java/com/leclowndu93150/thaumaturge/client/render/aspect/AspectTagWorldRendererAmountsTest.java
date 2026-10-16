package com.leclowndu93150.thaumaturge.client.render.aspect;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.client.render.TTFlatRenderTypes;
import com.leclowndu93150.thaumaturge.compat.iris.IrisCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

final class AspectTagWorldRendererAmountsTest {
    @Test
    void queryOverloadOmitsAmountTextWhileLegacyOverloadsRetainIt() throws Exception {
        Minecraft minecraft = mock(Minecraft.class);
        Camera camera = mock(Camera.class);
        when(camera.getPosition()).thenReturn(Vec3.ZERO);
        GameRenderer gameRenderer = mock(GameRenderer.class);
        when(gameRenderer.getMainCamera()).thenReturn(camera);
        setMinecraftField(minecraft, "gameRenderer", gameRenderer);

        Font font = mock(Font.class);
        when(font.width("7")).thenReturn(1);
        setMinecraftField(minecraft, "font", font);
        MultiBufferSource.BufferSource buffers = mock(MultiBufferSource.BufferSource.class);
        RenderBuffers renderBuffers = mock(RenderBuffers.class);
        when(renderBuffers.bufferSource()).thenReturn(buffers);
        when(minecraft.renderBuffers()).thenReturn(renderBuffers);

        IAspect aspect = mock(IAspect.class);
        ResourceLocation texture = ResourceLocation.withDefaultNamespace("textures/test.png");
        when(aspect.texture()).thenReturn(texture);
        Holder<IAspect> holder = Holder.direct(aspect);
        AspectList aspects = AspectList.of(new AspectInstance(holder, 7));
        RenderType renderType = mock(RenderType.class);
        VertexConsumer vertexConsumer = mock(VertexConsumer.class, invocation -> invocation.getMock());
        when(buffers.getBuffer(renderType)).thenReturn(vertexConsumer);

        try (MockedStatic<TTFlatRenderTypes> renderTypes = mockStatic(TTFlatRenderTypes.class);
                MockedStatic<IrisCompat> iris = mockStatic(IrisCompat.class)) {
            renderTypes
                    .when(() -> TTFlatRenderTypes.entityTranslucentFlat(texture))
                    .thenReturn(renderType);
            iris.when(() -> IrisCompat.entityEffectBuffers(buffers)).thenReturn(buffers);

            AspectTagWorldRenderer.renderTagCloud(
                    new PoseStack(), minecraft, 0.0, 0.0, 0.0, aspects, Direction.UP, 0.3F, 0.75F);
            verifyAmountDraws(font, buffers, 2);

            clearInvocations(font);
            AspectTagWorldRenderer.renderTagCloud(
                    new PoseStack(), minecraft, 0.0, 0.0, 0.0, aspects, Direction.UP, 0.3F, 0.75F, entry -> true);
            verifyAmountDraws(font, buffers, 2);

            clearInvocations(font);
            AspectTagWorldRenderer.renderTagCloud(
                    new PoseStack(),
                    minecraft,
                    0.0,
                    0.0,
                    0.0,
                    aspects,
                    Direction.UP,
                    0.3F,
                    0.75F,
                    entry -> true,
                    false);
            verify(font, times(0))
                    .drawInBatch(
                            anyString(),
                            anyFloat(),
                            anyFloat(),
                            anyInt(),
                            anyBoolean(),
                            any(),
                            eq(buffers),
                            eq(Font.DisplayMode.NORMAL),
                            anyInt(),
                            anyInt());
        }
    }

    private static void verifyAmountDraws(Font font, MultiBufferSource.BufferSource buffers, int count) {
        verify(font, times(count))
                .drawInBatch(
                        anyString(),
                        anyFloat(),
                        anyFloat(),
                        anyInt(),
                        anyBoolean(),
                        any(),
                        eq(buffers),
                        eq(Font.DisplayMode.NORMAL),
                        anyInt(),
                        anyInt());
    }

    private static void setMinecraftField(Minecraft minecraft, String name, Object value) throws Exception {
        java.lang.reflect.Field field = Minecraft.class.getField(name);
        field.setAccessible(true);
        field.set(minecraft, value);
    }
}
