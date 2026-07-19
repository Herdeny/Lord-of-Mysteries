package top.aurora.lordofmysteries.commission;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;

final class DynamicCaseEvidenceData {

    private static final String TAG_INSTANCE = "dynamic_case_instance";
    private static final String TAG_EVIDENCE_THEME = "dynamic_case_evidence_theme";

    private DynamicCaseEvidenceData() {
    }

    static void write(CompoundTag tag, DynamicCaseProfile profile) {
        tag.putString(TAG_INSTANCE, profile.instanceId());
        tag.putString(TAG_EVIDENCE_THEME, profile.evidenceTheme().id());
    }

    static boolean matches(
            @Nullable CompoundTag tag, DynamicCaseProfile profile) {
        return isBound(tag)
                && profile.instanceId().equals(instanceId(tag))
                && profile.evidenceTheme().id().equals(evidenceThemeId(tag));
    }

    static boolean isBound(@Nullable CompoundTag tag) {
        return tag != null
                && tag.contains(TAG_INSTANCE)
                && tag.contains(TAG_EVIDENCE_THEME)
                && !tag.getString(TAG_INSTANCE).isBlank()
                && !tag.getString(TAG_EVIDENCE_THEME).isBlank();
    }

    static String instanceId(@Nullable CompoundTag tag) {
        return isBound(tag) ? tag.getString(TAG_INSTANCE) : "";
    }

    static String evidenceThemeId(@Nullable CompoundTag tag) {
        return isBound(tag) ? tag.getString(TAG_EVIDENCE_THEME) : "";
    }
}
