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

public final class ClericArmorModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;

    private final ModelPart frontFlap;
    private final ModelPart frontFlapHem;
    private final ModelPart backFlap;
    private final ModelPart backFlapHem;

    public ClericArmorModel(ModelPart root) {
        super(root);
        ModelPart robe = body.getChild("robe");
        frontFlap = robe.getChild("front_flap_upper");
        frontFlapHem = frontFlap.getChild("front_flap_lower");
        backFlap = robe.getChild("back_flap_upper");
        backFlapHem = backFlap.getChild("back_flap_lower");
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
        if (entity instanceof ArmorStand) {
            return;
        }

        float speed = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float stride = Math.abs(Mth.cos(limbSwing * 0.6662F)) * speed;
        float crouch = entity.isCrouching() ? 1.0F : 0.0F;
        float flutter = Mth.sin(ageInTicks * 0.16F) * (0.012F + speed * 0.028F);
        float trailing = entity.isFallFlying() || entity.isVisuallySwimming() ? 0.65F : 0.0F;
        float rightLegSwing = rightLeg.xRot - body.xRot;
        float leftLegSwing = leftLeg.xRot - body.xRot;
        float forwardLeg = Math.min(Math.min(0.0F, rightLegSwing), Math.min(0.0F, leftLegSwing));
        float backwardLeg = Math.max(Math.max(0.0F, rightLegSwing), Math.max(0.0F, leftLegSwing));
        float seatedLift = entity.isPassenger() ? 0.3F : 0.0F;

        frontFlap.xRot += forwardLeg - stride * 0.3F - Math.abs(body.yRot) - speed * 0.08F - crouch - seatedLift;
        frontFlap.y -= crouch;
        frontFlap.xScale = entity.isPassenger() ? 0.4F : 1.0F;
        frontFlapHem.xRot += -stride * 0.12F - flutter + crouch;
        backFlap.xRot -= backwardLeg * 1.25F + stride * 0.1F + flutter + crouch * 0.5F + trailing;
        backFlapHem.xRot -= stride * 0.12F + flutter * 1.5F;
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = createMesh();
        PartDefinition head = mesh.getRoot().getChild("head");
        PartDefinition hood = head.addOrReplaceChild(
                "hood",
                CubeListBuilder.create()
                        .texOffs(5, 47)
                        .addBox(-4.5F, -5.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(-0.1F)),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        hood.addOrReplaceChild(
                "hood_r1",
                CubeListBuilder.create().texOffs(16, 73).addBox(-4.0F, -5.0F, -1.0F, 4.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(2.0F, 3.0F, 4.5F, -0.48F, 0.0F, 0.0F));
        hood.addOrReplaceChild(
                "hood_r2",
                CubeListBuilder.create().texOffs(15, 65).addBox(-4.0F, -5.0F, -1.0F, 5.0F, 5.0F, 3.0F),
                PartPose.offsetAndRotation(1.5F, 1.0F, 4.0F, -0.2618F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.getChild("body");
        PartDefinition robe = body.addOrReplaceChild(
                "robe",
                CubeListBuilder.create()
                        .texOffs(64, 33)
                        .addBox(-4.0F, -9.0F, -6.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.45F))
                        .texOffs(0, 5)
                        .addBox(-4.0F, -7.5F, -8.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(-0.25F))
                        .texOffs(0, 21)
                        .addBox(-4.0F, -8.0F, -2.0F, 8.0F, 10.0F, 2.0F, new CubeDeformation(-0.25F))
                        .texOffs(22, 17)
                        .addBox(-5.0F, -3.0F, -7.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.5F))
                        .texOffs(22, 17)
                        .addBox(-5.0F, -8.0F, -7.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.5F))
                        .texOffs(1, 36)
                        .addBox(-2.5F, -5.65F, -8.25F, 5.0F, 5.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offset(0.0F, 9.0F, 4.0F));
        robe.addOrReplaceChild(
                "robe_r1",
                CubeListBuilder.create()
                        .texOffs(22, 31)
                        .addBox(-3.0F, -4.0F, -1.5F, 6.0F, 8.0F, 3.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-0.25F, -2.75F, 0.75F, 0.0F, 0.0F, 0.6109F));
        robe.addOrReplaceChild(
                "robe_r2",
                CubeListBuilder.create()
                        .texOffs(61, 49)
                        .addBox(-4.0F, -1.0F, -3.5F, 8.0F, 2.0F, 7.0F, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(0.0F, -8.0F, -3.5F, 0.1745F, 0.0F, 0.0F));
        PartDefinition frontFlapUpper = robe.addOrReplaceChild(
                "front_flap_upper",
                CubeListBuilder.create()
                        .texOffs(90, 14)
                        .addBox(-3.0F, -0.25F, -0.5F, 6.0F, 7.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 3.5F, -7.0F, -0.0873F, 0.0F, 0.0F));
        frontFlapUpper.addOrReplaceChild(
                "front_flap_lower",
                CubeListBuilder.create()
                        .texOffs(90, 22)
                        .addBox(-3.0F, -0.25F, -0.5F, 6.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
                        .texOffs(91, 26)
                        .addBox(-2.5F, 2.25F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, -0.0873F, 0.0F, 0.0F));
        PartDefinition backFlapUpper = robe.addOrReplaceChild(
                "back_flap_upper",
                CubeListBuilder.create()
                        .texOffs(90, 14)
                        .addBox(-3.0F, -0.25F, -0.5F, 6.0F, 7.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 3.5F, -1.0F, -0.0873F, 3.1416F, 0.0F));
        backFlapUpper.addOrReplaceChild(
                "back_flap_lower",
                CubeListBuilder.create()
                        .texOffs(90, 22)
                        .addBox(-3.0F, -0.25F, -0.5F, 6.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
                        .texOffs(91, 26)
                        .addBox(-2.5F, 2.25F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition rightArm = root.getChild("right_arm");
        PartDefinition rightSleeve = rightArm.addOrReplaceChild(
                "right_sleeve",
                CubeListBuilder.create()
                        .texOffs(88, 33)
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.29F))
                        .texOffs(89, 49)
                        .addBox(-1.5F, 5.25F, 2.0F, 3.0F, 5.0F, 1.0F)
                        .texOffs(97, 49)
                        .addBox(-1.0F, 7.25F, 2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.1F))
                        .texOffs(50, 0)
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.7F)),
                PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition rightMantle = rightSleeve.addOrReplaceChild(
                "right_mantle",
                CubeListBuilder.create()
                        .texOffs(40, 40)
                        .addBox(-3.25F, 0.25F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F))
                        .texOffs(40, 40)
                        .addBox(-2.75F, 2.25F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.51F)),
                PartPose.offsetAndRotation(0.5F, -0.25F, 0.0F, 0.0F, 0.0F, 0.1745F));
        rightMantle.addOrReplaceChild(
                "right_mantle_r1",
                CubeListBuilder.create().texOffs(41, 29).addBox(1.0F, -2.0F, -3.0F, 1.0F, 5.0F, 6.0F),
                PartPose.offsetAndRotation(-1.5F, -2.75F, 0.0F, 0.0F, 0.0F, -0.3491F));
        rightMantle.addOrReplaceChild(
                "right_mantle_r2",
                CubeListBuilder.create()
                        .texOffs(61, 19)
                        .addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-1.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

        PartDefinition leftArm = root.getChild("left_arm");
        PartDefinition leftSleeve = leftArm.addOrReplaceChild(
                "left_sleeve",
                CubeListBuilder.create()
                        .texOffs(88, 33)
                        .mirror()
                        .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.29F))
                        .mirror(false)
                        .texOffs(89, 49)
                        .mirror()
                        .addBox(-1.5F, 5.25F, 2.0F, 3.0F, 5.0F, 1.0F)
                        .mirror(false)
                        .texOffs(97, 49)
                        .mirror()
                        .addBox(-1.0F, 7.25F, 2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(-0.1F))
                        .mirror(false)
                        .texOffs(50, 0)
                        .mirror()
                        .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.7F))
                        .mirror(false),
                PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition leftMantle = leftSleeve.addOrReplaceChild(
                "left_mantle",
                CubeListBuilder.create()
                        .texOffs(40, 40)
                        .mirror()
                        .addBox(1.25F, 0.25F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F))
                        .mirror(false)
                        .texOffs(40, 40)
                        .mirror()
                        .addBox(0.75F, 2.25F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.51F))
                        .mirror(false),
                PartPose.offsetAndRotation(0.5F, -0.25F, 0.0F, 0.0F, 0.0F, -0.1745F));
        leftMantle.addOrReplaceChild(
                "left_mantle_r1",
                CubeListBuilder.create()
                        .texOffs(41, 29)
                        .mirror()
                        .addBox(-2.0F, -2.0F, -3.0F, 1.0F, 5.0F, 6.0F)
                        .mirror(false),
                PartPose.offsetAndRotation(1.5F, -2.75F, 0.0F, 0.0F, 0.0F, 0.3491F));
        leftMantle.addOrReplaceChild(
                "left_mantle_r2",
                CubeListBuilder.create()
                        .texOffs(61, 19)
                        .mirror()
                        .addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F))
                        .mirror(false),
                PartPose.offsetAndRotation(1.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.3491F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition rightLeg = root.getChild("right_leg");
        PartDefinition rightSkirt = rightLeg.addOrReplaceChild(
                "right_skirt", CubeListBuilder.create(), PartPose.rotation(0.0F, 0.0F, 0.0349F));
        for (int index = 0; index < 3; index++) {
            rightSkirt.addOrReplaceChild(
                    "right_skirt_r" + (index + 1),
                    CubeListBuilder.create().texOffs(25, 9).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F),
                    PartPose.offsetAndRotation(-1.45F, 7.0F - index * 2.0F, -2.5F, -0.1745F, 0.0F, 0.0F));
        }
        rightSkirt.addOrReplaceChild(
                "right_skirt_panel",
                CubeListBuilder.create().texOffs(105, 32).addBox(-2.0F, 1.0F, -2.5F, 5.0F, 8.0F, 5.0F),
                PartPose.rotation(0.0F, 0.0F, 0.1745F));

        PartDefinition leftLeg = root.getChild("left_leg");
        PartDefinition leftSkirt = leftLeg.addOrReplaceChild(
                "left_skirt", CubeListBuilder.create(), PartPose.rotation(0.0F, 0.0F, -0.0349F));
        for (int index = 0; index < 3; index++) {
            leftSkirt.addOrReplaceChild(
                    "left_skirt_r" + (index + 1),
                    CubeListBuilder.create().texOffs(25, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 1.0F),
                    PartPose.offsetAndRotation(1.5F, 7.0F - index * 2.0F, -2.5F, -0.1745F, 0.0F, 0.0F));
        }
        leftSkirt.addOrReplaceChild(
                "left_skirt_panel",
                CubeListBuilder.create()
                        .texOffs(105, 32)
                        .mirror()
                        .addBox(-3.0F, 1.0F, -2.5F, 5.0F, 8.0F, 5.0F)
                        .mirror(false),
                PartPose.rotation(0.0F, 0.0F, -0.1745F));
        root.getChild("body")
                .addOrReplaceChild(
                        "hip",
                        CubeListBuilder.create()
                                .texOffs(91, 0)
                                .addBox(-5.0F, 9.5F, -3.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.4F)),
                        PartPose.ZERO);
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = KnightArmorModel.emptyMesh();
        PartDefinition body = mesh.getRoot().getChild("body");
        PartDefinition robe = KnightArmorModel.emptyChild(body, "robe");
        PartDefinition frontUpper = KnightArmorModel.emptyChild(robe, "front_flap_upper");
        KnightArmorModel.emptyChild(frontUpper, "front_flap_lower");
        PartDefinition backUpper = KnightArmorModel.emptyChild(robe, "back_flap_upper");
        KnightArmorModel.emptyChild(backUpper, "back_flap_lower");
        return mesh;
    }
}
