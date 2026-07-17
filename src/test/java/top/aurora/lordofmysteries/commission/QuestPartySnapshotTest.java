package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

class QuestPartySnapshotTest {

    private static final UUID FIRST = UUID.fromString(
            "00000000-0000-0000-0000-000000000001");
    private static final UUID SECOND = UUID.fromString(
            "00000000-0000-0000-0000-000000000002");

    @Test
    void nbtRoundTripRestoresOfflineMemberProgress() {
        PlayerMysteryData source = activeData(3, 2);
        source.escortedReporterUuid = SECOND.toString();
        source.questResolutionRoute = "stealth";
        source.questResolutionReady = true;
        QuestPartySnapshot snapshot = QuestPartySnapshot.create(source, FIRST, 400L);

        QuestPartySnapshot restored = QuestPartySnapshot.load(snapshot.save());
        PlayerMysteryData target = new PlayerMysteryData();

        assertTrue(restored.applyTo(target, FIRST));
        assertEquals(source.activeCommissionId, target.activeCommissionId);
        assertEquals(source.activeQuestChainId, target.activeQuestChainId);
        assertEquals(3, target.activeQuestStep);
        assertEquals(2, target.questObjectiveProgress);
        assertEquals(SECOND.toString(), target.escortedReporterUuid);
        assertEquals("stealth", target.questResolutionRoute);
        assertTrue(target.questResolutionReady);

        net.minecraft.nbt.CompoundTag malformed = snapshot.save();
        malformed.putInt("quest_step", -5);
        malformed.putInt("objective_progress", -2);
        malformed.putString("reporter_uuid", "not-a-uuid");
        malformed.putString("resolution_route", "invalid");
        malformed.putBoolean("resolution_ready", true);
        QuestPartySnapshot sanitized = QuestPartySnapshot.load(malformed);
        assertEquals(0, sanitized.questStep());
        PlayerMysteryData sanitizedTarget = new PlayerMysteryData();
        assertTrue(sanitized.applyTo(sanitizedTarget, FIRST));
        assertEquals("", sanitizedTarget.escortedReporterUuid);
        assertEquals("", sanitizedTarget.questResolutionRoute);
        assertFalse(sanitizedTarget.questResolutionReady);
    }

    @Test
    void savedDataRoundTripKeepsTeamLedger() {
        QuestPartySavedData savedData = new QuestPartySavedData();
        savedData.put("team:detectives", QuestPartySnapshot.create(
                activeData(4, 1), FIRST, 500L));

        QuestPartySavedData restored = QuestPartySavedData.load(
                savedData.save(new net.minecraft.nbt.CompoundTag()));

        assertTrue(restored.snapshot("team:detectives").isPresent());
        assertEquals(4, restored.snapshot("team:detectives")
                .orElseThrow().questStep());
    }

    @Test
    void furthestProgressWinsDuringMerge() {
        QuestPartySnapshot snapshot = QuestPartySnapshot.create(
                activeData(2, 1), FIRST, 100L);
        PlayerMysteryData advanced = activeData(5, 0);

        assertTrue(snapshot.addMember(SECOND, 4));
        assertTrue(snapshot.mergeProgress(advanced, SECOND, 200L));
        PlayerMysteryData returning = activeData(2, 1);
        assertTrue(snapshot.applyTo(returning, FIRST));
        assertEquals(5, returning.activeQuestStep);
        assertEquals(0, returning.questObjectiveProgress);
        assertTrue(snapshot.members().contains(SECOND));
    }

    @Test
    void settledMemberIsNotReenrolled() {
        QuestPartySnapshot snapshot = QuestPartySnapshot.create(
                activeData(6, 0), FIRST, 100L);
        assertTrue(snapshot.markSettled(FIRST));

        PlayerMysteryData target = new PlayerMysteryData();
        assertFalse(snapshot.applyTo(target, FIRST));
        assertEquals("", target.activeCommissionId);
        assertTrue(snapshot.isFinished());
    }

    @Test
    void conflictingActiveCommissionIsNeverOverwritten() {
        QuestPartySnapshot snapshot = QuestPartySnapshot.create(
                activeData(4, 0), FIRST, 100L);
        PlayerMysteryData target = activeData(1, 0);
        target.activeCommissionId = "lord_of_mysteries:commission/lost_cat";
        target.activeQuestChainId = "lord_of_mysteries:quest/lost_cat";

        assertFalse(snapshot.applyTo(target, FIRST));
        assertEquals("lord_of_mysteries:commission/lost_cat",
                target.activeCommissionId);
    }

    @Test
    void staleMemberCannotRegressAuthoritativeLedger() {
        QuestPartySnapshot snapshot = QuestPartySnapshot.create(
                activeData(5, 0), FIRST, 200L);
        PlayerMysteryData stale = activeData(1, 0);

        assertFalse(snapshot.updateAuthoritative(stale, 300L));
        PlayerMysteryData returning = activeData(2, 0);
        assertTrue(snapshot.applyTo(returning, FIRST));
        assertEquals(5, returning.activeQuestStep);
    }

    @Test
    void membershipHonorsCapAndCanBeReleased() {
        QuestPartySnapshot snapshot = QuestPartySnapshot.create(
                activeData(1, 0), FIRST, 100L);
        assertTrue(snapshot.addMember(SECOND, 2));
        assertFalse(snapshot.addMember(UUID.randomUUID(), 2));
        assertTrue(snapshot.removeMember(SECOND));
        assertTrue(snapshot.addMember(UUID.randomUUID(), 2));
    }

    @Test
    void progressMergeRequiresRegisteredUnsettledMember() {
        QuestPartySnapshot snapshot = QuestPartySnapshot.create(
                activeData(1, 0), FIRST, 100L);
        PlayerMysteryData advanced = activeData(3, 0);

        assertFalse(snapshot.mergeProgress(advanced, SECOND, 200L));
        assertFalse(snapshot.members().contains(SECOND));
        assertTrue(snapshot.addMember(SECOND, 4));
        assertTrue(snapshot.markSettled(SECOND));
        assertFalse(snapshot.mergeProgress(advanced, SECOND, 300L));
        assertEquals(1, snapshot.questStep());
    }

    @Test
    void savedDataSettlementDoesNotDependOnCurrentTeamSize() {
        QuestPartySavedData savedData = new QuestPartySavedData();
        QuestPartySnapshot snapshot = QuestPartySnapshot.create(
                activeData(6, 0), FIRST, 100L);
        savedData.put("team:detectives", snapshot);

        assertTrue(savedData.markSettled(FIRST,
                snapshot.commissionId(), snapshot.questChainId()));
        assertEquals(0, savedData.activePartyCount());
        assertEquals(0, savedData.activeMemberCount());
    }

    @Test
    void changingTeamsPrunesOnlyTheDepartingMembership() {
        QuestPartySavedData savedData = new QuestPartySavedData();
        QuestPartySnapshot snapshot = QuestPartySnapshot.create(
                activeData(3, 0), FIRST, 100L);
        assertTrue(snapshot.addMember(SECOND, 4));
        savedData.put("team:detectives", snapshot);

        assertTrue(savedData.retainMembership(FIRST, "team:archive"));
        QuestPartySnapshot remaining = savedData.snapshot(
                "team:detectives").orElseThrow();
        assertFalse(remaining.hasMember(FIRST));
        assertTrue(remaining.hasMember(SECOND));
        assertTrue(savedData.retainMembership(SECOND, null));
        assertEquals(0, savedData.activePartyCount());
    }

    @Test
    void loadDropsInvalidKeysAndFinishedLedgers() {
        QuestPartySavedData source = new QuestPartySavedData();
        QuestPartySnapshot invalidKey = QuestPartySnapshot.create(
                activeData(2, 0), FIRST, 100L);
        QuestPartySnapshot finished = QuestPartySnapshot.create(
                activeData(6, 0), SECOND, 100L);
        assertTrue(finished.markSettled(SECOND));
        source.put("player:invalid", invalidKey);
        source.put("team:finished", finished);

        QuestPartySavedData restored = QuestPartySavedData.load(
                source.save(new net.minecraft.nbt.CompoundTag()));

        assertEquals(0, restored.activePartyCount());
    }

    @Test
    void definitionValidationRejectsOutOfRangeLedgerProgress() {
        QuestChainDefinition chain = new QuestChainDefinition(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        "lord_of_mysteries", "quest/counterfeit_formula_case"),
                "quest.test.title",
                List.of(new QuestChainDefinition.Step(
                        "step", "quest.test.step",
                        new QuestChainDefinition.Objective(
                                "encounter", "", 3))),
                "step_retry", true, 4);

        assertTrue(QuestPartySnapshot.create(
                activeData(0, 2), FIRST, 100L).validFor(chain));
        assertFalse(QuestPartySnapshot.create(
                activeData(0, 3), FIRST, 100L).validFor(chain));
        assertFalse(QuestPartySnapshot.create(
                activeData(2, 0), FIRST, 100L).validFor(chain));
    }

    private static PlayerMysteryData activeData(int step, int progress) {
        PlayerMysteryData data = new PlayerMysteryData();
        data.activeCommissionId =
                "lord_of_mysteries:commission/counterfeit_formula";
        data.activeQuestChainId =
                "lord_of_mysteries:quest/counterfeit_formula_case";
        data.activeQuestStep = step;
        data.questObjectiveProgress = progress;
        data.commissionAcceptedTick = 80L;
        return data;
    }
}
