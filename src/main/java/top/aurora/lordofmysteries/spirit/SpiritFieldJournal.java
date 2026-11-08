package top.aurora.lordofmysteries.spirit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class SpiritFieldJournal extends SavedData {
    private final Map<UUID, Entry> entries = new HashMap<>();
    private final ListTag quarantined = new ListTag();
    private CompoundTag futureSnapshot;

    public static SpiritFieldJournal get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                SpiritFieldJournal::load, SpiritFieldJournal::new,
                "lord_of_mysteries_spirit_field_journal");
    }

    public static SpiritFieldJournal load(CompoundTag tag) {
        SpiritFieldJournal journal = new SpiritFieldJournal();
        if (tag.getInt("schema") > 1) {
            journal.futureSnapshot = tag.copy();
            return journal;
        }
        for (Tag raw : tag.getList("entries", Tag.TAG_COMPOUND)) {
            CompoundTag value = (CompoundTag) raw;
            if (!value.hasUUID("player")) {
                journal.quarantined.add(value.copy());
                continue;
            }
            UUID player = value.getUUID("player");
            Entry previous = journal.entry(player);
            journal.entries.put(player, new Entry(
                    previous.observed() | value.getInt("observed"),
                    previous.mastered() | value.getInt("mastered")));
        }
        tag.getList("quarantined", Tag.TAG_COMPOUND)
                .forEach(raw -> journal.quarantined.add(raw.copy()));
        return journal;
    }

    public Entry entry(UUID player) {
        return entries.getOrDefault(player, new Entry(0, 0));
    }

    public boolean record(UUID player, SpiritEncounter encounter, boolean mastered) {
        if (futureSnapshot != null || player == null || encounter == null) return false;
        Entry previous = entry(player);
        int flag = 1 << encounter.ordinal();
        Entry changed = new Entry(previous.observed() | flag,
                previous.mastered() | (mastered ? flag : 0));
        if (changed.equals(previous)) return false;
        entries.put(player, changed);
        setDirty();
        return true;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (futureSnapshot != null) return futureSnapshot.copy();
        tag.putInt("schema", 1);
        ListTag values = new ListTag();
        entries.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(value -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("player", value.getKey());
            entry.putInt("observed", value.getValue().observed());
            entry.putInt("mastered", value.getValue().mastered());
            values.add(entry);
        });
        tag.put("entries", values);
        tag.put("quarantined", quarantined.copy());
        return tag;
    }

    public record Entry(int observed, int mastered) {
        private static final int VALID_MASK = (1 << SpiritEncounter.values().length) - 1;

        public Entry {
            mastered &= VALID_MASK;
            observed = (observed | mastered) & VALID_MASK;
        }

        public boolean observed(SpiritEncounter kind) {
            return kind != null && (observed & (1 << kind.ordinal())) != 0;
        }

        public boolean mastered(SpiritEncounter kind) {
            return kind != null && (mastered & (1 << kind.ordinal())) != 0;
        }

        public int observedCount() { return Integer.bitCount(observed); }
        public int masteredCount() { return Integer.bitCount(mastered); }
    }
}
