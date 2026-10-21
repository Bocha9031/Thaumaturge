package com.leclowndu93150.thaumaturge.client.render.blockentity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.BlockEldritchLock;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.BlockEntityEldritchLock;
import com.leclowndu93150.thaumaturge.content.eldritch.lock.LockDoorway;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class EldritchLockRenderer implements BlockEntityRenderer<BlockEntityEldritchLock, EldritchLockRenderState> {
    private static final Identifier CUBE_TEXTURE = TTIds.rl("textures/entity/eldritch_cube.png");
    private static final int ARM_COUNT = 4;
    private static final int ARM_STAGGER = 5;
    private static final int CUBE_LIFETIME = 20;
    private static final int MAX_CUBES = 4;
    private static final int LAST_CUBE = 4;
    private static final float PULSE_AMPLITUDE = 0.1F;
    private static final float PULSE_PERIOD = 20.0F;
    private static final float PULSE_CUBE_STEP = 10.0F;
    private static final float PULSE_ARM_STEP = 20.0F;
    private static final float END_CUBE_PULSE_BASE = 0.2F;
    private static final float CUBE_LENGTH = 0.5F;
    private static final float CUBE_OFFSET = 0.25F;
    private static final float CENTER = 0.5F;
    private static final float HALF = 0.5F;
    private static final float TEXTURE_SIZE = 64.0F;
    private static final int[][] FACE_RECTANGLES = {{16, 0, 32, 16}, {32, 0, 48, 16}, {0, 16, 16, 32}, {16, 16, 32, 32}, {32, 16, 48, 32}, {48, 16, 64, 32}};
    private static final Direction[] FACE_DIRECTIONS = {Direction.UP, Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH};
    private static final int[][] CORNERS = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
    private static final float[] LOCAL_UP = {0.0F, 1.0F, 0.0F};
    private static final int WHITE = 0xFFFFFFFF;
    private static final float FACE_OFFSET = 0.5F;
    private static final float TABLET_LIFT = 0.025F;
    private static final float PORTAL_LIFT = 0.005F;
    private static final float PORTAL_HALF_SPAN = 2.5F;
    private static final float TABLET_Y = 0.41F;
    private static final float TABLET_SCALE = 1.025641F;
    private static final float FLIP_DEGREES = 180.0F;
    private static final float NORTH_YAW = 180.0F;
    private static final float WEST_YAW = 270.0F;
    private static final float EAST_YAW = 90.0F;
    private static final float BOUNDS_MARGIN = 3.0F;
    private static final int VIEW_DISTANCE = 64;
    private static final int OUTLINE_NONE = 0;
    private static final int DISPLAY_SEED = 0;

    private final ItemModelResolver itemModelResolver;
    private @Nullable ItemStack tabletStack;

    public EldritchLockRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public EldritchLockRenderState createRenderState() {
        return new EldritchLockRenderState();
    }

    @Override
    public void extractRenderState(BlockEntityEldritchLock lock, EldritchLockRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(lock, state, partialTicks, cameraPosition, breakProgress);
        state.count = lock.getCount();
        state.facing = lock.getBlockState().getValue(BlockEldritchLock.FACING);
        Level level = lock.getLevel();
        state.shift = 0.0F;
        if (level != null) {
            state.shift = LockDoorway.centreShift(level, lock.getBlockPos(), state.facing);
            state.lightCoords = LevelRenderer.getLightCoords(level, lock.getBlockPos().relative(state.facing));
        }
        state.animationTime = EldritchObeliskRenderer.animationTime(partialTicks);
        state.tablet = null;
        if (state.count >= 0) {
            if (tabletStack == null) {
                tabletStack = new ItemStack(TTItems.RUNED_TABLET.get());
            }
            state.tablet = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(state.tablet, tabletStack, ItemDisplayContext.FIXED, lock.getLevel(), null, DISPLAY_SEED);
        }
    }

    @Override
    public void submit(EldritchLockRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        int light = state.lightCoords;
        Direction facing = state.facing;
        int count = state.count;
        float time = state.animationTime;
        float[] hub = hub(facing, state.shift);
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(CUBE_TEXTURE), (pose, buffer) -> writeArms(pose, buffer, hub, facing, count, time, light));
        if (state.tablet != null) {
            poseStack.pushPose();
            poseStack.translate(hub[0] + TABLET_LIFT * facing.getStepX(), TABLET_Y + hub[1] - CENTER, hub[2] + TABLET_LIFT * facing.getStepZ());
            poseStack.mulPose(Axis.YP.rotationDegrees(tabletYaw(facing) + FLIP_DEGREES));
            poseStack.scale(TABLET_SCALE, TABLET_SCALE, TABLET_SCALE);
            state.tablet.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, OUTLINE_NONE);
            poseStack.popPose();
        }
        BlockPos pos = state.blockPos;
        collector.submitCustomGeometry(poseStack, EldritchPortalSurface.SURFACE, (pose, buffer) -> writePortal(pose, buffer, pos, facing, hub));
    }

    private static float[] hub(Direction facing, float shift) {
        Direction across = LockDoorway.across(facing);
        return new float[]{CENTER + FACE_OFFSET * facing.getStepX() + shift * across.getStepX(), CENTER + FACE_OFFSET * facing.getStepY() + shift * across.getStepY(),
                CENTER + FACE_OFFSET * facing.getStepZ() + shift * across.getStepZ()};
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntityEldritchLock lock) {
        return new AABB(lock.getBlockPos()).inflate(BOUNDS_MARGIN);
    }

    @Override
    public int getViewDistance() {
        return VIEW_DISTANCE;
    }

    private static float tabletYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> NORTH_YAW;
            case WEST -> WEST_YAW;
            case EAST -> EAST_YAW;
            default -> 0.0F;
        };
    }

    private static void writePortal(PoseStack.Pose pose, VertexConsumer buffer, BlockPos pos, Direction facing, float[] hub) {
        float x0 = hub[0] - PORTAL_HALF_SPAN;
        float x1 = hub[0] + PORTAL_HALF_SPAN;
        float y0 = hub[1] - PORTAL_HALF_SPAN;
        float y1 = hub[1] + PORTAL_HALF_SPAN;
        float z0 = hub[2] - PORTAL_HALF_SPAN;
        float z1 = hub[2] + PORTAL_HALF_SPAN;
        float px = hub[0] + PORTAL_LIFT * facing.getStepX();
        float py = hub[1] + PORTAL_LIFT * facing.getStepY();
        float pz = hub[2] + PORTAL_LIFT * facing.getStepZ();
        switch (facing.getAxis()) {
            case Z -> EldritchPortalSurface.quad(pose, buffer, pos, x0, y0, pz, x0, y1, pz, x1, y1, pz, x1, y0, pz);
            case X -> EldritchPortalSurface.quad(pose, buffer, pos, px, y0, z0, px, y1, z0, px, y1, z1, px, y0, z1);
            case Y -> EldritchPortalSurface.quad(pose, buffer, pos, x0, py, z0, x1, py, z0, x1, py, z1, x0, py, z1);
        }
    }

    private static void writeArms(PoseStack.Pose pose, VertexConsumer buffer, float[] hub, Direction facing, int count, float time, int light) {
        float[] axis = {facing.getStepX(), facing.getStepY(), facing.getStepZ()};
        boolean vertical = facing.getAxis() == Direction.Axis.Y;
        for (int index = 0; index < ARM_COUNT; index++) {
            int turns = vertical ? 0 : index;
            int cubes = MAX_CUBES - (count + ARM_STAGGER * index) / CUBE_LIFETIME;
            for (int cube = 1; cube <= cubes; cube++) {
                float pulse = PULSE_AMPLITUDE * Mth.sin((time + PULSE_CUBE_STEP * cube + PULSE_ARM_STEP * index) / PULSE_PERIOD);
                if (cube == 1 || cube == LAST_CUBE) {
                    pulse = pulse / 2.0F + END_CUBE_PULSE_BASE;
                }
                writeCube(pose, buffer, hub, axis, turns, cube, pulse, light);
            }
        }
    }

    private static float[] turn(float[] axis, float[] vector, int turns) {
        float[] result = vector;
        for (int step = 0; step < turns; step++) {
            result = quarterTurn(axis, result);
        }
        return result;
    }

    private static float[] quarterTurn(float[] axis, float[] vector) {
        float dot = axis[0] * vector[0] + axis[1] * vector[1] + axis[2] * vector[2];
        return new float[]{axis[1] * vector[2] - axis[2] * vector[1] + axis[0] * dot, axis[2] * vector[0] - axis[0] * vector[2] + axis[1] * dot,
                axis[0] * vector[1] - axis[1] * vector[0] + axis[2] * dot};
    }

    private static void writeCube(PoseStack.Pose pose, VertexConsumer buffer, float[] hub, float[] axis, int turns, int cube, float pulse, int light) {
        float[] direction = turn(axis, LOCAL_UP, turns);
        float distance = CUBE_OFFSET + CUBE_LENGTH * cube;
        float[] centre = {hub[0] + direction[0] * distance, hub[1] + direction[1] * distance, hub[2] + direction[2] * distance};
        float[] half = {(CUBE_LENGTH + pulse) * HALF, CUBE_LENGTH * HALF, (CUBE_LENGTH + pulse) * HALF};
        for (int face = 0; face < FACE_DIRECTIONS.length; face++) {
            writeFace(pose, buffer, FACE_DIRECTIONS[face], FACE_RECTANGLES[face], half, centre, axis, turns, light);
        }
    }

    private static void writeFace(PoseStack.Pose pose, VertexConsumer buffer, Direction face, int[] rectangle, float[] half, float[] centre, float[] axis, int turns, int light) {
        float[] normal = {face.getStepX(), face.getStepY(), face.getStepZ()};
        float[] up = face.getAxis() == Direction.Axis.Y ? new float[]{0.0F, 0.0F, 1.0F} : LOCAL_UP;
        float[] right = {up[1] * normal[2] - up[2] * normal[1], up[2] * normal[0] - up[0] * normal[2], up[0] * normal[1] - up[1] * normal[0]};
        float[] worldNormal = turn(axis, normal, turns);
        float uMin = rectangle[0] / TEXTURE_SIZE;
        float vMin = rectangle[1] / TEXTURE_SIZE;
        float uMax = rectangle[2] / TEXTURE_SIZE;
        float vMax = rectangle[3] / TEXTURE_SIZE;
        float[] us = {uMin, uMax, uMax, uMin};
        float[] vs = {vMax, vMax, vMin, vMin};
        for (int corner = 0; corner < CORNERS.length; corner++) {
            int r = CORNERS[corner][0];
            int u = CORNERS[corner][1];
            float[] offset = {half[0] * (normal[0] + r * right[0] + u * up[0]), half[1] * (normal[1] + r * right[1] + u * up[1]), half[2] * (normal[2] + r * right[2] + u * up[2])};
            float[] world = turn(axis, offset, turns);
            buffer.addVertex(pose, centre[0] + world[0], centre[1] + world[1], centre[2] + world[2]).setColor(WHITE).setUv(us[corner], vs[corner]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                    .setNormal(pose, worldNormal[0], worldNormal[1], worldNormal[2]);
        }
    }
}
