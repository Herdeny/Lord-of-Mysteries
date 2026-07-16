package top.aurora.lordofmysteries.knowledge;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.TickEvent;
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
    private static final ResourceLocation CAMP_FOUND =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID, "knowledge/investigator_camp_found");
    private static final ResourceLocation CAMP_ADVANCEMENT =
            ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID, "reach_investigator_camp");

    private PlayerGuideHandler() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.knownKnowledge.add(GETTING_STARTED)) return;

        restoreStarterKit(player, false);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.guide.welcome")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.guide.quick_start")
                .withStyle(ChatFormatting.GRAY));
        announceNextStep(player, true);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 100 != 0) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.knownKnowledge.contains(GETTING_STARTED)) return;

        recordCampVisit(player, data);
        M1ProgressAdvisor.Stage stage = currentStage(player, data);
        ResourceLocation hint = ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID,
                "knowledge/m1_hint/" + M1ProgressAdvisor.translationSuffix(stage));
        if (data.knownKnowledge.add(hint)) sendNextStep(player, stage);
    }

    public static int showNextStep(ServerPlayer player) {
        sendNextStep(player, currentStage(player, MysteryCapability.get(player)));
        return 1;
    }

    public static int restoreStarterKit(ServerPlayer player, boolean announce) {
        int restored = 0;
        restored += ensureItem(player, ModItems.INVESTIGATOR_NOTES.get());
        restored += ensureItem(player, ModItems.INVESTIGATOR_COMPASS.get());
        if (announce) {
            Component message = restored == 0
                    ? Component.translatable(
                            "message.lord_of_mysteries.guide.recover_complete")
                    : Component.translatable(
                            "message.lord_of_mysteries.guide.recovered", restored);
            player.sendSystemMessage(message.copy().withStyle(restored == 0
                    ? ChatFormatting.GRAY : ChatFormatting.GREEN));
        }
        return restored;
    }

    private static void announceNextStep(ServerPlayer player, boolean remember) {
        PlayerMysteryData data = MysteryCapability.get(player);
        M1ProgressAdvisor.Stage stage = currentStage(player, data);
        if (remember) {
            data.knownKnowledge.add(ResourceLocation.fromNamespaceAndPath(
                    ProjectMystery.MOD_ID,
                    "knowledge/m1_hint/" + M1ProgressAdvisor.translationSuffix(stage)));
        }
        sendNextStep(player, stage);
    }

    private static void sendNextStep(ServerPlayer player,
                                     M1ProgressAdvisor.Stage stage) {
        player.sendSystemMessage(Component.translatable(
                "guide.lord_of_mysteries.next.title")
                .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(
                "guide.lord_of_mysteries.next."
                        + M1ProgressAdvisor.translationSuffix(stage))
                .withStyle(stage == M1ProgressAdvisor.Stage.COMPLETE
                        ? ChatFormatting.GREEN : ChatFormatting.AQUA));
    }

    private static M1ProgressAdvisor.Stage currentStage(
            ServerPlayer player, PlayerMysteryData data) {
        boolean hasMagicianMaterial = containsItem(
                player, ModItems.SHAPESHIFTER_SERPENT_GLAND.get())
                || containsItem(player, ModItems.SEER_POTION_7.get());
        return M1ProgressAdvisor.evaluate(
                data.knownKnowledge.contains(CAMP_FOUND),
                data.pathway == null ? null : data.pathway.toString(),
                data.sequence, data.digestion, hasMagicianMaterial);
    }

    private static void recordCampVisit(ServerPlayer player,
                                        PlayerMysteryData data) {
        if (player.level().dimension() != Level.OVERWORLD
                || data.knownKnowledge.contains(CAMP_FOUND)) return;
        boolean reached = top.aurora.lordofmysteries.world.CampGenerationSavedData
                .get(player.serverLevel()).nearestCamp(player.blockPosition())
                .filter(camp -> camp.distSqr(player.blockPosition()) <= 24d * 24d)
                .isPresent();
        if (!reached || !data.knownKnowledge.add(CAMP_FOUND)) return;

        grantCampAdvancement(player);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.guide.camp_found")
                .withStyle(ChatFormatting.GOLD));
    }

    private static void grantCampAdvancement(ServerPlayer player) {
        Advancement advancement = player.server.getAdvancements()
                .getAdvancement(CAMP_ADVANCEMENT);
        if (advancement == null) return;
        AdvancementProgress progress = player.getAdvancements()
                .getOrStartProgress(advancement);
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }

    private static int ensureItem(ServerPlayer player, Item item) {
        if (containsItem(player, item)) return 0;
        ItemStack stack = new ItemStack(item);
        if (player.getInventory().add(stack)) return 1;
        ItemStack remaining = player.getEnderChestInventory().addItem(stack);
        if (!remaining.isEmpty()) player.drop(remaining, false);
        return 1;
    }

    private static boolean containsItem(ServerPlayer player, Item item) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(item)) return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.is(item)) return true;
        }
        for (int slot = 0; slot < player.getEnderChestInventory().getContainerSize(); slot++) {
            if (player.getEnderChestInventory().getItem(slot).is(item)) return true;
        }
        return false;
    }
}
