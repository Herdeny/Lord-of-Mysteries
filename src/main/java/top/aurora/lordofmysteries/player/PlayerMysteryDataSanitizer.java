package top.aurora.lordofmysteries.player;

import java.util.UUID;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.potion.PotionQuality;

public final class PlayerMysteryDataSanitizer {

    private PlayerMysteryDataSanitizer() {}

    public static int sanitize(PlayerMysteryData data) {
        int repairs = 0;
        if (data.pathway == null && data.sequence != -1) {
            data.sequence = -1;
            repairs++;
        } else if (data.pathway != null && (data.sequence < 0 || data.sequence > 9)) {
            data.sequence = Math.max(0, Math.min(9, data.sequence));
            repairs++;
        }
        repairs += repairCoreValues(data);

        String normalizedQuality = PotionQuality.fromId(data.potionQuality).id();
        if (!normalizedQuality.equals(data.potionQuality)) {
            data.potionQuality = normalizedQuality;
            repairs++;
        }

        repairs += repairTrialValues(data);
        repairs += repairCommissionValues(data);
        int originalCooldowns = data.commissionCooldowns.size();
        data.commissionCooldowns.entrySet().removeIf(entry ->
                entry.getKey() == null || entry.getValue() == null || entry.getValue() < 0L);
        repairs += originalCooldowns - data.commissionCooldowns.size();
        int originalCompleted = data.completedCommissions.size();
        data.completedCommissions.removeIf(id -> id == null);
        repairs += originalCompleted - data.completedCommissions.size();
        int originalKnowledge = data.knownKnowledge.size();
        data.knownKnowledge.removeIf(id -> id == null);
        repairs += originalKnowledge - data.knownKnowledge.size();
        int originalCounters = data.actingCounters.size();
        data.actingCounters.entrySet().removeIf(entry ->
                entry.getKey() == null || entry.getValue() == null || entry.getValue() < 0);
        repairs += originalCounters - data.actingCounters.size();
        return repairs;
    }

    private static int repairCoreValues(PlayerMysteryData data) {
        int repairs = 0;
        float maximum = finiteClamp(data.spiritualityMax, 1f, 10000f, 100f);
        repairs += setFloat(data.spiritualityMax, maximum,
                value -> data.spiritualityMax = value);
        repairs += setFloat(data.spirituality,
                finiteClamp(data.spirituality, 0f, data.spiritualityMax, 0f),
                value -> data.spirituality = value);
        repairs += setFloat(data.digestion,
                finiteClamp(data.digestion, 0f, 100f, 0f),
                value -> data.digestion = value);
        repairs += setFloat(data.pollution,
                finiteClamp(data.pollution, 0f, 100f, 0f),
                value -> data.pollution = value);
        repairs += setFloat(data.insanityPressure,
                finiteClamp(data.insanityPressure, 0f, 100f, 0f),
                value -> data.insanityPressure = value);
        return repairs;
    }

    private static int repairTrialValues(PlayerMysteryData data) {
        int repairs = 0;
        if (data.m1TrialStartTick < -1L) {
            data.m1TrialStartTick = -1L;
            repairs++;
        }
        if (!data.m1TrialActive && data.m1TrialStartTick != -1L) {
            data.m1TrialStartTick = -1L;
            repairs++;
        }
        repairs += clampNonNegativeLong(data.m1TrialElapsedTicks,
                value -> data.m1TrialElapsedTicks = value);
        repairs += clampNonNegativeInt(data.m1TrialOccultKills,
                value -> data.m1TrialOccultKills = value);
        repairs += clampNonNegativeInt(data.m1TrialDeaths,
                value -> data.m1TrialDeaths = value);
        repairs += clampNonNegativeInt(data.m1TrialRestRecoveries,
                value -> data.m1TrialRestRecoveries = value);
        repairs += clampNonNegativeInt(data.m1TrialCharmsConsumed,
                value -> data.m1TrialCharmsConsumed = value);
        repairs += clampNonNegativeInt(data.m1TrialActingEvents,
                value -> data.m1TrialActingEvents = value);
        repairs += clampNonNegativeInt(data.m1TrialReconnects,
                value -> data.m1TrialReconnects = value);
        repairs += clampNonNegativeInt(data.m1TrialServerRestarts,
                value -> data.m1TrialServerRestarts = value);
        repairs += clampNonNegativeInt(data.m1TrialDimensionChanges,
                value -> data.m1TrialDimensionChanges = value);
        repairs += clampNonNegativeInt(data.m1TrialDeathRecoveries,
                value -> data.m1TrialDeathRecoveries = value);
        repairs += setFloat(data.m1TrialMaxPressure,
                finiteClamp(data.m1TrialMaxPressure, 0f, 100f, 0f),
                value -> data.m1TrialMaxPressure = value);
        repairs += setFloat(data.m1TrialMaxPollution,
                finiteClamp(data.m1TrialMaxPollution, 0f, 100f, 0f),
                value -> data.m1TrialMaxPollution = value);
        repairs += clampOptionalTick(data.m1TrialCampReachedTick,
                value -> data.m1TrialCampReachedTick = value);
        repairs += clampOptionalTick(data.m1TrialSequence9Tick,
                value -> data.m1TrialSequence9Tick = value);
        repairs += clampOptionalTick(data.m1TrialSequence8Tick,
                value -> data.m1TrialSequence8Tick = value);
        repairs += clampOptionalTick(data.m1TrialSequence7Tick,
                value -> data.m1TrialSequence7Tick = value);
        repairs += clampOptionalTick(data.m1TrialFirstOccultKillTick,
                value -> data.m1TrialFirstOccultKillTick = value);
        repairs += clampOptionalTick(data.m1TrialFirstActingTick,
                value -> data.m1TrialFirstActingTick = value);
        repairs += clampOptionalTick(data.m1TrialRiskReachedTick,
                value -> data.m1TrialRiskReachedTick = value);
        if (!data.m1TrialSessionId.isBlank() && !validUuid(data.m1TrialSessionId)) {
            data.m1TrialSessionId = "";
            repairs++;
        }
        return repairs;
    }

    private static int repairCommissionValues(PlayerMysteryData data) {
        int repairs = 0;
        data.activeCommissionId = safe(data.activeCommissionId);
        data.activeQuestChainId = safe(data.activeQuestChainId);
        data.escortedReporterUuid = safe(data.escortedReporterUuid);
        boolean commissionValid = data.activeCommissionId.isBlank()
                || ResourceLocation.tryParse(data.activeCommissionId) != null;
        boolean questValid = data.activeQuestChainId.isBlank()
                || ResourceLocation.tryParse(data.activeQuestChainId) != null;
        boolean incompletePair = data.activeCommissionId.isBlank()
                != data.activeQuestChainId.isBlank();
        if (!commissionValid || !questValid || incompletePair) {
            clearActiveCommission(data);
            return repairs + 1;
        }
        if (data.activeCommissionId.isBlank()) {
            if (data.activeQuestStep != -1 || data.questObjectiveProgress != 0
                    || !data.escortedReporterUuid.isBlank()
                    || data.questDefenseWaveSpawned || data.questDefenseNextTick != 0L) {
                clearActiveCommission(data);
                repairs++;
            }
        } else {
            if (data.activeQuestStep < 0) {
                data.activeQuestStep = 0;
                repairs++;
            }
            if (data.questObjectiveProgress < 0) {
                data.questObjectiveProgress = 0;
                repairs++;
            }
            if (data.commissionAcceptedTick < 0L) {
                data.commissionAcceptedTick = 0L;
                repairs++;
            }
            if (!data.escortedReporterUuid.isBlank()
                    && !validUuid(data.escortedReporterUuid)) {
                data.escortedReporterUuid = "";
                repairs++;
            }
        }
        if (data.moneyPence < 0L) {
            data.moneyPence = 0L;
            repairs++;
        }
        return repairs;
    }

    private static void clearActiveCommission(PlayerMysteryData data) {
        data.activeCommissionId = "";
        data.activeQuestChainId = "";
        data.activeQuestStep = -1;
        data.questObjectiveProgress = 0;
        data.commissionAcceptedTick = 0L;
        data.escortedReporterUuid = "";
        data.questDefenseWaveSpawned = false;
        data.questDefenseNextTick = 0L;
    }

    private static float finiteClamp(float value, float minimum,
                                     float maximum, float fallback) {
        if (!Float.isFinite(value)) return fallback;
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static int setFloat(float current, float repaired, FloatSetter setter) {
        if (Float.compare(current, repaired) == 0) return 0;
        setter.set(repaired);
        return 1;
    }

    private static int clampNonNegativeLong(long current, LongSetter setter) {
        if (current >= 0L) return 0;
        setter.set(0L);
        return 1;
    }

    private static int clampOptionalTick(long current, LongSetter setter) {
        if (current >= -1L) return 0;
        setter.set(-1L);
        return 1;
    }

    private static int clampNonNegativeInt(int current, IntSetter setter) {
        if (current >= 0) return 0;
        setter.set(0);
        return 1;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static boolean validUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    @FunctionalInterface
    private interface FloatSetter {
        void set(float value);
    }

    @FunctionalInterface
    private interface LongSetter {
        void set(long value);
    }

    @FunctionalInterface
    private interface IntSetter {
        void set(int value);
    }
}
