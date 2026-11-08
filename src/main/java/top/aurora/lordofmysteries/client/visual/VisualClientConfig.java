package top.aurora.lordofmysteries.client.visual;

import net.minecraftforge.common.ForgeConfigSpec;

public final class VisualClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.EnumValue<VisualBudget.Density> PARTICLE_DENSITY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        PARTICLE_DENSITY = builder.defineEnum("particleDensity", VisualBudget.Density.STANDARD);
        SPEC = builder.build();
    }

    private VisualClientConfig() {}
}
