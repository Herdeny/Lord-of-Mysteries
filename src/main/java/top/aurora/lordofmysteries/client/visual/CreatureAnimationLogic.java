package top.aurora.lordofmysteries.client.visual;

public final class CreatureAnimationLogic {
    private CreatureAnimationLogic() {}

    public static Pose sample(float limbSwing, float amount, float age, float attack,
                              boolean aggressive, boolean grounded, boolean reducedMotion) {
        float stride = clamp(amount, 0, 1);
        float clock = Float.isFinite(age) ? age : 0;
        float swing = Float.isFinite(limbSwing) ? limbSwing : 0;
        float strike = (float) Math.sin(Math.sqrt(clamp(attack, 0, 1)) * Math.PI);
        float walk = (float) Math.cos(swing * 0.6662f) * stride;
        float breathing = reducedMotion ? 0 : (float) Math.sin(clock * 0.075f) * (1 - stride) * 0.25f;
        float flap = (float) Math.sin(clock * (grounded ? 0.12f : 0.55f))
                * (grounded ? (reducedMotion ? 0 : 0.08f) : 0.7f);
        float posture = aggressive ? -0.85f : 0;
        return new Pose(walk, strike, breathing, flap, posture - strike * 0.9f, strike * 0.14f);
    }

    public static float clamp(float value, float minimum, float maximum) {
        return Float.isFinite(value) ? Math.max(minimum, Math.min(maximum, value)) : minimum;
    }

    public record Pose(float walk, float strike, float breathing, float flap, float armPitch, float lean) {}
}
