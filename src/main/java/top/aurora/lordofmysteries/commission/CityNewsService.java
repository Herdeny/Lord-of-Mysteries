package top.aurora.lordofmysteries.commission;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class CityNewsService {

    private static final ResourceLocation NEWSPAPER_RUMORS =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID, "knowledge/m2/newspaper_rumors");

    private CityNewsService() {}

    public static int read(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        long day = player.level().getDayTime() / 24000L;
        CityNewsLogic.Issue issue = CityNewsLogic.issue(
                player.serverLevel().getSeed(), day,
                data.activeCommissionId, data.lastCityWorkDay == day);
        player.sendSystemMessage(Component.translatable(
                        "message.lord_of_mysteries.newspaper.masthead", day + 1)
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("◆ ").append(
                        Component.translatable(issue.headlineKey()))
                .withStyle(ChatFormatting.WHITE));
        player.sendSystemMessage(Component.literal("• ").append(
                        Component.translatable(issue.caseBulletinKey()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal("• ").append(
                        Component.translatable(issue.shiftBulletinKey(),
                                CommissionCurrency.format(data.moneyPence)))
                .withStyle(ChatFormatting.GRAY));
        if (data.knownKnowledge.add(NEWSPAPER_RUMORS)) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.newspaper.knowledge_unlocked")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
        return 1;
    }
}
