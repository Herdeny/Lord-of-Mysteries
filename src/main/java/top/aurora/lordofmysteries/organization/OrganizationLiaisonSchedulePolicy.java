package top.aurora.lordofmysteries.organization;

import net.minecraft.core.BlockPos;

public final class OrganizationLiaisonSchedulePolicy {

    private static final long DAY_LENGTH = 24_000L;

    private OrganizationLiaisonSchedulePolicy() {}

    public static Shift shift(long dayTime) {
        long timeOfDay = Math.floorMod(dayTime, DAY_LENGTH);
        if (timeOfDay < 2_000L) return Shift.BRIEFING;
        if (timeOfDay < 12_000L) return Shift.SERVICE;
        return Shift.WATCH;
    }

    public static BlockPos position(
            BlockPos outpost, int slot, long dayTime) {
        if (outpost == null || slot < 1
                || slot > OrganizationActionPolicy.DAILY_ACTION_COUNT) {
            throw new IllegalArgumentException(
                    "invalid organization liaison schedule");
        }
        int centeredSlot = slot - 2;
        return switch (shift(dayTime)) {
            case BRIEFING -> outpost.offset(centeredSlot * 2, 1, 4);
            case SERVICE -> outpost.offset(centeredSlot * 4, 1, 5);
            case WATCH -> outpost.offset(centeredSlot * 2, 1, 3);
        };
    }

    public enum Shift {
        BRIEFING("briefing"),
        SERVICE("service"),
        WATCH("watch");

        private final String id;

        Shift(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public String translationKey() {
            return "message.lord_of_mysteries.organization.shift." + id;
        }
    }
}
