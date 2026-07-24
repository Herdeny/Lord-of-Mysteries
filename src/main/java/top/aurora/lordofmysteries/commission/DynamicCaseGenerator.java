package top.aurora.lordofmysteries.commission;

public final class DynamicCaseGenerator {

    public static final long TICKS_PER_CASE_DAY = 24_000L;

    private DynamicCaseGenerator() {}

    public static DynamicCaseProfile generate(long worldSeed, long acceptedTick) {
        return generateForDay(worldSeed,
                Math.floorDiv(Math.max(0L, acceptedTick), TICKS_PER_CASE_DAY));
    }

    public static DynamicCaseProfile generateForDay(long worldSeed, long caseDay) {
        long safeDay = Math.max(0L, caseDay);
        long caseWeek = Math.floorDiv(safeDay, 7L);
        long entropy = mix(worldSeed ^ safeDay * 0x9E3779B97F4A7C15L);
        DynamicCaseProfile.Archetype archetype = rotatingArchetype(
                worldSeed, safeDay);
        DynamicCaseProfile.Subject subject = pick(
                DynamicCaseProfile.Subject.values(), entropy, 0x31L);
        DynamicCaseProfile.Conclusion conclusion = pick(
                DynamicCaseProfile.Conclusion.values(), entropy, 0x11L);
        DynamicCaseProfile.Method method = methodFor(
                conclusion, mix(entropy ^ 0x22L));
        String instanceId = archetype.id() + "-"
                + Long.toUnsignedString(safeDay, 36) + "-"
                + shortHash(mix(entropy ^ 0x5EEDL));
        return new DynamicCaseProfile(
                safeDay,
                caseWeek,
                instanceId,
                archetype,
                subject,
                rotatingOrganization(worldSeed, caseWeek),
                relationshipFor(subject),
                scheduleFor(subject),
                pick(DynamicCaseProfile.Motive.values(), entropy, 0x41L),
                method,
                pick(DynamicCaseProfile.CaseLocation.values(), entropy, 0x51L),
                pick(DynamicCaseProfile.Anomaly.values(), entropy, 0x61L),
                pick(DynamicCaseProfile.CoverUp.values(), entropy, 0x71L),
                pick(DynamicCaseProfile.VictimImpact.values(), entropy, 0x81L),
                pick(DynamicCaseProfile.EvidenceTheme.values(), entropy, 0x91L),
                conclusion);
    }

    private static DynamicCaseProfile.Organization rotatingOrganization(
            long worldSeed, long caseWeek) {
        DynamicCaseProfile.Organization[] values =
                DynamicCaseProfile.Organization.values();
        int worldOffset = Math.floorMod(mix(worldSeed ^ 0x0A6A_1A7EL),
                values.length);
        return values[Math.floorMod(caseWeek + worldOffset, values.length)];
    }

    private static DynamicCaseProfile.Relationship relationshipFor(
            DynamicCaseProfile.Subject subject) {
        return switch (subject) {
            case APPRENTICE_REPORTER ->
                    DynamicCaseProfile.Relationship.EDITORIAL_SUPERVISOR;
            case DOCK_ACCOUNTANT ->
                    DynamicCaseProfile.Relationship.DEPENDENT_RELATIVE;
            case HERBALIST_ASSISTANT ->
                    DynamicCaseProfile.Relationship.SHOP_MENTOR;
            case RETIRED_CONSTABLE ->
                    DynamicCaseProfile.Relationship.FORMER_PATROL_PARTNER;
        };
    }

    private static DynamicCaseProfile.Schedule scheduleFor(
            DynamicCaseProfile.Subject subject) {
        return switch (subject) {
            case APPRENTICE_REPORTER ->
                    DynamicCaseProfile.Schedule.PRESS_MORNING;
            case DOCK_ACCOUNTANT ->
                    DynamicCaseProfile.Schedule.DOCK_AFTERNOON;
            case HERBALIST_ASSISTANT ->
                    DynamicCaseProfile.Schedule.APOTHECARY_EVENING;
            case RETIRED_CONSTABLE ->
                    DynamicCaseProfile.Schedule.CONSTABLE_NIGHT;
        };
    }

    private static DynamicCaseProfile.Archetype rotatingArchetype(
            long worldSeed, long caseDay) {
        DynamicCaseProfile.Archetype[] values =
                DynamicCaseProfile.Archetype.values();
        int worldOffset = Math.floorMod(mix(worldSeed), values.length);
        return values[Math.floorMod(caseDay + worldOffset, values.length)];
    }

    private static DynamicCaseProfile.Method methodFor(
            DynamicCaseProfile.Conclusion conclusion, long entropy) {
        DynamicCaseProfile.Method[] options = switch (conclusion) {
            case HUMAN_CONCEALMENT -> new DynamicCaseProfile.Method[]{
                    DynamicCaseProfile.Method.STAGED_DEPARTURE,
                    DynamicCaseProfile.Method.FORGED_TRANSFER};
            case EXTRAORDINARY_DISTORTION -> new DynamicCaseProfile.Method[]{
                    DynamicCaseProfile.Method.MEMORY_OVERWRITE,
                    DynamicCaseProfile.Method.MIRROR_SUBSTITUTION};
            case RITUAL_DIVERSION -> new DynamicCaseProfile.Method[]{
                    DynamicCaseProfile.Method.RITUAL_LURE,
                    DynamicCaseProfile.Method.SYMBOLIC_EXCHANGE};
        };
        return options[Math.floorMod(entropy, options.length)];
    }

    private static <T> T pick(T[] values, long entropy, long salt) {
        return values[Math.floorMod(mix(entropy ^ salt), values.length)];
    }

    private static String shortHash(long value) {
        String hash = Long.toUnsignedString(value, 36);
        return ("000000" + hash).substring(Math.max(0, hash.length()));
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
