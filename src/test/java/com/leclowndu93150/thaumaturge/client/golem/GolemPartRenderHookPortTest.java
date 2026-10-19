package com.leclowndu93150.thaumaturge.client.golem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.leclowndu93150.thaumaturge.api.client.golems.GolemPartRenderHook;
import com.leclowndu93150.thaumaturge.api.client.golems.GolemPartRenderView;
import com.leclowndu93150.thaumaturge.api.client.golems.RegisterGolemPartRenderHooksEvent;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

class GolemPartRenderHookPortTest {
    @Test
    void eventRejectsDuplicateMeshAndReturnsImmutableSnapshot() {
        RegisterGolemPartRenderHooksEvent event = new RegisterGolemPartRenderHooksEvent();
        ResourceLocation mesh = ResourceLocation.fromNamespaceAndPath("test", "mesh");
        GolemPartRenderHook hook = new GolemPartRenderHook() {};
        event.register(mesh, hook);

        assertSame(hook, event.hooks().get(mesh));
        assertThrows(IllegalArgumentException.class, () -> event.register(mesh, new GolemPartRenderHook() {}));
        assertThrows(UnsupportedOperationException.class, () -> event.hooks().clear());

        GolemPartRenderHooks.install(event.hooks());
        try {
            GolemPartModel first = new GolemPartModel(mesh, null, GolemPartModel.AttachPoint.ARMS);
            GolemPartModel second = new GolemPartModel(mesh, null, GolemPartModel.AttachPoint.LEGS);
            assertNotSame(first, second);
            assertSame(hook, GolemPartRenderHooks.hookFor(first));
            assertSame(hook, GolemPartRenderHooks.hookFor(second));
        } finally {
            GolemPartRenderHooks.install(Map.of());
        }
    }

    @Test
    void launcherFormulaPreservesRadiansAndOnlyAimsWhileFighting() {
        RegisterGolemPartRenderHooksEvent event = new RegisterGolemPartRenderHooksEvent();
        TTGolemPartRenderHooks.onRegisterHooks(event);
        GolemPartRenderHook launcher = event.hooks()
                .get(ResourceLocation.fromNamespaceAndPath("thaumaturge", "models/mesh/golem_arms_darter.ttmesh"));
        GolemPartRenderView view = new TestView(0.25F, 12.0F, 30.0F, true, 0.0F, 0, 0, false, false, ItemStack.EMPTY);

        assertEquals(
                -((float) Math.PI / 2.0F) + 30.0F * ((float) Math.PI / 180.0F) + 0.2F * 0.12F,
                launcher.aimArmPitch(view, GolemPartModel.LimbSide.LEFT, 0.2F),
                1.0E-6F);
        assertEquals(0.3F * 0.12F, launcher.aimArmYaw(view, GolemPartModel.LimbSide.RIGHT, 0.3F), 1.0E-6F);

        GolemPartRenderView resting = new TestView(0.0F, 0.0F, 30.0F, false, 0.0F, 0, 0, false, false, ItemStack.EMPTY);
        assertEquals(0.2F, launcher.aimArmPitch(resting, GolemPartModel.LimbSide.LEFT, 0.2F), 0.0F);
    }

    @Test
    void clawUsesSourceSwingCurveAndOnlyMovesTheTwoNamedJaws() {
        RegisterGolemPartRenderHooksEvent event = new RegisterGolemPartRenderHooksEvent();
        TTGolemPartRenderHooks.onRegisterHooks(event);
        GolemPartRenderHook claws = event.hooks()
                .get(ResourceLocation.fromNamespaceAndPath("thaumaturge", "models/mesh/golem_arms_claws.ttmesh"));
        PoseStack restPose = new PoseStack();
        PoseStack openPose = new PoseStack();
        GolemPartRenderView resting = new TestView(0.0F, 0.0F, 0.0F, false, 0.0F, 0, 0, false, false, ItemStack.EMPTY);
        GolemPartRenderView swinging = new TestView(0.5F, 0.0F, 0.0F, false, 0.0F, 0, 0, false, false, ItemStack.EMPTY);
        claws.posePiece("claw001", resting, restPose, GolemPartModel.LimbSide.MIDDLE);
        claws.posePiece("claw001", swinging, openPose, GolemPartModel.LimbSide.MIDDLE);
        Vector3f restTip = restPose.last().pose().transformPosition(0.0F, 0.01F, 1.0F, new Vector3f());
        Vector3f openTip = openPose.last().pose().transformPosition(0.0F, 0.01F, 1.0F, new Vector3f());
        float openDegrees = 34.0F * (float) Math.sin(0.25F * Math.PI);
        float expectedDeltaY = -(float) Math.sin(Math.toRadians(openDegrees));

        assertEquals(expectedDeltaY, openTip.y - restTip.y, 1.0E-4F);
        assertEquals(0.01F, restTip.y, 0.0F);

        PoseStack unrelatedPose = new PoseStack();
        claws.posePiece("claw003", swinging, unrelatedPose, GolemPartModel.LimbSide.MIDDLE);
        Vector3f unrelatedTip = unrelatedPose.last().pose().transformPosition(0.0F, 0.01F, 1.0F, new Vector3f());
        assertEquals(0.01F, unrelatedTip.y, 0.0F);
    }

    @Test
    void grinderUsesMainSpinRateAndCubicAttackEase() {
        assertEquals(25.0F, TTGolemPartRenderHooks.grinderRotation(10.0F, 0.0F), 0.0F);
        assertEquals(655.0F, TTGolemPartRenderHooks.grinderRotation(10.0F, 0.5F), 0.0F);
        assertEquals(745.0F, TTGolemPartRenderHooks.grinderRotation(10.0F, 1.0F), 0.0F);
    }

    @Test
    void armHooksChainInPartOrderForBothArms() {
        GolemProperties props = GolemProperties.createDefault();
        ResourceLocation headMesh = ResourceLocation.fromNamespaceAndPath("test", "head_mesh");
        ResourceLocation armMesh = TTGolemParts.ARMS_DARTS.get().models().get(0).objModel();
        GolemHead head = new GolemHead(
                List.of(),
                ResourceLocation.fromNamespaceAndPath("test", "head_icon"),
                new GolemPartModel(headMesh, null, GolemPartModel.AttachPoint.HEAD),
                List.of(),
                null,
                List.of());
        RegisterGolemPartRenderHooksEvent event = new RegisterGolemPartRenderHooksEvent();
        event.register(headMesh, new GolemPartRenderHook() {
            @Override
            public float aimArmPitch(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigPitch) {
                return rigPitch + 0.25F;
            }
        });
        event.register(armMesh, new GolemPartRenderHook() {
            @Override
            public float aimArmPitch(GolemPartRenderView golem, GolemPartModel.LimbSide side, float rigPitch) {
                return rigPitch * 2.0F;
            }
        });

        GolemProperties darts = props.withHead(head).withArms(TTGolemParts.ARMS_DARTS.get());
        GolemRenderState state = new GolemRenderState();
        state.props = darts;
        CopperGolemRig rig = new CopperGolemRig(CopperGolemRig.createBodyLayer().bakeRoot());
        rig.setupAnim(state);
        float basePitch = rig.root().getChild("body").getChild("right_arm").xRot;
        float baseLeftPitch = rig.root().getChild("body").getChild("left_arm").xRot;

        GolemPartRenderHooks.install(event.hooks());
        try {
            rig.setupAnim(state);
            float expectedRight = (basePitch + 0.25F) * 2.0F;
            float expectedLeft = (baseLeftPitch + 0.25F) * 2.0F;
            assertTrue(Float.isFinite(expectedRight));
            assertEquals(expectedRight, rig.root().getChild("body").getChild("right_arm").xRot, 1.0E-6F);
            assertEquals(expectedLeft, rig.root().getChild("body").getChild("left_arm").xRot, 1.0E-6F);
        } finally {
            GolemPartRenderHooks.install(Map.of());
        }
    }

    private record TestView(
            float swingProgress,
            float ageInTicks,
            float headPitch,
            boolean fighting,
            float wheelRotation,
            int lightCoords,
            int outlineColor,
            boolean hasSecondHandItem,
            boolean secondHandItemIsBlock,
            ItemStack secondHandItem)
            implements GolemPartRenderView {}
}
