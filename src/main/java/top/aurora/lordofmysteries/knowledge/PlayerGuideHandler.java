package top.aurora.lordofmysteries.knowledge;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerGuideHandler {

    private static final ResourceLocation GETTING_STARTED =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID, "knowledge/getting_started");

    private PlayerGuideHandler() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.knownKnowledge.add(GETTING_STARTED)) return;

        ItemStack notes = new ItemStack(ModItems.INVESTIGATOR_NOTES.get());
        if (!player.getInventory().add(notes)) player.drop(notes, false);
        ItemStack compass = new ItemStack(ModItems.INVESTIGATOR_COMPASS.get());
        if (!player.getInventory().add(compass)) player.drop(compass, false);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.guide.welcome")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
    }
}
