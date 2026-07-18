package top.aurora.lordofmysteries.commission;

public enum CaseGrade {
    S,
    A,
    B,
    C,
    D;

    public static CaseGrade fromScore(int score) {
        if (score >= 90) return S;
        if (score >= 80) return A;
        if (score >= 65) return B;
        if (score >= 50) return C;
        return D;
    }
}
