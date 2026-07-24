package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

class DynamicCaseContactMemoryIntegrationTest {

    @Test
    void duplicateCompletionCannotDuplicateStandingOrMemory() {
        PlayerMysteryData data = new PlayerMysteryData();
        DynamicCaseProfile profile =
                DynamicCaseGenerator.generateForDay(77123L, 12L);
        CaseDebriefRecord debrief = new CaseDebriefRecord(
                40, 30, 20, 10,
                1000L, 1400L, "field");
        DynamicCaseFeedbackPolicy.Feedback feedback =
                DynamicCaseFeedbackPolicy.evaluate(
                        profile.organization(),
                        debrief.grade(),
                        0);

        DynamicCaseService.recordCompletion(
                data, profile, debrief, feedback);
        DynamicCaseService.recordCompletion(
                data, profile, debrief, feedback);

        assertEquals(1, data.dynamicCaseHistory.size());
        assertEquals(1, data.dynamicCaseContactEvents.size());
        assertEquals(3, data.dynamicCaseContactStandings.get(
                profile.subject()));
    }
}
