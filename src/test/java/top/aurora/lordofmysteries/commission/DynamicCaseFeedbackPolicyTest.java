package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DynamicCaseFeedbackPolicyTest {

    @Test
    void strongDebriefsIncreaseWeeklyOrganizationReputation() {
        DynamicCaseFeedbackPolicy.Feedback feedback =
                DynamicCaseFeedbackPolicy.evaluate(
                        DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                        CaseGrade.S, 12);

        assertEquals(3, feedback.adjustment());
        assertEquals(15, feedback.updatedReputation());
        assertEquals(DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                feedback.organization());
    }

    @Test
    void weakDebriefsCreateARecoverableReputationConsequence() {
        DynamicCaseFeedbackPolicy.Feedback feedback =
                DynamicCaseFeedbackPolicy.evaluate(
                        DynamicCaseProfile.Organization.CONSTABULARY,
                        CaseGrade.D, 1);

        assertEquals(-2, feedback.adjustment());
        assertEquals(-1, feedback.updatedReputation());
    }

    @Test
    void middleDebriefsRemainNeutral() {
        DynamicCaseFeedbackPolicy.Feedback feedback =
                DynamicCaseFeedbackPolicy.evaluate(
                        DynamicCaseProfile.Organization.DETECTIVE_AGENCY,
                        CaseGrade.C, 7);

        assertEquals(0, feedback.adjustment());
        assertEquals(7, feedback.updatedReputation());
    }

    @Test
    void reputationArithmeticSaturatesAtIntegerBounds() {
        assertEquals(Integer.MAX_VALUE,
                DynamicCaseFeedbackPolicy.evaluate(
                        DynamicCaseProfile.Organization.DETECTIVE_AGENCY,
                        CaseGrade.S, Integer.MAX_VALUE).updatedReputation());
        assertEquals(Integer.MIN_VALUE,
                DynamicCaseFeedbackPolicy.evaluate(
                        DynamicCaseProfile.Organization.CONSTABULARY,
                        CaseGrade.D, Integer.MIN_VALUE).updatedReputation());
    }
}
