package top.aurora.lordofmysteries.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.core.config.ServerConfig;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerDataSection;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

/** Synchronizes compact core state on lifecycle boundaries and dirty changes. */
@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerSummarySyncHandler {

    static final long POLL_INTERVAL_TICKS = 20L;
    static final long CORRECTION_INTERVAL_TICKS = 100L;

    private static final Map<UUID, Long> LAST_SYNC_TICKS = new ConcurrentHashMap<>();

    private PlayerSummarySyncHandler() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) send(player, true);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) send(player, true);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) send(player, true);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % POLL_INTERVAL_TICKS != 0L) {
            return;
        }
        send(player, false);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_SYNC_TICKS.remove(event.getEntity().getUUID());
    }

    static boolean shouldSend(boolean coreDirty, long lastSyncTick,
                              long currentTick) {
        return coreDirty || currentTick - lastSyncTick >= CORRECTION_INTERVAL_TICKS;
    }

    private static void send(ServerPlayer player, boolean force) {
        if (player.connection == null
                || player.connection.connection.channel() == null) {
            return;
        }
        long currentTick = player.serverLevel().getGameTime();
        long lastSyncTick = LAST_SYNC_TICKS.getOrDefault(
                player.getUUID(), Long.MIN_VALUE / 2L);
        PlayerMysteryData data = MysteryCapability.get(player);
        boolean coreDirty = data.isDirty(PlayerDataSection.CORE);
        if (!force && !shouldSend(coreDirty, lastSyncTick, currentTick)) return;

        PlayerMysterySummaryS2CPacket packet = PlayerMysterySummaryS2CPacket.from(
                data, ServerConfig.SHOW_EXACT_DIGESTION.get());
        PMNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        data.acknowledgeDirty(PlayerDataSection.CORE);
        LAST_SYNC_TICKS.put(player.getUUID(), currentTick);
    }
}
