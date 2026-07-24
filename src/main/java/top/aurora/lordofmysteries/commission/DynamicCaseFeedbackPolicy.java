package top.aurora.lordofmysteries.commission;

final class DynamicCaseFeedbackPolicy {

    private DynamicCaseFeedbackPolicy() {
    }

    static Feedback evaluate(
            DynamicCaseProfile.Organization organization,
            CaseGrade grade,
            int currentReputation) {
        if (organization == null || grade == null) {
            throw new IllegalArgumentException(
                    "dynamic case feedback requires organization and grade");
        }
        int adjustment = switch (grade) {
            case S -> 3;
            case A -> 2;
            case B -> 1;
            case C -> 0;
            case D -> -2;
        };
        return new Feedback(
                organization, grade, adjustment, currentReputation + adjustment);
    }

    record Feedback(
            DynamicCaseProfile.Organization organization,
            CaseGrade grade,
            int adjustment,
            int updatedReputation) {
    }
}
