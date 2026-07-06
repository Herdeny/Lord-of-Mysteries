package top.aurora.lordofmysteries.knowledge;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.SeerPotionItem;
import top.aurora.lordofmysteries.potion.SpectatorPotionItem;
import top.aurora.lordofmysteries.potion.HunterPotionItem;
import top.aurora.lordofmysteries.potion.M2PathwayPotionItem;

public final class InvestigatorNotesItem extends Item {

    public InvestigatorNotesItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        showGuide(serverPlayer);
        return InteractionResultHolder.success(stack);
    }

    public static void showGuide(ServerPlayer serverPlayer) {
        PlayerMysteryData data = MysteryCapability.get(serverPlayer);
        serverPlayer.sendSystemMessage(Component.translatable(
                "guide.lord_of_mysteries.title").withStyle(ChatFormatting.LIGHT_PURPLE));
        if (!data.isExtraordinary()) {
            send(serverPlayer, "guide.lord_of_mysteries.commoner.1");
            send(serverPlayer, "guide.lord_of_mysteries.commoner.2");
            send(serverPlayer, "guide.lord_of_mysteries.commoner.3");
            send(serverPlayer, "guide.lord_of_mysteries.commoner.4");
            send(serverPlayer, "guide.lord_of_mysteries.commoner.5");
        } else if (SeerPotionItem.SEER_PATHWAY.equals(data.pathway)) {
            send(serverPlayer, "guide.lord_of_mysteries.seer." + data.sequence);
            send(serverPlayer, "guide.lord_of_mysteries.seer.controls");
            if (data.digestion >= 100f && data.sequence > 7) {
                send(serverPlayer, "guide.lord_of_mysteries.seer.ready",
                        data.sequence - 1);
            } else if (data.sequence == 7) {
                send(serverPlayer, "guide.lord_of_mysteries.seer.m1");
            } else {
                send(serverPlayer, "guide.lord_of_mysteries.digestion",
                        Math.round(data.digestion));
            }
        } else if (SpectatorPotionItem.SPECTATOR_PATHWAY.equals(data.pathway)) {
            send(serverPlayer,
                    "guide.lord_of_mysteries.spectator." + data.sequence);
            send(serverPlayer, "guide.lord_of_mysteries.m2.controls");
            sendDigestion(serverPlayer, data);
        } else if (HunterPotionItem.HUNTER_PATHWAY.equals(data.pathway)) {
            send(serverPlayer,
                    "guide.lord_of_mysteries.hunter." + data.sequence);
            send(serverPlayer, "guide.lord_of_mysteries.m2.controls");
            sendDigestion(serverPlayer, data);
        } else if (M2PathwayPotionItem.Pathway.THIEF.id().equals(data.pathway)) {
            send(serverPlayer, "guide.lord_of_mysteries.thief.9");
            send(serverPlayer, "guide.lord_of_mysteries.m2.controls");
            sendDigestion(serverPlayer, data);
        } else if (M2PathwayPotionItem.Pathway.APPRENTICE.id().equals(data.pathway)) {
            send(serverPlayer, "guide.lord_of_mysteries.apprentice.9");
            send(serverPlayer, "guide.lord_of_mysteries.m2.controls");
            sendDigestion(serverPlayer, data);
        } else {
            send(serverPlayer, "guide.lord_of_mysteries.other_pathway");
        }
        send(serverPlayer, "guide.lord_of_mysteries.field_rules");
        send(serverPlayer, "guide.lord_of_mysteries.recovery");
        send(serverPlayer, "guide.lord_of_mysteries.status_key");
    }

    private static void sendDigestion(ServerPlayer player,
                                      PlayerMysteryData data) {
        if (data.digestion >= 100f && data.sequence > 7) {
            send(player, "guide.lord_of_mysteries.seer.ready",
                    data.sequence - 1);
        } else {
            send(player, "guide.lord_of_mysteries.digestion",
                    Math.round(data.digestion));
        }
    }

    public static int showHandbookOverview(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        player.sendSystemMessage(Component.translatable(
                "guide.lord_of_mysteries.handbook.title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        int unlocked = 0;
        for (int chapter = 1; chapter <= GuideJournalProgress.CHAPTER_COUNT; chapter++) {
            boolean available = isChapterUnlocked(data, chapter);
            if (available) unlocked++;
            player.sendSystemMessage(Component.literal(available ? "◆ " : "◇ ")
                    .append(Component.translatable(
                            "guide.lord_of_mysteries.handbook.chapter."
                                    + chapter + ".title"))
                    .withStyle(available ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY));
        }
        player.sendSystemMessage(Component.translatable(
                "guide.lord_of_mysteries.handbook.hint", unlocked,
                GuideJournalProgress.CHAPTER_COUNT)
                .withStyle(ChatFormatting.GRAY));
        return unlocked;
    }

    public static int showHandbookChapter(ServerPlayer player, int chapter) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!isChapterUnlocked(data, chapter)) {
            player.sendSystemMessage(Component.translatable(
                    "guide.lord_of_mysteries.handbook.locked")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return 0;
        }
        player.sendSystemMessage(Component.translatable(
                "guide.lord_of_mysteries.handbook.chapter." + chapter + ".title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.translatable(
                "guide.lord_of_mysteries.handbook.chapter." + chapter + ".summary")
                .withStyle(ChatFormatting.GRAY));
        return 1;
    }

    private static boolean isChapterUnlocked(PlayerMysteryData data, int chapter) {
        boolean seer = SeerPotionItem.SEER_PATHWAY.equals(data.pathway);
        boolean hasM2Rumor = data.knownKnowledge.stream()
                .anyMatch(id -> id.getPath().startsWith("knowledge/m2/"));
        boolean hasGrayFogInvitation = data.knownKnowledge.stream()
                .anyMatch(id -> id.getPath().equals(
                        "knowledge/m4/gray_fog_invitation"));
        return GuideJournalProgress.isUnlocked(
                chapter, data.isExtraordinary(), seer,
                hasM2Rumor, hasGrayFogInvitation);
    }

    private static void send(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(Component.translatable(key, args)
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.investigator_notes")
                .withStyle(ChatFormatting.GRAY));
    }
}
