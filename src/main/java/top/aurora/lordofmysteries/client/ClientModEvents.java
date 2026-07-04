package top.aurora.lordofmysteries.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.CaveSpiderRenderer;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.registry.ModEntities;

/**
 * 客户端 mod 总线事件（Forge 1.20.1）。
 *
 * <p>KeyMapping 必须在 mod 总线的 {@link RegisterKeyMappingsEvent} 上注册，
 * 因此本类使用 {@code Bus.MOD} + {@code Dist.CLIENT}，避免专用服务器加载客户端类。
 */
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {}

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        PMKeyBindings.register(event);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SEER_BREAKDOWN.get(), ZombieRenderer::new);
        event.registerEntityRenderer(
                ModEntities.SHAPESHIFTER_SERPENT.get(), CaveSpiderRenderer::new);
    }
}
