package top.aurora.lordofmysteries.client.visual;

import net.minecraftforge.common.ForgeConfigSpec;

public final class VisualClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.EnumValue<VisualBudget.Density> PARTICLE_DENSITY;

    public static final ForgeConfigSpec.BooleanValue REDUCED_MOTION;
    public static final ForgeConfigSpec.BooleanValue EMISSIVE_DETAILS;
    public static final ForgeConfigSpec.BooleanValue AMBIENT_PARTICLES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        PARTICLE_DENSITY = builder.defineEnum("particleDensity", VisualBudget.Density.STANDARD);
        REDUCED_MOTION = builder.define("reducedMotion", false);
        EMISSIVE_DETAILS = builder.define("emissiveDetails", true);
        AMBIENT_PARTICLES = builder.define("ambientParticles", true);
        SPEC = builder.build();
    }

    private VisualClientConfig() {}
}
