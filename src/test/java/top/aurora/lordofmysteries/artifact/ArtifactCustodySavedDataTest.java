package top.aurora.lordofmysteries.artifact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class ArtifactCustodySavedDataTest {

    private static final ResourceLocation OVERWORLD =
            ResourceLocation.parse("minecraft:overworld");
    private static final BlockPos VAULT = new BlockPos(4, 70, 4);

    @Test
    void enforcesUniqueIssueLeakStabilizeAndReturnLifecycle() {
        ArtifactCustodySavedData data =
                new ArtifactCustodySavedData();
        UUID responsible = UUID.randomUUID();
        UUID instance = data.issue(
                definition(), responsible, 3L, 100L,
                OVERWORLD, VAULT);

        assertNotNull(instance);
        assertNull(data.issue(
                definition(), UUID.randomUUID(), 3L, 101L,
                OVERWORLD, VAULT));
        assertEquals(ArtifactCustodyState.BORROWED,
                data.recordUse(instance, 2, 2, 99));
        assertEquals(ArtifactCustodyState.BORROWED,
                data.recordUse(instance, 2, 2, 99));
        assertEquals(ArtifactCustodyState.LEAKED,
                data.recordUse(instance, 2, 2, 99));
        assertFalse(data.stabilize(instance, UUID.randomUUID()));
        assertTrue(data.stabilize(instance, responsible));
        assertEquals(ArtifactCustodyState.RECOVERED,
                data.record(instance).state());
        assertTrue(data.returnToVault(instance, responsible));
        assertEquals(ArtifactCustodyState.RETURNED,
                data.record(instance).state());
        assertNull(data.activeForDefinition(definition().id()));
    }

    @Test
    void contaminationThresholdAndOfflineExpiryBothCauseLeaks() {
        ArtifactCustodySavedData contamination =
                new ArtifactCustodySavedData();
        UUID holder = UUID.randomUUID();
        UUID first = contamination.issue(
                definition(), holder, 0L, 1L, OVERWORLD, VAULT);
        assertEquals(ArtifactCustodyState.BORROWED,
                contamination.recordUse(first, 4, 64, 8));
        assertEquals(ArtifactCustodyState.LEAKED,
                contamination.recordUse(first, 4, 64, 8));

        ArtifactCustodySavedData overdue =
                new ArtifactCustodySavedData();
        UUID second = overdue.issue(
                definition(), holder, 4L, 10L, OVERWORLD, VAULT);
        assertTrue(overdue.markDropped(
                second, OVERWORLD, new BlockPos(20, 65, 20), 11L));
        assertEquals(0, overdue.expireOverdue(6L));
        assertEquals(1, overdue.expireOverdue(7L));
        assertEquals(ArtifactCustodyState.LEAKED,
                overdue.record(second).state());
    }

    @Test
    void holderChangeRecoversButSameTickDualHolderIsAbuse() {
        ArtifactCustodySavedData recovered =
                new ArtifactCustodySavedData();
        UUID responsible = UUID.randomUUID();
        UUID rescuer = UUID.randomUUID();
        UUID instance = recovered.issue(
                definition(), responsible, 1L, 20L,
                OVERWORLD, VAULT);
        assertEquals(
                ArtifactCustodySavedData.Observation.HOLDER_CHANGED,
                recovered.observe(
                        instance, definition().id(), rescuer,
                        OVERWORLD, new BlockPos(8, 70, 8),
                        1L, 21L));
        assertEquals(ArtifactCustodyState.RECOVERED,
                recovered.record(instance).state());
        assertTrue(recovered.returnToVault(instance, rescuer));

        ArtifactCustodySavedData duplicated =
                new ArtifactCustodySavedData();
        UUID duplicate = duplicated.issue(
                definition(), responsible, 1L, 30L,
                OVERWORLD, VAULT);
        assertEquals(ArtifactCustodySavedData.Observation.OK,
                duplicated.observe(
                        duplicate, definition().id(), responsible,
                        OVERWORLD, VAULT, 1L, 31L));
        assertEquals(ArtifactCustodySavedData.Observation.DUPLICATE,
                duplicated.observe(
                        duplicate, definition().id(), rescuer,
                        OVERWORLD, VAULT, 1L, 31L));
        assertEquals(ArtifactCustodyState.ABUSED,
                duplicated.record(duplicate).state());
        assertFalse(duplicated.returnToVault(duplicate, responsible));
        assertTrue(duplicated.retireAbused(duplicate));
        assertEquals(ArtifactCustodyState.RETURNED,
                duplicated.record(duplicate).state());
    }

    @Test
    void ledgerRoundTripPreservesRecordsAndOrphansUnknownStates() {
        ArtifactCustodySavedData data =
                new ArtifactCustodySavedData();
        UUID holder = UUID.randomUUID();
        UUID instance = data.issue(
                definition(), holder, 2L, 50L, OVERWORLD, VAULT);
        data.recordUse(instance, 2, 5, 15);
        CompoundTag saved = data.save(new CompoundTag());

        ListTag records = saved.getList(
                "records", Tag.TAG_COMPOUND);
        CompoundTag malformed = records.getCompound(0).copy();
        malformed.putUUID("instance", UUID.randomUUID());
        malformed.putString("state", "unverified_state");
        records.add(malformed);

        ArtifactCustodySavedData restored =
                ArtifactCustodySavedData.load(saved);
        assertEquals(1, restored.records().size());
        assertEquals(1, restored.orphanedCount());
        assertEquals(1, restored.record(instance).uses());
    }

    private static SealedArtifactDefinition definition() {
        return SealedArtifactDefinition.parse(
                SealedArtifactDefinitionTest.validDefinition(),
                SealedArtifactDefinitionTest.id("fallback"));
    }
}
