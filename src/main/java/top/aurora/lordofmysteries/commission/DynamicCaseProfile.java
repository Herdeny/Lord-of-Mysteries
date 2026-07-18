package top.aurora.lordofmysteries.commission;

import java.util.Locale;

public record DynamicCaseProfile(
        long caseDay,
        String instanceId,
        Archetype archetype,
        Subject subject,
        Motive motive,
        Method method,
        CaseLocation location,
        Anomaly anomaly,
        CoverUp coverUp,
        VictimImpact victimImpact,
        EvidenceTheme evidenceTheme,
        Conclusion conclusion) {

    public DynamicCaseProfile {
        if (caseDay < 0L || instanceId == null || instanceId.isBlank()
                || archetype == null || subject == null || motive == null
                || method == null || location == null || anomaly == null
                || coverUp == null || victimImpact == null
                || evidenceTheme == null || conclusion == null) {
            throw new IllegalArgumentException("dynamic case profile is incomplete");
        }
        if (!conclusion.supports(method)) {
            throw new IllegalArgumentException(
                    "dynamic case method does not match its conclusion");
        }
    }

    public interface SlotOption {
        String id();

        default String translationKey(String slot) {
            return "dynamic_case.lord_of_mysteries." + slot + "." + id();
        }
    }

    public enum Archetype implements SlotOption {
        MISSING_PERSON,
        ANOMALOUS_ITEM,
        OCCULT_CRIME;

        @Override
        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Subject implements SlotOption {
        APPRENTICE_REPORTER,
        DOCK_ACCOUNTANT,
        HERBALIST_ASSISTANT,
        RETIRED_CONSTABLE;

        @Override
        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Motive implements SlotOption {
        DEBT_PRESSURE,
        FORBIDDEN_CURIOSITY,
        WITNESS_SILENCING,
        REPUTATION_COVER;

        @Override
        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Method implements SlotOption {
        STAGED_DEPARTURE,
        FORGED_TRANSFER,
        MEMORY_OVERWRITE,
        MIRROR_SUBSTITUTION,
        RITUAL_LURE,
        SYMBOLIC_EXCHANGE;

        @Override
        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum CaseLocation implements SlotOption {
        MIST_CITY_OUTPOST,
        ABANDONED_CHURCH,
        CULTIST_CAMP,
        OCCULTIST_HUT;

        @Override
        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Anomaly implements SlotOption {
        REVERSED_FOOTPRINTS,
        COLD_ASH,
        SILENT_CLOCK,
        MIRROR_SCRIPT;

        @Override
        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum CoverUp implements SlotOption {
        FORGED_RECEIPT,
        PAID_TESTIMONY,
        CLEANED_SCENE,
        FALSE_BULLETIN;

        @Override
        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum VictimImpact implements SlotOption {
        FAMILY_PANIC,
        DISTRICT_SHORTAGE,
        GUARD_CRACKDOWN,
        OCCULT_RUMOR;

        @Override
        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum EvidenceTheme implements SlotOption {
        BRASS_TOKEN,
        INK_TRACE,
        CANDLE_WAX,
        TORN_SCHEDULE;

        @Override
        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Conclusion implements SlotOption {
        HUMAN_CONCEALMENT,
        EXTRAORDINARY_DISTORTION,
        RITUAL_DIVERSION;

        @Override
        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public boolean supports(Method candidate) {
            return switch (this) {
                case HUMAN_CONCEALMENT -> candidate == Method.STAGED_DEPARTURE
                        || candidate == Method.FORGED_TRANSFER;
                case EXTRAORDINARY_DISTORTION ->
                        candidate == Method.MEMORY_OVERWRITE
                                || candidate == Method.MIRROR_SUBSTITUTION;
                case RITUAL_DIVERSION -> candidate == Method.RITUAL_LURE
                        || candidate == Method.SYMBOLIC_EXCHANGE;
            };
        }

        public static Conclusion fromId(String value) {
            if (value == null) return null;
            for (Conclusion conclusion : values()) {
                if (conclusion.id().equals(value)) return conclusion;
            }
            return null;
        }
    }
}
