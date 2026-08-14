package top.aurora.lordofmysteries.player;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import top.aurora.lordofmysteries.dream.SharedDreamSavedData;
import top.aurora.lordofmysteries.dream.SharedDreamService;
import top.aurora.lordofmysteries.spirit.SpiritExpeditionSavedData;
import top.aurora.lordofmysteries.spirit.SpiritExpeditionService;

public final class OccultActivityService {

    private OccultActivityService() {}

    public static Snapshot inspect(ServerPlayer player) {
        SharedDreamSavedData dreamData = SharedDreamSavedData.get(
                player.serverLevel());
        SharedDreamSavedData.DreamSession dreamSession = dreamData.forPlayer(
                player.getUUID());
        boolean dreamRecovery = dreamData.recovery(player.getUUID()) != null;
        OccultActivityPolicy.DreamPhase dreamPhase = dreamRecovery
                ? OccultActivityPolicy.DreamPhase.RECOVERY
                : dreamSession == null
                ? OccultActivityPolicy.DreamPhase.NONE
                : dreamSession.state() == SharedDreamSavedData.DreamState.LOBBY
                ? OccultActivityPolicy.DreamPhase.LOBBY
                : OccultActivityPolicy.DreamPhase.ACTIVE;
        boolean dreamDimension = SharedDreamService.DREAM_WORLD.equals(
                player.level().dimension());
        boolean spiritDimension = SpiritExpeditionService.SPIRIT_WORLD.equals(
                player.level().dimension());
        boolean spiritRecord = SpiritExpeditionSavedData.get(
                player.serverLevel()).get(player.getUUID()) != null;
        OccultActivityPolicy.Activity activity = OccultActivityPolicy.classify(
                dreamDimension, dreamPhase, spiritDimension, spiritRecord);
        return new Snapshot(
                activity, dreamDimension, dreamPhase,
                spiritDimension, spiritRecord);
    }

    public static boolean isAvailable(ServerPlayer player) {
        return inspect(player).activity().available();
    }

    public static boolean isProtectedDimension(ResourceKey<Level> dimension) {
        return SharedDreamService.DREAM_WORLD.equals(dimension)
                || SpiritExpeditionService.SPIRIT_WORLD.equals(dimension);
    }

    public static int showStatus(ServerPlayer player) {
        Snapshot snapshot = inspect(player);
        Component state = Component.translatable(
                "message.lord_of_mysteries.activity.state."
                        + snapshot.activity().id());
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.activity.status", state)
                .withStyle(snapshot.activity().available()
                        ? ChatFormatting.GREEN : ChatFormatting.GOLD));
        if (!snapshot.activity().available()) {
            PlayerFeedback.send(player, Component.translatable(
                    "message.lord_of_mysteries.activity.hint."
                            + snapshot.activity().recoveryHint())
                    .withStyle(ChatFormatting.GRAY));
        }
        return 1;
    }

    public static void sendDenied(ServerPlayer player) {
        Snapshot snapshot = inspect(player);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.activity.denied",
                Component.translatable(
                        "message.lord_of_mysteries.activity.state."
                                + snapshot.activity().id()))
                .withStyle(ChatFormatting.RED));
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.activity.check")
                .withStyle(ChatFormatting.GRAY));
    }

    public record Snapshot(
            OccultActivityPolicy.Activity activity,
            boolean dreamDimension,
            OccultActivityPolicy.DreamPhase dreamPhase,
            boolean spiritDimension,
            boolean spiritRecord) {}
}
