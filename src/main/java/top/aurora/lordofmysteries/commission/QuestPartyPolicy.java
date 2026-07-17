package top.aurora.lordofmysteries.commission;

import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

public final class QuestPartyPolicy {

    private QuestPartyPolicy() {}

    public static boolean sharingAllowed(boolean sharedProgress,
                                         int maximumPartySize,
                                         int eligiblePlayers) {
        return sharedProgress && maximumPartySize > 1
                && eligiblePlayers > 1 && eligiblePlayers <= maximumPartySize;
    }

    public static boolean teamEligible(boolean sharedProgress,
                                       int maximumPartySize,
                                       int rosterSize) {
        return sharedProgress && maximumPartySize > 1
                && rosterSize > 1 && rosterSize <= maximumPartySize;
    }

    public static UUID coordinator(Collection<UUID> players) {
        return players.stream().min(Comparator.comparing(UUID::toString)).orElse(null);
    }
}
