package top.aurora.lordofmysteries.commission;

import java.util.Locale;
import java.util.Map;

public final class DynamicCaseRelationshipPolicy {

    public static final int MIN_STANDING = -20;
    public static final int MAX_STANDING = 20;

    private DynamicCaseRelationshipPolicy() {
    }

    public static int recordCaseResult(
            Map<DynamicCaseProfile.Subject, Integer> standings,
            DynamicCaseProfile.Subject contact,
            CaseGrade grade) {
        if (grade == null) {
            throw new IllegalArgumentException("case grade is required");
        }
        int adjustment = switch (grade) {
            case S -> 3;
            case A -> 2;
            case B -> 1;
            case C -> 0;
            case D -> -2;
        };
        return adjust(standings, contact, adjustment);
    }

    public static int adjust(
            Map<DynamicCaseProfile.Subject, Integer> standings,
            DynamicCaseProfile.Subject contact,
            int adjustment) {
        if (standings == null || contact == null) {
            throw new IllegalArgumentException(
                    "contact standings and contact are required");
        }
        int current = standings.getOrDefault(contact, 0);
        long candidate = (long) current + adjustment;
        int updated = (int) Math.max(
                MIN_STANDING, Math.min(MAX_STANDING, candidate));
        if (updated == 0) {
            standings.remove(contact);
        } else {
            standings.put(contact, updated);
        }
        return updated;
    }

    public static int sanitize(
            Map<DynamicCaseProfile.Subject, Integer> standings) {
        if (standings == null) return 0;
        int repairs = 0;
        var iterator = standings.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<DynamicCaseProfile.Subject, Integer> entry =
                    iterator.next();
            if (entry.getKey() == null || entry.getValue() == null) {
                iterator.remove();
                repairs++;
                continue;
            }
            int repaired = Math.max(
                    MIN_STANDING,
                    Math.min(MAX_STANDING, entry.getValue()));
            if (repaired == 0) {
                iterator.remove();
                repairs++;
            } else if (repaired != entry.getValue()) {
                entry.setValue(repaired);
                repairs++;
            }
        }
        return repairs;
    }

    public static Attitude attitude(int standing) {
        int normalized = Math.max(
                MIN_STANDING, Math.min(MAX_STANDING, standing));
        if (normalized <= -8) return Attitude.HOSTILE;
        if (normalized <= -3) return Attitude.WARY;
        if (normalized <= 2) return Attitude.NEUTRAL;
        if (normalized <= 7) return Attitude.TRUSTING;
        return Attitude.ALLIED;
    }

    public enum Attitude {
        HOSTILE,
        WARY,
        NEUTRAL,
        TRUSTING,
        ALLIED;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String translationKey() {
            return "dynamic_case.lord_of_mysteries.contact_attitude." + id();
        }
    }
}
