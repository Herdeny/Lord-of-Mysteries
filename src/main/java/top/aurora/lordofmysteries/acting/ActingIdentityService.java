package top.aurora.lordofmysteries.acting;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class ActingIdentityService {

    private ActingIdentityService() {}

    public static void recordPractice(PlayerMysteryData data,
                                      float novelty, float digestionGain) {
        if (digestionGain <= 0f) return;
        float normalizedNovelty = clamp(novelty, 0f, 1f);
        data.principleInsight = clamp(data.principleInsight
                + Math.max(0.1f, digestionGain * 0.08f * normalizedNovelty),
                0f, 100f);
        if (normalizedNovelty < 0.25f && data.insanityPressure >= 35f) {
            data.roleOveridentification = clamp(
                    data.roleOveridentification
                            + 0.5f + data.insanityPressure / 100f,
                    0f, 100f);
        } else if (normalizedNovelty >= 0.75f) {
            data.roleOveridentification = clamp(
                    data.roleOveridentification - 0.25f, 0f, 100f);
        }
    }

    public static void recordAdvancement(PlayerMysteryData data) {
        if (data.insanityPressure < 50f) return;
        data.roleOveridentification = clamp(
                data.roleOveridentification
                        + 2f + data.insanityPressure / 25f,
                0f, 100f);
    }

    public static ReflectionResult reflect(PlayerMysteryData data, long day) {
        if (!data.isExtraordinary()) return ReflectionResult.COMMONER;
        if (data.lastActingReflectionDay == day) {
            return ReflectionResult.ALREADY_REFLECTED;
        }
        data.lastActingReflectionDay = day;
        data.actingReflectionCount++;
        data.principleInsight = clamp(data.principleInsight + 1f, 0f, 100f);
        data.roleOveridentification = clamp(
                data.roleOveridentification - 8f, 0f, 100f);
        data.insanityPressure = clamp(data.insanityPressure - 2f, 0f, 100f);
        return ReflectionResult.SUCCESS;
    }

    private static float clamp(float value, float minimum, float maximum) {
        if (!Float.isFinite(value)) return minimum;
        return Math.max(minimum, Math.min(maximum, value));
    }

    public enum ReflectionResult {
        SUCCESS,
        COMMONER,
        ALREADY_REFLECTED
    }
}
