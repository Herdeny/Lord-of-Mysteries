package top.aurora.lordofmysteries.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import top.aurora.lordofmysteries.ProjectMystery;

public final class OccultCreatureRenderer<T extends Mob>
        extends MobRenderer<T, OccultCreatureModel<T>> {
    public static final ResourceLocation ATLAS = ResourceLocation.fromNamespaceAndPath(
            ProjectMystery.MOD_ID, "textures/entity/occult_atlas.png");
    private final CreatureVisual visual;

    public OccultCreatureRenderer(EntityRendererProvider.Context context, CreatureVisual visual) {
        super(context, new OccultCreatureModel<>(context.bakeLayer(visual.layer), visual), 0.35f);
        this.visual = visual;
        if (visual.shape == CreatureVisual.Shape.HUMANOID) {
            addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        }
        addLayer(new RenderLayer<T, OccultCreatureModel<T>>(this) {
            @Override
            public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity,
                               float swing, float amount, float partialTick, float age,
                               float headYaw, float headPitch) {
                if (!entity.isInvisible()) {
                    getParentModel().renderGlow(pose,
                            buffers.getBuffer(RenderType.eyes(ATLAS)), OverlayTexture.NO_OVERLAY);
                }
            }
        });
    }

    @Override
    protected void scale(T entity, PoseStack pose, float partialTick) {
        float scale = entity.getBbHeight() * 16f / visual.shape.height;
        pose.scale(scale, scale, scale);
        shadowRadius = entity.getBbWidth() * 0.5f;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return ATLAS;
    }
}
