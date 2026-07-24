package top.aurora.lordofmysteries.world;

public final class MistCityWorldEventPolicy {

    public static final int CYCLE_DAYS = 16;

    private MistCityWorldEventPolicy() {}

    public static MistCityWorldEvent eventForDay(long worldSeed, long day) {
        if (day < 0L) {
            throw new IllegalArgumentException("world event day must be non-negative");
        }
        int offset = Math.floorMod(mix(worldSeed), CYCLE_DAYS);
        int cycleDay = Math.floorMod(day + offset, CYCLE_DAYS);
        return switch (cycleDay) {
            case 0, 1 -> MistCityWorldEvent.DENSE_FOG;
            case 3 -> MistCityWorldEvent.SPIRITUAL_SURGE;
            case 5 -> MistCityWorldEvent.EVIL_GAZE;
            case 8 -> MistCityWorldEvent.RITUAL_RESONANCE;
            case 11 -> MistCityWorldEvent.BLOOD_MOON;
            case 13, 14, 15 -> MistCityWorldEvent.WITCH_HUNT_NIGHT;
            default -> MistCityWorldEvent.CLEAR;
        };
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
