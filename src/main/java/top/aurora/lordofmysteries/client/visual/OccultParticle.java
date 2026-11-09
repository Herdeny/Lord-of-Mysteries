package top.aurora.lordofmysteries.client.visual;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class OccultParticle extends TextureSheetParticle {
    private static final VisualBudget BUDGET = new VisualBudget();
    private static ClientLevel budgetLevel;
    private final ParticleMotion motion;
    private final float baseSize;
    private final float phase;
    private final boolean reducedMotion;

    private OccultParticle(ClientLevel level, double x, double y, double z,
                           double speedX, double speedY, double speedZ,
                           SpriteSet sprites, int color, int duration, float size, ParticleMotion motion) {
        super(level, x, y, z);
        this.motion = motion;
        baseSize = size;
        phase = level.random.nextFloat() * (float) Math.PI * 2;
        reducedMotion = VisualClientConfig.REDUCED_MOTION.get();
        xd = speedX;
        yd = speedY;
        zd = speedZ;
        friction = 0.91f;
        hasPhysics = false;
        lifetime = duration;
        quadSize = size;
        alpha = 0;
        setColor(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f);
        pickSprite(sprites);
    }

    @Override
    public void tick() {
        var before = motion.sample((float) age / lifetime, phase, reducedMotion);
        super.tick();
        var after = motion.sample((float) age / lifetime, phase, reducedMotion);
        alpha = after.alpha();
        quadSize = baseSize * after.scale();
        oRoll = roll;
        roll = after.roll();
        move(after.offsetX() - before.offsetX(), after.offsetY() - before.offsetY(),
                after.offsetZ() - before.offsetZ());
    }

    @Override
    protected int getLightColor(float partialTick) {
        return VisualClientConfig.EMISSIVE_DETAILS.get() ? 0xF000F0 : super.getLightColor(partialTick);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static void clearBudget() {
        budgetLevel = null;
        BUDGET.reset();
    }

    public record Provider(SpriteSet sprites, int color, int duration, float size)
            implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double speedX, double speedY, double speedZ) {
            if (budgetLevel != level) {
                budgetLevel = level;
                BUDGET.reset();
            }
            if (!BUDGET.allow(level.getGameTime(), VisualClientConfig.PARTICLE_DENSITY.get(), duration)) return null;
            return new OccultParticle(level, x, y, z, speedX, speedY, speedZ,
                    sprites, color, duration, size, ParticleMotion.forId(
                            net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE.getKey(type).getPath()));
        }
    }
}
