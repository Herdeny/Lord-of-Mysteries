package top.aurora.lordofmysteries.knowledge;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GuideJournalProgressTest {

    @Test
    void commonerStartsWithSafetyPotionRiskAndPathwayChapters() {
        assertTrue(GuideJournalProgress.isUnlocked(1, false, false, false, false));
        assertTrue(GuideJournalProgress.isUnlocked(2, false, false, false, false));
        assertTrue(GuideJournalProgress.isUnlocked(4, false, false, false, false));
        assertTrue(GuideJournalProgress.isUnlocked(9, false, false, false, false));
        assertFalse(GuideJournalProgress.isUnlocked(3, false, false, false, false));
    }

    @Test
    void seerUnlocksActingDivinationAndRitualSafety() {
        assertTrue(GuideJournalProgress.isUnlocked(3, true, true, false, false));
        assertTrue(GuideJournalProgress.isUnlocked(5, true, true, false, false));
        assertTrue(GuideJournalProgress.isUnlocked(6, true, true, false, false));
    }

    @Test
    void futureChaptersRequireTheirOwnDiscovery() {
        assertFalse(GuideJournalProgress.isUnlocked(7, true, true, false, false));
        assertTrue(GuideJournalProgress.isUnlocked(7, true, true, true, false));
        assertFalse(GuideJournalProgress.isUnlocked(8, true, true, true, false));
        assertTrue(GuideJournalProgress.isUnlocked(8, true, true, true, true));
    }
}
