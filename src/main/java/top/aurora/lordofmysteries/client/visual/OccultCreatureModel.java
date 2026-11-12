package top.aurora.lordofmysteries.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;

public final class OccultCreatureModel<T extends Mob>
        extends HierarchicalModel<T> implements ArmedModel {
    private final ModelPart root;
    private final CreatureVisual visual;

    public OccultCreatureModel(ModelPart baked, CreatureVisual visual) {
        this.root = baked.getChild("root");
        this.visual = visual;
    }

    public static LayerDefinition createLayer(CreatureVisual visual) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild(
                "root", CubeListBuilder.create(), PartPose.offset(0, 24, 0));
        int skin = visual.surface;
        int trim = visual.accent;
        switch (visual.shape) {
            case HUMANOID -> {
                box(root, "body", skin, -5, -23, -3, 10, 14, 6);
                box(root, "coat", skin, -6, -12, -3.5f, 12, 4, 7);
                pivotBox(root, "head", trim, -3.5f, -7, -3.5f, 7, 7, 7, 0, -24, 0);
                pivotBox(root, "left_arm", skin, -1.5f, 0, -1.5f, 3, 14, 3, 6.5f, -23, 0);
                pivotBox(root, "right_arm", skin, -1.5f, 0, -1.5f, 3, 14, 3, -6.5f, -23, 0);
                pivotBox(root, "left_leg", trim, -1.5f, 0, -2, 3, 9, 4, 3, -9, 0);
                pivotBox(root, "right_leg", trim, -1.5f, 0, -2, 3, 9, 4, -3, -9, 0);
                box(root.getChild("head"), "eyes", 15, -2.5f, -4.5f, -3.65f, 5, 1, 0.3f);
                if (visual == CreatureVisual.SLEEPLESS_SHADE) {
                    box(root.getChild("head"), "hood", 0, -4.5f, -8, -3, 9, 9, 8);
                    box(root, "moon_left", 4, -3, -20, -3.2f, 1, 5, 0.5f);
                    box(root, "moon_lower", 4, -2, -16, -3.2f, 4, 1, 0.5f);
                    tendrils(root, 8, 5, 0, -10, 3);
                } else if (visual == CreatureVisual.SEER_BREAKDOWN) {
                    box(root, "crown", 14, -5, -32, -5, 10, 1, 10);
                    box(root, "crown_top", 0, -3, -36, -3, 6, 4, 6);
                    tendrils(root, 0, 4, 0, -22, 4);
                } else if (visual == CreatureVisual.ASHEN_PUPPET) {
                    box(root, "crossbar", 9, -8, -35, -1, 16, 1, 2);
                    for (int index = 0; index < 3; index++) {
                        box(root, "string_" + index, 15, index * 6 - 6, -34, 0, 0.25f, 14, 0.25f);
                    }
                } else if (visual == CreatureVisual.SCRIBE_GOLEM
                        || visual == CreatureVisual.APPRENTICE_BREAKDOWN) {
                    for (int index = 0; index < 3; index++) {
                        box(root, "folio_" + index, 1, index * 5 - 6, -27 + index * 3, 4, 4, 8, 2);
                    }
                    box(root, "collar", 3, -7, -24, -4, 14, 2, 8);
                } else if (visual == CreatureVisual.SPIRIT_FERRYMAN) {
                    box(root, "hood", 0, -5, -32, -4, 10, 9, 9);
                    box(root, "face", 2, -3, -30, -4.2f, 6, 6, 0.5f);
                    box(root, "oar", 9, -10, -32, 0, 1, 31, 1);
                    box(root, "paddle", 9, -12, -9, 0, 5, 8, 1);
                    box(root, "lantern", 11, 7, -13, -2, 4, 5, 4);
                } else if (visual == CreatureVisual.PYROMANIAC_BREAKDOWN
                        || visual == CreatureVisual.WAR_ROSE_HUSK) {
                    tendrils(root, trim, 6, 0, -25, 2);
                    box(root, "furnace", 13, -2, -21, -3.2f, 4, 6, 0.5f);
                } else if (visual == CreatureVisual.PSYCHIATRIST_BREAKDOWN) {
                    box(root, "mask_left", 4, -8, -28, -1, 4, 5, 2);
                    box(root, "mask_right", 4, 4, -28, -1, 4, 5, 2);
                } else {
                    tendrils(root, trim, 4, 0, -12, 3);
                }
            }
            case SERPENT, LEECH -> {
                for (int index = 0; index < 6; index++) {
                    float width = Math.max(1.5f, 6 - index * 0.7f);
                    pivotBox(root, "segment_" + index, skin, -width / 2, -width, -2,
                            width, width, 4, 0, 0, index * 3 - 5);
                }
                box(root, "head", trim, -3, -7, -9, 6, 5, 5);
                box(root, "eyes", 15, -2.5f, -5, -9.1f, 5, 1, 0.3f);
                if (visual == CreatureVisual.TWIN_SERPENT) {
                    box(root, "second_head", trim, 2, -8, -6, 5, 5, 5);
                    box(root, "second_eyes", 15, 2.5f, -6, -6.1f, 4, 1, 0.3f);
                }
                if (visual == CreatureVisual.RIFTLING) {
                    tendrils(root, 14, 4, 0, -4, 1);
                }
            }
            case MOTH, BIRD -> {
                box(root, "body", skin, -3, -12, -3, 6, 8, 7);
                pivotBox(root, "head", trim, -2.5f, -5, -4, 5, 5, 5, 0, -11, 0);
                box(root.getChild("head"), "eyes", 15, -2, -3, -4.1f, 4, 1, 0.3f);
                pivotBox(root, "left_wing", skin, 0, -1, -4, 11, 1, 10, 2, -11, 0);
                pivotBox(root, "right_wing", skin, -11, -1, -4, 11, 1, 10, -2, -11, 0);
                if (visual.shape == CreatureVisual.Shape.BIRD) {
                    box(root.getChild("head"), "beak", trim, -1, -2, -8, 2, 2, 5);
                    box(root, "tail", skin, -2, -7, 3, 4, 1, 7);
                    box(root, "left_foot", trim, 1, -4, -1, 1, 4, 3);
                    box(root, "right_foot", trim, -2, -4, -1, 1, 4, 3);
                    if (visual == CreatureVisual.NIGHT_OWL) {
                        box(root.getChild("head"), "face_disc", 4, -3.5f, -5.5f, -4, 7, 6, 0.5f);
                        box(root.getChild("head"), "owl_eyes", 15, -2.5f, -3.5f, -4.3f, 5, 1.5f, 0.3f);
                        box(root.getChild("head"), "ear_left", 8, 1.5f, -8, -2, 1.5f, 4, 2);
                        box(root.getChild("head"), "ear_right", 8, -3, -8, -2, 1.5f, 4, 2);
                        for (int feather = 0; feather < 3; feather++) {
                            box(root.getChild("left_wing"), "feather_" + feather, 4,
                                    4 + feather * 2, -0.5f, 4, 2, 1, 3 + feather);
                            box(root.getChild("right_wing"), "feather_" + feather, 4,
                                    -6 - feather * 2, -0.5f, 4, 2, 1, 3 + feather);
                        }
                    } else if (visual == CreatureVisual.COMPASS_BIRD) {
                        box(root, "compass", 3, -2, -10, -3.2f, 4, 4, 0.5f);
                        box(root.getChild("head"), "crest", 3, -0.5f, -9, -1, 1, 4, 3);
                    }
                } else {
                    box(root.getChild("head"), "antenna_left", trim, 1, -9, -2, 0.5f, 5, 0.5f);
                    box(root.getChild("head"), "antenna_right", trim, -1.5f, -9, -2, 0.5f, 5, 0.5f);
                    if (visual == CreatureVisual.LANTERN_MOTH_SWARM) {
                        box(root, "satellite_left", 11, -9, -6, 2, 3, 3, 3);
                        box(root, "satellite_right", 11, 6, -17, 2, 3, 3, 3);
                    }
                }
            }
            case SPIDER, VINE -> {
                box(root, "body", skin, -4, -10, -3, 8, 6, 9);
                box(root, "head", trim, -3, -9, -7, 6, 4, 4);
                box(root, "eyes", 15, -2, -8, -7.1f, 4, 1, 0.3f);
                for (int index = 0; index < 8; index++) {
                    int side = index % 2 == 0 ? 1 : -1;
                    pivotBox(root, "leg_" + index, skin, side > 0 ? 0 : -8, -1, -1,
                            8, 2, 2, side * 3, -6, index / 2 * 3 - 5);
                }
                if (visual.shape == CreatureVisual.Shape.VINE) {
                    tendrils(root, 6, 5, 0, -11, 0);
                    box(root, "core", 7, -2, -12, -2, 4, 4, 4);
                } else {
                    box(root, "book", 1, -4, -12, -2, 8, 2, 8);
                }
            }
            case LANTERN, WISP, MASK -> {
                if (visual.shape == CreatureVisual.Shape.MASK) {
                    box(root, "head", 4, -5, -23, -2, 10, 12, 3);
                    box(root, "left_eye", 2, -3.5f, -19, -2.2f, 2, 2, 0.5f);
                    box(root, "right_eye", 2, 1.5f, -19, -2.2f, 2, 2, 0.5f);
                    box(root, "mouth", 6, -2, -15, -2.2f, 4, 1, 0.5f);
                } else {
                    box(root, "core", trim, -3, -17, -3, 6, 8, 6);
                    box(root, "cap", skin, -5, -19, -5, 10, 2, 10);
                    box(root, "base", skin, -4, -8, -4, 8, 2, 8);
                    for (int index = 0; index < 4; index++) {
                        box(root, "rail_" + index, skin, (index % 2) * 7 - 4,
                                -17, (index / 2) * 7 - 4, 1, 9, 1);
                    }
                    box(root, "handle", trim, -3, -23, -0.5f, 6, 1, 1);
                    if (visual == CreatureVisual.DOOR_WISP) {
                        box(root, "lintel", 3, -6, -22, -3, 12, 2, 2);
                    }
                }
                tendrils(root, skin, 4, 0, -7, 0);
            }
        }
        return LayerDefinition.create(mesh, 256, 256);
    }

    private static void tendrils(PartDefinition root, int material, int count,
                                 float offsetX, float offsetY, float offsetZ) {
        for (int index = 0; index < count; index++) {
            pivotBox(root, "tendril_" + index, material, -0.5f, 0, -0.5f,
                    1, 7 + index % 3, 1, offsetX + index * 2 - count,
                    offsetY, offsetZ + index % 2);
        }
    }

    private static CubeListBuilder cube(int material, float x, float y, float z,
                                        float width, float height, float depth) {
        return CubeListBuilder.create().texOffs(material % 4 * 64, material / 4 * 64)
                .addBox(x, y, z, width, height, depth);
    }

    private static void box(PartDefinition parent, String name, int material,
                            float x, float y, float z, float width, float height, float depth) {
        parent.addOrReplaceChild(name, cube(material, x, y, z, width, height, depth), PartPose.ZERO);
    }

    private static void pivotBox(PartDefinition parent, String name, int material,
                                 float x, float y, float z, float width, float height, float depth,
                                 float pivotX, float pivotY, float pivotZ) {
        parent.addOrReplaceChild(name, cube(material, x, y, z, width, height, depth),
                PartPose.offset(pivotX, pivotY, pivotZ));
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbAmount,
                          float age, float headYaw, float headPitch) {
        applyPose(limbSwing, limbAmount, age + entity.getId() * 2.7f, headYaw, headPitch,
                attackTime, entity.isAggressive(), entity.onGround(), riding, VisualClientConfig.REDUCED_MOTION.get());
    }

    public void applyPose(float limbSwing, float limbAmount, float age, float headYaw, float headPitch,
                          float attack, boolean aggressive, boolean grounded, boolean seated, boolean reducedMotion) {
        root.getAllParts().forEach(ModelPart::resetPose);
        var pose = CreatureAnimationLogic.sample(limbSwing, limbAmount, age, attack, aggressive, grounded, reducedMotion);
        limbAmount = CreatureAnimationLogic.clamp(limbAmount, 0, 1);
        age = Float.isFinite(age) ? age : 0;
        limbSwing = Float.isFinite(limbSwing) ? limbSwing : 0;
        if (root.hasChild("head") && (visual.shape == CreatureVisual.Shape.HUMANOID
                || visual.shape == CreatureVisual.Shape.BIRD || visual.shape == CreatureVisual.Shape.MOTH)) {
            root.getChild("head").yRot = CreatureAnimationLogic.clamp(headYaw, -85, 85) * Mth.DEG_TO_RAD;
            root.getChild("head").xRot = CreatureAnimationLogic.clamp(headPitch, -65, 65) * Mth.DEG_TO_RAD * 0.5f;
        }
        float walk = pose.walk();
        root.y += pose.breathing();
        root.xRot = -pose.lean();
        rotate("left_arm", walk + pose.armPitch(), 0, 0.08f);
        rotate("right_arm", -walk + pose.armPitch(), 0, -0.08f);
        if (visual.shape == CreatureVisual.Shape.HUMANOID && aggressive) {
            float strike = pose.strike();
            rotate("left_arm", -0.95f - strike * 0.75f + walk * 0.3f, 0.12f, 0.08f);
            rotate("right_arm", -0.95f - strike * 0.75f - walk * 0.3f, -0.12f, -0.08f);
        }
        rotate("left_leg", -walk, 0, 0);
        rotate("right_leg", walk, 0, 0);
        float flap = pose.flap();
        rotate("left_wing", 0, 0, flap);
        rotate("right_wing", 0, 0, -flap);
        for (int index = 0; index < 8; index++) {
            rotate("segment_" + index, 0, Mth.sin(age * 0.12f + index * 0.7f) * (reducedMotion ? limbAmount * 0.15f : 0.25f), 0);
            rotate("tendril_" + index, reducedMotion ? 0 : Mth.sin(age * 0.08f + index) * 0.25f, 0,
                    reducedMotion ? 0 : Mth.cos(age * 0.1f + index) * 0.2f);
            float side = index % 2 == 0 ? 1 : -1;
            rotate("leg_" + index, 0, (index / 2 - 1.5f) * 0.3f,
                    side * (0.55f + Mth.sin(limbSwing + index) * limbAmount * 0.3f));
        }
        if (visual.shape == CreatureVisual.Shape.WISP
                || visual.shape == CreatureVisual.Shape.LANTERN
                || visual.shape == CreatureVisual.Shape.MASK) {
            if (!reducedMotion) root.y += Mth.sin(age * 0.08f) * 0.6f;
        }
        if (seated) {
            rotate("left_leg", -1.2f, 0, 0.2f);
            rotate("right_leg", -1.2f, 0, -0.2f);
        }
    }

    private void rotate(String name, float pitch, float yaw, float roll) {
        if (root.hasChild(name)) {
            ModelPart part = root.getChild(name);
            part.xRot = pitch;
            part.yRot = yaw;
            part.zRot = roll;
        }
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack pose) {
        root.translateAndRotate(pose);
        String name = arm == HumanoidArm.LEFT ? "left_arm" : "right_arm";
        if (root.hasChild(name)) root.getChild(name).translateAndRotate(pose);
    }

    public void renderGlow(PoseStack pose, VertexConsumer vertices, int overlay) {
        float red = ((visual.glow >> 16) & 255) / 255f;
        float green = ((visual.glow >> 8) & 255) / 255f;
        float blue = (visual.glow & 255) / 255f;
        pose.pushPose();
        root.translateAndRotate(pose);
        for (String name : new String[] {"eyes", "core", "second_eyes", "owl_eyes"}) {
            if (root.hasChild(name)) {
                root.getChild(name).render(pose, vertices, 0xF000F0, overlay, red, green, blue, 1);
            }
        }
        if (root.hasChild("head") && root.getChild("head").hasChild("eyes")) {
            root.getChild("head").translateAndRotate(pose);
            root.getChild("head").getChild("eyes").render(
                    pose, vertices, 0xF000F0, overlay, red, green, blue, 1);
            if (root.getChild("head").hasChild("owl_eyes")) root.getChild("head").getChild("owl_eyes").render(
                    pose, vertices, 0xF000F0, overlay, red, green, blue, 1);
        }
        pose.popPose();
    }
}
