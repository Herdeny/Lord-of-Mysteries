package top.aurora.lordofmysteries.commission;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;

final class DynamicEvidencePortfolioData {

    private static final String TAG_INSTANCE = "dynamic_case_instance";
    private static final String TAG_ARCHETYPE = "dynamic_case_archetype";
    private static final String TAG_EVIDENCE_THEME = "dynamic_case_evidence_theme";
    private static final String TAG_STAGE = "dynamic_case_evidence_stage";

    private DynamicEvidencePortfolioData() {
    }

    static void write(
            CompoundTag tag, DynamicCaseProfile profile, int collectedStage) {
        tag.putString(TAG_INSTANCE, profile.instanceId());
        tag.putString(TAG_ARCHETYPE, profile.archetype().id());
        tag.putString(TAG_EVIDENCE_THEME, profile.evidenceTheme().id());
        tag.putInt(TAG_STAGE, clampStage(collectedStage));
    }

    static boolean matches(
            @Nullable CompoundTag tag, DynamicCaseProfile profile) {
        return isBound(tag) && profile.instanceId().equals(instanceId(tag));
    }

    static boolean isBound(@Nullable CompoundTag tag) {
        return tag != null
                && tag.contains(TAG_INSTANCE)
                && tag.contains(TAG_EVIDENCE_THEME);
    }

    static String instanceId(@Nullable CompoundTag tag) {
        return isBound(tag) ? tag.getString(TAG_INSTANCE) : "";
    }

    static String evidenceThemeId(@Nullable CompoundTag tag) {
        return isBound(tag) ? tag.getString(TAG_EVIDENCE_THEME) : "";
    }

    static int stage(@Nullable CompoundTag tag) {
        return isBound(tag) ? clampStage(tag.getInt(TAG_STAGE)) : 0;
    }

    private static int clampStage(int stage) {
        return Math.min(3, Math.max(0, stage));
    }
}
