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
    private static final long MAX_EXACT_REPLAY_DAYS =
            OrganizationStrategyPolicy.WEEK_LENGTH_DAYS * 2L;
    private long currentDay = Long.MIN_VALUE;
    private long currentWeek = Long.MIN_VALUE;
    private List<OrganizationActionPolicy.PlannedAction> actions = List.of();
    private final Map<ResourceLocation, OrganizationStrategyPolicy.Directive>
            strategies = new HashMap<>();
    private final Map<UUID, Assignment> assignments = new HashMap<>();
    private final Set<String> completed = new HashSet<>();
    private final Set<Integer> resolvedSlots = new HashSet<>();
    private int lastWeekSuccesses;
    private int lastWeekFailures;
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
        data.currentWeek = tag.contains("current_week", Tag.TAG_LONG)
                ? tag.getLong("current_week") : Long.MIN_VALUE;
        data.lastWeekSuccesses = Math.max(
                0, tag.getInt("last_week_successes"));
        data.lastWeekFailures = Math.max(
                0, tag.getInt("last_week_failures"));
        ListTag strategiesTag =
                tag.getList("strategies", Tag.TAG_COMPOUND);
        for (Tag raw : strategiesTag) {
            CompoundTag entry = (CompoundTag) raw;
            try {
                ResourceLocation organization = ResourceLocation.tryParse(
                        entry.getString("organization"));
                if (organization == null) {
                    throw new IllegalArgumentException(
                            "invalid strategy organization");
                }
                OrganizationStrategyPolicy.Directive directive =
                        new OrganizationStrategyPolicy.Directive(
                                organization,
                                OrganizationActionType.fromId(
                                        entry.getString("focus")),
                                entry.getInt("risk"),
                                entry.getInt("successes"),
                                entry.getInt("failures"));
                if (data.strategies.putIfAbsent(
                        organization, directive) != null) {
                    throw new IllegalArgumentException(
                            "duplicate strategy organization");
                }
            } catch (RuntimeException exception) {
                data.orphanedEntries.add(entry.copy());
            }
        }
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
                if (assignment.slot()
                        > OrganizationActionPolicy.DAILY_ACTION_COUNT
                        || data.actions.stream().noneMatch(action ->
                                action.slot() == assignment.slot())
                        || data.assignments.putIfAbsent(
                                player, assignment) != null) {
                    throw new IllegalArgumentException(
                            "invalid or duplicate organization assignment");
                }
            } catch (RuntimeException exception) {
                data.orphanedEntries.add(entry.copy());
            }
        }
        ListTag completedTag = tag.getList("completed", Tag.TAG_STRING);
        completedTag.forEach(raw -> data.completed.add(raw.getAsString()));
        for (int slot : tag.getIntArray("resolved_slots")) {
            if (slot >= 1 && slot <= OrganizationActionPolicy
                    .DAILY_ACTION_COUNT) {
                data.resolvedSlots.add(slot);
            }
        }
        ListTag savedOrphans =
                tag.getList("orphaned_entries", Tag.TAG_COMPOUND);
        savedOrphans.forEach(raw -> data.orphanedEntries.add(raw.copy()));
        return data;
    }

    public boolean refresh(
            long worldSeed, long day, float exposure,
            Map<ResourceLocation, OrganizationDefinition> definitions) {
        if (snapshotMatches(day, definitions)) return false;
        if (currentDay == Long.MIN_VALUE || strategies.isEmpty()
                || day <= currentDay) {
            prepareDay(worldSeed, day, exposure, definitions, false);
        } else {
            if (day - currentDay > MAX_EXACT_REPLAY_DAYS) {
                long targetWeekStart = Math.floorDiv(
                        day, OrganizationStrategyPolicy.WEEK_LENGTH_DAYS)
                        * OrganizationStrategyPolicy.WEEK_LENGTH_DAYS;
                long replayStart = Math.max(
                        0L, targetWeekStart
                                - OrganizationStrategyPolicy.WEEK_LENGTH_DAYS);
                prepareDay(worldSeed, replayStart, exposure,
                        definitions, false);
            }
            while (currentDay < day) {
                resolveAutonomousActions(worldSeed);
                prepareDay(
                        worldSeed, currentDay + 1L,
                        exposure, definitions, true);
            }
        }
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
        if (resolvedSlots.add(assignment.slot())) {
            OrganizationActionPolicy.PlannedAction action =
                    action(assignment.slot());
            if (action != null) recordOutcome(
                    action.organization(), true);
        }
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

    public long currentWeek() {
        return currentWeek;
    }

    public List<OrganizationStrategyPolicy.Directive> strategies() {
        return strategies.values().stream()
                .sorted(java.util.Comparator.comparing(
                        value -> value.organization().toString()))
                .toList();
    }

    public OrganizationStrategyPolicy.Directive strategy(
            ResourceLocation organization) {
        return strategies.get(organization);
    }

    public int lastWeekSuccesses() {
        return lastWeekSuccesses;
    }

    public int lastWeekFailures() {
        return lastWeekFailures;
    }

    public int orphanedCount() {
        return orphanedEntries.size();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("current_day", currentDay);
        tag.putLong("current_week", currentWeek);
        tag.putInt("last_week_successes", lastWeekSuccesses);
        tag.putInt("last_week_failures", lastWeekFailures);
        ListTag strategiesTag = new ListTag();
        for (OrganizationStrategyPolicy.Directive directive
                : strategies()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("organization",
                    directive.organization().toString());
            entry.putString("focus", directive.focus().id());
            entry.putInt("risk", directive.risk());
            entry.putInt("successes", directive.successes());
            entry.putInt("failures", directive.failures());
            strategiesTag.add(entry);
        }
        tag.put("strategies", strategiesTag);
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
        tag.putIntArray("resolved_slots", resolvedSlots.stream()
                .sorted()
                .mapToInt(Integer::intValue)
                .toArray());
        tag.put("orphaned_entries", orphanedEntries.copy());
        return tag;
    }

    private void resolveAutonomousActions(long worldSeed) {
        for (OrganizationActionPolicy.PlannedAction action : actions) {
            if (!resolvedSlots.add(action.slot())) continue;
            boolean success = OrganizationStrategyPolicy
                    .autonomousSuccess(
                            worldSeed, currentDay, action,
                            strategies.get(action.organization()));
            recordOutcome(action.organization(), success);
        }
    }

    private void prepareDay(
            long worldSeed, long day, float exposure,
            Map<ResourceLocation, OrganizationDefinition> definitions,
            boolean archiveCompletedWeek) {
        long week = Math.floorDiv(day,
                OrganizationStrategyPolicy.WEEK_LENGTH_DAYS);
        if (week != currentWeek || strategies.isEmpty()) {
            if (archiveCompletedWeek && !strategies.isEmpty()) {
                lastWeekSuccesses = outcomeTotal(true);
                lastWeekFailures = outcomeTotal(false);
            }
            strategies.clear();
            strategies.putAll(OrganizationStrategyPolicy.generate(
                    worldSeed, week, exposure, definitions));
            currentWeek = week;
        }
        actions = OrganizationActionPolicy.generate(
                worldSeed, day, exposure, definitions, strategies);
        currentDay = day;
        resolvedSlots.clear();
    }

    private void recordOutcome(
            ResourceLocation organization, boolean success) {
        OrganizationStrategyPolicy.Directive directive =
                strategies.get(organization);
        if (directive != null) {
            strategies.put(organization,
                    directive.withOutcome(success));
        }
    }

    private boolean snapshotMatches(
            long day,
            Map<ResourceLocation, OrganizationDefinition> definitions) {
        if (day != currentDay || definitions == null
                || definitions.isEmpty()
                || currentWeek != Math.floorDiv(
                        day, OrganizationStrategyPolicy.WEEK_LENGTH_DAYS)
                || !strategies.keySet().equals(definitions.keySet())
                || actions.size() != Math.min(
                        OrganizationActionPolicy.DAILY_ACTION_COUNT,
                        definitions.size())) {
            return false;
        }
        Set<Integer> slots = new HashSet<>();
        for (OrganizationActionPolicy.PlannedAction action : actions) {
            OrganizationDefinition definition = definitions.get(
                    action.organization());
            OrganizationStrategyPolicy.Directive directive =
                    strategies.get(action.organization());
            if (!slots.add(action.slot()) || definition == null
                    || directive == null
                    || !definition.strategyWeights().containsKey(
                            action.type())
                    || !definition.strategyWeights().containsKey(
                            directive.focus())) {
                return false;
            }
        }
        return true;
    }

    private int outcomeTotal(boolean successes) {
        long total = 0L;
        for (OrganizationStrategyPolicy.Directive directive
                : strategies.values()) {
            total += successes
                    ? directive.successes() : directive.failures();
            if (total >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
        }
        return (int) total;
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
