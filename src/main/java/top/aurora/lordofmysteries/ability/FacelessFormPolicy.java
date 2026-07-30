package top.aurora.lordofmysteries.ability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.minecraftforge.registries.ForgeRegistries;

public final class FacelessFormPolicy {

    public static final int MAX_FORMS = 8;
    public static final int MAX_NAME_LENGTH = 48;

    private FacelessFormPolicy() {}

    public static boolean canRecord(LivingEntity target) {
        return target != null && !(target instanceof Player);
    }

    public static CompoundTag createRecord(LivingEntity target) {
        if (!canRecord(target)) {
            throw new IllegalArgumentException(
                    "player identity forms are not supported");
        }
        ResourceLocation entityType =
                ForgeRegistries.ENTITY_TYPES.getKey(target.getType());
        if (entityType == null) {
            throw new IllegalArgumentException("target entity type is unregistered");
        }
        CompoundTag record = new CompoundTag();
        record.putUUID("record_uuid", UUID.randomUUID());
        record.putString("entity_type", entityType.toString());
        record.putString("display_name", sanitizeName(
                target.getDisplayName().getString()));
        record.putBoolean("player_form", false);
        record.putFloat("width", target.getBbWidth());
        record.putFloat("height", target.getBbHeight());
        record.putFloat("max_health", target.getMaxHealth());
        return normalizeRecord(record);
    }

    public static List<CompoundTag> normalizeRecords(
            List<CompoundTag> records) {
        List<CompoundTag> normalized = new ArrayList<>();
        if (records == null) return normalized;
        Set<UUID> seen = new HashSet<>();
        for (CompoundTag record : records) {
            if (normalized.size() >= MAX_FORMS || !isValid(record)) continue;
            CompoundTag repaired = normalizeRecord(record);
            UUID recordId = repaired.getUUID("record_uuid");
            if (seen.add(recordId)) normalized.add(repaired);
        }
        return normalized;
    }

    public static Selection store(
            List<CompoundTag> records, CompoundTag record, int selectedIndex) {
        if (!isValid(record)) {
            throw new IllegalArgumentException("invalid faceless form record");
        }
        List<CompoundTag> normalized = normalizeRecords(records);
        CompoundTag stored = normalizeRecord(record);
        if (normalized.size() < MAX_FORMS) {
            normalized.add(stored);
            return new Selection(List.copyOf(normalized), normalized.size() - 1);
        }
        int replacement = normalizeSelection(normalized, selectedIndex);
        if (replacement < 0) replacement = 0;
        normalized.set(replacement, stored);
        return new Selection(List.copyOf(normalized), replacement);
    }

    public static int normalizeSelection(
            List<CompoundTag> records, int selectedIndex) {
        int size = records == null ? 0 : records.size();
        return selectedIndex >= 0 && selectedIndex < size
                ? selectedIndex : size == 0 ? -1 : 0;
    }

    public static boolean isValid(CompoundTag record) {
        if (record == null
                || !record.hasUUID("record_uuid")
                || !record.contains("entity_type")
                || !record.contains("display_name")) {
            return false;
        }
        ResourceLocation entityType =
                ResourceLocation.tryParse(record.getString("entity_type"));
        String displayName = sanitizeName(record.getString("display_name"));
        return entityType != null
                && !displayName.isBlank();
    }

    public static UUID recordId(CompoundTag record) {
        return isValid(record)
                ? record.getUUID("record_uuid") : new UUID(0L, 0L);
    }

    public static String displayName(CompoundTag record) {
        return isValid(record)
                ? sanitizeName(record.getString("display_name")) : "";
    }

    private static CompoundTag normalizeRecord(CompoundTag source) {
        CompoundTag record = source.copy();
        record.putString("display_name", sanitizeName(
                record.getString("display_name")));
        record.putFloat("width", finiteClamp(
                record.getFloat("width"), 0.1f, 4f, 0.6f));
        record.putFloat("height", finiteClamp(
                record.getFloat("height"), 0.1f, 6f, 1.8f));
        record.putFloat("max_health", finiteClamp(
                record.getFloat("max_health"), 1f, 2048f, 20f));
        return record;
    }

    private static float finiteClamp(
            float value, float minimum, float maximum, float fallback) {
        return Float.isFinite(value)
                ? Mth.clamp(value, minimum, maximum) : fallback;
    }

    private static String sanitizeName(String value) {
        if (value == null) return "";
        StringBuilder clean = new StringBuilder();
        value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .limit(MAX_NAME_LENGTH)
                .forEach(clean::appendCodePoint);
        return clean.toString().trim();
    }

    public record Selection(List<CompoundTag> records, int selectedIndex) {}
}
