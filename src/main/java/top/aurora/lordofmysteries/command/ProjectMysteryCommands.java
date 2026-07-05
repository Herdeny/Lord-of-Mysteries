package top.aurora.lordofmysteries.command;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.knowledge.InvestigatorCompassItem;
import top.aurora.lordofmysteries.knowledge.InvestigatorNotesItem;
import top.aurora.lordofmysteries.knowledge.M1Readiness;
import top.aurora.lordofmysteries.knowledge.KnowledgeText;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.registry.ModItems;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ProjectMysteryCommands {

    private ProjectMysteryCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pm")
                .then(Commands.literal("guide").executes(context -> {
                    InvestigatorNotesItem.showGuide(context.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("status").executes(context ->
                        showStatus(context.getSource().getPlayerOrException())))
                .then(Commands.literal("camp").executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    ItemStack compass = findCompass(player);
                    InvestigatorCompassItem.reportCamp(player, compass);
                    return 1;
                }))
                .then(Commands.literal("rules").executes(context ->
                        showLines(context.getSource().getPlayerOrException(), "rules", 5)))
                .then(Commands.literal("items").executes(context ->
                        showLines(context.getSource().getPlayerOrException(), "items", 6)))
                .then(Commands.literal("bestiary").executes(context ->
                        showLines(context.getSource().getPlayerOrException(), "bestiary", 4)))
                .then(Commands.literal("journal").executes(context ->
                        showJournal(context.getSource().getPlayerOrException())))
                .then(Commands.literal("m1check").executes(context ->
                        showM1Check(context.getSource().getPlayerOrException()))));
    }

    private static int showStatus(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.status",
                Component.translatable(KnowledgeText.pathwayTranslationKey(
                        data.pathway == null ? "" : data.pathway.toString())),
                data.sequence,
                Math.round(data.spirituality),
                Math.round(data.spiritualityMax),
                Math.round(data.digestion),
                Math.round(data.pollution),
                Math.round(data.insanityPressure))
                .withStyle(ChatFormatting.AQUA));
        return 1;
    }

    private static int showM1Check(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        M1Readiness.Stage stage = M1Readiness.evaluate(
                data.pathway == null ? null : data.pathway.toString(),
                data.sequence, data.digestion);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.m1check." + stage.name().toLowerCase())
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        return 1;
    }

    private static ItemStack findCompass(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.INVESTIGATOR_COMPASS.get())) return stack;
        }
        return new ItemStack(ModItems.INVESTIGATOR_COMPASS.get());
    }

    private static int showLines(ServerPlayer player, String section, int count) {
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries." + section + ".title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        for (int line = 1; line <= count; line++) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries." + section + "." + line)
                    .withStyle(ChatFormatting.GRAY));
        }
        return count;
    }

    private static int showJournal(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        player.sendSystemMessage(Component.translatable(
                "command.lord_of_mysteries.journal.title")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        if (data.knownKnowledge.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "command.lord_of_mysteries.journal.empty")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }
        data.knownKnowledge.stream()
                .map(Object::toString)
                .sorted()
                .limit(20)
                .forEach(id -> player.sendSystemMessage(Component.literal("• ")
                        .append(Component.translatable(KnowledgeText.translationKey(id)))
                        .withStyle(ChatFormatting.GRAY)));
        return data.knownKnowledge.size();
    }
}
