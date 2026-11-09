package top.aurora.lordofmysteries.client.visual;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import top.aurora.lordofmysteries.ProjectMystery;

public enum CreatureVisual {
    NIGHT_OWL(Shape.BIRD, 8, 4, 0xBDBDFF),
    SLEEPLESS_SHADE(Shape.HUMANOID, 0, 8, 0xBDBDFF),
    SEER_BREAKDOWN(Shape.HUMANOID, 0, 4, 0xBA9BFF),
    THIEF_BREAKDOWN(Shape.HUMANOID, 0, 2, 0x72DDCD),
    APPRENTICE_BREAKDOWN(Shape.HUMANOID, 1, 3, 0x72E8FF),
    PSYCHIATRIST_BREAKDOWN(Shape.HUMANOID, 0, 4, 0xFFDD8B),
    PYROMANIAC_BREAKDOWN(Shape.HUMANOID, 2, 13, 0xFF984D),
    ASHEN_PUPPET(Shape.HUMANOID, 9, 12, 0xE7CCAE),
    SCRIBE_GOLEM(Shape.HUMANOID, 1, 3, 0x9CDFFF),
    WAR_ROSE_HUSK(Shape.HUMANOID, 6, 13, 0xFF647D),
    SHAPESHIFTER_SERPENT(Shape.SERPENT, 5, 4, 0x9EF7CD),
    TWIN_SERPENT(Shape.SERPENT, 5, 6, 0xF1CB71),
    ABILITY_LEECH(Shape.LEECH, 5, 2, 0x8BE7DD),
    MEMORY_LEECH(Shape.LEECH, 14, 4, 0xC7A7FF),
    RIFTLING(Shape.LEECH, 10, 14, 0xA9F4FF),
    SPIRIT_WISP(Shape.WISP, 11, 15, 0xB4FFF1),
    PRAYER_ECHO(Shape.WISP, 15, 3, 0xFFE7AF),
    COLOR_EATER(Shape.WISP, 14, 2, 0xDFA9FF),
    DOOR_WISP(Shape.WISP, 10, 3, 0x91ECFF),
    GASLIGHT_SPECTER(Shape.WISP, 11, 2, 0x83F0BD),
    CRADLE_MOTH(Shape.MOTH, 7, 0, 0xE0C6FF),
    LANTERN_MOTH_SWARM(Shape.MOTH, 7, 3, 0xFFD58B),
    WHISPER_CROW(Shape.BIRD, 8, 2, 0xD4B4FF),
    COMPASS_BIRD(Shape.BIRD, 8, 3, 0xA4ECFF),
    ARCHIVE_SPIDER(Shape.SPIDER, 1, 2, 0xF5DD96),
    MARIONETTE_VINE(Shape.VINE, 6, 9, 0xCFAEFF),
    SPIRIT_FERRYMAN(Shape.HUMANOID, 0, 9, 0x84F4DD),
    THEATRE_MASKLING(Shape.MASK, 4, 6, 0xE6A5FF),
    GRAVE_LANTERN(Shape.LANTERN, 2, 11, 0x9BF1CB);

    public final Shape shape;
    public final int surface;
    public final int accent;
    public final int glow;
    public final ModelLayerLocation layer;

    CreatureVisual(Shape shape, int surface, int accent, int glow) {
        this.shape = shape;
        this.surface = surface;
        this.accent = accent;
        this.glow = glow;
        this.layer = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, name().toLowerCase(java.util.Locale.ROOT)), "main");
    }

    public enum Shape {
        HUMANOID(32), SERPENT(8), LEECH(8), WISP(23), MOTH(20),
        BIRD(20), SPIDER(12), VINE(12), MASK(24), LANTERN(24);

        public final float height;

        Shape(float height) {
            this.height = height;
        }
    }
}
