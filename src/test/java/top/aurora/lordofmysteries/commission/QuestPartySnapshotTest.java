package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        source.escortedReporterUuid = "reporter";
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
        assertEquals("reporter", target.escortedReporterUuid);
        assertEquals("stealth", target.questResolutionRoute);
        assertTrue(target.questResolutionReady);

        net.minecraft.nbt.CompoundTag malformed = snapshot.save();
        malformed.putInt("quest_step", -5);
        malformed.putInt("objective_progress", -2);
        malformed.putString("resolution_route", "invalid");
        malformed.putBoolean("resolution_ready", true);
        QuestPartySnapshot sanitized = QuestPartySnapshot.load(malformed);
        assertEquals(0, sanitized.questStep());
        PlayerMysteryData sanitizedTarget = new PlayerMysteryData();
        assertTrue(sanitized.applyTo(sanitizedTarget, FIRST));
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
