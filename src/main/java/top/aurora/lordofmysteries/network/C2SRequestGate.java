package top.aurora.lordofmysteries.network;

import java.util.Map;

public final class C2SRequestGate {

    private C2SRequestGate() {}

    public static boolean allow(Map<Integer, Long> lastAcceptedTicks,
                                int packetId, long gameTime,
                                long minimumIntervalTicks) {
        if (packetId < 0 || minimumIntervalTicks < 0L) return false;
        Long previous = lastAcceptedTicks.get(packetId);
        if (previous != null && gameTime >= previous
                && gameTime - previous < minimumIntervalTicks) return false;
        lastAcceptedTicks.put(packetId, gameTime);
        return true;
    }
}
