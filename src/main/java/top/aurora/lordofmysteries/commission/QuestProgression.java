package top.aurora.lordofmysteries.commission;

public final class QuestProgression {

    private QuestProgression() {}

    public static Result record(QuestChainDefinition chain, int stepIndex,
                                int currentProgress, String objectiveType,
                                String objectiveTarget, int amount) {
        if (stepIndex < 0 || stepIndex >= chain.steps().size() || amount <= 0) {
            return new Result(stepIndex, currentProgress, false, false, false);
        }
        QuestChainDefinition.Objective objective =
                chain.steps().get(stepIndex).objective();
        if (!objective.type().equals(objectiveType)
                || (!objective.target().isBlank()
                && !objective.target().equals(objectiveTarget))) {
            return new Result(stepIndex, currentProgress, false, false, false);
        }
        long accumulated = (long) Math.max(0, currentProgress) + amount;
        int progress = (int) Math.min(objective.count(), accumulated);
        if (progress < objective.count()) {
            return new Result(stepIndex, progress, true, false, false);
        }
        int nextStep = stepIndex + 1;
        return new Result(nextStep, 0, true, true,
                nextStep >= chain.steps().size());
    }

    public record Result(int stepIndex, int progress, boolean matched,
                         boolean stepCompleted, boolean chainCompleted) {}
}
