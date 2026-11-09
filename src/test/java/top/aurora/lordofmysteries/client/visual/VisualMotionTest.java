package top.aurora.lordofmysteries.client.visual;

import org.junit.jupiter.api.Test;
import net.minecraft.world.entity.Mob;
import static org.junit.jupiter.api.Assertions.*;

class VisualMotionTest {
    @Test
    void everyParticleCurveHasBoundedOpacityScaleAndFiniteMotion() {
        for (var motion : ParticleMotion.values()) {
            for (int sample = -10; sample <= 110; sample++) {
                var frame = motion.sample(sample / 100f, 1.7f, false);
                assertTrue(frame.alpha() >= 0 && frame.alpha() <= 1, motion.name());
                assertTrue(frame.scale() >= 0.25f && frame.scale() <= 3.2f, motion.name());
                assertTrue(Float.isFinite(frame.roll() + frame.offsetX() + frame.offsetY() + frame.offsetZ()));
            }
            assertEquals(0, motion.sample(0, 0, false).alpha());
            assertEquals(0, motion.sample(1, 0, false).alpha());
            assertEquals(0, motion.sample(Float.NaN, Float.POSITIVE_INFINITY, false).alpha());
        }
    }

    @Test
    void reducedMotionRetainsReadableFadeButRemovesSpinAndOrbit() {
        for (var motion : ParticleMotion.values()) {
            var normal = motion.sample(0.3f, 1, false);
            var reduced = motion.sample(0.3f, 1, true);
            assertEquals(normal.alpha(), reduced.alpha());
            assertEquals(normal.scale(), reduced.scale());
            assertEquals(0, reduced.roll());
            assertEquals(0, reduced.offsetX());
            assertEquals(0, reduced.offsetY());
            assertEquals(0, reduced.offsetZ());
        }
    }

    @Test
    void semanticProfilesAreDistinctNotSixteenIdenticalMotes() {
        assertEquals(ParticleMotion.RING, ParticleMotion.forId("air_ring"));
        assertEquals(ParticleMotion.MIST, ParticleMotion.forId("grayfog_veil"));
        assertEquals(ParticleMotion.PETAL, ParticleMotion.forId("petal_rose_shadow"));
        assertTrue(ParticleMotion.RING.sample(0.5f, 0, false).scale() > 2);
        assertTrue(ParticleMotion.PETAL.sample(0.5f, 0, false).offsetY() < 0);
        assertTrue(ParticleMotion.BUBBLE.sample(0.5f, 0, false).offsetY() > 0);
    }

    @Test
    void everyCreatureResetsPoseAndRemainsFiniteForBadAnimationInputs() {
        for (var visual : CreatureVisual.values()) {
            var model = new OccultCreatureModel<Mob>(OccultCreatureModel.createLayer(visual).bakeRoot(), visual);
            model.applyPose(1, 0.8f, 10, 30, 20, 0.5f, true, false, false, false);
            assertTrue(model.root().xRot < 0, visual.name() + " has an attack lean");
            model.applyPose(0, 0, 0, 0, 0, 0, false, true, false, true);
            assertEquals(0, model.root().xRot, 0.000001, visual.name() + " does not retain an old attack");
            assertEquals(24, model.root().y, visual.name() + " does not accumulate bobbing");
            model.applyPose(Float.NaN, Float.POSITIVE_INFINITY, Float.NaN, Float.NaN, Float.NaN,
                    Float.NaN, true, false, true, false);
            model.root().getAllParts().forEach(part -> assertTrue(
                    Float.isFinite(part.xRot + part.yRot + part.zRot + part.x + part.y + part.z), visual.name()));
        }
    }

    @Test
    void reducedMotionNeverRemovesAttackTelegraphOrFlightWings() {
        var normal = CreatureAnimationLogic.sample(1, 0.4f, 20, 0.5f, true, false, false);
        var reduced = CreatureAnimationLogic.sample(1, 0.4f, 20, 0.5f, true, false, true);
        assertEquals(normal.strike(), reduced.strike());
        assertEquals(normal.lean(), reduced.lean());
        assertEquals(normal.flap(), reduced.flap());
        assertEquals(0, reduced.breathing());
    }

    @Test
    void nightOwlAndShadeHaveDistinctGeometryRatherThanOnlyRecoloredBodies() {
        var owl = OccultCreatureModel.createLayer(CreatureVisual.NIGHT_OWL).bakeRoot().getChild("root");
        assertTrue(owl.hasChild("face_disc") && owl.hasChild("ear_left"));
        assertTrue(owl.getChild("left_wing").hasChild("feather_2"));
        var shade = OccultCreatureModel.createLayer(CreatureVisual.SLEEPLESS_SHADE).bakeRoot().getChild("root");
        assertTrue(shade.hasChild("moon_left") && shade.getChild("head").hasChild("hood"));
    }
}
