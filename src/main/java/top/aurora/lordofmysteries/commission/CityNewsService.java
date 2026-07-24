package top.aurora.lordofmysteries.commission;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.world.MistCityWorldEvent;
import top.aurora.lordofmysteries.world.MistCityWorldEventPolicy;

public final class CityNewsService {

    private static final ResourceLocation NEWSPAPER_RUMORS =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID, "knowledge/m2/newspaper_rumors");

    private CityNewsService() {}

    public static int read(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        ServerLevel cityLevel = player.getServer().overworld();
        long day = Math.max(
                0L, Math.floorDiv(cityLevel.getDayTime(), 24_000L));
        MistCityWorldEvent worldEvent =
                MistCityWorldEventPolicy.eventForDay(
                        cityLevel.getSeed(), day);
        CityNewsLogic.Issue issue = CityNewsLogic.issue(
                cityLevel.getSeed(), day,
                data.activeCommissionId, data.lastCityWorkDay == day,
                worldEvent, data.mysticalExposure);
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
        player.sendSystemMessage(Component.literal("• ").append(
                        Component.translatable(
                                "message.lord_of_mysteries.newspaper.world_event",
                                Component.translatable(issue.worldEventKey())))
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal("• ").append(
                        Component.translatable(
                                "message.lord_of_mysteries.newspaper.exposure",
                                Math.round(data.mysticalExposure),
                                Component.translatable(
                                        issue.exposureBandKey())))
                .withStyle(ChatFormatting.DARK_AQUA));
        if (data.knownKnowledge.add(NEWSPAPER_RUMORS)) {
            player.sendSystemMessage(Component.translatable(
                            "message.lord_of_mysteries.newspaper.knowledge_unlocked")
                    .withStyle(ChatFormatting.DARK_AQUA));
        }
        return 1;
    }
}
