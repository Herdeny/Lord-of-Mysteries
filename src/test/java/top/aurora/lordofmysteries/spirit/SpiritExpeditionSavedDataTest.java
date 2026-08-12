package top.aurora.lordofmysteries.spirit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

class SpiritExpeditionSavedDataTest {

    private static final ResourceLocation OVERWORLD =
            ResourceLocation.parse("minecraft:overworld");

    @Test
    void concurrentPlayersReceiveIsolatedPersistentRoutes() {
        SpiritExpeditionSavedData data = new SpiritExpeditionSavedData();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        SpiritExpeditionSavedData.Expedition firstRoute = data.start(
                first, OVERWORLD, new BlockPos(4, 70, 4),
                SpiritProjection.CHURCH, 111L, 20L);
        SpiritExpeditionSavedData.Expedition secondRoute = data.start(
                second, OVERWORLD, new BlockPos(-8, 64, 12),
                SpiritProjection.HARBOR, 222L, 20L);

        assertNotNull(firstRoute);
        assertNotNull(secondRoute);
        assertNotEquals(firstRoute.lane(), secondRoute.lane());
        assertNotEquals(firstRoute.currentPad(), secondRoute.currentPad());
        assertNull(data.start(
                first, OVERWORLD, BlockPos.ZERO,
                SpiritProjection.MANOR, 333L, 30L));

        SpiritDirection expected = SpiritExpeditionPolicy.expectedDirection(
                firstRoute.routeSeed(), firstRoute.step(), firstRoute.tide(),
                firstRoute.projection(), firstRoute.weather());
        SpiritExpeditionPolicy.NavigationOutcome navigation =
                SpiritExpeditionPolicy.navigate(
                        firstRoute.routeSeed(), firstRoute.step(),
                        firstRoute.tide(), firstRoute.projection(),
                        firstRoute.weather(), expected);
        BlockPos destination = SpiritExpeditionWorldBuilder.nextPad(
                firstRoute, expected);
        data.update(first, current -> current.afterNavigation(
                navigation, destination, 40L));

        assertEquals(1, data.get(first).step());
        assertEquals(0, data.get(second).step());
        assertEquals(secondRoute, data.get(second));

        SpiritDirection secondExpected =
                SpiritExpeditionPolicy.expectedDirection(
                        secondRoute.routeSeed(), secondRoute.step(),
                        secondRoute.tide(), secondRoute.projection(),
                        secondRoute.weather());
        SpiritDirection wrong = SpiritDirection.values()[
                (secondExpected.ordinal() + 1)
                        % SpiritDirection.values().length];
        SpiritExpeditionPolicy.NavigationOutcome wrongNavigation =
                SpiritExpeditionPolicy.navigate(
                        secondRoute.routeSeed(), secondRoute.step(),
                        secondRoute.tide(), secondRoute.projection(),
                        secondRoute.weather(), wrong);
        data.update(second, current -> current.afterNavigation(
                wrongNavigation,
                SpiritExpeditionWorldBuilder.nextPad(current, wrong), 41L));
        assertEquals(0, data.get(second).step());
        assertNotNull(data.get(second).pendingEncounter());
        assertTrue(data.get(second).drift() > secondRoute.drift());

        SpiritExpeditionSavedData restored = SpiritExpeditionSavedData.load(
                data.save(new CompoundTag()));
        assertEquals(2, restored.size());
        assertEquals(data.get(first), restored.get(first));
        assertEquals(data.get(second), restored.get(second));
    }

    @Test
    void navigationEncounterAndStabilizationPreserveBounds() {
        SpiritExpeditionSavedData data = new SpiritExpeditionSavedData();
        UUID player = UUID.randomUUID();
        SpiritExpeditionSavedData.Expedition route = data.start(
                player, OVERWORLD, BlockPos.ZERO,
                SpiritProjection.THEATRE, 987654321L, 10L);
        assertNotNull(route);

        for (int leg = 0;
             leg < SpiritExpeditionPolicy.ROUTE_LEGS; leg++) {
            long navigationTime = 20L + leg * 2L;
            long encounterTime = navigationTime + 1L;
            SpiritExpeditionSavedData.Expedition current = data.get(player);
            SpiritDirection expected =
                    SpiritExpeditionPolicy.expectedDirection(
                            current.routeSeed(), current.step(), current.tide(),
                            current.projection(), current.weather());
            SpiritExpeditionPolicy.NavigationOutcome navigation =
                    SpiritExpeditionPolicy.navigate(
                            current.routeSeed(), current.step(), current.tide(),
                            current.projection(), current.weather(), expected);
            data.update(player, value -> value.afterNavigation(
                    navigation,
                    SpiritExpeditionWorldBuilder.nextPad(value, expected),
                    navigationTime));
            SpiritExpeditionSavedData.Expedition pending = data.get(player);
            assertNotNull(pending.pendingEncounter());
            SpiritExpeditionPolicy.EncounterOutcome encounter =
                    SpiritExpeditionPolicy.resolve(
                            pending.pendingEncounter(),
                            pending.pendingEncounter().preferredAction());
            data.update(player, value -> value.afterEncounter(
                    encounter, encounterTime));
        }

        SpiritExpeditionSavedData.Expedition complete = data.get(player);
        assertTrue(complete.extractionReady());
        assertFalse(complete.failed());
        assertEquals(SpiritExpeditionPolicy.ROUTE_LEGS,
                complete.resolvedEncounters());
        assertTrue(complete.rewardScore()
                >= SpiritExpeditionPolicy.EXTRACTION_REWARD_THRESHOLD);
        data.update(player, value -> value.stabilized(100L));
        assertTrue(data.get(player).stability()
                <= SpiritExpeditionPolicy.MAX_STABILITY);
        assertTrue(data.get(player).drift() >= 0);
        assertNotNull(data.remove(player));
        assertNull(data.get(player));
    }

    @Test
    void malformedAndDuplicateEntriesAreQuarantined() {
        SpiritExpeditionSavedData data = new SpiritExpeditionSavedData();
        UUID player = UUID.randomUUID();
        data.start(player, OVERWORLD, BlockPos.ZERO,
                SpiritProjection.CEMETERY, 44L, 0L);
        CompoundTag saved = data.save(new CompoundTag());
        ListTag expeditions = saved.getList(
                "expeditions", Tag.TAG_COMPOUND);
        expeditions.add(expeditions.getCompound(0).copy());
        CompoundTag malformed = expeditions.getCompound(0).copy();
        malformed.putUUID("player", UUID.randomUUID());
        malformed.putString("weather", "not_a_weather");
        expeditions.add(malformed);

        SpiritExpeditionSavedData restored =
                SpiritExpeditionSavedData.load(saved);
        assertEquals(1, restored.size());
        assertEquals(2, restored.orphanedCount());
        assertNotNull(restored.get(player));

        CompoundTag future = saved.copy();
        future.putInt("schema",
                SpiritExpeditionSavedData.SCHEMA_VERSION + 1);
        future.putString("future_payload", "preserve-me");
        SpiritExpeditionSavedData futureData =
                SpiritExpeditionSavedData.load(future);
        assertTrue(futureData.isReadOnlyFutureSchema());
        assertNull(futureData.start(
                UUID.randomUUID(), OVERWORLD, BlockPos.ZERO,
                SpiritProjection.MANOR, 1L, 1L));
        CompoundTag futureRoundTrip = futureData.save(new CompoundTag());
        assertEquals(SpiritExpeditionSavedData.SCHEMA_VERSION + 1,
                futureRoundTrip.getInt("schema"));
        assertEquals("preserve-me",
                futureRoundTrip.getString("future_payload"));
    }
}
