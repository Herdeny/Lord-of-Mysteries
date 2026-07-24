package top.aurora.lordofmysteries.commission;

import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

public final class QuestPartyPolicy {

    public static final int MAXIMUM_PERSISTENT_PARTY_SIZE = 4;

    private QuestPartyPolicy() {}

    public static boolean sharingAllowed(boolean sharedProgress,
                                         int maximumPartySize,
                                         int eligiblePlayers) {
        return sharedProgress && maximumPartySize > 1
                && maximumPartySize <= MAXIMUM_PERSISTENT_PARTY_SIZE
                && eligiblePlayers > 1 && eligiblePlayers <= maximumPartySize;
    }

    public static boolean teamEligible(boolean sharedProgress,
                                       int maximumPartySize,
                                       int rosterSize) {
        return sharedProgress && maximumPartySize > 1
                && maximumPartySize <= MAXIMUM_PERSISTENT_PARTY_SIZE
                && rosterSize > 1 && rosterSize <= maximumPartySize;
    }

    public static boolean continuationAllowed(
            boolean sharedProgress,
            int maximumPartySize,
            int rosterSize,
            boolean registeredMember) {
        return sharedProgress && (registeredMember || teamEligible(
                true, maximumPartySize, rosterSize));
    }

    public static UUID coordinator(Collection<UUID> players) {
        return players.stream().min(Comparator.comparing(UUID::toString)).orElse(null);
    }
}
