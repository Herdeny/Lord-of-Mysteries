package top.aurora.lordofmysteries.dream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

import org.junit.jupiter.api.Test;

class SharedDreamSavedDataTest {

    private static final UUID HOST = UUID.fromString(
            "20000000-0000-0000-0000-000000000001");
    private static final UUID GUEST = UUID.fromString(
            "20000000-0000-0000-0000-000000000002");
    private static final ResourceLocation ORGANIZATION = id(
            "organization/secret_mind_alchemy");
    private static final ResourceLocation OVERWORLD = ResourceLocation.withDefaultNamespace(
            "overworld");

    @Test
    void explicitConsentIsRequiredBeforeActivation() {
        SharedDreamSavedData data = new SharedDreamSavedData();
        var lobby = data.createLobby(
                HOST, GUEST, ORGANIZATION, "team", 91L, 20L);
        assertNotNull(lobby);
        assertFalse(lobby.allAccepted());
        assertNull(data.activate(HOST, origins(), 30L));
        assertNotNull(data.accept(GUEST, 25L));
        var active = data.activate(HOST, origins(), 30L);
        assertNotNull(active);
        assertEquals(SharedDreamSavedData.DreamState.ACTIVE, active.state());
        assertNotNull(active.pendingSymbol());
        assertNull(data.createLobby(
                HOST, UUID.randomUUID(), ORGANIZATION, "team", 1L, 40L));
    }

    @Test
    void everyParticipantVotesOnceAndFourSymbolsComplete() {
        SharedDreamSavedData data = activeData();
        for (int step = 0; step < SharedDreamPolicy.SYMBOL_STEPS; step++) {
            var session = data.forPlayer(HOST);
            DreamAction action = session.pendingSymbol().preferredAction();
            var waiting = data.vote(HOST, action, 40L + step * 2L);
            assertNotNull(waiting);
            assertFalse(waiting.resolved());
            assertNull(data.vote(HOST, action, 41L + step * 2L));
            var resolved = data.vote(GUEST, action, 41L + step * 2L);
            assertNotNull(resolved);
            assertTrue(resolved.resolved());
            assertTrue(resolved.outcome().correct());
        }
        var complete = data.forPlayer(HOST);
        assertEquals(SharedDreamSavedData.DreamState.COMPLETE, complete.state());
        assertEquals(SharedDreamPolicy.SYMBOL_STEPS, complete.step());
        assertTrue(complete.clueScore() > 0);
    }

    @Test
    void activeVotesAndOriginsSurviveNbtRoundTrip() {
        SharedDreamSavedData data = activeData();
        var before = data.forPlayer(HOST);
        data.vote(HOST, before.pendingSymbol().preferredAction(), 50L);
        CompoundTag saved = data.save(new CompoundTag());
        SharedDreamSavedData restored = SharedDreamSavedData.load(saved);
        assertEquals(data.forPlayer(HOST), restored.forPlayer(HOST));
        assertEquals(data.forPlayer(GUEST), restored.forPlayer(GUEST));
        assertEquals(1, restored.forPlayer(HOST).votes().size());
        assertEquals(0, restored.orphanedCount());
    }

    @Test
    void offlineRecoveryIsPersistentAndCannotBeClaimedTwice() {
        SharedDreamSavedData data = activeData();
        var session = data.forPlayer(HOST);
        data.close(session.id());
        assertTrue(data.queueRecovery(
                GUEST, session.id(), origins().get(GUEST),
                DreamCloseReason.DISCONNECTED, 7,
                ORGANIZATION, false, 100L));
        assertFalse(data.queueRecovery(
                GUEST, session.id(), origins().get(GUEST),
                DreamCloseReason.DISCONNECTED, 7,
                ORGANIZATION, false, 100L));
        SharedDreamSavedData restored = SharedDreamSavedData.load(
                data.save(new CompoundTag()));
        assertNotNull(restored.recovery(GUEST));
        assertNotNull(restored.clearRecovery(GUEST));
        assertNull(restored.clearRecovery(GUEST));
    }

    @Test
    void malformedSessionsAreQuarantinedWithoutPartialMembership() {
        CompoundTag root = new CompoundTag();
        root.putInt("schema", SharedDreamSavedData.SCHEMA_VERSION);
        ListTag sessions = new ListTag();
        CompoundTag malformed = new CompoundTag();
        malformed.putUUID("id", UUID.randomUUID());
        malformed.putUUID("host", HOST);
        malformed.putString("organization", "not an id");
        sessions.add(malformed);
        root.put("sessions", sessions);
        SharedDreamSavedData loaded = SharedDreamSavedData.load(root);
        assertEquals(0, loaded.size());
        assertEquals(1, loaded.orphanedCount());
        assertNull(loaded.forPlayer(HOST));
        assertNotNull(loaded.createLobby(
                HOST, GUEST, ORGANIZATION, "", 9L, 10L));
    }

    @Test
    void futureSchemaIsPreservedReadOnly() {
        CompoundTag future = new CompoundTag();
        future.putInt("schema", SharedDreamSavedData.SCHEMA_VERSION + 10);
        future.putString("future_payload", "keep-me");
        SharedDreamSavedData loaded = SharedDreamSavedData.load(future);
        assertTrue(loaded.isReadOnlyFutureSchema());
        assertNull(loaded.createLobby(
                HOST, GUEST, ORGANIZATION, "", 9L, 10L));
        assertEquals(future, loaded.save(new CompoundTag()));
    }

    private static SharedDreamSavedData activeData() {
        SharedDreamSavedData data = new SharedDreamSavedData();
        assertNotNull(data.createLobby(
                HOST, GUEST, ORGANIZATION, "team", 91L, 20L));
        assertNotNull(data.accept(GUEST, 25L));
        assertNotNull(data.activate(HOST, origins(), 30L));
        return data;
    }

    private static Map<UUID, SharedDreamSavedData.Origin> origins() {
        return Map.of(
                HOST, new SharedDreamSavedData.Origin(
                        OVERWORLD, new BlockPos(1, 70, 1)),
                GUEST, new SharedDreamSavedData.Origin(
                        OVERWORLD, new BlockPos(2, 70, 2)));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(
                "lord_of_mysteries", path);
    }
}
