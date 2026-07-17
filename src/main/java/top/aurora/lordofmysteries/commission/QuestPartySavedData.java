package top.aurora.lordofmysteries.commission;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class QuestPartySavedData extends SavedData {

    private static final String DATA_NAME = "lord_of_mysteries_quest_parties";
    private final Map<String, QuestPartySnapshot> snapshots = new LinkedHashMap<>();

    public static QuestPartySavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                QuestPartySavedData::load,
                QuestPartySavedData::new,
                DATA_NAME);
    }

    public static QuestPartySavedData load(CompoundTag tag) {
        QuestPartySavedData data = new QuestPartySavedData();
        ListTag list = tag.getList("parties", Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            CompoundTag entry = list.getCompound(index);
            String key = entry.getString("key");
            QuestPartySnapshot snapshot = QuestPartySnapshot.load(
                    entry.getCompound("snapshot"));
            if (!key.isBlank() && !snapshot.commissionId().isBlank()
                    && !snapshot.questChainId().isBlank()) {
                data.snapshots.put(key, snapshot);
            }
        }
        return data;
    }

    public Optional<QuestPartySnapshot> snapshot(String key) {
        return Optional.ofNullable(snapshots.get(key));
    }

    public void put(String key, QuestPartySnapshot snapshot) {
        snapshots.put(key, snapshot);
        setDirty();
    }

    public void remove(String key) {
        if (snapshots.remove(key) != null) setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        snapshots.forEach((key, snapshot) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("key", key);
            entry.put("snapshot", snapshot.save());
            list.add(entry);
        });
        tag.put("parties", list);
        return tag;
    }
}
