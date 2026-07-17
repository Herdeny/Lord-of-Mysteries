package top.aurora.lordofmysteries.commission;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public record CaseEvidenceView(
        String commissionId,
        String caseTitleKey,
        int discovered,
        int total,
        boolean conclusionReady,
        List<Entry> entries) {

    private static final ResourceLocation MISSING_SQUAD_EVIDENCE = id(
            "knowledge/m2/missing_squad_evidence");
    private static final ResourceLocation REPORTER_RESCUED = id(
            "knowledge/m2/reporter_rescued");
    private static final ResourceLocation FORMULA_AUTHENTICITY = id(
            "knowledge/m2/formula_authenticity");

    public static final CaseEvidenceView EMPTY = new CaseEvidenceView(
            "", "", 0, 0, false, List.of());

    public CaseEvidenceView {
        entries = List.copyOf(entries);
    }

    public static CaseEvidenceView from(
            PlayerMysteryData data,
            FormulaAppraisalService.DossierEvidence dossier) {
        ResourceLocation commissionId = ResourceLocation.tryParse(
                data.activeCommissionId);
        if (CommissionService.LOST_CAT.equals(commissionId)) {
            return lostCat(data);
        }
        if (CommissionService.MISSING_SQUAD.equals(commissionId)) {
            return missingSquad(data);
        }
        if (CommissionService.COUNTERFEIT_FORMULA.equals(commissionId)) {
            return counterfeitFormula(data, dossier);
        }
        return EMPTY;
    }

    private static CaseEvidenceView lostCat(PlayerMysteryData data) {
        List<Entry> entries = List.of(
                progress("lost_cat", "press_record", completedStep(data, 0)),
                progress("lost_cat", "camp_tracks", completedStep(data, 2)),
                progress("lost_cat", "cat_recovered", completedStep(data, 3)));
        return view(CommissionService.LOST_CAT,
                "commission.lord_of_mysteries.lost_cat.title",
                completedStep(data, 3), entries);
    }

    private static CaseEvidenceView missingSquad(PlayerMysteryData data) {
        List<Entry> entries = List.of(
                progress("missing_squad", "departure_log", completedStep(data, 0)),
                progress("missing_squad", "last_camp", completedStep(data, 2)),
                progress("missing_squad", "bloodstained_notes",
                        completedStep(data, 3)
                                || data.knownKnowledge.contains(MISSING_SQUAD_EVIDENCE)),
                progress("missing_squad", "authorization", completedStep(data, 6)),
                progress("missing_squad", "survivor_testimony",
                        completedStep(data, 9)
                                || data.knownKnowledge.contains(REPORTER_RESCUED)),
                progress("missing_squad", "night_defense", completedStep(data, 11)));
        return view(CommissionService.MISSING_SQUAD,
                "commission.lord_of_mysteries.missing_squad.title",
                completedStep(data, 11), entries);
    }

    private static CaseEvidenceView counterfeitFormula(
            PlayerMysteryData data,
            FormulaAppraisalService.DossierEvidence dossier) {
        List<Entry> entries = new ArrayList<>();
        entries.add(progress("counterfeit_formula", "hut_registry",
                completedStep(data, 0)));
        entries.add(progress("counterfeit_formula", "appraiser_statement",
                completedStep(data, 1)));
        entries.add(progress("counterfeit_formula", "sealed_dossier",
                dossier.present() || completedStep(data, 2)));
        entries.add(formulaClue("watermark", dossier, 0b001));
        entries.add(formulaClue("ink", dossier, 0b010));
        entries.add(formulaClue("sequence", dossier, 0b100));
        entries.add(progress("counterfeit_formula", "verdict",
                dossier.verdictSubmitted()
                        || data.knownKnowledge.contains(FORMULA_AUTHENTICITY)
                        || completedStep(data, 4)));
        return view(CommissionService.COUNTERFEIT_FORMULA,
                "commission.lord_of_mysteries.counterfeit_formula.title",
                dossier.appraised(), entries);
    }

    private static Entry progress(String caseId, String evidenceId,
                                  boolean discovered) {
        String prefix = "screen.lord_of_mysteries.evidence."
                + caseId + "." + evidenceId;
        return new Entry(prefix + ".title",
                discovered ? prefix + ".detail"
                        : "screen.lord_of_mysteries.evidence.not_recorded",
                discovered ? EvidenceState.CONFIRMED : EvidenceState.MISSING);
    }

    private static Entry formulaClue(
            String clue,
            FormulaAppraisalService.DossierEvidence dossier,
            int bit) {
        String titleKey = "screen.lord_of_mysteries.evidence.counterfeit_formula."
                + clue + ".title";
        if (!dossier.appraised()) {
            return new Entry(titleKey,
                    "screen.lord_of_mysteries.evidence.not_recorded",
                    EvidenceState.MISSING);
        }
        boolean consistent = (dossier.clueMask() & bit) != 0;
        return new Entry(titleKey,
                "message.lord_of_mysteries.formula.clue." + clue + "."
                        + (consistent ? "consistent" : "suspicious"),
                consistent ? EvidenceState.CONFIRMED : EvidenceState.SUSPICIOUS);
    }

    private static CaseEvidenceView view(
            ResourceLocation commissionId,
            String titleKey,
            boolean conclusionReady,
            List<Entry> entries) {
        int discovered = (int) entries.stream()
                .filter(entry -> entry.state() != EvidenceState.MISSING)
                .count();
        return new CaseEvidenceView(
                commissionId.toString(), titleKey, discovered,
                entries.size(), conclusionReady, entries);
    }

    private static boolean completedStep(PlayerMysteryData data, int stepIndex) {
        return data.activeQuestStep > stepIndex;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, path);
    }

    public record Entry(
            String titleKey,
            String detailKey,
            EvidenceState state) {}
}
