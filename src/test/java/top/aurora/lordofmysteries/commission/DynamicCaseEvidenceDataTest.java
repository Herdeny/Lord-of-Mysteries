package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

class DynamicCaseEvidenceDataTest {

    private static final DynamicCaseProfile PROFILE =
            DynamicCaseGenerator.generateForDay(40119L, 17L);

    @Test
    void sealedSampleStoresInstanceAndTheme() {
        CompoundTag tag = new CompoundTag();

        DynamicCaseEvidenceData.write(tag, PROFILE);

        assertTrue(DynamicCaseEvidenceData.matches(tag, PROFILE));
        assertEquals(PROFILE.instanceId(),
                DynamicCaseEvidenceData.instanceId(tag));
        assertEquals(PROFILE.evidenceTheme().id(),
                DynamicCaseEvidenceData.evidenceThemeId(tag));
    }

    @Test
    void sampleCannotCrossCaseRotations() {
        CompoundTag tag = new CompoundTag();
        DynamicCaseEvidenceData.write(tag, PROFILE);
        DynamicCaseProfile another =
                DynamicCaseGenerator.generateForDay(40119L, 18L);

        assertFalse(DynamicCaseEvidenceData.matches(tag, another));
        assertFalse(DynamicCaseEvidenceData.isBound(null));
    }

    @Test
    void alteredThemeInvalidatesTheSample() {
        CompoundTag tag = new CompoundTag();
        DynamicCaseEvidenceData.write(tag, PROFILE);
        tag.putString("dynamic_case_evidence_theme", "altered");

        assertFalse(DynamicCaseEvidenceData.matches(tag, PROFILE));
    }
}
