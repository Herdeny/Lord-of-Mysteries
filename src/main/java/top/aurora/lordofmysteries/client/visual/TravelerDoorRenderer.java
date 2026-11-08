package top.aurora.lordofmysteries.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.entity.TravelerDoorEntity;

public final class TravelerDoorRenderer extends EntityRenderer<TravelerDoorEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, "traveler_door"), "main");
    private final ModelPart frame;
    private final ModelPart veil;

    public TravelerDoorRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart root = context.bakeLayer(LAYER);
        frame = root.getChild("frame");
        veil = root.getChild("veil");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("frame", CubeListBuilder.create().texOffs(192, 0)
                .addBox(-8.5f, -38, -1.5f, 2, 38, 3)
                .addBox(6.5f, -38, -1.5f, 2, 38, 3)
                .addBox(-6.5f, -38, -1.5f, 13, 2, 3)
                .addBox(-6.5f, -2, -1.5f, 13, 2, 3), PartPose.ZERO);
        root.addOrReplaceChild("veil", CubeListBuilder.create().texOffs(128, 192)
                .addBox(-6.5f, -36, -0.05f, 13, 34, 0.1f), PartPose.ZERO);
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void render(TravelerDoorEntity entity, float yaw, float partialTick,
                       PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180 - yaw));
        pose.scale(-1, -1, 1);
        frame.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity))),
                light, OverlayTexture.NO_OVERLAY);
        float alpha = 0.63f + (float) Math.sin((entity.tickCount + partialTick) * 0.06) * 0.07f;
        veil.render(pose, buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity))),
                0xF000F0, OverlayTexture.NO_OVERLAY, 0.65f, 0.85f, 1, alpha);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(TravelerDoorEntity entity) {
        return OccultCreatureRenderer.ATLAS;
    }
}
