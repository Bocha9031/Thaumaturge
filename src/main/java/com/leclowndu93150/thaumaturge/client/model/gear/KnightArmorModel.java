package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

public final class KnightArmorModel extends AbstractTTArmorModel {
    static final int TEXTURE_WIDTH = 256;
    static final int TEXTURE_HEIGHT = 128;

    private final ModelPart tabard;
    private final ModelPart tabardHem;
    private final ModelPart cape;

    public KnightArmorModel(ModelPart root) {
        super(root);
        ModelPart chestplate = body.getChild("chestplate");
        tabard = chestplate.getChild("tabard_upper").getChild("tabard_middle");
        tabardHem = tabard.getChild("tabard_lower");
        cape = chestplate.getChild("cape_upper");
    }

    @Override
    public void setupAnim(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entity instanceof ArmorStand) return;

        float speed = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float stride = Math.abs(Mth.cos(limbSwing * 0.6662F)) * speed;
        float crouch = entity.isCrouching() ? 1.0F : 0.0F;
        float flutter = Mth.sin(ageInTicks * 0.16F) * (0.012F + speed * 0.028F);
        float forwardLeg = Math.min(0.0F, Math.min(leftLeg.xRot, rightLeg.xRot) - body.xRot);
        tabard.xRot += forwardLeg - stride * 0.3F - Math.abs(body.yRot) - speed * 0.08F - crouch;
        tabard.y -= crouch;
        tabard.xScale = entity.isPassenger() ? 0.4F : 1.0F;
        tabardHem.xRot += -stride * 0.12F - flutter + crouch;
        KnightCapeSway.sample(entity, ageInTicks - entity.tickCount).swing(cape);
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = createMesh();
        PartDefinition head = mesh.getRoot().getChild("head");
        PartDefinition helmet = head.addOrReplaceChild(
                "helmet",
                CubeListBuilder.create().texOffs(0, 0).addBox(-8, -12, -8, 16, 16, 16, new CubeDeformation(-3.4F)),
                PartPose.ZERO);
        PartDefinition faceplate =
                helmet.addOrReplaceChild("faceplate", CubeListBuilder.create(), PartPose.offset(0, -4.65F, -0.35F));
        faceplate.addOrReplaceChild(
                "faceplate_r1",
                CubeListBuilder.create()
                        .texOffs(70, 31)
                        .addBox(-8, -3.6F, -3.4F, 16, 11, 11, new CubeDeformation(-3.15F)),
                PartPose.offsetAndRotation(0, 1.15F, -5.3F, 0.1309F, 0, 0));
        faceplate.addOrReplaceChild(
                "faceplate_r2",
                CubeListBuilder.create()
                        .texOffs(70, 3)
                        .addBox(-8, -8.6F, -3.4F, 16, 12, 11, new CubeDeformation(-3.1F)),
                PartPose.offsetAndRotation(0, 0.5F, -5.25F, -0.1745F, 0, 0));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        PartDefinition chestplate = body.addOrReplaceChild(
                "chestplate",
                CubeListBuilder.create()
                        .texOffs(131, 5)
                        .addBox(-4, -8, -8, 8, 8, 2, new CubeDeformation(-0.25F))
                        .texOffs(131, 21)
                        .addBox(-4, -8, -2, 8, 10, 2, new CubeDeformation(-0.25F))
                        .texOffs(131, 15)
                        .addBox(-4, -1.5F, -8.25F, 8, 4, 2, new CubeDeformation(-0.2F))
                        .texOffs(153, 17)
                        .addBox(-5, -3, -7.5F, 10, 4, 7, new CubeDeformation(-0.5F))
                        .texOffs(153, 17)
                        .addBox(-5, -8, -7.5F, 10, 4, 7, new CubeDeformation(-0.5F)),
                PartPose.offset(0, 9, 4));
        PartDefinition tabardUpper = chestplate.addOrReplaceChild(
                "tabard_upper",
                CubeListBuilder.create()
                        .texOffs(133, 42)
                        .addBox(-3, -0.25F, -0.5F, 6, 8, 1, new CubeDeformation(-0.2F)),
                PartPose.offset(0, -7, -8));
        PartDefinition tabardMiddle = tabardUpper.addOrReplaceChild(
                "tabard_middle",
                CubeListBuilder.create()
                        .texOffs(133, 51)
                        .addBox(-3, -0.25F, -0.5F, 6, 7, 1, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0, 7.5F, 0, -0.0873F, 0, 0));
        tabardMiddle.addOrReplaceChild(
                "tabard_lower",
                CubeListBuilder.create()
                        .texOffs(133, 59)
                        .addBox(-3, -0.25F, -0.5F, 6, 3, 1, new CubeDeformation(-0.2F))
                        .texOffs(134, 63)
                        .addBox(-2.5F, 2.25F, -0.5F, 5, 2, 1, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0, 6.5F, 0, -0.0873F, 0, 0));
        PartDefinition capeUpper = chestplate.addOrReplaceChild(
                "cape_upper",
                CubeListBuilder.create()
                        .texOffs(152, 42)
                        .addBox(-5, -0.25F, -0.5F, 10, 8, 1, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0, -8, 0, 0.0873F, 0, 0));
        PartDefinition capeMiddle = capeUpper.addOrReplaceChild(
                "cape_middle",
                CubeListBuilder.create()
                        .texOffs(152, 51)
                        .addBox(-5, -0.25F, -0.5F, 10, 7, 1, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0, 7.5F, 0, -0.1309F, 0, 0));
        capeMiddle.addOrReplaceChild(
                "cape_lower",
                CubeListBuilder.create()
                        .texOffs(152, 59)
                        .addBox(-5, -0.25F, -0.5F, 10, 3, 1, new CubeDeformation(-0.2F))
                        .texOffs(152, 63)
                        .addBox(-5, 2.25F, -0.5F, 10, 2, 1, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0, 6.5F, 0, 0.1745F, 0, 0));
        addShoulder(root, true);
        addShoulder(root, false);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static void addShoulder(PartDefinition root, boolean right) {
        boolean mirror = !right;
        String armName = right ? "right_arm" : "left_arm";
        String shoulderName = right ? "right_shoulder" : "left_shoulder";
        String plateName = right ? "right_pauldron" : "left_pauldron";
        PartDefinition shoulder = root.getChild(armName)
                .addOrReplaceChild(
                        shoulderName,
                        CubeListBuilder.create()
                                .texOffs(181, 0)
                                .mirror(mirror)
                                .addBox(-4, -4, -4, 8, 8, 8, new CubeDeformation(-1.7F))
                                .texOffs(215, 0)
                                .mirror(mirror)
                                .addBox(right ? -4 : -2, 2, -4, 6, 9, 8, new CubeDeformation(-1.5F))
                                .texOffs(163, 8)
                                .mirror(mirror)
                                .addBox(right ? 0 : -2, 5.5F, -2.5F, 2, 3, 5, new CubeDeformation(-0.2F)),
                        PartPose.offset(right ? -1 : 1, 0, 0));
        CubeListBuilder plate = CubeListBuilder.create()
                .texOffs(192, 19)
                .mirror(mirror)
                .addBox(right ? -4 : -2, -3, -3, 6, 4, 6, new CubeDeformation(-0.5F))
                .texOffs(194, 29)
                .mirror(mirror)
                .addBox(right ? -3.5F : -0.5F, 0, -3, 4, 3, 6, new CubeDeformation(-0.5F));
        shoulder.addOrReplaceChild(
                plateName,
                plate,
                PartPose.offsetAndRotation(right ? 0.5F : -0.5F, -0.25F, 0, 0, 0, right ? 0.1745F : -0.1745F));
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition right = root.getChild("right_leg");
        right.addOrReplaceChild(
                "right_tasset",
                CubeListBuilder.create()
                        .texOffs(220, 30)
                        .addBox(-4.6F, -2, -4, 8, 8, 8, new CubeDeformation(-1.5F))
                        .texOffs(221, 47)
                        .addBox(-4.1F, 1.5F, -4, 7, 8, 8, new CubeDeformation(-1.7F)),
                PartPose.rotation(0, 0, 0.2094F));
        root.getChild("left_leg")
                .addOrReplaceChild(
                        "left_tasset",
                        CubeListBuilder.create()
                                .texOffs(220, 30)
                                .mirror()
                                .addBox(-3.4F, -2, -4, 8, 8, 8, new CubeDeformation(-1.5F))
                                .mirror(false)
                                .texOffs(221, 47)
                                .mirror()
                                .addBox(-2.9F, 1.5F, -4, 7, 8, 8, new CubeDeformation(-1.7F))
                                .mirror(false),
                        PartPose.rotation(0, 0, -0.2094F));
        root.getChild("body")
                .addOrReplaceChild(
                        "hip",
                        CubeListBuilder.create()
                                .texOffs(219, 18)
                                .addBox(-5, 9.5F, -3.5F, 10, 4, 7, new CubeDeformation(-0.4F)),
                        PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = emptyMesh();
        PartDefinition body = mesh.getRoot().getChild("body");
        PartDefinition chestplate = emptyChild(body, "chestplate");
        PartDefinition tabardUpper = emptyChild(chestplate, "tabard_upper");
        PartDefinition tabardMiddle = emptyChild(tabardUpper, "tabard_middle");
        emptyChild(tabardMiddle, "tabard_lower");
        PartDefinition capeUpper = emptyChild(chestplate, "cape_upper");
        PartDefinition capeMiddle = emptyChild(capeUpper, "cape_middle");
        emptyChild(capeMiddle, "cape_lower");
        return mesh;
    }

    static MeshDefinition emptyMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        emptyChild(body, "frontcloth1");
        emptyChild(body, "frontcloth2");
        emptyChild(body, "cloak1");
        emptyChild(body, "cloak2");
        emptyChild(body, "cloak3");
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12, 0));
        return mesh;
    }

    static PartDefinition emptyChild(PartDefinition parent, String name) {
        return parent.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.ZERO);
    }
}
