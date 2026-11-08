package top.aurora.lordofmysteries.spirit;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpiritEncounterBindingTest {
    private SpiritExpeditionSavedData pending(UUID player) {
        var data = new SpiritExpeditionSavedData();
        var route = data.start(player, ResourceLocation.parse("minecraft:overworld"),
                BlockPos.ZERO, SpiritProjection.THEATRE, 1234L, 0L);
        var direction = SpiritExpeditionPolicy.expectedDirection(route.routeSeed(), route.step(),
                route.tide(), route.projection(), route.weather());
        data.update(player, value -> value.afterNavigation(SpiritExpeditionPolicy.navigate(
                value.routeSeed(), value.step(), value.tide(), value.projection(), value.weather(), direction),
                value.currentPad(), 1L));
        return data;
    }

    @Test
    void onlyOneCanonicalEntitySurvivesBindingReplacementAndRestart() {
        UUID player = UUID.randomUUID();
        UUID oldEntity = UUID.randomUUID();
        UUID newEntity = UUID.randomUUID();
        var data = pending(player);
        assertTrue(data.bindEncounterEntity(player, oldEntity));
        assertTrue(data.bindEncounterEntity(player, newEntity));
        assertFalse(data.isEncounterEntity(player, oldEntity));
        assertFalse(data.isEncounterEntity(UUID.randomUUID(), newEntity));
        var restored = SpiritExpeditionSavedData.load(data.save(new CompoundTag()));
        assertTrue(restored.isEncounterEntity(player, newEntity));
        assertFalse(restored.isEncounterEntity(player, oldEntity));
    }

    @Test
    void resolutionRevokesTheEntityButStabilizationDoesNot() {
        UUID player = UUID.randomUUID();
        UUID entity = UUID.randomUUID();
        var data = pending(player);
        data.bindEncounterEntity(player, entity);
        data.update(player, value -> value.stabilized(3));
        assertTrue(data.isEncounterEntity(player, entity));
        data.update(player, value -> value.afterEncounter(SpiritExpeditionPolicy.resolve(
                value.pendingEncounter(), value.pendingEncounter().preferredAction()), 4));
        assertFalse(data.isEncounterEntity(player, entity));
        assertFalse(data.bindEncounterEntity(player, entity));
    }

    @Test
    void missingLegacyBindingsAndRemovedRoutesFailClosed() {
        UUID player = UUID.randomUUID();
        UUID entity = UUID.randomUUID();
        var data = pending(player);
        assertFalse(data.isEncounterEntity(player, entity));
        assertFalse(data.bindEncounterEntity(player, null));
        data.bindEncounterEntity(player, entity);
        data.remove(player);
        assertFalse(data.isEncounterEntity(player, entity));
        assertFalse(data.bindEncounterEntity(player, entity));
    }
}
