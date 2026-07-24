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
        int confidence,
        int confirmed,
        int suspicious,
        int missing,
        boolean conclusionReady,
        CaseAnalysisStage analysisStage,
        String theoryKey,
        String nextActionKey,
        CaseHypothesisView hypothesis,
        List<Entry> entries,
        List<Relation> relations) {

    private static final ResourceLocation MISSING_SQUAD_EVIDENCE = id(
            "knowledge/m2/missing_squad_evidence");
    private static final ResourceLocation REPORTER_RESCUED = id(
            "knowledge/m2/reporter_rescued");
    private static final ResourceLocation FORMULA_AUTHENTICITY = id(
            "knowledge/m2/formula_authenticity");

    public static final CaseEvidenceView EMPTY = new CaseEvidenceView(
            "", "", 0, 0, 0, 0, 0, 0, false,
            CaseAnalysisStage.NO_CASE, "", "", CaseHypothesisView.EMPTY,
            List.of(), List.of());

    public CaseEvidenceView {
        total = Math.max(0, total);
        discovered = Math.min(total, Math.max(0, discovered));
        confidence = Math.min(100, Math.max(0, confidence));
        confirmed = Math.min(total, Math.max(0, confirmed));
        suspicious = Math.min(total, Math.max(0, suspicious));
        missing = Math.min(total, Math.max(0, missing));
        hypothesis = hypothesis == null ? CaseHypothesisView.EMPTY : hypothesis;
        entries = List.copyOf(entries);
        relations = List.copyOf(relations);
    }

    public static CaseEvidenceView from(
            PlayerMysteryData data,
            FormulaAppraisalService.DossierEvidence dossier) {
        return from(data, dossier, null);
    }

    public static CaseEvidenceView from(
            PlayerMysteryData data,
            FormulaAppraisalService.DossierEvidence dossier,
            DynamicCaseProfile dynamicCase) {
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
        if (CommissionService.DYNAMIC_CASE.equals(commissionId)
                && dynamicCase != null) {
            return dynamicCase(data, dynamicCase);
        }
        return EMPTY;
    }

    private static CaseEvidenceView lostCat(PlayerMysteryData data) {
        boolean pressRecord = completedStep(data, 0);
        boolean campTracks = completedStep(data, 2);
        boolean catRecovered = completedStep(data, 3);
        List<Entry> entries = List.of(
                progress("lost_cat", "press_record", pressRecord),
                progress("lost_cat", "camp_tracks", campTracks),
                progress("lost_cat", "cat_recovered", catRecovered));
        List<Relation> relations = List.of(
                relation("lost_cat", "record_to_tracks",
                        EvidenceRelationKind.LEADS_TO,
                        linkState(entries.get(0), entries.get(1))),
                relation("lost_cat", "tracks_to_recovery",
                        EvidenceRelationKind.SUPPORTS,
                        linkState(entries.get(1), entries.get(2))));
        return view(data, CommissionService.LOST_CAT,
                "commission.lord_of_mysteries.lost_cat.title",
                catRecovered, entries, relations,
                nextAction("lost_cat", pressRecord
                        ? campTracks ? catRecovered ? "return" : "recover"
                        : "camp" : "press"), null);
    }

    private static CaseEvidenceView missingSquad(PlayerMysteryData data) {
        boolean departureLog = completedStep(data, 0);
        boolean lastCamp = completedStep(data, 2);
        boolean bloodstainedNotes = completedStep(data, 3)
                || data.knownKnowledge.contains(MISSING_SQUAD_EVIDENCE);
        boolean authorization = completedStep(data, 6);
        boolean survivorTestimony = completedStep(data, 9)
                || data.knownKnowledge.contains(REPORTER_RESCUED);
        boolean nightDefense = completedStep(data, 11);
        List<Entry> entries = List.of(
                progress("missing_squad", "departure_log", departureLog),
                progress("missing_squad", "last_camp", lastCamp),
                progress("missing_squad", "bloodstained_notes", bloodstainedNotes),
                progress("missing_squad", "authorization", authorization),
                progress("missing_squad", "survivor_testimony", survivorTestimony),
                progress("missing_squad", "night_defense", nightDefense));
        List<Relation> relations = List.of(
                relation("missing_squad", "departure_to_camp",
                        EvidenceRelationKind.LEADS_TO,
                        linkState(entries.get(0), entries.get(1))),
                relation("missing_squad", "camp_to_notes",
                        EvidenceRelationKind.SUPPORTS,
                        linkState(entries.get(1), entries.get(2))),
                relation("missing_squad", "notes_to_authorization",
                        EvidenceRelationKind.LEADS_TO,
                        linkState(entries.get(2), entries.get(3))),
                relation("missing_squad", "notes_to_testimony",
                        EvidenceRelationKind.SUPPORTS,
                        linkState(entries.get(2), entries.get(4))),
                relation("missing_squad", "testimony_to_defense",
                        EvidenceRelationKind.SUPPORTS,
                        linkState(entries.get(4), entries.get(5))));
        String next = !departureLog ? "press"
                : !lastCamp ? "camp"
                : !bloodstainedNotes ? "notes"
                : !authorization ? "authorization"
                : !survivorTestimony ? "rescue"
                : !nightDefense ? "defense" : "return";
        return view(data, CommissionService.MISSING_SQUAD,
                "commission.lord_of_mysteries.missing_squad.title",
                nightDefense, entries, relations,
                nextAction("missing_squad", next), null);
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
        List<Relation> relations = new ArrayList<>();
        relations.add(relation("counterfeit_formula", "registry_to_dossier",
                EvidenceRelationKind.SUPPORTS,
                linkState(entries.get(0), entries.get(2))));
        relations.add(formulaRelation("watermark", entries.get(3)));
        relations.add(formulaRelation("ink", entries.get(4)));
        relations.add(formulaRelation("sequence", entries.get(5)));
        EvidenceState synthesisState = synthesisState(entries.subList(3, 6));
        relations.add(relation("counterfeit_formula", "clue_synthesis",
                synthesisState == EvidenceState.SUSPICIOUS
                        ? EvidenceRelationKind.CONTRADICTS
                        : EvidenceRelationKind.SUPPORTS,
                synthesisState));
        String next = !completedStep(data, 0) ? "hut"
                : !completedStep(data, 1) ? "appraiser"
                : !dossier.present() ? "dossier"
                : !dossier.appraised() ? "inspect"
                : !dossier.verdictSubmitted()
                        && !data.knownKnowledge.contains(FORMULA_AUTHENTICITY)
                        && !completedStep(data, 4) ? "verdict" : "return";
        String readyTheory = synthesisState == EvidenceState.SUSPICIOUS
                ? "screen.lord_of_mysteries.analysis.counterfeit_formula.theory.contradiction"
                : "screen.lord_of_mysteries.analysis.counterfeit_formula.theory.consistent";
        return view(data, CommissionService.COUNTERFEIT_FORMULA,
                "commission.lord_of_mysteries.counterfeit_formula.title",
                dossier.appraised(), entries, relations,
                nextAction("counterfeit_formula", next), readyTheory);
    }

    private static CaseEvidenceView dynamicCase(
            PlayerMysteryData data, DynamicCaseProfile profile) {
        boolean scene = completedStep(data, 0);
        boolean testimony = completedStep(data, 1);
        boolean records = completedStep(data, 2);
        boolean recovered = "recovered".equals(data.questResolutionRoute);
        List<Entry> entries = List.of(
                new Entry(
                        "screen.lord_of_mysteries.evidence.dynamic_case.brief.title",
                        profile.archetype().translationKey("archetype"),
                        EvidenceState.CONFIRMED),
                generatedEntry("scene", profile.anomaly().translationKey("anomaly"),
                        scene, EvidenceState.CONFIRMED),
                generatedEntry("testimony", profile.motive().translationKey("motive"),
                        testimony, EvidenceState.CONFIRMED),
                generatedEntry("records", profile.method().translationKey("method"),
                        records, EvidenceState.CONFIRMED),
                new Entry(
                        recovered
                                ? "screen.lord_of_mysteries.evidence.dynamic_case.false_lead.revealed.title"
                                : "screen.lord_of_mysteries.evidence.dynamic_case.false_lead.title",
                        records
                                ? recovered
                                        ? "screen.lord_of_mysteries.evidence.dynamic_case.false_lead.recovered"
                                        : profile.coverUp().translationKey("cover_up")
                                : "screen.lord_of_mysteries.evidence.not_recorded",
                        records
                                ? recovered ? EvidenceState.CONFIRMED
                                        : EvidenceState.SUSPICIOUS
                                : EvidenceState.MISSING));
        List<Relation> relations = List.of(
                relation("dynamic_case", "brief_to_scene",
                        EvidenceRelationKind.LEADS_TO,
                        linkState(entries.get(0), entries.get(1))),
                relation("dynamic_case", "schedule_to_scene",
                        EvidenceRelationKind.LEADS_TO,
                        entries.get(1).state()),
                relation("dynamic_case", "scene_to_testimony",
                        EvidenceRelationKind.SUPPORTS,
                        linkState(entries.get(1), entries.get(2))),
                relation("dynamic_case", "relationship_to_testimony",
                        EvidenceRelationKind.SUPPORTS,
                        entries.get(2).state()),
                relation("dynamic_case", "testimony_to_records",
                        EvidenceRelationKind.LEADS_TO,
                        linkState(entries.get(2), entries.get(3))),
                relation("dynamic_case", "false_lead_conflict",
                        EvidenceRelationKind.CONTRADICTS,
                        entries.get(4).state()),
                relation("dynamic_case", "synthesis_to_conclusion",
                        EvidenceRelationKind.SUPPORTS,
                        records ? EvidenceState.CONFIRMED : EvidenceState.MISSING));
        String next = !scene ? "scene"
                : !testimony ? "witness"
                : !records ? "records"
                : "reconsider".equals(data.questResolutionRoute)
                        ? "recover"
                        : data.activeQuestStep >= 4 ? "return" : "conclude";
        return view(data, CommissionService.DYNAMIC_CASE,
                "commission.lord_of_mysteries.dynamic_case.title",
                records, entries, relations,
                nextAction("dynamic_case", next),
                "screen.lord_of_mysteries.analysis.dynamic_case.theory.ready");
    }

    private static Entry generatedEntry(
            String evidenceId,
            String detailKey,
            boolean discovered,
            EvidenceState discoveredState) {
        return new Entry(
                "screen.lord_of_mysteries.evidence.dynamic_case."
                        + evidenceId + ".title",
                discovered ? detailKey
                        : "screen.lord_of_mysteries.evidence.not_recorded",
                discovered ? discoveredState : EvidenceState.MISSING);
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
            PlayerMysteryData data,
            ResourceLocation commissionId,
            String titleKey,
            boolean conclusionReady,
            List<Entry> entries,
            List<Relation> relations,
            String nextActionKey,
            String readyTheoryKey) {
        int discovered = (int) entries.stream()
                .filter(entry -> entry.state() != EvidenceState.MISSING)
                .count();
        int confirmed = (int) entries.stream()
                .filter(entry -> entry.state() == EvidenceState.CONFIRMED)
                .count();
        int suspicious = (int) entries.stream()
                .filter(entry -> entry.state() == EvidenceState.SUSPICIOUS)
                .count();
        int missing = entries.size() - discovered;
        int confidence = entries.isEmpty() ? 0
                : Math.round(discovered * 100f / entries.size());
        CaseAnalysisStage stage = conclusionReady
                ? CaseAnalysisStage.READY
                : discovered * 2 >= entries.size()
                        ? CaseAnalysisStage.CORRELATING
                        : CaseAnalysisStage.COLLECTING;
        String theoryKey = stage == CaseAnalysisStage.READY
                ? readyTheoryKey == null
                        ? "screen.lord_of_mysteries.analysis."
                                + casePath(commissionId) + ".theory.ready"
                        : readyTheoryKey
                : "screen.lord_of_mysteries.analysis."
                        + casePath(commissionId) + ".theory."
                        + stage.name().toLowerCase(java.util.Locale.ROOT);
        List<Relation> revealedRelations = relations.stream()
                .filter(relation -> relation.state() != EvidenceState.MISSING)
                .toList();
        return new CaseEvidenceView(
                commissionId.toString(), titleKey, discovered,
                entries.size(), confidence, confirmed, suspicious, missing,
                conclusionReady, stage, theoryKey, nextActionKey,
                CaseHypothesisView.from(data.caseHypotheses.get(commissionId)),
                entries, revealedRelations);
    }

    private static Relation formulaRelation(String clue, Entry entry) {
        return relation("counterfeit_formula", "appraisal_to_" + clue,
                entry.state() == EvidenceState.SUSPICIOUS
                        ? EvidenceRelationKind.CONTRADICTS
                        : EvidenceRelationKind.SUPPORTS,
                entry.state());
    }

    private static EvidenceState synthesisState(List<Entry> clues) {
        if (clues.stream().anyMatch(entry -> entry.state() == EvidenceState.MISSING)) {
            return EvidenceState.MISSING;
        }
        if (clues.stream().anyMatch(
                entry -> entry.state() == EvidenceState.SUSPICIOUS)) {
            return EvidenceState.SUSPICIOUS;
        }
        return EvidenceState.CONFIRMED;
    }

    private static EvidenceState linkState(Entry source, Entry target) {
        if (source.state() == EvidenceState.MISSING
                || target.state() == EvidenceState.MISSING) {
            return EvidenceState.MISSING;
        }
        if (source.state() == EvidenceState.SUSPICIOUS
                || target.state() == EvidenceState.SUSPICIOUS) {
            return EvidenceState.SUSPICIOUS;
        }
        return EvidenceState.CONFIRMED;
    }

    private static Relation relation(
            String caseId,
            String relationId,
            EvidenceRelationKind kind,
            EvidenceState state) {
        String prefix = "screen.lord_of_mysteries.analysis."
                + caseId + ".relation." + relationId;
        return new Relation(
                relationId, prefix + ".title", prefix + ".detail", kind, state);
    }

    private static String nextAction(String caseId, String action) {
        return "screen.lord_of_mysteries.analysis."
                + caseId + ".next." + action;
    }

    private static String casePath(ResourceLocation commissionId) {
        if (CommissionService.MISSING_SQUAD.equals(commissionId)) {
            return "missing_squad";
        }
        if (CommissionService.DYNAMIC_CASE.equals(commissionId)) {
            return "dynamic_case";
        }
        String path = commissionId.getPath();
        int separator = path.lastIndexOf('/');
        return separator >= 0 ? path.substring(separator + 1) : path;
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

    public record Relation(
            String id,
            String titleKey,
            String detailKey,
            EvidenceRelationKind kind,
            EvidenceState state) {

        public Relation {
            id = id == null ? "" : id;
        }
    }
}
