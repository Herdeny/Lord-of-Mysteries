package top.aurora.lordofmysteries.client.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.registry.ModParticles;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, value = Dist.CLIENT)
public final class AmbientVisuals {
    private AmbientVisuals() {}

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || client.level == null
                || client.player == null || client.isPaused() || !VisualClientConfig.AMBIENT_PARTICLES.get()
                || client.player.tickCount % 10 != 0) return;
        var dimension = client.level.dimension().location();
        if (!ProjectMystery.MOD_ID.equals(dimension.getNamespace())) return;
        SimpleParticleType particle = switch (dimension.getPath()) {
            case "spirit_world" -> ModParticles.SPIRIT_WISP.get();
            case "shared_dream" -> ModParticles.DREAM_BUBBLE.get();
            case "gray_fog" -> ModParticles.GRAYFOG_VEIL.get();
            default -> null;
        };
        if (particle == null) return;
        int count = VisualClientConfig.PARTICLE_DENSITY.get() == VisualBudget.Density.LOW ? 1 : 3;
        for (int index = 0; index < count; index++) {
            double angle = client.level.random.nextDouble() * Math.PI * 2;
            client.level.addParticle(particle,
                    client.player.getX() + Math.cos(angle) * 3,
                    client.player.getY() + client.level.random.nextDouble() * 2,
                    client.player.getZ() + Math.sin(angle) * 3, 0, 0.012, 0);
        }
    }
}
