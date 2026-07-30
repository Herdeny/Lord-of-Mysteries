package top.aurora.lordofmysteries.organization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class OrganizationActionSavedData extends SavedData {

    private static final String DATA_NAME =
            "lord_of_mysteries_organization_actions";
    private long currentDay = Long.MIN_VALUE;
    private List<OrganizationActionPolicy.PlannedAction> actions = List.of();
    private final Map<UUID, Assignment> assignments = new HashMap<>();
    private final Set<String> completed = new HashSet<>();
    private final ListTag orphanedEntries = new ListTag();

    public static OrganizationActionSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                OrganizationActionSavedData::load,
                OrganizationActionSavedData::new,
                DATA_NAME);
    }

    public static OrganizationActionSavedData load(CompoundTag tag) {
        OrganizationActionSavedData data =
                new OrganizationActionSavedData();
        data.currentDay = tag.contains("current_day", Tag.TAG_LONG)
                ? tag.getLong("current_day") : Long.MIN_VALUE;
        List<OrganizationActionPolicy.PlannedAction> loadedActions =
                new ArrayList<>();
        ListTag actionsTag = tag.getList("actions", Tag.TAG_COMPOUND);
        for (Tag raw : actionsTag) {
            CompoundTag entry = (CompoundTag) raw;
            try {
                ResourceLocation organization = ResourceLocation.tryParse(
                        entry.getString("organization"));
                if (organization == null) {
                    throw new IllegalArgumentException(
                            "invalid organization id");
                }
                loadedActions.add(
                        new OrganizationActionPolicy.PlannedAction(
                                entry.getInt("slot"),
                                organization,
                                OrganizationActionType.fromId(
                                        entry.getString("type")),
                                entry.getInt("risk")));
            } catch (RuntimeException exception) {
                data.orphanedEntries.add(entry.copy());
            }
        }
        data.actions = List.copyOf(loadedActions);
        ListTag assignmentsTag =
                tag.getList("assignments", Tag.TAG_COMPOUND);
        for (Tag raw : assignmentsTag) {
            CompoundTag entry = (CompoundTag) raw;
            try {
                UUID player = entry.getUUID("player");
                Assignment assignment = new Assignment(
                        entry.getLong("day"),
                        entry.getInt("slot"),
                        entry.getInt("progress"),
                        entry.getLong("claimed_at"));
                data.assignments.put(player, assignment);
            } catch (RuntimeException exception) {
                data.orphanedEntries.add(entry.copy());
            }
        }
        ListTag completedTag = tag.getList("completed", Tag.TAG_STRING);
        completedTag.forEach(raw -> data.completed.add(raw.getAsString()));
        ListTag savedOrphans =
                tag.getList("orphaned_entries", Tag.TAG_COMPOUND);
        savedOrphans.forEach(raw -> data.orphanedEntries.add(raw.copy()));
        return data;
    }

    public boolean refresh(
            long worldSeed, long day, float exposure,
            Map<ResourceLocation, OrganizationDefinition> definitions) {
        if (day == currentDay) return false;
        actions = OrganizationActionPolicy.generate(
                worldSeed, day, exposure, definitions);
        currentDay = day;
        assignments.entrySet().removeIf(
                entry -> entry.getValue().day() != day);
        completed.removeIf(key -> !key.startsWith(day + ":"));
        setDirty();
        return true;
    }

    public boolean assign(UUID player, int slot, long gameTime) {
        if (player == null || action(slot) == null
                || assignments.containsKey(player)
                || completed.contains(completionKey(
                        currentDay, player, slot))) {
            return false;
        }
        assignments.put(
                player,
                new Assignment(currentDay, slot, 0, gameTime));
        setDirty();
        return true;
    }

    public boolean abandon(UUID player) {
        if (assignments.remove(player) == null) return false;
        setDirty();
        return true;
    }

    public int addProgress(UUID player, int amount) {
        Assignment assignment = assignments.get(player);
        if (assignment == null || amount <= 0) return 0;
        int progress = Math.min(
                99, assignment.progress() + amount);
        assignments.put(
                player,
                new Assignment(
                        assignment.day(), assignment.slot(),
                        progress, assignment.claimedAt()));
        setDirty();
        return progress;
    }

    public boolean complete(UUID player) {
        Assignment assignment = assignments.remove(player);
        if (assignment == null || assignment.day() != currentDay) {
            return false;
        }
        completed.add(completionKey(
                currentDay, player, assignment.slot()));
        setDirty();
        return true;
    }

    public OrganizationActionPolicy.PlannedAction action(int slot) {
        return actions.stream()
                .filter(action -> action.slot() == slot)
                .findFirst()
                .orElse(null);
    }

    public OrganizationActionPolicy.PlannedAction assignedAction(
            UUID player) {
        Assignment assignment = assignments.get(player);
        return assignment == null ? null : action(assignment.slot());
    }

    public Assignment assignment(UUID player) {
        return assignments.get(player);
    }

    public List<OrganizationActionPolicy.PlannedAction> actions() {
        return actions;
    }

    public long currentDay() {
        return currentDay;
    }

    public int orphanedCount() {
        return orphanedEntries.size();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("current_day", currentDay);
        ListTag actionsTag = new ListTag();
        for (OrganizationActionPolicy.PlannedAction action : actions) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("slot", action.slot());
            entry.putString(
                    "organization", action.organization().toString());
            entry.putString("type", action.type().id());
            entry.putInt("risk", action.risk());
            actionsTag.add(entry);
        }
        tag.put("actions", actionsTag);
        ListTag assignmentsTag = new ListTag();
        assignments.forEach((player, assignment) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("player", player);
            entry.putLong("day", assignment.day());
            entry.putInt("slot", assignment.slot());
            entry.putInt("progress", assignment.progress());
            entry.putLong("claimed_at", assignment.claimedAt());
            assignmentsTag.add(entry);
        });
        tag.put("assignments", assignmentsTag);
        ListTag completedTag = new ListTag();
        completed.stream().sorted().forEach(value ->
                completedTag.add(net.minecraft.nbt.StringTag.valueOf(value)));
        tag.put("completed", completedTag);
        tag.put("orphaned_entries", orphanedEntries.copy());
        return tag;
    }

    private static String completionKey(
            long day, UUID player, int slot) {
        return day + ":" + player + ":" + slot;
    }

    public record Assignment(
            long day, int slot, int progress, long claimedAt) {

        public Assignment {
            if (day < 0L || slot < 1 || progress < 0
                    || claimedAt < 0L) {
                throw new IllegalArgumentException(
                        "invalid organization action assignment");
            }
        }
    }
}
