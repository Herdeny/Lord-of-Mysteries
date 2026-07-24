package top.aurora.lordofmysteries.commission;

import java.util.Locale;

import top.aurora.lordofmysteries.world.MistCityWorldEvent;

public final class CityEconomyPolicy {

    private CityEconomyPolicy() {}

    public static boolean canWork(long lastWorkDay, long currentDay) {
        if (currentDay < 0L) {
            throw new IllegalArgumentException("current day must be non-negative");
        }
        return lastWorkDay != currentDay;
    }

    public static ShiftTerms terms(Job job, MistCityWorldEvent worldEvent) {
        if (job == null || worldEvent == null) {
            throw new IllegalArgumentException(
                    "city job and world event are required");
        }
        ShiftTerms base = switch (job) {
            case PRESS -> new ShiftTerms(24L, 3, 0f, 4f);
            case AGENCY -> new ShiftTerms(22L, 0, 1f, 2f);
            case PATROL -> new ShiftTerms(28L, 0, 2f, 3f);
        };
        return switch (worldEvent) {
            case CLEAR -> base;
            case DENSE_FOG -> base.adjust(4L, 1f, 1f);
            case SPIRITUAL_SURGE -> base.adjust(5L, 2f, 1f);
            case BLOOD_MOON -> base.adjust(8L, 3f, 2f);
            case EVIL_GAZE -> base.adjust(10L, 5f, 1f);
            case WITCH_HUNT_NIGHT -> base.adjust(12L, 6f, 4f);
            case RITUAL_RESONANCE -> base.adjust(6L, 1f, 2f);
        };
    }

    public enum Job {
        PRESS,
        AGENCY,
        PATROL;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public String translationKey() {
            return "city_job.lord_of_mysteries." + id();
        }
    }

    public record ShiftTerms(
            long rewardPence,
            int paperCost,
            float pressureIncrease,
            float exposureReduction) {

        public ShiftTerms {
            if (rewardPence < 0L || paperCost < 0
                    || pressureIncrease < 0f || exposureReduction < 0f) {
                throw new IllegalArgumentException(
                        "city shift terms must be non-negative");
            }
        }

        ShiftTerms adjust(
                long rewardIncrease,
                float pressureIncrease,
                float exposureReduction) {
            return new ShiftTerms(
                    rewardPence + rewardIncrease,
                    paperCost,
                    this.pressureIncrease + pressureIncrease,
                    this.exposureReduction + exposureReduction);
        }
    }
}
