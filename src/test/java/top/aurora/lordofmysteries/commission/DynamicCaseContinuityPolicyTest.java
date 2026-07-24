package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class DynamicCaseContinuityPolicyTest {

    @Test
    void recordingANewCaseExpiresThePreviousFollowUp() {
        List<DynamicCaseHistoryEntry> history = new ArrayList<>();
        DynamicCaseContinuityPolicy.record(history,
                DynamicCaseHistoryEntryTest.entry(
                        7L, "first", CaseGrade.A,
                        DynamicCaseHistoryEntry.FollowUpStatus.PENDING));
        DynamicCaseContinuityPolicy.record(history,
                DynamicCaseHistoryEntryTest.entry(
                        8L, "second", CaseGrade.B,
                        DynamicCaseHistoryEntry.FollowUpStatus.PENDING));

        assertEquals(2, history.size());
        assertEquals(DynamicCaseHistoryEntry.FollowUpStatus.EXPIRED,
                history.get(0).followUpStatus());
        assertEquals("second",
                DynamicCaseContinuityPolicy.latestPending(history)
                        .orElseThrow().instanceId());
    }

    @Test
    void historyRetainsOnlyTheLatestEightUniqueCases() {
        List<DynamicCaseHistoryEntry> history = new ArrayList<>();
        for (int index = 0; index < 10; index++) {
            DynamicCaseContinuityPolicy.record(history,
                    DynamicCaseHistoryEntryTest.entry(
                            index, "case-" + index, CaseGrade.C,
                            DynamicCaseHistoryEntry.FollowUpStatus.PENDING));
        }

        assertEquals(DynamicCaseContinuityPolicy.MAX_HISTORY_ENTRIES,
                history.size());
        assertEquals("case-2", history.get(0).instanceId());
        assertEquals("case-9", history.get(7).instanceId());
    }

    @Test
    void sanitizerDeduplicatesAndKeepsOnlyNewestPendingEntry() {
        DynamicCaseHistoryEntry older =
                DynamicCaseHistoryEntryTest.entry(
                        7L, "duplicate", CaseGrade.B,
                        DynamicCaseHistoryEntry.FollowUpStatus.PENDING);
        DynamicCaseHistoryEntry newer =
                DynamicCaseHistoryEntryTest.entry(
                        8L, "duplicate", CaseGrade.A,
                        DynamicCaseHistoryEntry.FollowUpStatus.PENDING);
        DynamicCaseHistoryEntry latest =
                DynamicCaseHistoryEntryTest.entry(
                        9L, "latest", CaseGrade.S,
                        DynamicCaseHistoryEntry.FollowUpStatus.PENDING);
        List<DynamicCaseHistoryEntry> history = new ArrayList<>(
                List.of(older, newer, latest));

        assertEquals(2, DynamicCaseContinuityPolicy.sanitize(history));
        assertEquals(2, history.size());
        assertEquals(DynamicCaseHistoryEntry.FollowUpStatus.EXPIRED,
                history.get(0).followUpStatus());
        assertEquals("latest",
                DynamicCaseContinuityPolicy.latestPending(history)
                        .orElseThrow().instanceId());
    }

    @Test
    void rewardTableIsRecoverableAndBounded() {
        DynamicCaseContinuityPolicy.Reward strong =
                DynamicCaseContinuityPolicy.reward(CaseGrade.S);
        DynamicCaseContinuityPolicy.Reward weak =
                DynamicCaseContinuityPolicy.reward(CaseGrade.D);

        assertEquals(8L, strong.moneyPence());
        assertEquals(1, strong.reputation());
        assertEquals(0f, strong.pressureRecovery());
        assertEquals(0L, weak.moneyPence());
        assertEquals(1, weak.reputation());
        assertTrue(weak.pressureRecovery() > 0f);
    }
}
