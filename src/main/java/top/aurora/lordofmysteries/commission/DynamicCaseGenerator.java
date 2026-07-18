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
        long entropy = mix(worldSeed ^ safeDay * 0x9E3779B97F4A7C15L);
        DynamicCaseProfile.Archetype archetype = rotatingArchetype(
                worldSeed, safeDay);
        DynamicCaseProfile.Conclusion conclusion = pick(
                DynamicCaseProfile.Conclusion.values(), entropy, 0x11L);
        DynamicCaseProfile.Method method = methodFor(
                conclusion, mix(entropy ^ 0x22L));
        String instanceId = archetype.id() + "-"
                + Long.toUnsignedString(safeDay, 36) + "-"
                + shortHash(mix(entropy ^ 0x5EEDL));
        return new DynamicCaseProfile(
                safeDay,
                instanceId,
                archetype,
                pick(DynamicCaseProfile.Subject.values(), entropy, 0x31L),
                pick(DynamicCaseProfile.Motive.values(), entropy, 0x41L),
                method,
                pick(DynamicCaseProfile.CaseLocation.values(), entropy, 0x51L),
                pick(DynamicCaseProfile.Anomaly.values(), entropy, 0x61L),
                pick(DynamicCaseProfile.CoverUp.values(), entropy, 0x71L),
                pick(DynamicCaseProfile.VictimImpact.values(), entropy, 0x81L),
                pick(DynamicCaseProfile.EvidenceTheme.values(), entropy, 0x91L),
                conclusion);
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
