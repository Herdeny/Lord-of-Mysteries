package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MysticalExposurePolicyTest {

    @Test
    void caseGradesCreateConsequencesInsteadOfOnlyRewards() {
        assertEquals(-4, MysticalExposurePolicy.caseDelta(CaseGrade.S));
        assertEquals(-2, MysticalExposurePolicy.caseDelta(CaseGrade.A));
        assertEquals(0, MysticalExposurePolicy.caseDelta(CaseGrade.B));
        assertEquals(2, MysticalExposurePolicy.caseDelta(CaseGrade.C));
        assertEquals(6, MysticalExposurePolicy.caseDelta(CaseGrade.D));
    }

    @Test
    void exposureIsFiniteAndClamped() {
        assertEquals(0f, MysticalExposurePolicy.adjust(2f, -20f));
        assertEquals(100f, MysticalExposurePolicy.adjust(95f, 20f));
        assertEquals(0f,
                MysticalExposurePolicy.adjust(Float.NaN, Float.NaN));
    }

    @Test
    void allPublicExposureBandsHaveStableBoundaries() {
        assertEquals(MysticalExposurePolicy.Band.HIDDEN,
                MysticalExposurePolicy.band(19.9f));
        assertEquals(MysticalExposurePolicy.Band.RUMORED,
                MysticalExposurePolicy.band(20f));
        assertEquals(MysticalExposurePolicy.Band.NOTICED,
                MysticalExposurePolicy.band(45f));
        assertEquals(MysticalExposurePolicy.Band.WATCHED,
                MysticalExposurePolicy.band(70f));
        assertEquals(MysticalExposurePolicy.Band.EXPOSED,
                MysticalExposurePolicy.band(90f));
    }
}
