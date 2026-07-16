package top.aurora.lordofmysteries.knowledge;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModEntities;
import top.aurora.lordofmysteries.commission.CommissionService;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class KnowledgeDiscoveryHandler {

    private KnowledgeDiscoveryHandler() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        EntityType<?> type = event.getEntity().getType();
        String path = null;
        if (type == ModEntities.SHAPESHIFTER_SERPENT.get()) {
            path = "bestiary/shapeshifter_serpent";
        } else if (type == ModEntities.SPIRIT_WISP.get()) {
            path = "bestiary/spirit_wisp";
        } else if (type == ModEntities.ASHEN_PUPPET.get()) {
            path = "bestiary/ashen_puppet";
        } else if (type == ModEntities.SEER_BREAKDOWN.get()) {
            path = "bestiary/seer_breakdown";
        } else if (type == ModEntities.THIEF_BREAKDOWN.get()) {
            path = "bestiary/thief_breakdown";
        } else if (type == ModEntities.APPRENTICE_BREAKDOWN.get()) {
            path = "bestiary/apprentice_breakdown";
        } else if (type == ModEntities.PSYCHIATRIST_BREAKDOWN.get()) {
            path = "bestiary/psychiatrist_breakdown";
        } else if (type == ModEntities.PYROMANIAC_BREAKDOWN.get()) {
            path = "bestiary/pyromaniac_breakdown";
        }
        if (path == null) return;
        M1TrialTracker.recordOccultKill(player);
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (entityId != null) CommissionService.recordOccultKill(player, entityId);
        discover(player, path);
    }

    private static void discover(ServerPlayer player, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, "knowledge/" + path);
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.knownKnowledge.add(id)) return;
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.knowledge.discovered",
                Component.translatable(KnowledgeText.translationKey(id.toString())))
                .withStyle(ChatFormatting.GOLD));
    }
}
