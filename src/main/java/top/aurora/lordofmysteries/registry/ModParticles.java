package top.aurora.lordofmysteries.registry;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.aurora.lordofmysteries.ProjectMystery;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ProjectMystery.MOD_ID);

    public static final RegistryObject<SimpleParticleType> SPIRIT_WISP = register("spirit_wisp");
    public static final RegistryObject<SimpleParticleType> POLLUTION_MOTE = register("pollution_mote");
    public static final RegistryObject<SimpleParticleType> RITUAL_RUNE = register("ritual_rune");
    public static final RegistryObject<SimpleParticleType> DIVINATION_THREAD = register("divination_thread");
    public static final RegistryObject<SimpleParticleType> DREAM_BUBBLE = register("dream_bubble");
    public static final RegistryObject<SimpleParticleType> EMBER_BURST = register("ember_burst");
    public static final RegistryObject<SimpleParticleType> SHADOW_FLICKER = register("shadow_flicker");
    public static final RegistryObject<SimpleParticleType> PAGE_GLYPH = register("page_glyph");
    public static final RegistryObject<SimpleParticleType> GRAYFOG_VEIL = register("grayfog_veil");
    public static final RegistryObject<SimpleParticleType> CHARACTERISTIC_GLOW = register("characteristic_glow");
    public static final RegistryObject<SimpleParticleType> MIRROR_RIPPLE = register("mirror_ripple");
    public static final RegistryObject<SimpleParticleType> PETAL_ROSE_SHADOW = register("petal_rose_shadow");
    public static final RegistryObject<SimpleParticleType> CARD_TRACE = register("card_trace");
    public static final RegistryObject<SimpleParticleType> AIR_RING = register("air_ring");
    public static final RegistryObject<SimpleParticleType> MARIONETTE_THREAD = register("marionette_thread");
    public static final RegistryObject<SimpleParticleType> DOOR_SPARK = register("door_spark");

    private ModParticles() {}

    private static RegistryObject<SimpleParticleType> register(String id) {
        return PARTICLES.register(id, () -> new SimpleParticleType(false));
    }
}
