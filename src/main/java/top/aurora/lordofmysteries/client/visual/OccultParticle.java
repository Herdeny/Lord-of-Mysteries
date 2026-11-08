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

    private OccultParticle(ClientLevel level, double x, double y, double z,
                           double speedX, double speedY, double speedZ,
                           SpriteSet sprites, int color, int duration, float size) {
        super(level, x, y, z);
        xd = speedX;
        yd = speedY;
        zd = speedZ;
        friction = 0.91f;
        hasPhysics = false;
        lifetime = duration;
        quadSize = size;
        setColor(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f);
        pickSprite(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        alpha = Math.max(0, 1f - (float) age / lifetime);
    }

    @Override
    protected int getLightColor(float partialTick) {
        return 0xF000F0;
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
            if (!BUDGET.allow(level.getGameTime(), VisualClientConfig.PARTICLE_DENSITY.get())) return null;
            return new OccultParticle(level, x, y, z, speedX, speedY, speedZ,
                    sprites, color, duration, size);
        }
    }
}
