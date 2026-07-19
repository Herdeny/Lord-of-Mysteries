package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

class DynamicEvidencePortfolioDataTest {

    private static final DynamicCaseProfile PROFILE =
            DynamicCaseGenerator.generateForDay(7719L, 12L);

    @Test
    void bindingStoresTheStableCaseIdentity() {
        CompoundTag tag = new CompoundTag();

        DynamicEvidencePortfolioData.write(tag, PROFILE, 0);

        assertTrue(DynamicEvidencePortfolioData.matches(tag, PROFILE));
        assertEquals(PROFILE.instanceId(),
                DynamicEvidencePortfolioData.instanceId(tag));
        assertEquals(PROFILE.evidenceTheme().id(),
                DynamicEvidencePortfolioData.evidenceThemeId(tag));
    }

    @Test
    void evidenceStageIsClamped() {
        CompoundTag tag = new CompoundTag();

        DynamicEvidencePortfolioData.write(tag, PROFILE, 9);
        assertEquals(3, DynamicEvidencePortfolioData.stage(tag));

        DynamicEvidencePortfolioData.write(tag, PROFILE, -2);
        assertEquals(0, DynamicEvidencePortfolioData.stage(tag));
    }

    @Test
    void portfolioCannotBeReusedForAnotherRotation() {
        CompoundTag tag = new CompoundTag();
        DynamicEvidencePortfolioData.write(tag, PROFILE, 1);
        DynamicCaseProfile another =
                DynamicCaseGenerator.generateForDay(7719L, 13L);

        assertFalse(DynamicEvidencePortfolioData.matches(tag, another));
        assertFalse(DynamicEvidencePortfolioData.isBound(null));
    }
}
