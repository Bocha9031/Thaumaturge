package com.leclowndu93150.thaumaturge.client.golem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.registry.TTGolemAccessories;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class GolemVisualPortTest {
    @Test
    void nativeCopperRigKeepsVanillaPartLayoutAndDimensions() {
        CopperGolemRig rig = new CopperGolemRig(CopperGolemRig.createBodyLayer().bakeRoot());
        ModelPart body = rig.root().getChild("body");
        ModelPart head = body.getChild("head");

        assertEquals(24.0F, rig.root().y);
        assertEquals(-5.0F, body.y);
        assertEquals(-6.0F, head.y);
        assertEquals(-4.0F, body.getChild("right_arm").x);
        assertEquals(4.0F, body.getChild("left_arm").x);
        assertTrue(head.hasChild("antenna"));

        ModelPart rightLeg = rig.root().getChild("right_leg");
        ModelPart leftLeg = rig.root().getChild("left_leg");
        assertEquals(0.0F, rightLeg.x);
        assertEquals(0.0F, leftLeg.x);
        assertEquals(-5.0F, rightLeg.y);
        assertEquals(-5.0F, leftLeg.y);
        assertEquals(0.0F, rightLeg.zRot);
        assertEquals(0.0F, leftLeg.zRot);
        assertCubeDimensions(rightLeg, 4.0F, 5.0F, 4.0F);
        assertCubeDimensions(leftLeg, 4.0F, 5.0F, 4.0F);
        assertEquals(-4.0F, rightLeg.getRandomCube(RandomSource.create(1L)).minX);
        assertEquals(0.0F, leftLeg.getRandomCube(RandomSource.create(1L)).minX);

        ModelPart.Cube bodyCube = body.getRandomCube(RandomSource.create(1L));
        assertEquals(8.0F, bodyCube.maxX - bodyCube.minX, 1.0E-6F);
        assertEquals(6.0F, bodyCube.maxY - bodyCube.minY, 1.0E-6F);
        assertEquals(6.0F, bodyCube.maxZ - bodyCube.minZ, 1.0E-6F);

        List<ModelPart.Cube> headCubes = new ArrayList<>();
        head.visit(new PoseStack(), (pose, path, index, cube) -> {
            if (path.isEmpty()) {
                headCubes.add(cube);
            }
        });
        assertEquals(2, headCubes.size());
        assertEquals(10.0F, headCubes.get(0).maxZ - headCubes.get(0).minZ, 1.0E-6F);
        assertEquals(2.0F, headCubes.get(1).maxX - headCubes.get(1).minX, 1.0E-6F);
    }

    @Test
    void antennaVisibilityFollowsMaterialAndHat() {
        CopperGolemRig rig = new CopperGolemRig(CopperGolemRig.createBodyLayer().bakeRoot());
        ModelPart antenna = rig.root().getChild("body").getChild("head").getChild("antenna");
        GolemProperties props = GolemProperties.createDefault();
        GolemRenderState state = new GolemRenderState();
        state.props = props;

        rig.setupAnim(state);
        assertFalse(antenna.visible);

        state.props = props.withMaterial(materialWithAntenna(true));
        rig.setupAnim(state);
        assertTrue(antenna.visible);

        state.accessories = List.of(TTGolemAccessories.TOP_HAT);
        rig.setupAnim(state);
        assertFalse(antenna.visible);
    }

    @Test
    void rendererUsesCopperReplacementAsRequiredNativeFallback() {
        GolemRenderer renderer = mock(GolemRenderer.class, CALLS_REAL_METHODS);
        EntityThaumaturgeGolem golem = mock(EntityThaumaturgeGolem.class);

        assertEquals(GolemSkins.COPPER, renderer.getTextureLocation(golem));
    }

    private static GolemMaterial materialWithAntenna(boolean antenna) {
        return new GolemMaterial(
                List.of(),
                GolemSkins.COPPER,
                0,
                0,
                0,
                0,
                () -> ItemStack.EMPTY,
                () -> ItemStack.EMPTY,
                List.of(),
                antenna);
    }

    private static void assertCubeDimensions(ModelPart part, float width, float height, float depth) {
        ModelPart.Cube cube = part.getRandomCube(RandomSource.create(1L));
        assertEquals(width, cube.maxX - cube.minX, 1.0E-6F);
        assertEquals(height, cube.maxY - cube.minY, 1.0E-6F);
        assertEquals(depth, cube.maxZ - cube.minZ, 1.0E-6F);
    }
}
