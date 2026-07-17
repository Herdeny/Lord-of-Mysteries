package top.aurora.lordofmysteries.client;

import java.util.Optional;

import top.aurora.lordofmysteries.network.PlayerMysterySummaryS2CPacket;

/** Client-side read-only cache populated only by server summary packets. */
public final class ClientMysteryState {

    private static volatile PlayerMysterySummaryS2CPacket latest;

    private ClientMysteryState() {}

    public static void update(PlayerMysterySummaryS2CPacket summary) {
        latest = summary;
    }

    public static Optional<PlayerMysterySummaryS2CPacket> latest() {
        return Optional.ofNullable(latest);
    }

    public static void clear() {
        latest = null;
    }
}
