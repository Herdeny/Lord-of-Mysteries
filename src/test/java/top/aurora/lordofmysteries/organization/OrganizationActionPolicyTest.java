package top.aurora.lordofmysteries.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

class OrganizationActionPolicyTest {

    @Test
    void dailyActionsAreDeterministicDistinctAndExposureSensitive() {
        Map<ResourceLocation, OrganizationDefinition> definitions =
                definitions(5);
        var calm = OrganizationActionPolicy.generate(
                7788L, 12L, 10f, definitions);
        var repeated = OrganizationActionPolicy.generate(
                7788L, 12L, 10f, definitions);
        var exposed = OrganizationActionPolicy.generate(
                7788L, 12L, 75f, definitions);

        assertEquals(calm, repeated);
        assertEquals(OrganizationActionPolicy.DAILY_ACTION_COUNT,
                calm.size());
        Set<ResourceLocation> selected = calm.stream()
                .map(OrganizationActionPolicy.PlannedAction::organization)
                .collect(Collectors.toSet());
        assertEquals(calm.size(), selected.size());
        for (int index = 0; index < calm.size(); index++) {
            assertEquals(calm.get(index).type(),
                    exposed.get(index).type());
            assertEquals(Math.min(5, calm.get(index).risk() + 2),
                    exposed.get(index).risk());
        }
    }

    @Test
    void weeklyStrategiesCoverEveryOrganizationAndGuideDailyActions() {
        Map<ResourceLocation, OrganizationDefinition> definitions =
                definitions(6);
        var first = OrganizationStrategyPolicy.generate(
                9988L, 3L, 75f, definitions);
        var repeated = OrganizationStrategyPolicy.generate(
                9988L, 3L, 75f, definitions);

        assertEquals(first, repeated);
        assertEquals(definitions.keySet(), first.keySet());
        assertTrue(first.values().stream().allMatch(
                directive -> directive.risk() >= 3));
        OrganizationStrategyPolicy.Directive saturated =
                new OrganizationStrategyPolicy.Directive(
                        first.values().iterator().next().organization(),
                        first.values().iterator().next().focus(),
                        5, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE,
                saturated.withOutcome(true).successes());
        assertEquals(Integer.MAX_VALUE,
                saturated.withOutcome(false).failures());

        var actions = OrganizationActionPolicy.generate(
                9988L, 22L, 75f, definitions, first);
        assertTrue(actions.stream().allMatch(action -> {
            OrganizationStrategyPolicy.Directive directive =
                    first.get(action.organization());
            return directive != null
                    && (!action.type().equals(directive.focus())
                    || action.risk() >= directive.risk());
        }));
    }

    @Test
    void weeklyRolloverResolvesUnattendedActionsAndPersistsSummary() {
        OrganizationActionSavedData data =
                new OrganizationActionSavedData();
        Map<ResourceLocation, OrganizationDefinition> definitions =
                definitions(4);
        UUID player = UUID.randomUUID();

        assertTrue(data.refresh(5511L, 6L, 45f, definitions));
        assertEquals(0L, data.currentWeek());
        assertEquals(4, data.strategies().size());
        assertTrue(data.assign(player, 1, 10L));
        assertTrue(data.complete(player));

        assertTrue(data.refresh(5511L, 7L, 45f, definitions));
        assertEquals(1L, data.currentWeek());
        assertEquals(OrganizationActionPolicy.DAILY_ACTION_COUNT,
                data.lastWeekSuccesses() + data.lastWeekFailures());

        CompoundTag saved = data.save(new CompoundTag());
        saved.putLong("current_week", 99L);
        OrganizationActionSavedData restored =
                OrganizationActionSavedData.load(saved);
        assertTrue(restored.refresh(5511L, 7L, 45f, definitions));
        assertEquals(data.currentWeek(), restored.currentWeek());
        assertEquals(data.strategies(), restored.strategies());
        assertEquals(data.lastWeekSuccesses(),
                restored.lastWeekSuccesses());
        assertEquals(data.lastWeekFailures(),
                restored.lastWeekFailures());
    }

    @Test
    void skippedDaysResolveEveryActionAndExpireAllAssignments() {
        OrganizationActionSavedData data =
                new OrganizationActionSavedData();
        Map<ResourceLocation, OrganizationDefinition> definitions =
                definitions(5);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        assertTrue(data.refresh(8822L, 0L, 45f, definitions));
        assertTrue(data.assign(first, 1, 10L));
        assertTrue(data.assign(second, 2, 11L));
        assertTrue(data.complete(first));
        assertTrue(data.refresh(8822L, 8L, 45f, definitions));

        assertEquals(1L, data.currentWeek());
        assertEquals(21,
                data.lastWeekSuccesses() + data.lastWeekFailures());
        assertEquals(3, data.strategies().stream()
                .mapToInt(value ->
                        value.successes() + value.failures())
                .sum());
        assertNull(data.assignment(first));
        assertNull(data.assignment(second));

        assertTrue(data.refresh(
                8822L, 1_000_008L, 45f, definitions));
        assertEquals(1_000_008L, data.currentDay());
        assertEquals(142_858L, data.currentWeek());
        assertEquals(21,
                data.lastWeekSuccesses() + data.lastWeekFailures());
        assertEquals(6, data.strategies().stream()
                .mapToInt(value ->
                        value.successes() + value.failures())
                .sum());
    }

    @Test
    void backwardClockResetRegeneratesWithoutCountingFutureActions() {
        OrganizationActionSavedData data =
                new OrganizationActionSavedData();
        Map<ResourceLocation, OrganizationDefinition> definitions =
                definitions(4);

        assertTrue(data.refresh(9911L, 15L, 10f, definitions));
        assertTrue(data.refresh(9911L, 2L, 10f, definitions));

        assertEquals(2L, data.currentDay());
        assertEquals(0L, data.currentWeek());
        assertEquals(0, data.strategies().stream()
                .mapToInt(value ->
                        value.successes() + value.failures())
                .sum());
    }

    @Test
    void savedAssignmentsRoundTripAndCannotBeOverwritten() {
        OrganizationActionSavedData data =
                new OrganizationActionSavedData();
        UUID player = UUID.randomUUID();
        assertTrue(data.refresh(44L, 3L, 0f, definitions(4)));
        assertEquals(3, data.actions().size());
        assertTrue(data.assign(player, 1, 120L));
        assertFalse(data.assign(player, 2, 121L));
        assertEquals(2, data.addProgress(player, 2));

        OrganizationActionSavedData restored =
                OrganizationActionSavedData.load(
                        data.save(new CompoundTag()));
        assertEquals(3L, restored.currentDay());
        assertEquals(1, restored.assignment(player).slot());
        assertEquals(2, restored.assignment(player).progress());
        assertTrue(restored.complete(player));
        assertNull(restored.assignment(player));
        assertFalse(restored.assign(player, 1, 150L));
    }

    @Test
    void dailyRefreshExpiresAssignmentsAndMalformedEntriesAreOrphaned() {
        OrganizationActionSavedData data =
                new OrganizationActionSavedData();
        UUID player = UUID.randomUUID();
        data.refresh(1L, 8L, 0f, definitions(3));
        assertTrue(data.assign(player, 1, 10L));
        assertTrue(data.refresh(1L, 9L, 0f, definitions(3)));
        assertNull(data.assignment(player));

        CompoundTag malformedSave = new CompoundTag();
        ListTag actions = new ListTag();
        CompoundTag malformed = new CompoundTag();
        malformed.putInt("slot", 1);
        malformed.putString(
                "organization",
                "lord_of_mysteries:organization/test");
        malformed.putString("type", "unknown_action");
        malformed.putInt("risk", 2);
        actions.add(malformed);
        malformedSave.put("actions", actions);
        UUID lockedPlayer = UUID.randomUUID();
        ListTag assignments = new ListTag();
        CompoundTag invalidAssignment = new CompoundTag();
        invalidAssignment.putUUID("player", lockedPlayer);
        invalidAssignment.putLong("day", 9L);
        invalidAssignment.putInt("slot", 1);
        invalidAssignment.putInt("progress", 0);
        invalidAssignment.putLong("claimed_at", 10L);
        assignments.add(invalidAssignment);
        malformedSave.put("assignments", assignments);
        OrganizationActionSavedData restored =
                OrganizationActionSavedData.load(malformedSave);
        assertEquals(0, restored.actions().size());
        assertNull(restored.assignment(lockedPlayer));
        assertEquals(2, restored.orphanedCount());
    }

    private static Map<ResourceLocation, OrganizationDefinition>
            definitions(int count) {
        Map<ResourceLocation, OrganizationDefinition> definitions =
                new LinkedHashMap<>();
        for (int index = 0; index < count; index++) {
            JsonObject json =
                    OrganizationDefinitionTest.validDefinition();
            json.addProperty(
                    "id",
                    "lord_of_mysteries:organization/test_" + index);
            OrganizationDefinition definition =
                    OrganizationDefinition.parse(
                            json,
                            OrganizationDefinitionTest.id(
                                    "fallback_" + index));
            definitions.put(definition.id(), definition);
        }
        return definitions;
    }
}
