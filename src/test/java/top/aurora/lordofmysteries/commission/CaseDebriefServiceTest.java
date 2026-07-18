package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

class CaseDebriefServiceTest {

    @Test
    void completeSafeInvestigationEarnsSGrade() {
        CaseDebriefRecord record = CaseDebriefService.evaluate(
                CommissionService.COUNTERFEIT_FORMULA,
                evidence(7, 7, true),
                1_000L, 30_000L, "divination",
                0f, 0f, 0, 0);

        assertEquals(100, record.score());
        assertEquals(CaseGrade.S, record.grade());
        assertEquals("divination", record.resolutionRoute());
    }

    @Test
    void incompleteRiskyAndSlowCaseProducesActionableLowGrade() {
        CaseDebriefRecord record = CaseDebriefService.evaluate(
                CommissionService.MISSING_SQUAD,
                evidence(1, 4, false),
                1_000L, 200_000L, "assault",
                100f, 100f, 0, 0);

        assertEquals(27, record.score());
        assertEquals(CaseGrade.D, record.grade());
        assertEquals(CaseDebriefFocus.EVIDENCE, record.improvementFocus());
    }

    @Test
    void failedFormulaVerdictsPenalizeProcedureOnly() {
        CaseDebriefRecord record = CaseDebriefService.evaluate(
                CommissionService.COUNTERFEIT_FORMULA,
                evidence(7, 7, true),
                0L, 20_000L, "",
                0f, 0f, 3, 0);

        assertEquals(40, record.evidenceScore());
        assertEquals(15, record.procedureScore());
        assertEquals(20, record.safetyScore());
        assertEquals(10, record.efficiencyScore());
        assertEquals(CaseGrade.A, record.grade());
    }

    @Test
    void unresolvedHypothesisStrainPenalizesProcedureUntilRecovered() {
        CaseDebriefRecord record = CaseDebriefService.evaluate(
                CommissionService.MISSING_SQUAD,
                evidence(6, 6, true),
                0L, 20_000L, "stealth",
                0f, 0f, 0, 3);

        assertEquals(18, record.procedureScore());
        assertEquals(88, record.score());
        assertEquals(CaseGrade.A, record.grade());
    }

    @Test
    void recordClampsAndRoundTripsCorruptValues() {
        CaseDebriefRecord source = new CaseDebriefRecord(
                99, -2, 25, 12, -40L, -1L,
                "route-name-that-is-deliberately-longer-than-thirty-two-characters");
        CompoundTag saved = source.save();
        CaseDebriefRecord restored = CaseDebriefRecord.load(saved);

        assertEquals(40, restored.evidenceScore());
        assertEquals(0, restored.procedureScore());
        assertEquals(20, restored.safetyScore());
        assertEquals(10, restored.efficiencyScore());
        assertEquals(0L, restored.durationTicks());
        assertEquals("", restored.resolutionRoute());
        assertEquals(CaseGrade.B, restored.grade());
    }

    @Test
    void dynamicConclusionSurvivesDebriefRoundTrip() {
        CaseDebriefRecord source = CaseDebriefService.evaluate(
                CommissionService.DYNAMIC_CASE,
                evidence(5, 5, true),
                0L, 10_000L, "extraordinary_distortion",
                6f, 0f, 0, 0);

        CaseDebriefRecord restored = CaseDebriefRecord.load(source.save());

        assertEquals("extraordinary_distortion",
                restored.resolutionRoute());
        assertEquals(source.score(), restored.score());
    }

    private static CaseEvidenceView evidence(
            int discovered, int total, boolean conclusionReady) {
        return new CaseEvidenceView(
                "lord_of_mysteries:commission/test", "test.case",
                discovered, total, 100, discovered, 0,
                total - discovered, conclusionReady,
                conclusionReady ? CaseAnalysisStage.READY
                        : CaseAnalysisStage.COLLECTING,
                "test.theory", "test.next", CaseHypothesisView.EMPTY,
                List.of(), List.of());
    }
}
