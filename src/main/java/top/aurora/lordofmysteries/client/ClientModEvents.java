package top.aurora.lordofmysteries.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.registry.ModEntities;
import top.aurora.lordofmysteries.registry.ModParticles;
import top.aurora.lordofmysteries.client.visual.CreatureVisual;
import top.aurora.lordofmysteries.client.visual.OccultCreatureModel;
import top.aurora.lordofmysteries.client.visual.OccultCreatureRenderer;
import top.aurora.lordofmysteries.client.visual.OccultParticle;
import top.aurora.lordofmysteries.client.visual.TravelerDoorRenderer;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        PMKeyBindings.register(event);
    }

    @SubscribeEvent
    public static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (CreatureVisual visual : CreatureVisual.values()) {
            event.registerLayerDefinition(visual.layer, () -> OccultCreatureModel.createLayer(visual));
        }
        event.registerLayerDefinition(TravelerDoorRenderer.LAYER, TravelerDoorRenderer::createLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SEER_BREAKDOWN.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.SEER_BREAKDOWN));
        event.registerEntityRenderer(ModEntities.THIEF_BREAKDOWN.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.THIEF_BREAKDOWN));
        event.registerEntityRenderer(ModEntities.APPRENTICE_BREAKDOWN.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.APPRENTICE_BREAKDOWN));
        event.registerEntityRenderer(ModEntities.PSYCHIATRIST_BREAKDOWN.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.PSYCHIATRIST_BREAKDOWN));
        event.registerEntityRenderer(ModEntities.PYROMANIAC_BREAKDOWN.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.PYROMANIAC_BREAKDOWN));
        event.registerEntityRenderer(ModEntities.ASHEN_PUPPET.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.ASHEN_PUPPET));
        event.registerEntityRenderer(ModEntities.SCRIBE_GOLEM.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.SCRIBE_GOLEM));
        event.registerEntityRenderer(ModEntities.WAR_ROSE_HUSK.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.WAR_ROSE_HUSK));
        event.registerEntityRenderer(ModEntities.SHAPESHIFTER_SERPENT.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.SHAPESHIFTER_SERPENT));
        event.registerEntityRenderer(ModEntities.TWIN_SERPENT.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.TWIN_SERPENT));
        event.registerEntityRenderer(ModEntities.ABILITY_LEECH.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.ABILITY_LEECH));
        event.registerEntityRenderer(ModEntities.MEMORY_LEECH.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.MEMORY_LEECH));
        event.registerEntityRenderer(ModEntities.RIFTLING.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.RIFTLING));
        event.registerEntityRenderer(ModEntities.SPIRIT_WISP.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.SPIRIT_WISP));
        event.registerEntityRenderer(ModEntities.PRAYER_ECHO.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.PRAYER_ECHO));
        event.registerEntityRenderer(ModEntities.COLOR_EATER.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.COLOR_EATER));
        event.registerEntityRenderer(ModEntities.DOOR_WISP.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.DOOR_WISP));
        event.registerEntityRenderer(ModEntities.GASLIGHT_SPECTER.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.GASLIGHT_SPECTER));
        event.registerEntityRenderer(ModEntities.CRADLE_MOTH.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.CRADLE_MOTH));
        event.registerEntityRenderer(ModEntities.LANTERN_MOTH_SWARM.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.LANTERN_MOTH_SWARM));
        event.registerEntityRenderer(ModEntities.WHISPER_CROW.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.WHISPER_CROW));
        event.registerEntityRenderer(ModEntities.COMPASS_BIRD.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.COMPASS_BIRD));
        event.registerEntityRenderer(ModEntities.ARCHIVE_SPIDER.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.ARCHIVE_SPIDER));
        event.registerEntityRenderer(ModEntities.MARIONETTE_VINE.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.MARIONETTE_VINE));
        event.registerEntityRenderer(ModEntities.SPIRIT_FERRYMAN.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.SPIRIT_FERRYMAN));
        event.registerEntityRenderer(ModEntities.THEATRE_MASKLING.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.THEATRE_MASKLING));
        event.registerEntityRenderer(ModEntities.GRAVE_LANTERN.get(),
                context -> new OccultCreatureRenderer<>(context, CreatureVisual.GRAVE_LANTERN));
        event.registerEntityRenderer(ModEntities.TRAVELER_DOOR.get(), TravelerDoorRenderer::new);
    }

    @SubscribeEvent
    public static void onParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SPIRIT_WISP.get(),
                sprites -> new OccultParticle.Provider(sprites, 0x8cecdc, 24, 0.12f));
        event.registerSpriteSet(ModParticles.POLLUTION_MOTE.get(),
                sprites -> new OccultParticle.Provider(sprites, 0x9363b0, 30, 0.12f));
        event.registerSpriteSet(ModParticles.RITUAL_RUNE.get(),
                sprites -> new OccultParticle.Provider(sprites, 0xf5d798, 26, 0.23f));
        event.registerSpriteSet(ModParticles.DIVINATION_THREAD.get(),
                sprites -> new OccultParticle.Provider(sprites, 0xc7deef, 24, 0.18f));
        event.registerSpriteSet(ModParticles.DREAM_BUBBLE.get(),
                sprites -> new OccultParticle.Provider(sprites, 0xd8b7ee, 30, 0.2f));
        event.registerSpriteSet(ModParticles.EMBER_BURST.get(),
                sprites -> new OccultParticle.Provider(sprites, 0xffae64, 18, 0.13f));
        event.registerSpriteSet(ModParticles.SHADOW_FLICKER.get(),
                sprites -> new OccultParticle.Provider(sprites, 0x756391, 20, 0.18f));
        event.registerSpriteSet(ModParticles.PAGE_GLYPH.get(),
                sprites -> new OccultParticle.Provider(sprites, 0xf2e3c0, 26, 0.2f));
        event.registerSpriteSet(ModParticles.GRAYFOG_VEIL.get(),
                sprites -> new OccultParticle.Provider(sprites, 0xcdcfdc, 32, 0.24f));
        event.registerSpriteSet(ModParticles.CHARACTERISTIC_GLOW.get(),
                sprites -> new OccultParticle.Provider(sprites, 0xffdf91, 26, 0.14f));
        event.registerSpriteSet(ModParticles.MIRROR_RIPPLE.get(),
                sprites -> new OccultParticle.Provider(sprites, 0xacdfee, 22, 0.3f));
        event.registerSpriteSet(ModParticles.PETAL_ROSE_SHADOW.get(),
                sprites -> new OccultParticle.Provider(sprites, 0xd56d8c, 26, 0.14f));
        event.registerSpriteSet(ModParticles.CARD_TRACE.get(),
                sprites -> new OccultParticle.Provider(sprites, 0xf3deb1, 16, 0.18f));
        event.registerSpriteSet(ModParticles.AIR_RING.get(),
                sprites -> new OccultParticle.Provider(sprites, 0xdaf7ff, 12, 0.25f));
        event.registerSpriteSet(ModParticles.MARIONETTE_THREAD.get(),
                sprites -> new OccultParticle.Provider(sprites, 0xdac8f1, 22, 0.22f));
        event.registerSpriteSet(ModParticles.DOOR_SPARK.get(),
                sprites -> new OccultParticle.Provider(sprites, 0x82dff4, 24, 0.2f));
    }
}
