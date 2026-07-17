package top.aurora.lordofmysteries.commission;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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
            if (validKey(key)
                    && ResourceLocation.tryParse(snapshot.commissionId()) != null
                    && ResourceLocation.tryParse(snapshot.questChainId()) != null
                    && !snapshot.isFinished()) {
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

    public boolean markSettled(UUID member, String commissionId,
                               String questChainId) {
        boolean changed = false;
        Iterator<Map.Entry<String, QuestPartySnapshot>> iterator =
                snapshots.entrySet().iterator();
        while (iterator.hasNext()) {
            QuestPartySnapshot snapshot = iterator.next().getValue();
            if (!snapshot.matches(commissionId, questChainId)
                    || !snapshot.markSettled(member)) continue;
            changed = true;
            if (snapshot.isFinished()) iterator.remove();
        }
        if (changed) setDirty();
        return changed;
    }

    public boolean removeMember(UUID member, String commissionId,
                                String questChainId) {
        boolean changed = false;
        Iterator<Map.Entry<String, QuestPartySnapshot>> iterator =
                snapshots.entrySet().iterator();
        while (iterator.hasNext()) {
            QuestPartySnapshot snapshot = iterator.next().getValue();
            if (!snapshot.matches(commissionId, questChainId)
                    || !snapshot.removeMember(member)) continue;
            changed = true;
            if (snapshot.isFinished()) iterator.remove();
        }
        if (changed) setDirty();
        return changed;
    }

    public boolean retainMembership(UUID member, String activePartyKey) {
        boolean changed = false;
        Iterator<Map.Entry<String, QuestPartySnapshot>> iterator =
                snapshots.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, QuestPartySnapshot> entry = iterator.next();
            if (entry.getKey().equals(activePartyKey)
                    || !entry.getValue().removeMember(member)) continue;
            changed = true;
            if (entry.getValue().isFinished()) iterator.remove();
        }
        if (changed) setDirty();
        return changed;
    }

    public int activePartyCount() {
        return snapshots.size();
    }

    public int activeMemberCount() {
        return snapshots.values().stream()
                .mapToInt(snapshot -> snapshot.members().size()).sum();
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

    private static boolean validKey(String key) {
        return key.startsWith("team:") && key.length() > "team:".length()
                && key.length() <= 64;
    }
}
