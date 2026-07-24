package top.aurora.lordofmysteries;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.commission.CaseDebriefRecord;
import top.aurora.lordofmysteries.commission.CaseGrade;
import top.aurora.lordofmysteries.commission.CaseHypothesisRecord;
import top.aurora.lordofmysteries.commission.CaseHypothesisStance;
import top.aurora.lordofmysteries.commission.DynamicCaseContactEvent;
import top.aurora.lordofmysteries.commission.DynamicCaseHistoryEntry;
import top.aurora.lordofmysteries.commission.DynamicCaseProfile;
import top.aurora.lordofmysteries.commission.DynamicCaseResponseTask;
import top.aurora.lordofmysteries.commission.DynamicCaseWeeklyDirective;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

/**
 * M0 基础单元测试框架（设计文档 §20 M0 任务：基础单元测试框架）。
 * 验证 PlayerMysteryData 默认值与普通人/非凡者判定逻辑。
 *
 * 注：test 源集默认不在 Minecraft 运行时类路径上（NeoForge ModDev），
 * 因此这里只覆盖不依赖 net.minecraft.* 的纯 POJO 逻辑。涉及 ResourceLocation /
 * Codec 反序列化 / 能力检定的测试放到 gameTestServer（§20 M0 任务）。
 *
 * <p>这些测试看起来很小，但它们保护的是玩家数据最底层的默认状态。默认值一旦变化，
 * 魔药服用、死亡复制、HUD 显示和服务端配置都可能出现连锁影响。
 */
class PlayerMysteryDataTest {

    /** 新建数据必须代表“普通人”，否则新玩家进服会直接被当作非凡者处理。 */
    @Test
    void defaultsAreCommoner() {
        PlayerMysteryData d = new PlayerMysteryData();
        assertNull(d.pathway, "默认应为普通人（pathway=null）");
        assertEquals(-1, d.sequence, "默认序列应为 -1");
        assertFalse(d.isExtraordinary(), "默认不应为非凡者");
        assertEquals(0f, d.spirituality);
        assertEquals(100f, d.spiritualityMax);
        assertEquals(0f, d.pollution);
        assertEquals(PlayerMysteryData.CURRENT_SCHEMA_VERSION, d.schemaVersion);
        assertFalse(d.emotionReadActive);
        assertEquals("", d.hunterTrackedTarget);
        assertEquals(0L, d.provokeCooldownEndTick);
        assertEquals(0L, d.enrageCooldownEndTick);
        assertEquals(0L, d.battleWillCooldownEndTick);
        assertEquals(0L, d.paperSubstituteArmedEndTick);
        assertEquals("", d.paperSubstituteDimension);
        assertEquals(0, d.clownDodgeCount);
        assertEquals(Long.MIN_VALUE, d.lastRestRecoveryDay);
        assertFalse(d.m1TrialActive);
        assertEquals(-1L, d.m1TrialStartTick);
        assertEquals(-1, d.m1TrialBestSequence);
        assertEquals(0L, d.m1TrialElapsedTicks);
        assertEquals(-1L, d.m1TrialCampReachedTick);
        assertEquals(-1L, d.m1TrialSequence9Tick);
        assertEquals(-1L, d.m1TrialSequence8Tick);
        assertEquals(-1L, d.m1TrialSequence7Tick);
        assertEquals(-1L, d.m1TrialIdentityAnchoredTick);
        assertEquals(-1L, d.m1TrialReflectionCompletedTick);
        assertEquals(-1L, d.m1TrialStreetLifeCompletedTick);
        assertTrue(d.actingCounters.isEmpty());
        assertEquals(0f, d.principleInsight);
        assertEquals(0f, d.roleOveridentification);
        assertEquals(0, d.actingReflectionCount);
        assertEquals(Long.MIN_VALUE, d.lastActingReflectionDay);
        assertFalse(d.identityAnchored);
        assertTrue(d.characteristicBundles.isEmpty());
        assertTrue(d.orphanedEntries.isEmpty());
        assertTrue(d.migrationBackups.isEmpty());
        assertTrue(d.migrationHistory.isEmpty());
        assertFalse(d.futureSchemaDetected);
        assertEquals(0L, d.moneyPence);
        assertEquals(Long.MIN_VALUE, d.lastCityWorkDay);
        assertEquals(0, d.cityWorkShifts);
        assertEquals("", d.activeCommissionId);
        assertEquals(-1, d.activeQuestStep);
        assertEquals("", d.escortedReporterUuid);
        assertFalse(d.questDefenseWaveSpawned);
        assertEquals(0L, d.questDefenseNextTick);
        assertEquals("", d.questResolutionRoute);
        assertFalse(d.questResolutionReady);
        assertTrue(d.completedCommissions.isEmpty());
        assertTrue(d.commissionCooldowns.isEmpty());
        assertTrue(d.caseDebriefs.isEmpty());
        assertTrue(d.caseHypotheses.isEmpty());
        assertTrue(d.dynamicCaseHistory.isEmpty());
        assertTrue(d.dynamicCaseContactEvents.isEmpty());
        assertTrue(d.dynamicCaseContactStandings.isEmpty());
        assertNull(d.organizationResponseTask);
    }

    /** 只有序列值不足以成为非凡者，必须同时拥有途径 ID。 */
    @Test
    void notExtraordinaryWithoutPathwayEvenIfSequenceSet() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.sequence = 9; // 仅有序列、无途径 → 仍非非凡者
        assertFalse(d.isExtraordinary(), "无途径时不应判定为非凡者");
    }

    /** 负数序列代表未入途径，防止边界值误判。 */
    @Test
    void sequenceBoundaryStaysCommonerWhenNegative() {
        PlayerMysteryData d = new PlayerMysteryData();
        d.sequence = -1;
        assertFalse(d.isExtraordinary());
    }

    @Test
    void sequenceSevenAbilityStateSurvivesCapabilityCopy() {
        PlayerMysteryData source = new PlayerMysteryData();
        source.paperSubstituteArmedEndTick = 900L;
        source.paperSubstituteDimension = "minecraft:overworld";
        source.paperSubstituteX = 12.5d;
        source.paperSubstituteY = 70d;
        source.paperSubstituteZ = -4.5d;
        source.flameLeapCooldownEndTick = 400L;
        source.clownDodgeCount = 4;
        source.lastRestRecoveryDay = 12L;
        source.m1TrialActive = true;
        source.m1TrialStartTick = 100L;
        source.m1TrialElapsedTicks = 200L;
        source.m1TrialCampVisited = true;
        source.m1TrialBestSequence = 7;
        source.m1TrialOccultKills = 3;
        source.m1TrialActingEvents = 2;
        source.m1TrialMaxPressure = 42f;
        source.m1TrialReconnects = 1;
        source.m1TrialServerRestarts = 2;
        source.m1TrialDimensionChanges = 3;
        source.m1TrialDeathRecoveries = 1;
        source.m1TrialPendingReconnect = true;
        source.m1TrialSessionId = "d21fbf02-2d69-4e09-ac2f-0a8e0f71d79d";
        source.m1TrialCampReachedTick = 1200L;
        source.m1TrialSequence9Tick = 2400L;
        source.m1TrialSequence8Tick = 4800L;
        source.m1TrialSequence7Tick = 7000L;
        source.m1TrialFirstOccultKillTick = 2600L;
        source.m1TrialFirstActingTick = 2800L;
        source.m1TrialRiskReachedTick = 3200L;
        source.m1TrialIdentityAnchoredTick = 3400L;
        source.m1TrialReflectionCompletedTick = 3600L;
        source.m1TrialStreetLifeCompletedTick = 3800L;
        source.thiefPilferCooldownEndTick = 300L;
        source.thiefEscapeCooldownEndTick = 600L;
        source.apprenticeTrickCooldownEndTick = 200L;
        source.apprenticeCopyCooldownEndTick = 400L;
        source.psychPacifyCooldownEndTick = 500L;
        source.psychShockCooldownEndTick = 600L;
        source.pyroSpearCooldownEndTick = 700L;
        source.pyroRingCooldownEndTick = 800L;
        source.thiefSwapCooldownEndTick = 901L;
        source.thiefDecoyCooldownEndTick = 902L;
        source.thiefRuneCooldownEndTick = 903L;
        source.thiefLockpickCooldownEndTick = 904L;
        source.thiefEraseCooldownEndTick = 905L;
        source.apprenticeRelocateCooldownEndTick = 906L;
        source.apprenticeLinkCooldownEndTick = 907L;
        source.apprenticeMirrorCooldownEndTick = 908L;
        source.apprenticeDivinationCooldownEndTick = 909L;
        source.apprenticeWardCooldownEndTick = 910L;
        ResourceLocation commission = ResourceLocation.fromNamespaceAndPath(
                "lord_of_mysteries", "commission/test");
        source.moneyPence = 253L;
        source.lastCityWorkDay = 17L;
        source.cityWorkShifts = 4;
        source.identityAnchored = true;
        source.activeCommissionId = commission.toString();
        source.activeQuestChainId = "lord_of_mysteries:quest/test";
        source.activeQuestStep = 4;
        source.questObjectiveProgress = 2;
        source.commissionAcceptedTick = 1200L;
        source.escortedReporterUuid = "c7838ad4-a600-45c6-a747-7d954892158f";
        source.questDefenseWaveSpawned = true;
        source.questDefenseNextTick = 1800L;
        source.questResolutionRoute = "divination";
        source.questResolutionReady = true;
        source.completedCommissions.add(commission);
        source.commissionCooldowns.put(commission, 2400L);
        source.caseDebriefs.put(commission,
                new CaseDebriefRecord(40, 25, 18, 8,
                        1800L, 3000L, "divination"));
        source.caseHypotheses.put(commission,
                CaseHypothesisRecord.EMPTY.propose(
                        "clue_synthesis", CaseHypothesisStance.CONTRADICTS,
                        "The clues conflict."));
        DynamicCaseHistoryEntry historyEntry = new DynamicCaseHistoryEntry(
                14L, 2L, "copy-test",
                DynamicCaseProfile.Archetype.MISSING_PERSON,
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER,
                DynamicCaseProfile.Organization.MIST_CITY_PRESS,
                DynamicCaseProfile.CaseLocation.MIST_CITY_OUTPOST,
                CaseGrade.A, 84, 3000L, 2,
                DynamicCaseHistoryEntry.FollowUpStatus.CLAIMED);
        source.dynamicCaseHistory.add(historyEntry);
        source.dynamicCaseContactEvents.add(
                DynamicCaseContactEvent.caseClosed(historyEntry));
        source.dynamicCaseContactStandings.put(
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER, 6);
        source.organizationResponseTask = new DynamicCaseResponseTask(
                historyEntry.instanceId(),
                historyEntry.organization(),
                historyEntry.subject(),
                DynamicCaseWeeklyDirective.SOURCE_VERIFICATION,
                14L, 17L,
                DynamicCaseResponseTask.Stage.ASSIGNED);

        PlayerMysteryData copied = new PlayerMysteryData();
        copied.copyFrom(source);

        assertEquals(900L, copied.paperSubstituteArmedEndTick);
        assertEquals("minecraft:overworld", copied.paperSubstituteDimension);
        assertEquals(12.5d, copied.paperSubstituteX);
        assertEquals(70d, copied.paperSubstituteY);
        assertEquals(-4.5d, copied.paperSubstituteZ);
        assertEquals(400L, copied.flameLeapCooldownEndTick);
        assertEquals(4, copied.clownDodgeCount);
        assertEquals(12L, copied.lastRestRecoveryDay);
        assertTrue(copied.m1TrialActive);
        assertEquals(100L, copied.m1TrialStartTick);
        assertEquals(200L, copied.m1TrialElapsedTicks);
        assertTrue(copied.m1TrialCampVisited);
        assertEquals(7, copied.m1TrialBestSequence);
        assertEquals(3, copied.m1TrialOccultKills);
        assertEquals(2, copied.m1TrialActingEvents);
        assertEquals(42f, copied.m1TrialMaxPressure);
        assertEquals(1, copied.m1TrialReconnects);
        assertEquals(2, copied.m1TrialServerRestarts);
        assertEquals(3, copied.m1TrialDimensionChanges);
        assertEquals(1, copied.m1TrialDeathRecoveries);
        assertTrue(copied.m1TrialPendingReconnect);
        assertEquals("d21fbf02-2d69-4e09-ac2f-0a8e0f71d79d",
                copied.m1TrialSessionId);
        assertEquals(1200L, copied.m1TrialCampReachedTick);
        assertEquals(2400L, copied.m1TrialSequence9Tick);
        assertEquals(4800L, copied.m1TrialSequence8Tick);
        assertEquals(7000L, copied.m1TrialSequence7Tick);
        assertEquals(2600L, copied.m1TrialFirstOccultKillTick);
        assertEquals(2800L, copied.m1TrialFirstActingTick);
        assertEquals(3200L, copied.m1TrialRiskReachedTick);
        assertEquals(3400L, copied.m1TrialIdentityAnchoredTick);
        assertEquals(3600L, copied.m1TrialReflectionCompletedTick);
        assertEquals(3800L, copied.m1TrialStreetLifeCompletedTick);
        assertEquals(300L, copied.thiefPilferCooldownEndTick);
        assertEquals(600L, copied.thiefEscapeCooldownEndTick);
        assertEquals(200L, copied.apprenticeTrickCooldownEndTick);
        assertEquals(400L, copied.apprenticeCopyCooldownEndTick);
        assertEquals(500L, copied.psychPacifyCooldownEndTick);
        assertEquals(600L, copied.psychShockCooldownEndTick);
        assertEquals(700L, copied.pyroSpearCooldownEndTick);
        assertEquals(800L, copied.pyroRingCooldownEndTick);
        assertEquals(901L, copied.thiefSwapCooldownEndTick);
        assertEquals(902L, copied.thiefDecoyCooldownEndTick);
        assertEquals(903L, copied.thiefRuneCooldownEndTick);
        assertEquals(904L, copied.thiefLockpickCooldownEndTick);
        assertEquals(905L, copied.thiefEraseCooldownEndTick);
        assertEquals(906L, copied.apprenticeRelocateCooldownEndTick);
        assertEquals(907L, copied.apprenticeLinkCooldownEndTick);
        assertEquals(908L, copied.apprenticeMirrorCooldownEndTick);
        assertEquals(909L, copied.apprenticeDivinationCooldownEndTick);
        assertEquals(910L, copied.apprenticeWardCooldownEndTick);
        assertEquals(253L, copied.moneyPence);
        assertEquals(17L, copied.lastCityWorkDay);
        assertEquals(4, copied.cityWorkShifts);
        assertTrue(copied.identityAnchored);
        assertEquals(commission.toString(), copied.activeCommissionId);
        assertEquals("lord_of_mysteries:quest/test", copied.activeQuestChainId);
        assertEquals(4, copied.activeQuestStep);
        assertEquals(2, copied.questObjectiveProgress);
        assertEquals(1200L, copied.commissionAcceptedTick);
        assertEquals("c7838ad4-a600-45c6-a747-7d954892158f",
                copied.escortedReporterUuid);
        assertTrue(copied.questDefenseWaveSpawned);
        assertEquals(1800L, copied.questDefenseNextTick);
        assertEquals("divination", copied.questResolutionRoute);
        assertTrue(copied.questResolutionReady);
        assertTrue(copied.completedCommissions.contains(commission));
        assertEquals(2400L, copied.commissionCooldowns.get(commission));
        assertEquals(91, copied.caseDebriefs.get(commission).score());
        assertEquals("The clues conflict.",
                copied.caseHypotheses.get(commission).note());
        assertEquals(historyEntry, copied.dynamicCaseHistory.get(0));
        assertNotSame(source.dynamicCaseHistory, copied.dynamicCaseHistory);
        assertEquals(source.dynamicCaseContactEvents,
                copied.dynamicCaseContactEvents);
        assertNotSame(source.dynamicCaseContactEvents,
                copied.dynamicCaseContactEvents);
        assertEquals(6, copied.dynamicCaseContactStandings.get(
                DynamicCaseProfile.Subject.APPRENTICE_REPORTER));
        assertNotSame(source.dynamicCaseContactStandings,
                copied.dynamicCaseContactStandings);
        assertEquals(source.organizationResponseTask,
                copied.organizationResponseTask);
    }

    @Test
    void continuityEvidenceSurvivesNbtRoundTrip() {
        PlayerMysteryData source = new PlayerMysteryData();
        source.m1TrialActive = true;
        source.m1TrialStartTick = 500L;
        source.m1TrialElapsedTicks = 900L;
        source.m1TrialReconnects = 2;
        source.m1TrialServerRestarts = 1;
        source.m1TrialDimensionChanges = 4;
        source.m1TrialDeathRecoveries = 1;
        source.m1TrialSessionId = "67418fd3-2b1d-450e-af60-6a4827e04612";
        source.m1TrialCampReachedTick = 100L;
        source.m1TrialSequence9Tick = 200L;
        source.m1TrialSequence8Tick = 500L;
        source.m1TrialSequence7Tick = 800L;
        source.m1TrialFirstOccultKillTick = 300L;
        source.m1TrialFirstActingTick = 400L;
        source.m1TrialRiskReachedTick = 600L;
        source.m1TrialIdentityAnchoredTick = 650L;
        source.m1TrialReflectionCompletedTick = 700L;
        source.m1TrialStreetLifeCompletedTick = 750L;
        source.identityAnchored = true;
        source.lastCityWorkDay = 8L;
        source.cityWorkShifts = 2;
        source.activeCommissionId = "lord_of_mysteries:commission/test";
        source.activeQuestChainId = "lord_of_mysteries:quest/test";
        source.activeQuestStep = 9;
        source.questResolutionRoute = "stealth";
        source.questResolutionReady = true;
        ResourceLocation debriefCase = ResourceLocation.fromNamespaceAndPath(
                "lord_of_mysteries", "commission/lost_cat");
        source.caseDebriefs.put(debriefCase,
                new CaseDebriefRecord(40, 30, 20, 10,
                        1200L, 1700L, ""));
        DynamicCaseHistoryEntry historyEntry = new DynamicCaseHistoryEntry(
                21L, 3L, "round-trip",
                DynamicCaseProfile.Archetype.OCCULT_CRIME,
                DynamicCaseProfile.Subject.RETIRED_CONSTABLE,
                DynamicCaseProfile.Organization.CONSTABULARY,
                DynamicCaseProfile.CaseLocation.ABANDONED_CHURCH,
                CaseGrade.S, 100, 1700L, 3,
                DynamicCaseHistoryEntry.FollowUpStatus.CLAIMED);
        source.dynamicCaseHistory.add(historyEntry);
        source.dynamicCaseContactEvents.add(
                DynamicCaseContactEvent.caseClosed(historyEntry));
        source.dynamicCaseContactStandings.put(
                DynamicCaseProfile.Subject.RETIRED_CONSTABLE, 9);
        source.organizationResponseTask = new DynamicCaseResponseTask(
                historyEntry.instanceId(),
                historyEntry.organization(),
                historyEntry.subject(),
                DynamicCaseWeeklyDirective.CHAIN_OF_CUSTODY,
                21L, 24L,
                DynamicCaseResponseTask.Stage.BRIEFED);

        PlayerMysteryData restored = new PlayerMysteryData();
        restored.load(source.save());

        assertTrue(restored.m1TrialActive);
        assertEquals(500L, restored.m1TrialStartTick);
        assertEquals(900L, restored.m1TrialElapsedTicks);
        assertEquals(2, restored.m1TrialReconnects);
        assertEquals(1, restored.m1TrialServerRestarts);
        assertEquals(4, restored.m1TrialDimensionChanges);
        assertEquals(1, restored.m1TrialDeathRecoveries);
        assertEquals(100L, restored.m1TrialCampReachedTick);
        assertEquals(200L, restored.m1TrialSequence9Tick);
        assertEquals(500L, restored.m1TrialSequence8Tick);
        assertEquals(800L, restored.m1TrialSequence7Tick);
        assertEquals(300L, restored.m1TrialFirstOccultKillTick);
        assertEquals(400L, restored.m1TrialFirstActingTick);
        assertEquals(600L, restored.m1TrialRiskReachedTick);
        assertEquals(650L, restored.m1TrialIdentityAnchoredTick);
        assertEquals(700L, restored.m1TrialReflectionCompletedTick);
        assertEquals(750L, restored.m1TrialStreetLifeCompletedTick);
        assertTrue(restored.identityAnchored);
        assertEquals(8L, restored.lastCityWorkDay);
        assertEquals(2, restored.cityWorkShifts);
        assertEquals("stealth", restored.questResolutionRoute);
        assertTrue(restored.questResolutionReady);
        assertEquals(100, restored.caseDebriefs.get(debriefCase).score());
        assertEquals(historyEntry, restored.dynamicCaseHistory.get(0));
        assertEquals(source.dynamicCaseContactEvents,
                restored.dynamicCaseContactEvents);
        assertEquals(9, restored.dynamicCaseContactStandings.get(
                DynamicCaseProfile.Subject.RETIRED_CONSTABLE));
        assertEquals(source.organizationResponseTask,
                restored.organizationResponseTask);
        assertEquals(PlayerMysteryData.CURRENT_SCHEMA_VERSION,
                restored.schemaVersion);
    }

    @Test
    void migrationBackupAndOrphansSurviveRoundTripWithoutDuplication() {
        net.minecraft.nbt.CompoundTag legacy = new net.minecraft.nbt.CompoundTag();
        legacy.putInt("schema_version", 15);
        legacy.putString("pathway", "lord_of_mysteries:seer");
        legacy.putInt("sequence", 9);
        legacy.putString("potion_quality", "complete");
        net.minecraft.nbt.ListTag knowledge = new net.minecraft.nbt.ListTag();
        knowledge.add(net.minecraft.nbt.StringTag.valueOf("invalid id"));
        legacy.put("known_knowledge", knowledge);
        legacy.putString("active_commission", "invalid id");
        legacy.putString("active_quest_chain", "lord_of_mysteries:quest/test");
        net.minecraft.nbt.CompoundTag brokenDebriefs =
                new net.minecraft.nbt.CompoundTag();
        brokenDebriefs.putString(
                "lord_of_mysteries:commission/lost_cat", "broken");
        legacy.put("case_debriefs", brokenDebriefs);
        net.minecraft.nbt.CompoundTag brokenHypotheses =
                new net.minecraft.nbt.CompoundTag();
        brokenHypotheses.putString(
                "lord_of_mysteries:commission/lost_cat", "broken");
        legacy.put("case_hypotheses", brokenHypotheses);

        PlayerMysteryData migrated = new PlayerMysteryData();
        migrated.load(legacy);

        assertEquals(1, migrated.characteristicBundles.size());
        assertEquals(1, migrated.migrationBackups.size());
        assertEquals(8, migrated.migrationHistory.size());
        assertTrue(migrated.orphanedEntries.stream().anyMatch(entry ->
                entry.getString("section").equals("known_knowledge")));
        assertTrue(migrated.orphanedEntries.stream().anyMatch(entry ->
                entry.getString("section").equals("active_quest")));
        assertTrue(migrated.orphanedEntries.stream().anyMatch(entry ->
                entry.getString("section").equals("case_debriefs")));
        assertTrue(migrated.orphanedEntries.stream().anyMatch(entry ->
                entry.getString("section").equals("case_hypotheses")));
        assertEquals("", migrated.activeCommissionId);
        assertEquals("", migrated.activeQuestChainId);

        PlayerMysteryData restored = new PlayerMysteryData();
        restored.load(migrated.save());

        assertEquals(1, restored.migrationBackups.size());
        assertEquals(8, restored.migrationHistory.size());
        assertEquals(migrated.orphanedEntries.size(),
                restored.orphanedEntries.size());
    }
}
