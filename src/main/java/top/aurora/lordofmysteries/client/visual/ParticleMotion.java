package top.aurora.lordofmysteries.client.visual;

public enum ParticleMotion {
    WISP(0.08f, 0.18f, 0.8f, 0.35f),
    MIST(0.12f, 0.04f, 1.1f, 0.1f),
    RUNE(0.02f, 0.03f, 0.3f, 1.3f),
    THREAD(0.015f, 0.04f, 0.1f, 0),
    BUBBLE(0.03f, 0.35f, 0.5f, 0.2f),
    EMBER(0.035f, 0.2f, -0.55f, 1.8f),
    RING(0, 0, 2.2f, 0),
    PETAL(0.08f, -0.2f, -0.2f, 2.0f);

    private final float orbit;
    private final float lift;
    private final float growth;
    private final float spin;

    ParticleMotion(float orbit, float lift, float growth, float spin) {
        this.orbit = orbit;
        this.lift = lift;
        this.growth = growth;
        this.spin = spin;
    }

    public static ParticleMotion forId(String id) {
        return switch (id) {
            case "pollution_mote", "grayfog_veil", "shadow_flicker" -> MIST;
            case "ritual_rune", "page_glyph", "door_spark" -> RUNE;
            case "divination_thread", "marionette_thread", "card_trace" -> THREAD;
            case "dream_bubble" -> BUBBLE;
            case "ember_burst" -> EMBER;
            case "mirror_ripple", "air_ring" -> RING;
            case "petal_rose_shadow" -> PETAL;
            default -> WISP;
        };
    }

    public Frame sample(float progress, float phase, boolean reducedMotion) {
        float time = Float.isFinite(progress) ? Math.max(0, Math.min(1, progress)) : 1;
        float seed = Float.isFinite(phase) ? phase : 0;
        float fadeIn = Math.min(1, time * 6);
        float remaining = 1 - time;
        float alpha = fadeIn * remaining * remaining;
        float angle = seed + time * (float) Math.PI * 2;
        return new Frame(alpha, Math.max(0.25f, 1 + growth * time),
                reducedMotion ? 0 : spin * time,
                reducedMotion ? 0 : (float) Math.cos(angle) * orbit,
                reducedMotion ? 0 : lift * time,
                reducedMotion ? 0 : (float) Math.sin(angle) * orbit);
    }

    public record Frame(float alpha, float scale, float roll, float offsetX, float offsetY, float offsetZ) {}
}
