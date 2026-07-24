package top.aurora.lordofmysteries.player;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PlayerFeedback {

    private PlayerFeedback() {}

    public static void send(Player player, Component message) {
        if (player instanceof ServerPlayer serverPlayer
                && serverPlayer.connection == null) {
            return;
        }
        player.sendSystemMessage(message);
    }
}
