package top.aurora.lordofmysteries.knowledge;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModEntities;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class KnowledgeDiscoveryHandler {

    private KnowledgeDiscoveryHandler() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        EntityType<?> type = event.getEntity().getType();
        if (type == ModEntities.SHAPESHIFTER_SERPENT.get()) {
            discover(player, "bestiary/shapeshifter_serpent");
        } else if (type == ModEntities.SPIRIT_WISP.get()) {
            discover(player, "bestiary/spirit_wisp");
        } else if (type == ModEntities.ASHEN_PUPPET.get()) {
            discover(player, "bestiary/ashen_puppet");
        } else if (type == ModEntities.SEER_BREAKDOWN.get()) {
            discover(player, "bestiary/seer_breakdown");
        }
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
