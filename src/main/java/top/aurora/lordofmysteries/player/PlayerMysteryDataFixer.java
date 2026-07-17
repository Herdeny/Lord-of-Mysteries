package top.aurora.lordofmysteries.player;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.characteristic.CharacteristicBundle;
import top.aurora.lordofmysteries.characteristic.CharacteristicLedger;

public final class PlayerMysteryDataFixer {

    private static final int LEGACY_BASELINE_SCHEMA = 15;
    private static final List<DataFix> FIXES = List.of(
            new DataFix("legacy_key_normalization", LEGACY_BASELINE_SCHEMA,
                    PlayerMysteryDataFixer::normalizeLegacyKeys),
            new DataFix("characteristic_bundle_upgrade", 16,
                    PlayerMysteryDataFixer::upgradeCharacteristicBundles));

    private PlayerMysteryDataFixer() {}

    public static MigrationResult migrate(CompoundTag source) {
        if (source == null) throw new IllegalArgumentException("source is required");
        int sourceSchema = readSchema(source);
        CompoundTag working = source.copy();
        List<MigrationStep> applied = new ArrayList<>();
        List<CompoundTag> orphanedEntries = new ArrayList<>();

        if (sourceSchema > PlayerMysteryData.CURRENT_SCHEMA_VERSION) {
            orphanedEntries.add(orphan("future_schema", "unsupported_schema",
                    source.copy()));
            return new MigrationResult(working, sourceSchema, List.of(),
                    orphanedEntries, null, true);
        }

        int currentSchema = sourceSchema;
        for (DataFix fix : FIXES) {
            if (currentSchema >= fix.targetSchema()) continue;
            int previousSchema = currentSchema;
            fix.operation().apply(working, orphanedEntries);
            currentSchema = fix.targetSchema();
            working.putInt("schema_version", currentSchema);
            applied.add(new MigrationStep(fix.id(), previousSchema, currentSchema));
        }

        CompoundTag backup = applied.isEmpty() ? null
                : migrationBackup(source, sourceSchema, currentSchema);
        return new MigrationResult(working, sourceSchema, List.copyOf(applied),
                List.copyOf(orphanedEntries), backup, false);
    }

    private static int readSchema(CompoundTag tag) {
        if (!tag.contains("schema_version", Tag.TAG_INT)) return 0;
        return Math.max(0, tag.getInt("schema_version"));
    }

    private static void normalizeLegacyKeys(CompoundTag tag,
                                            List<CompoundTag> orphanedEntries) {
        copyAlias(tag, "knownKnowledge", "known_knowledge");
        copyAlias(tag, "actingHistory", "acting_history");
        copyAlias(tag, "actingCounters", "acting_counters");
        copyAlias(tag, "orgReputation", "org_reputation");
        if (tag.contains("Characteristics")
                && !tag.contains("characteristic_bundles")) {
            Tag legacy = tag.get("Characteristics");
            if (legacy != null) {
                orphanedEntries.add(orphan("characteristics",
                        "legacy_shape_requires_manual_recovery", legacy.copy()));
            }
        }
    }

    private static void copyAlias(CompoundTag tag, String legacyKey,
                                  String currentKey) {
        if (tag.contains(currentKey) || !tag.contains(legacyKey)) return;
        Tag legacy = tag.get(legacyKey);
        if (legacy != null) tag.put(currentKey, legacy.copy());
    }

    private static void upgradeCharacteristicBundles(
            CompoundTag tag, List<CompoundTag> orphanedEntries) {
        Tag rawBundles = tag.get("characteristic_bundles");
        boolean validContainer = rawBundles == null
                || rawBundles instanceof ListTag list
                && (list.isEmpty() || list.getElementType() == Tag.TAG_COMPOUND);
        boolean hasUsableBundles = rawBundles instanceof ListTag list
                && !list.isEmpty() && list.getElementType() == Tag.TAG_COMPOUND;
        if (!validContainer) {
            orphanedEntries.add(orphan("characteristic_bundles",
                    "invalid_legacy_payload", rawBundles.copy()));
        }

        if (!hasUsableBundles) {
            ResourceLocation pathway = tag.contains("pathway", Tag.TAG_STRING)
                    ? ResourceLocation.tryParse(tag.getString("pathway")) : null;
            int sequence = tag.contains("sequence", Tag.TAG_INT)
                    ? tag.getInt("sequence") : -1;
            String quality = tag.contains("potion_quality", Tag.TAG_STRING)
                    ? tag.getString("potion_quality") : "complete";
            ListTag migratedBundles = new ListTag();
            for (CharacteristicBundle bundle : CharacteristicLedger.migrateLegacy(
                    pathway, sequence, quality)) {
                migratedBundles.add(bundle.save());
            }
            tag.put("characteristic_bundles", migratedBundles);
        }

        if (!tag.contains("principle_insight", Tag.TAG_FLOAT)) {
            tag.putFloat("principle_insight", 0f);
        }
        if (!tag.contains("role_overidentification", Tag.TAG_FLOAT)) {
            tag.putFloat("role_overidentification", 0f);
        }
        if (!tag.contains("acting_reflection_count", Tag.TAG_INT)) {
            tag.putInt("acting_reflection_count", 0);
        }
        if (!tag.contains("last_acting_reflection_day", Tag.TAG_LONG)) {
            tag.putLong("last_acting_reflection_day", Long.MIN_VALUE);
        }
    }

    private static CompoundTag migrationBackup(CompoundTag source,
                                               int sourceSchema,
                                               int targetSchema) {
        CompoundTag payload = source.copy();
        payload.remove("migration_backups");
        CompoundTag backup = new CompoundTag();
        backup.putInt("source_schema", sourceSchema);
        backup.putInt("target_schema", targetSchema);
        backup.putString("reason", "schema_migration");
        backup.put("payload", payload);
        return backup;
    }

    public static CompoundTag orphan(String section, String reason, Tag payload) {
        CompoundTag entry = new CompoundTag();
        entry.putString("section", section);
        entry.putString("reason", reason);
        entry.put("payload", payload.copy());
        return entry;
    }

    private record DataFix(String id, int targetSchema, FixOperation operation) {}

    @FunctionalInterface
    private interface FixOperation {
        void apply(CompoundTag tag, List<CompoundTag> orphanedEntries);
    }

    public record MigrationStep(String id, int sourceSchema, int targetSchema) {
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("fix_id", id);
            tag.putInt("source_schema", sourceSchema);
            tag.putInt("target_schema", targetSchema);
            return tag;
        }
    }

    public record MigrationResult(
            CompoundTag data,
            int sourceSchema,
            List<MigrationStep> appliedSteps,
            List<CompoundTag> orphanedEntries,
            CompoundTag backup,
            boolean futureSchema) {

        public boolean migrated() {
            return !appliedSteps.isEmpty();
        }
    }
}
