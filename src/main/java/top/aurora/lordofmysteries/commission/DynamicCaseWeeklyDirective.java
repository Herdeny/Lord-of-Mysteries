package top.aurora.lordofmysteries.commission;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public enum DynamicCaseWeeklyDirective {
    PATTERN_AUDIT(DynamicCaseProfile.Organization.DETECTIVE_AGENCY),
    CLIENT_REINTERVIEW(DynamicCaseProfile.Organization.DETECTIVE_AGENCY),
    WITNESS_TIMELINE(DynamicCaseProfile.Organization.DETECTIVE_AGENCY),
    CLIENT_SAFETY_REVIEW(DynamicCaseProfile.Organization.DETECTIVE_AGENCY),
    SOURCE_VERIFICATION(DynamicCaseProfile.Organization.MIST_CITY_PRESS),
    PUBLIC_REASSURANCE(DynamicCaseProfile.Organization.MIST_CITY_PRESS),
    EDITORIAL_CORROBORATION(DynamicCaseProfile.Organization.MIST_CITY_PRESS),
    RUMOR_CONTAINMENT(DynamicCaseProfile.Organization.MIST_CITY_PRESS),
    CHAIN_OF_CUSTODY(DynamicCaseProfile.Organization.CONSTABULARY),
    DISTRICT_PATROL(DynamicCaseProfile.Organization.CONSTABULARY),
    WATCH_HANDOVER(DynamicCaseProfile.Organization.CONSTABULARY),
    CIVILIAN_SAFETY_CHECK(DynamicCaseProfile.Organization.CONSTABULARY);

    private final DynamicCaseProfile.Organization organization;

    DynamicCaseWeeklyDirective(
            DynamicCaseProfile.Organization organization) {
        this.organization = organization;
    }

    public DynamicCaseProfile.Organization organization() {
        return organization;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "dynamic_case.lord_of_mysteries.directive." + id();
    }

    public static DynamicCaseWeeklyDirective fromId(String value) {
        if (value == null) return null;
        for (DynamicCaseWeeklyDirective directive : values()) {
            if (directive.id().equals(value)) return directive;
        }
        return null;
    }

    public static DynamicCaseWeeklyDirective select(
            long worldSeed,
            long caseWeek,
            DynamicCaseProfile.Organization organization) {
        if (caseWeek < 0L || organization == null) {
            throw new IllegalArgumentException(
                    "dynamic case directive requires a valid week and organization");
        }
        List<DynamicCaseWeeklyDirective> candidates = new ArrayList<>();
        for (DynamicCaseWeeklyDirective directive : values()) {
            if (directive.organization == organization) {
                candidates.add(directive);
            }
        }
        long mixed = worldSeed
                ^ caseWeek * 0x9e3779b97f4a7c15L
                ^ (long) organization.ordinal() * 0xbf58476d1ce4e5b9L;
        mixed ^= mixed >>> 30;
        mixed *= 0xbf58476d1ce4e5b9L;
        mixed ^= mixed >>> 27;
        mixed *= 0x94d049bb133111ebL;
        mixed ^= mixed >>> 31;
        int index = (int) Math.floorMod(mixed, candidates.size());
        return candidates.get(index);
    }
}
