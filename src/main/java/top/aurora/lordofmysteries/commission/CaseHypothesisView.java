package top.aurora.lordofmysteries.commission;

public record CaseHypothesisView(
        String relationId,
        CaseHypothesisStance stance,
        String note,
        CaseHypothesisStatus status,
        int unresolvedStrain,
        int failedTests) {

    public static final CaseHypothesisView EMPTY = new CaseHypothesisView(
            "", CaseHypothesisStance.SUPPORTS, "",
            CaseHypothesisStatus.DRAFT, 0, 0);

    public CaseHypothesisView {
        relationId = relationId == null ? "" : relationId;
        stance = stance == null ? CaseHypothesisStance.SUPPORTS : stance;
        note = note == null ? "" : note;
        status = status == null ? CaseHypothesisStatus.DRAFT : status;
        unresolvedStrain = Math.max(0, Math.min(
                CaseHypothesisRecord.MAX_STRAIN, unresolvedStrain));
        failedTests = Math.max(0, failedTests);
    }

    public boolean hasDraft() {
        return !relationId.isBlank() && !note.isBlank();
    }

    public static CaseHypothesisView from(CaseHypothesisRecord record) {
        if (record == null) return EMPTY;
        return new CaseHypothesisView(
                record.relationId(), record.stance(), record.note(),
                record.status(), record.unresolvedStrain(),
                record.failedTests());
    }
}
