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
        OrganizationActionSavedData restored =
                OrganizationActionSavedData.load(malformedSave);
        assertEquals(0, restored.actions().size());
        assertEquals(1, restored.orphanedCount());
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
