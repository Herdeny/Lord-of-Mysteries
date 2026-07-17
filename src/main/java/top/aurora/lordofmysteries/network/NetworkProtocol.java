package top.aurora.lordofmysteries.network;

import java.util.List;

public final class NetworkProtocol {

    public static final String VERSION = "9";
    public static final int TOGGLE_SPIRIT_VISION = 0;
    public static final int USE_SIMPLE_DIVINATION = 1;
    public static final int REQUEST_STATUS = 2;
    public static final int PLAYER_STATUS = 3;
    public static final int TOGGLE_EMOTION_READ = 4;
    public static final int USE_SURFACE_READ = 5;
    public static final int USE_MENTAL_SUGGESTION = 6;
    public static final int USE_PROVOKE = 7;
    public static final int USE_ENRAGE = 8;
    public static final int USE_SEER_ABILITY = 9;
    public static final int USE_M2_FOUNDATION_ABILITY = 10;
    public static final int PLAYER_SUMMARY = 11;
    public static final int INVESTIGATION_BOARD = 12;
    public static final int INVESTIGATION_BOARD_ACTION = 13;
    public static final int PACKET_COUNT = 14;

    private NetworkProtocol() {}

    public static boolean accepts(String remoteVersion) {
        return VERSION.equals(remoteVersion);
    }

    public static List<Integer> packetIds() {
        return List.of(
                TOGGLE_SPIRIT_VISION,
                USE_SIMPLE_DIVINATION,
                REQUEST_STATUS,
                PLAYER_STATUS,
                TOGGLE_EMOTION_READ,
                USE_SURFACE_READ,
                USE_MENTAL_SUGGESTION,
                USE_PROVOKE,
                USE_ENRAGE,
                USE_SEER_ABILITY,
                USE_M2_FOUNDATION_ABILITY,
                PLAYER_SUMMARY,
                INVESTIGATION_BOARD,
                INVESTIGATION_BOARD_ACTION);
    }
}
