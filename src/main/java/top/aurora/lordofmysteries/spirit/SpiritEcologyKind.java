package top.aurora.lordofmysteries.spirit;

import net.minecraft.world.entity.EntityType;

import top.aurora.lordofmysteries.registry.ModEntities;

public enum SpiritEcologyKind {
    LANTERN_MOTH_SWARM("lantern_moth_swarm"),
    MEMORY_LEECH("memory_leech"),
    PRAYER_ECHO("prayer_echo"),
    SPIRIT_FERRYMAN("spirit_ferryman"),
    COLOR_EATER("color_eater"),
    DOOR_WISP("door_wisp"),
    WHISPER_CROW("whisper_crow"),
    COMPASS_BIRD("compass_bird"),
    ARCHIVE_SPIDER("archive_spider"),
    GASLIGHT_SPECTER("gaslight_specter"),
    THEATRE_MASKLING("theatre_maskling"),
    GRAVE_LANTERN("grave_lantern");

    private final String id;

    SpiritEcologyKind(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public SpiritEncounter encounter() {
        return SpiritEncounter.fromId(id);
    }

    public String translationKey() {
        return "entity.lord_of_mysteries." + id;
    }

    public EntityType<SpiritEcologyEntity> entityType() {
        return switch (this) {
            case LANTERN_MOTH_SWARM -> ModEntities.LANTERN_MOTH_SWARM.get();
            case MEMORY_LEECH -> ModEntities.MEMORY_LEECH.get();
            case PRAYER_ECHO -> ModEntities.PRAYER_ECHO.get();
            case SPIRIT_FERRYMAN -> ModEntities.SPIRIT_FERRYMAN.get();
            case COLOR_EATER -> ModEntities.COLOR_EATER.get();
            case DOOR_WISP -> ModEntities.DOOR_WISP.get();
            case WHISPER_CROW -> ModEntities.WHISPER_CROW.get();
            case COMPASS_BIRD -> ModEntities.COMPASS_BIRD.get();
            case ARCHIVE_SPIDER -> ModEntities.ARCHIVE_SPIDER.get();
            case GASLIGHT_SPECTER -> ModEntities.GASLIGHT_SPECTER.get();
            case THEATRE_MASKLING -> ModEntities.THEATRE_MASKLING.get();
            case GRAVE_LANTERN -> ModEntities.GRAVE_LANTERN.get();
        };
    }

    public static SpiritEcologyKind fromId(String id) {
        if (id == null) return null;
        for (SpiritEcologyKind kind : values()) {
            if (kind.id.equalsIgnoreCase(id)) return kind;
        }
        return null;
    }
}
