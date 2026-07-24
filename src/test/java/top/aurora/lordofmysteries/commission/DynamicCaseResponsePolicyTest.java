package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DynamicCaseResponsePolicyTest {

    @Test
    void assignmentCarriesCaseIdentityAndCanonicalDeadline() {
        DynamicCaseHistoryEntry entry = historyEntry();

        DynamicCaseResponseTask task =
                DynamicCaseResponsePolicy.assign(
                        entry,
                        DynamicCaseWeeklyDirective.CHAIN_OF_CUSTODY,
                        22L);

        assertEquals(entry.instanceId(), task.instanceId());
        assertEquals(entry.subject(), task.contact());
        assertEquals(25L, task.expiresDay());
        assertEquals(DynamicCaseResponseTask.Stage.ASSIGNED,
                task.stage());
    }

    @Test
    void expiryStartsAtDeadlineWithoutEarlyFailure() {
        DynamicCaseResponseTask task =
                DynamicCaseResponsePolicy.assign(
                        historyEntry(),
                        DynamicCaseWeeklyDirective.CHAIN_OF_CUSTODY,
                        22L);

        assertFalse(DynamicCaseResponsePolicy.isExpired(task, 24L));
        assertTrue(DynamicCaseResponsePolicy.isExpired(task, 25L));
    }

    @Test
    void directiveRewardsFavorAuditOrRelationshipWork() {
        DynamicCaseResponsePolicy.Reward audit =
                DynamicCaseResponsePolicy.reward(
                        DynamicCaseWeeklyDirective.PATTERN_AUDIT);
        DynamicCaseResponsePolicy.Reward interview =
                DynamicCaseResponsePolicy.reward(
                        DynamicCaseWeeklyDirective.CLIENT_REINTERVIEW);

        assertEquals(8L, audit.moneyPence());
        assertEquals(1, audit.contactStanding());
        assertEquals(6L, interview.moneyPence());
        assertEquals(2, interview.contactStanding());
        assertEquals(1, interview.reputation());
    }

    private static DynamicCaseHistoryEntry historyEntry() {
        return new DynamicCaseHistoryEntry(
                21L, 3L, "response-history",
                DynamicCaseProfile.Archetype.OCCULT_CRIME,
                DynamicCaseProfile.Subject.RETIRED_CONSTABLE,
                DynamicCaseProfile.Organization.CONSTABULARY,
                DynamicCaseProfile.CaseLocation.ABANDONED_CHURCH,
                CaseGrade.A, 84, 3000L, 2,
                DynamicCaseHistoryEntry.FollowUpStatus.CLAIMED);
    }
}
