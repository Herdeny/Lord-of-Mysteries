package top.aurora.lordofmysteries.ability;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class TravelerDoorPolicy {

    public static final int MAX_NAME_LENGTH = 32;
    public static final int MAX_BLOCKED_PLAYERS = 32;

    private TravelerDoorPolicy() {}

    public static boolean allows(
            UUID owner,
            String ownerTeam,
            TravelerDoorAccessMode accessMode,
            Collection<UUID> blockedPlayers,
            UUID candidate,
            String candidateTeam) {
        return allows(
                owner,
                ownerTeam,
                accessMode,
                "",
                0,
                blockedPlayers,
                candidate,
                candidateTeam,
                0);
    }

    public static boolean allows(
            UUID owner,
            String ownerTeam,
            TravelerDoorAccessMode accessMode,
            String organizationId,
            int ownerOrganizationReputation,
            Collection<UUID> blockedPlayers,
            UUID candidate,
            String candidateTeam,
            int candidateOrganizationReputation) {
        if (owner == null || candidate == null) return false;
        if (owner.equals(candidate)) return true;
        if (blockedPlayers != null && blockedPlayers.contains(candidate)) {
            return false;
        }
        TravelerDoorAccessMode mode = accessMode == null
                ? TravelerDoorAccessMode.PARTY : accessMode;
        if (mode == TravelerDoorAccessMode.ORGANIZATION) {
            return TravelerDoorOrganizationPolicy.allows(
                    organizationId,
                    ownerOrganizationReputation,
                    candidateOrganizationReputation);
        }
        return mode.allows(owner, ownerTeam, candidate, candidateTeam);
    }

    public static String normalizeName(String requestedName) {
        if (requestedName == null) return "";
        StringBuilder normalized = new StringBuilder();
        boolean previousWhitespace = false;
        for (int offset = 0;
                offset < requestedName.length()
                        && normalized.codePointCount(
                                0, normalized.length()) < MAX_NAME_LENGTH;) {
            int codePoint = requestedName.codePointAt(offset);
            offset += Character.charCount(codePoint);
            if (codePoint == '\u00a7') {
                if (offset < requestedName.length()) {
                    offset += Character.charCount(
                            requestedName.codePointAt(offset));
                }
                continue;
            }
            if (Character.isWhitespace(codePoint)) {
                if (normalized.length() > 0 && !previousWhitespace) {
                    normalized.append(' ');
                    previousWhitespace = true;
                }
                continue;
            }
            if (Character.isISOControl(codePoint)) continue;
            normalized.appendCodePoint(codePoint);
            previousWhitespace = false;
        }
        return normalized.toString().trim();
    }

    public static Set<UUID> normalizeBlacklist(
            Collection<UUID> blockedPlayers) {
        LinkedHashSet<UUID> normalized = new LinkedHashSet<>();
        if (blockedPlayers == null) return normalized;
        blockedPlayers.stream()
                .filter(value -> value != null)
                .sorted(Comparator.comparing(UUID::toString))
                .distinct()
                .limit(MAX_BLOCKED_PLAYERS)
                .forEach(normalized::add);
        return normalized;
    }
}
