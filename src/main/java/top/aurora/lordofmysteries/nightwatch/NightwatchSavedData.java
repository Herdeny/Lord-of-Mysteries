package top.aurora.lordofmysteries.nightwatch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class NightwatchSavedData extends SavedData {
    private final Map<UUID, NightwatchProgress> players = new HashMap<>();
    private final ListTag quarantined = new ListTag();
    private CompoundTag futureSnapshot;

    public static NightwatchSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                NightwatchSavedData::load, NightwatchSavedData::new, "lord_of_mysteries_nightwatch");
    }

    public boolean writable() { return futureSnapshot == null; }

    public NightwatchProgress find(UUID owner) { return players.get(owner); }

    public NightwatchProgress getOrCreate(UUID owner) {
        if (!writable()) throw new IllegalStateException("Future nightwatch schema is read-only");
        return players.computeIfAbsent(owner, ignored -> new NightwatchProgress());
    }

    public static NightwatchSavedData load(CompoundTag root) {
        NightwatchSavedData data = new NightwatchSavedData();
        if (root.getInt("schema") > 1 || root.getInt("schema") < 0
                || malformedList(root, "players") || malformedList(root, "quarantined")
                || (root.contains("schema") && !root.contains("schema", Tag.TAG_INT))) {
            data.futureSnapshot = root.copy();
            return data;
        }
        for (Tag raw : root.getList("quarantined", Tag.TAG_COMPOUND)) data.quarantined.add(raw.copy());
        for (Tag raw : root.getList("players", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            if (!entry.hasUUID("owner") || data.players.containsKey(entry.getUUID("owner"))) {
                data.quarantined.add(entry.copy());
            } else {
                data.players.put(entry.getUUID("owner"), NightwatchProgress.load(entry));
            }
        }
        return data;
    }

    private static boolean malformedList(CompoundTag root, String key) {
        if (!root.contains(key)) return false;
        if (!(root.get(key) instanceof ListTag list)) return true;
        return !list.isEmpty() && list.getElementType() != Tag.TAG_COMPOUND;
    }

    @Override
    public CompoundTag save(CompoundTag root) {
        if (futureSnapshot != null) return futureSnapshot.copy();
        root.putInt("schema", 1);
        ListTag entries = new ListTag();
        players.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            CompoundTag tag = entry.getValue().save();
            tag.putUUID("owner", entry.getKey());
            entries.add(tag);
        });
        root.put("players", entries);
        root.put("quarantined", quarantined.copy());
        return root;
    }
}
