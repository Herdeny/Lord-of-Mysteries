package top.aurora.lordofmysteries.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.characteristic.CharacteristicBundle;
import top.aurora.lordofmysteries.characteristic.CharacteristicLedger;
import top.aurora.lordofmysteries.commission.CaseDebriefRecord;
import top.aurora.lordofmysteries.commission.CaseHypothesisRecord;
import top.aurora.lordofmysteries.commission.DynamicCaseHistoryEntry;

/**
 * 玩家非凡者数据（设计文档 §5.1）。
 *
 * Forge 1.20.1 没有 NeoForge 的 Attachments API，本类作为 Capability 的载荷，
 * 通过 NBT 序列化随玩家存档；附着逻辑见 {@link MysteryCapability}。
 *
 * <p>这个类刻意保持为“纯数据对象”：不直接依赖 Player、Level 或事件对象。这样做有
 * 三个好处：单元测试简单，NBT 迁移清晰，后续网络同步时也容易把数据快照拆出来。
 */
public class PlayerMysteryData {

    public static final int CURRENT_SCHEMA_VERSION = 20;
    private static final int MAX_MIGRATION_BACKUPS = 3;
    private static final int MAX_MIGRATION_HISTORY = 64;

    // 途径 & 序列。
    // pathway 使用 ResourceLocation 以便完全数据驱动，例如 lord_of_mysteries:seer。
    public ResourceLocation pathway = null; // null = 普通人，尚未服食魔药/进入途径
    public int sequence = -1;               // -1 = 未入途径；9~0 对应从低到高的序列

    // 核心数值（§5）。这些值目前公开访问，方便 M0 快速接线；
    // 当规则稳定后可以收敛为 getter/setter，在 setter 中统一 clamp 和触发同步。
    public float spirituality = 0f;         // 当前灵性值；能力消耗和自然恢复都会改它
    public float spiritualityMax = 100f;    // 灵性上限；由途径、序列、物品或仪式修正
    public float digestion = 0f;            // 消化进度，范围约定为 0-100%
    public float pollution = 0f;            // 污染值，范围约定为 0-100，100 触发失控
    public float insanityPressure = 0f;     // 失控压力，范围约定为 0-100，用于短期精神状态
    public String potionQuality = "complete"; // 当前序列魔药品质，影响扮演消化倍率
    public List<CharacteristicBundle> characteristicBundles = new ArrayList<>();
    public List<CompoundTag> orphanedEntries = new ArrayList<>();
    public List<CompoundTag> migrationBackups = new ArrayList<>();
    public List<CompoundTag> migrationHistory = new ArrayList<>();
    public boolean futureSchemaDetected = false;

    // 批次1 能力状态。
    // 灵视是否开启：服务端权威，登入登出保留状态。
    public boolean spiritVisionActive = false;
    // 简易占卜冷却截止 gameTime（tick）。<=0 表示不在冷却。
    public long divinationCooldownEndTick = 0L;
    // 危险直觉冷却截止 gameTime（tick）。<=0 表示不在冷却。
    public long dangerIntuitionCooldownEndTick = 0L;
    public long breakdownCooldownEndTick = 0L;
    public long mentalTraumaEndTick = 0L;
    public boolean emotionReadActive = false;
    public long behaviorPredictionCooldownEndTick = 0L;
    public long surfaceReadCooldownEndTick = 0L;
    public long mentalSuggestionCooldownEndTick = 0L;
    public String hunterTrackedTarget = "";
    public long hunterTrackingStartTick = 0L;
    public long hunterTrackingEndTick = 0L;
    public long provokeCooldownEndTick = 0L;
    public long enrageCooldownEndTick = 0L;
    public long battleWillCooldownEndTick = 0L;
    public long battleWillEndTick = 0L;
    public long cardBladeCooldownEndTick = 0L;
    public long clownDodgeCooldownEndTick = 0L;
    public int clownDodgeCount = 0;
    public long expressionControlCooldownEndTick = 0L;
    public long flameLeapCooldownEndTick = 0L;
    public long flameLeapStrikeEndTick = 0L;
    public long paperSubstituteCooldownEndTick = 0L;
    public long paperSubstituteArmedEndTick = 0L;
    public String paperSubstituteDimension = "";
    public double paperSubstituteX = 0d;
    public double paperSubstituteY = 0d;
    public double paperSubstituteZ = 0d;
    public long airBulletCooldownEndTick = 0L;
    public long stageIllusionCooldownEndTick = 0L;
    public long thiefPilferCooldownEndTick = 0L;
    public long thiefEscapeCooldownEndTick = 0L;
    public long apprenticeTrickCooldownEndTick = 0L;
    public long apprenticeCopyCooldownEndTick = 0L;
    public long thiefSwapCooldownEndTick = 0L;
    public long thiefDecoyCooldownEndTick = 0L;
    public long thiefRuneCooldownEndTick = 0L;
    public long thiefLockpickCooldownEndTick = 0L;
    public long thiefEraseCooldownEndTick = 0L;
    public long apprenticeRelocateCooldownEndTick = 0L;
    public long apprenticeLinkCooldownEndTick = 0L;
    public long apprenticeMirrorCooldownEndTick = 0L;
    public long apprenticeDivinationCooldownEndTick = 0L;
    public long apprenticeWardCooldownEndTick = 0L;
    public long psychPacifyCooldownEndTick = 0L;
    public long psychShockCooldownEndTick = 0L;
    public long pyroSpearCooldownEndTick = 0L;
    public long pyroRingCooldownEndTick = 0L;
    public long lastRestRecoveryDay = Long.MIN_VALUE;
    public boolean m1TrialActive = false;
    public long m1TrialStartTick = -1L;
    public long m1TrialElapsedTicks = 0L;
    public boolean m1TrialCampVisited = false;
    public int m1TrialBestSequence = -1;
    public int m1TrialOccultKills = 0;
    public int m1TrialDeaths = 0;
    public int m1TrialRestRecoveries = 0;
    public int m1TrialCharmsConsumed = 0;
    public int m1TrialActingEvents = 0;
    public float m1TrialMaxPressure = 0f;
    public float m1TrialMaxPollution = 0f;
    public int m1TrialReconnects = 0;
    public int m1TrialServerRestarts = 0;
    public int m1TrialDimensionChanges = 0;
    public int m1TrialDeathRecoveries = 0;
    public boolean m1TrialPendingReconnect = false;
    public String m1TrialSessionId = "";
    public long m1TrialCampReachedTick = -1L;
    public long m1TrialSequence9Tick = -1L;
    public long m1TrialSequence8Tick = -1L;
    public long m1TrialSequence7Tick = -1L;
    public long m1TrialFirstOccultKillTick = -1L;
    public long m1TrialFirstActingTick = -1L;
    public long m1TrialRiskReachedTick = -1L;
    public long m1TrialIdentityAnchoredTick = -1L;
    public long m1TrialReflectionCompletedTick = -1L;
    public long m1TrialStreetLifeCompletedTick = -1L;

    public long moneyPence = 0L;
    public long lastCityWorkDay = Long.MIN_VALUE;
    public int cityWorkShifts = 0;
    public String activeCommissionId = "";
    public String activeQuestChainId = "";
    public int activeQuestStep = -1;
    public int questObjectiveProgress = 0;
    public long commissionAcceptedTick = 0L;
    public String escortedReporterUuid = "";
    public boolean questDefenseWaveSpawned = false;
    public long questDefenseNextTick = 0L;
    public String questResolutionRoute = "";
    public boolean questResolutionReady = false;
    public Set<ResourceLocation> completedCommissions = new HashSet<>();
    public Map<ResourceLocation, Long> commissionCooldowns = new HashMap<>();
    public Map<ResourceLocation, CaseDebriefRecord> caseDebriefs = new HashMap<>();
    public Map<ResourceLocation, CaseHypothesisRecord> caseHypotheses =
            new HashMap<>();
    public List<DynamicCaseHistoryEntry> dynamicCaseHistory =
            new ArrayList<>();

    // 知识系统。保存玩家已经解锁的知识条目 ID，供手册、仪式和配方门槛读取。
    public Set<ResourceLocation> knownKnowledge = new HashSet<>();

    // 扮演事件历史（事件ID → 最后触发的 gameTime）。
    // key 暂用 String 便于直接作为 CompoundTag 键，value 用服务器 gameTime 做冷却/衰减判断。
    public Map<String, Long> actingHistory = new HashMap<>();
    public Map<String, Integer> actingCounters = new HashMap<>();
    public float principleInsight = 0f;
    public float roleOveridentification = 0f;
    public int actingReflectionCount = 0;
    public long lastActingReflectionDay = Long.MIN_VALUE;
    public boolean identityAnchored = false;

    // 组织声望（组织ID → 声望值）。组织 ID 仍使用 ResourceLocation，支持数据包新增组织。
    public Map<ResourceLocation, Integer> orgReputation = new HashMap<>();

    // 序列化版本号（用于迁移）。未来字段改名或结构升级时，可根据旧版本补默认值。
    public int schemaVersion = CURRENT_SCHEMA_VERSION;

    private final long[] sectionFingerprints =
            new long[PlayerDataSection.values().length];
    private int dirtySectionMask = PlayerDataSection.ALL_MASK;

    /** 默认构造器需要保留，Capability Provider 和测试都会直接创建空数据。 */
    public PlayerMysteryData() {
        resetDirtyTracking();
    }

    /**
     * 判断玩家是否已经成为非凡者。
     *
     * <p>途径和序列必须同时有效，避免只写入 sequence 或只写入 pathway 时误判状态。
     */
    public boolean isExtraordinary() {
        return pathway != null && sequence >= 0;
    }

    /**
     * 死亡/跨维度时把数据拷贝到新实体（§5：默认保留非凡者身份）。
     *
     * <p>Forge 在玩家死亡或穿越终末之门等场景会创建新的 Player 实例。旧实体上的
     * Capability 不会自动转移，所以 PlayerEvent.Clone 中必须显式调用本方法。
     */
    public void copyFrom(PlayerMysteryData src) {
        this.pathway = src.pathway;
        this.sequence = src.sequence;
        this.spirituality = src.spirituality;
        this.spiritualityMax = src.spiritualityMax;
        this.digestion = src.digestion;
        this.pollution = src.pollution;
        this.insanityPressure = src.insanityPressure;
        this.potionQuality = src.potionQuality;
        this.characteristicBundles = CharacteristicLedger.copy(
                src.characteristicBundles);
        this.orphanedEntries = copyCompoundTags(src.orphanedEntries);
        this.migrationBackups = copyCompoundTags(src.migrationBackups);
        this.migrationHistory = copyCompoundTags(src.migrationHistory);
        this.futureSchemaDetected = src.futureSchemaDetected;
        this.spiritVisionActive = src.spiritVisionActive;
        this.divinationCooldownEndTick = src.divinationCooldownEndTick;
        this.dangerIntuitionCooldownEndTick = src.dangerIntuitionCooldownEndTick;
        this.breakdownCooldownEndTick = src.breakdownCooldownEndTick;
        this.mentalTraumaEndTick = src.mentalTraumaEndTick;
        this.emotionReadActive = src.emotionReadActive;
        this.behaviorPredictionCooldownEndTick = src.behaviorPredictionCooldownEndTick;
        this.surfaceReadCooldownEndTick = src.surfaceReadCooldownEndTick;
        this.mentalSuggestionCooldownEndTick = src.mentalSuggestionCooldownEndTick;
        this.hunterTrackedTarget = src.hunterTrackedTarget;
        this.hunterTrackingStartTick = src.hunterTrackingStartTick;
        this.hunterTrackingEndTick = src.hunterTrackingEndTick;
        this.provokeCooldownEndTick = src.provokeCooldownEndTick;
        this.enrageCooldownEndTick = src.enrageCooldownEndTick;
        this.battleWillCooldownEndTick = src.battleWillCooldownEndTick;
        this.battleWillEndTick = src.battleWillEndTick;
        this.cardBladeCooldownEndTick = src.cardBladeCooldownEndTick;
        this.clownDodgeCooldownEndTick = src.clownDodgeCooldownEndTick;
        this.clownDodgeCount = src.clownDodgeCount;
        this.expressionControlCooldownEndTick = src.expressionControlCooldownEndTick;
        this.flameLeapCooldownEndTick = src.flameLeapCooldownEndTick;
        this.flameLeapStrikeEndTick = src.flameLeapStrikeEndTick;
        this.paperSubstituteCooldownEndTick = src.paperSubstituteCooldownEndTick;
        this.paperSubstituteArmedEndTick = src.paperSubstituteArmedEndTick;
        this.paperSubstituteDimension = src.paperSubstituteDimension;
        this.paperSubstituteX = src.paperSubstituteX;
        this.paperSubstituteY = src.paperSubstituteY;
        this.paperSubstituteZ = src.paperSubstituteZ;
        this.airBulletCooldownEndTick = src.airBulletCooldownEndTick;
        this.stageIllusionCooldownEndTick = src.stageIllusionCooldownEndTick;
        this.thiefPilferCooldownEndTick = src.thiefPilferCooldownEndTick;
        this.thiefEscapeCooldownEndTick = src.thiefEscapeCooldownEndTick;
        this.apprenticeTrickCooldownEndTick = src.apprenticeTrickCooldownEndTick;
        this.apprenticeCopyCooldownEndTick = src.apprenticeCopyCooldownEndTick;
        this.thiefSwapCooldownEndTick = src.thiefSwapCooldownEndTick;
        this.thiefDecoyCooldownEndTick = src.thiefDecoyCooldownEndTick;
        this.thiefRuneCooldownEndTick = src.thiefRuneCooldownEndTick;
        this.thiefLockpickCooldownEndTick = src.thiefLockpickCooldownEndTick;
        this.thiefEraseCooldownEndTick = src.thiefEraseCooldownEndTick;
        this.apprenticeRelocateCooldownEndTick = src.apprenticeRelocateCooldownEndTick;
        this.apprenticeLinkCooldownEndTick = src.apprenticeLinkCooldownEndTick;
        this.apprenticeMirrorCooldownEndTick = src.apprenticeMirrorCooldownEndTick;
        this.apprenticeDivinationCooldownEndTick = src.apprenticeDivinationCooldownEndTick;
        this.apprenticeWardCooldownEndTick = src.apprenticeWardCooldownEndTick;
        this.psychPacifyCooldownEndTick = src.psychPacifyCooldownEndTick;
        this.psychShockCooldownEndTick = src.psychShockCooldownEndTick;
        this.pyroSpearCooldownEndTick = src.pyroSpearCooldownEndTick;
        this.pyroRingCooldownEndTick = src.pyroRingCooldownEndTick;
        this.lastRestRecoveryDay = src.lastRestRecoveryDay;
        this.m1TrialActive = src.m1TrialActive;
        this.m1TrialStartTick = src.m1TrialStartTick;
        this.m1TrialElapsedTicks = src.m1TrialElapsedTicks;
        this.m1TrialCampVisited = src.m1TrialCampVisited;
        this.m1TrialBestSequence = src.m1TrialBestSequence;
        this.m1TrialOccultKills = src.m1TrialOccultKills;
        this.m1TrialDeaths = src.m1TrialDeaths;
        this.m1TrialRestRecoveries = src.m1TrialRestRecoveries;
        this.m1TrialCharmsConsumed = src.m1TrialCharmsConsumed;
        this.m1TrialActingEvents = src.m1TrialActingEvents;
        this.m1TrialMaxPressure = src.m1TrialMaxPressure;
        this.m1TrialMaxPollution = src.m1TrialMaxPollution;
        this.m1TrialReconnects = src.m1TrialReconnects;
        this.m1TrialServerRestarts = src.m1TrialServerRestarts;
        this.m1TrialDimensionChanges = src.m1TrialDimensionChanges;
        this.m1TrialDeathRecoveries = src.m1TrialDeathRecoveries;
        this.m1TrialPendingReconnect = src.m1TrialPendingReconnect;
        this.m1TrialSessionId = src.m1TrialSessionId;
        this.m1TrialCampReachedTick = src.m1TrialCampReachedTick;
        this.m1TrialSequence9Tick = src.m1TrialSequence9Tick;
        this.m1TrialSequence8Tick = src.m1TrialSequence8Tick;
        this.m1TrialSequence7Tick = src.m1TrialSequence7Tick;
        this.m1TrialFirstOccultKillTick = src.m1TrialFirstOccultKillTick;
        this.m1TrialFirstActingTick = src.m1TrialFirstActingTick;
        this.m1TrialRiskReachedTick = src.m1TrialRiskReachedTick;
        this.m1TrialIdentityAnchoredTick = src.m1TrialIdentityAnchoredTick;
        this.m1TrialReflectionCompletedTick = src.m1TrialReflectionCompletedTick;
        this.m1TrialStreetLifeCompletedTick = src.m1TrialStreetLifeCompletedTick;
        this.moneyPence = src.moneyPence;
        this.lastCityWorkDay = src.lastCityWorkDay;
        this.cityWorkShifts = src.cityWorkShifts;
        this.activeCommissionId = src.activeCommissionId;
        this.activeQuestChainId = src.activeQuestChainId;
        this.activeQuestStep = src.activeQuestStep;
        this.questObjectiveProgress = src.questObjectiveProgress;
        this.commissionAcceptedTick = src.commissionAcceptedTick;
        this.escortedReporterUuid = src.escortedReporterUuid;
        this.questDefenseWaveSpawned = src.questDefenseWaveSpawned;
        this.questDefenseNextTick = src.questDefenseNextTick;
        this.questResolutionRoute = src.questResolutionRoute;
        this.questResolutionReady = src.questResolutionReady;
        this.completedCommissions = new HashSet<>(src.completedCommissions);
        this.commissionCooldowns = new HashMap<>(src.commissionCooldowns);
        this.caseDebriefs = new HashMap<>(src.caseDebriefs);
        this.caseHypotheses = new HashMap<>(src.caseHypotheses);
        this.dynamicCaseHistory = new ArrayList<>(src.dynamicCaseHistory);
        this.knownKnowledge = new HashSet<>(src.knownKnowledge);
        this.actingHistory = new HashMap<>(src.actingHistory);
        this.actingCounters = new HashMap<>(src.actingCounters);
        this.principleInsight = src.principleInsight;
        this.roleOveridentification = src.roleOveridentification;
        this.actingReflectionCount = src.actingReflectionCount;
        this.lastActingReflectionDay = src.lastActingReflectionDay;
        this.identityAnchored = src.identityAnchored;
        this.orgReputation = new HashMap<>(src.orgReputation);
        this.schemaVersion = CURRENT_SCHEMA_VERSION;
        sanitize();
        resetDirtyTracking();
    }

    // —— NBT 序列化 ——

    /**
     * 把玩家非凡者数据写入 NBT。
     *
     * <p>这里的 key 名会进入玩家存档，属于长期兼容面。后续需要改结构时应保留读取旧 key
     * 的能力，并通过 schemaVersion 做迁移，而不是直接删除旧字段。
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (pathway != null) tag.putString("pathway", pathway.toString());
        tag.putInt("sequence", sequence);
        tag.putFloat("spirituality", spirituality);
        tag.putFloat("spirituality_max", spiritualityMax);
        tag.putFloat("digestion", digestion);
        tag.putFloat("pollution", pollution);
        tag.putFloat("insanity_pressure", insanityPressure);
        tag.putString("potion_quality", potionQuality);
        ListTag characteristics = new ListTag();
        for (CharacteristicBundle bundle : characteristicBundles) {
            characteristics.add(bundle.save());
        }
        tag.put("characteristic_bundles", characteristics);
        tag.putBoolean("spirit_vision_active", spiritVisionActive);
        tag.putLong("divination_cd_end", divinationCooldownEndTick);
        tag.putLong("danger_intuition_cd_end", dangerIntuitionCooldownEndTick);
        tag.putLong("breakdown_cd_end", breakdownCooldownEndTick);
        tag.putLong("mental_trauma_end", mentalTraumaEndTick);
        tag.putBoolean("emotion_read_active", emotionReadActive);
        tag.putLong("behavior_prediction_cd_end", behaviorPredictionCooldownEndTick);
        tag.putLong("surface_read_cd_end", surfaceReadCooldownEndTick);
        tag.putLong("mental_suggestion_cd_end", mentalSuggestionCooldownEndTick);
        tag.putString("hunter_tracked_target", hunterTrackedTarget);
        tag.putLong("hunter_tracking_start", hunterTrackingStartTick);
        tag.putLong("hunter_tracking_end", hunterTrackingEndTick);
        tag.putLong("provoke_cd_end", provokeCooldownEndTick);
        tag.putLong("enrage_cd_end", enrageCooldownEndTick);
        tag.putLong("battle_will_cd_end", battleWillCooldownEndTick);
        tag.putLong("battle_will_end", battleWillEndTick);
        tag.putLong("card_blade_cd_end", cardBladeCooldownEndTick);
        tag.putLong("clown_dodge_cd_end", clownDodgeCooldownEndTick);
        tag.putInt("clown_dodge_count", clownDodgeCount);
        tag.putLong("expression_control_cd_end", expressionControlCooldownEndTick);
        tag.putLong("flame_leap_cd_end", flameLeapCooldownEndTick);
        tag.putLong("flame_leap_strike_end", flameLeapStrikeEndTick);
        tag.putLong("paper_substitute_cd_end", paperSubstituteCooldownEndTick);
        tag.putLong("paper_substitute_armed_end", paperSubstituteArmedEndTick);
        tag.putString("paper_substitute_dimension", paperSubstituteDimension);
        tag.putDouble("paper_substitute_x", paperSubstituteX);
        tag.putDouble("paper_substitute_y", paperSubstituteY);
        tag.putDouble("paper_substitute_z", paperSubstituteZ);
        tag.putLong("air_bullet_cd_end", airBulletCooldownEndTick);
        tag.putLong("stage_illusion_cd_end", stageIllusionCooldownEndTick);
        tag.putLong("thief_pilfer_cd_end", thiefPilferCooldownEndTick);
        tag.putLong("thief_escape_cd_end", thiefEscapeCooldownEndTick);
        tag.putLong("apprentice_trick_cd_end", apprenticeTrickCooldownEndTick);
        tag.putLong("apprentice_copy_cd_end", apprenticeCopyCooldownEndTick);
        tag.putLong("thief_swap_cd_end", thiefSwapCooldownEndTick);
        tag.putLong("thief_decoy_cd_end", thiefDecoyCooldownEndTick);
        tag.putLong("thief_rune_cd_end", thiefRuneCooldownEndTick);
        tag.putLong("thief_lockpick_cd_end", thiefLockpickCooldownEndTick);
        tag.putLong("thief_erase_cd_end", thiefEraseCooldownEndTick);
        tag.putLong("apprentice_relocate_cd_end", apprenticeRelocateCooldownEndTick);
        tag.putLong("apprentice_link_cd_end", apprenticeLinkCooldownEndTick);
        tag.putLong("apprentice_mirror_cd_end", apprenticeMirrorCooldownEndTick);
        tag.putLong("apprentice_divination_cd_end", apprenticeDivinationCooldownEndTick);
        tag.putLong("apprentice_ward_cd_end", apprenticeWardCooldownEndTick);
        tag.putLong("psych_pacify_cd_end", psychPacifyCooldownEndTick);
        tag.putLong("psych_shock_cd_end", psychShockCooldownEndTick);
        tag.putLong("pyro_spear_cd_end", pyroSpearCooldownEndTick);
        tag.putLong("pyro_ring_cd_end", pyroRingCooldownEndTick);
        tag.putLong("last_rest_recovery_day", lastRestRecoveryDay);
        tag.putBoolean("m1_trial_active", m1TrialActive);
        tag.putLong("m1_trial_start_tick", m1TrialStartTick);
        tag.putLong("m1_trial_elapsed_ticks", m1TrialElapsedTicks);
        tag.putBoolean("m1_trial_camp_visited", m1TrialCampVisited);
        tag.putInt("m1_trial_best_sequence", m1TrialBestSequence);
        tag.putInt("m1_trial_occult_kills", m1TrialOccultKills);
        tag.putInt("m1_trial_deaths", m1TrialDeaths);
        tag.putInt("m1_trial_rest_recoveries", m1TrialRestRecoveries);
        tag.putInt("m1_trial_charms_consumed", m1TrialCharmsConsumed);
        tag.putInt("m1_trial_acting_events", m1TrialActingEvents);
        tag.putFloat("m1_trial_max_pressure", m1TrialMaxPressure);
        tag.putFloat("m1_trial_max_pollution", m1TrialMaxPollution);
        tag.putInt("m1_trial_reconnects", m1TrialReconnects);
        tag.putInt("m1_trial_server_restarts", m1TrialServerRestarts);
        tag.putInt("m1_trial_dimension_changes", m1TrialDimensionChanges);
        tag.putInt("m1_trial_death_recoveries", m1TrialDeathRecoveries);
        tag.putBoolean("m1_trial_pending_reconnect", m1TrialPendingReconnect);
        tag.putString("m1_trial_session_id", m1TrialSessionId);
        tag.putLong("m1_trial_camp_reached_tick", m1TrialCampReachedTick);
        tag.putLong("m1_trial_sequence_9_tick", m1TrialSequence9Tick);
        tag.putLong("m1_trial_sequence_8_tick", m1TrialSequence8Tick);
        tag.putLong("m1_trial_sequence_7_tick", m1TrialSequence7Tick);
        tag.putLong("m1_trial_first_occult_kill_tick", m1TrialFirstOccultKillTick);
        tag.putLong("m1_trial_first_acting_tick", m1TrialFirstActingTick);
        tag.putLong("m1_trial_risk_reached_tick", m1TrialRiskReachedTick);
        tag.putLong("m1_trial_identity_anchored_tick", m1TrialIdentityAnchoredTick);
        tag.putLong("m1_trial_reflection_completed_tick", m1TrialReflectionCompletedTick);
        tag.putLong("m1_trial_street_life_completed_tick", m1TrialStreetLifeCompletedTick);
        tag.putLong("money_pence", moneyPence);
        tag.putLong("last_city_work_day", lastCityWorkDay);
        tag.putInt("city_work_shifts", cityWorkShifts);
        tag.putString("active_commission", activeCommissionId);
        tag.putString("active_quest_chain", activeQuestChainId);
        tag.putInt("active_quest_step", activeQuestStep);
        tag.putInt("quest_objective_progress", questObjectiveProgress);
        tag.putLong("commission_accepted_tick", commissionAcceptedTick);
        tag.putString("escorted_reporter_uuid", escortedReporterUuid);
        tag.putBoolean("quest_defense_wave_spawned", questDefenseWaveSpawned);
        tag.putLong("quest_defense_next_tick", questDefenseNextTick);
        tag.putString("quest_resolution_route", questResolutionRoute);
        tag.putBoolean("quest_resolution_ready", questResolutionReady);
        tag.putInt("schema_version", CURRENT_SCHEMA_VERSION);

        ListTag completed = new ListTag();
        for (ResourceLocation id : completedCommissions) {
            completed.add(StringTag.valueOf(id.toString()));
        }
        tag.put("completed_commissions", completed);

        CompoundTag commissionCooldownTag = new CompoundTag();
        commissionCooldowns.forEach((id, tick) ->
                commissionCooldownTag.putLong(id.toString(), tick));
        tag.put("commission_cooldowns", commissionCooldownTag);

        CompoundTag caseDebriefTag = new CompoundTag();
        caseDebriefs.forEach((id, record) -> {
            if (id != null && record != null) {
                caseDebriefTag.put(id.toString(), record.save());
            }
        });
        tag.put("case_debriefs", caseDebriefTag);

        CompoundTag caseHypothesisTag = new CompoundTag();
        caseHypotheses.forEach((id, record) -> {
            if (id != null && record != null) {
                caseHypothesisTag.put(id.toString(), record.save());
            }
        });
        tag.put("case_hypotheses", caseHypothesisTag);

        ListTag dynamicCaseHistoryTag = new ListTag();
        for (DynamicCaseHistoryEntry entry : dynamicCaseHistory) {
            if (entry != null) dynamicCaseHistoryTag.add(entry.save());
        }
        tag.put("dynamic_case_history", dynamicCaseHistoryTag);

        ListTag known = new ListTag();
        // ListTag 只能存 Tag 对象，因此 ResourceLocation 统一序列化成字符串。
        for (ResourceLocation k : knownKnowledge) known.add(StringTag.valueOf(k.toString()));
        tag.put("known_knowledge", known);

        CompoundTag acting = new CompoundTag();
        // CompoundTag 的键天然是字符串，适合保存“事件 ID -> 最后触发时间”的稀疏表。
        actingHistory.forEach(acting::putLong);
        tag.put("acting_history", acting);

        CompoundTag counters = new CompoundTag();
        actingCounters.forEach(counters::putInt);
        tag.put("acting_counters", counters);
        tag.putFloat("principle_insight", principleInsight);
        tag.putFloat("role_overidentification", roleOveridentification);
        tag.putInt("acting_reflection_count", actingReflectionCount);
        tag.putLong("last_acting_reflection_day", lastActingReflectionDay);
        tag.putBoolean("identity_anchored", identityAnchored);

        CompoundTag rep = new CompoundTag();
        // ResourceLocation 不能直接作为 NBT 键，转成 namespace:path 字符串存储。
        orgReputation.forEach((id, v) -> rep.putInt(id.toString(), v));
        tag.put("org_reputation", rep);

        tag.put("orphaned_entries", saveCompoundTags(orphanedEntries));
        tag.put("migration_backups", saveCompoundTags(migrationBackups));
        tag.put("migration_history", saveCompoundTags(migrationHistory));

        return tag;
    }

    /**
     * 从 NBT 读取玩家非凡者数据。
     *
     * <p>读取时尽量容错：ResourceLocation 使用 tryParse，遇到非法 ID 会跳过；新增字段
     * 使用 contains 判断并提供默认值，确保旧存档也能继续加载。
     */
    public void load(CompoundTag tag) {
        PlayerMysteryDataFixer.MigrationResult migration =
                PlayerMysteryDataFixer.migrate(tag);
        tag = migration.data();
        loadMigrationMetadata(tag, migration);
        String pathwayId = tag.contains("pathway") ? tag.getString("pathway") : "";
        pathway = pathwayId.isBlank() ? null : ResourceLocation.tryParse(pathwayId);
        if (!pathwayId.isBlank() && pathway == null) {
            addOrphan("pathway", "invalid_id", StringTag.valueOf(pathwayId));
        }
        sequence = tag.getInt("sequence");
        spirituality = tag.getFloat("spirituality");
        // 旧存档可能没有 spirituality_max；保留 100 作为基线默认值。
        spiritualityMax = tag.contains("spirituality_max") ? tag.getFloat("spirituality_max") : 100f;
        digestion = tag.getFloat("digestion");
        pollution = tag.getFloat("pollution");
        insanityPressure = tag.getFloat("insanity_pressure");
        potionQuality = tag.contains("potion_quality") ? tag.getString("potion_quality") : "complete";
        spiritVisionActive = tag.getBoolean("spirit_vision_active");
        divinationCooldownEndTick = tag.getLong("divination_cd_end");
        dangerIntuitionCooldownEndTick = tag.getLong("danger_intuition_cd_end");
        breakdownCooldownEndTick = tag.getLong("breakdown_cd_end");
        mentalTraumaEndTick = tag.getLong("mental_trauma_end");
        emotionReadActive = tag.getBoolean("emotion_read_active");
        behaviorPredictionCooldownEndTick = tag.getLong("behavior_prediction_cd_end");
        surfaceReadCooldownEndTick = tag.getLong("surface_read_cd_end");
        mentalSuggestionCooldownEndTick = tag.getLong("mental_suggestion_cd_end");
        hunterTrackedTarget = tag.contains("hunter_tracked_target")
                ? tag.getString("hunter_tracked_target") : "";
        hunterTrackingStartTick = tag.getLong("hunter_tracking_start");
        hunterTrackingEndTick = tag.getLong("hunter_tracking_end");
        provokeCooldownEndTick = tag.getLong("provoke_cd_end");
        enrageCooldownEndTick = tag.getLong("enrage_cd_end");
        battleWillCooldownEndTick = tag.getLong("battle_will_cd_end");
        battleWillEndTick = tag.getLong("battle_will_end");
        cardBladeCooldownEndTick = tag.getLong("card_blade_cd_end");
        clownDodgeCooldownEndTick = tag.getLong("clown_dodge_cd_end");
        clownDodgeCount = tag.getInt("clown_dodge_count");
        expressionControlCooldownEndTick = tag.getLong("expression_control_cd_end");
        flameLeapCooldownEndTick = tag.getLong("flame_leap_cd_end");
        flameLeapStrikeEndTick = tag.getLong("flame_leap_strike_end");
        paperSubstituteCooldownEndTick = tag.getLong("paper_substitute_cd_end");
        paperSubstituteArmedEndTick = tag.getLong("paper_substitute_armed_end");
        paperSubstituteDimension = tag.contains("paper_substitute_dimension")
                ? tag.getString("paper_substitute_dimension") : "";
        paperSubstituteX = tag.getDouble("paper_substitute_x");
        paperSubstituteY = tag.getDouble("paper_substitute_y");
        paperSubstituteZ = tag.getDouble("paper_substitute_z");
        airBulletCooldownEndTick = tag.getLong("air_bullet_cd_end");
        stageIllusionCooldownEndTick = tag.getLong("stage_illusion_cd_end");
        thiefPilferCooldownEndTick = tag.getLong("thief_pilfer_cd_end");
        thiefEscapeCooldownEndTick = tag.getLong("thief_escape_cd_end");
        apprenticeTrickCooldownEndTick = tag.getLong("apprentice_trick_cd_end");
        apprenticeCopyCooldownEndTick = tag.getLong("apprentice_copy_cd_end");
        thiefSwapCooldownEndTick = tag.getLong("thief_swap_cd_end");
        thiefDecoyCooldownEndTick = tag.getLong("thief_decoy_cd_end");
        thiefRuneCooldownEndTick = tag.getLong("thief_rune_cd_end");
        thiefLockpickCooldownEndTick = tag.getLong("thief_lockpick_cd_end");
        thiefEraseCooldownEndTick = tag.getLong("thief_erase_cd_end");
        apprenticeRelocateCooldownEndTick = tag.getLong("apprentice_relocate_cd_end");
        apprenticeLinkCooldownEndTick = tag.getLong("apprentice_link_cd_end");
        apprenticeMirrorCooldownEndTick = tag.getLong("apprentice_mirror_cd_end");
        apprenticeDivinationCooldownEndTick = tag.getLong("apprentice_divination_cd_end");
        apprenticeWardCooldownEndTick = tag.getLong("apprentice_ward_cd_end");
        psychPacifyCooldownEndTick = tag.getLong("psych_pacify_cd_end");
        psychShockCooldownEndTick = tag.getLong("psych_shock_cd_end");
        pyroSpearCooldownEndTick = tag.getLong("pyro_spear_cd_end");
        pyroRingCooldownEndTick = tag.getLong("pyro_ring_cd_end");
        lastRestRecoveryDay = tag.contains("last_rest_recovery_day")
                ? tag.getLong("last_rest_recovery_day") : Long.MIN_VALUE;
        m1TrialActive = tag.getBoolean("m1_trial_active");
        m1TrialStartTick = tag.contains("m1_trial_start_tick")
                ? tag.getLong("m1_trial_start_tick") : -1L;
        m1TrialElapsedTicks = tag.getLong("m1_trial_elapsed_ticks");
        m1TrialCampVisited = tag.getBoolean("m1_trial_camp_visited");
        m1TrialBestSequence = tag.contains("m1_trial_best_sequence")
                ? tag.getInt("m1_trial_best_sequence") : -1;
        m1TrialOccultKills = tag.getInt("m1_trial_occult_kills");
        m1TrialDeaths = tag.getInt("m1_trial_deaths");
        m1TrialRestRecoveries = tag.getInt("m1_trial_rest_recoveries");
        m1TrialCharmsConsumed = tag.getInt("m1_trial_charms_consumed");
        m1TrialActingEvents = tag.getInt("m1_trial_acting_events");
        m1TrialMaxPressure = tag.getFloat("m1_trial_max_pressure");
        m1TrialMaxPollution = tag.getFloat("m1_trial_max_pollution");
        m1TrialReconnects = tag.getInt("m1_trial_reconnects");
        m1TrialServerRestarts = tag.getInt("m1_trial_server_restarts");
        m1TrialDimensionChanges = tag.getInt("m1_trial_dimension_changes");
        m1TrialDeathRecoveries = tag.getInt("m1_trial_death_recoveries");
        m1TrialPendingReconnect = tag.getBoolean("m1_trial_pending_reconnect");
        m1TrialSessionId = tag.contains("m1_trial_session_id")
                ? tag.getString("m1_trial_session_id") : "";
        m1TrialCampReachedTick = optionalTrialTick(tag, "m1_trial_camp_reached_tick");
        m1TrialSequence9Tick = optionalTrialTick(tag, "m1_trial_sequence_9_tick");
        m1TrialSequence8Tick = optionalTrialTick(tag, "m1_trial_sequence_8_tick");
        m1TrialSequence7Tick = optionalTrialTick(tag, "m1_trial_sequence_7_tick");
        m1TrialFirstOccultKillTick = optionalTrialTick(
                tag, "m1_trial_first_occult_kill_tick");
        m1TrialFirstActingTick = optionalTrialTick(tag, "m1_trial_first_acting_tick");
        m1TrialRiskReachedTick = optionalTrialTick(tag, "m1_trial_risk_reached_tick");
        m1TrialIdentityAnchoredTick = optionalTrialTick(
                tag, "m1_trial_identity_anchored_tick");
        m1TrialReflectionCompletedTick = optionalTrialTick(
                tag, "m1_trial_reflection_completed_tick");
        m1TrialStreetLifeCompletedTick = optionalTrialTick(
                tag, "m1_trial_street_life_completed_tick");
        moneyPence = tag.getLong("money_pence");
        lastCityWorkDay = tag.contains("last_city_work_day")
                ? tag.getLong("last_city_work_day") : Long.MIN_VALUE;
        cityWorkShifts = tag.getInt("city_work_shifts");
        activeCommissionId = tag.contains("active_commission")
                ? tag.getString("active_commission") : "";
        activeQuestChainId = tag.contains("active_quest_chain")
                ? tag.getString("active_quest_chain") : "";
        activeQuestStep = tag.contains("active_quest_step")
                ? tag.getInt("active_quest_step") : -1;
        questObjectiveProgress = tag.getInt("quest_objective_progress");
        commissionAcceptedTick = tag.getLong("commission_accepted_tick");
        escortedReporterUuid = tag.contains("escorted_reporter_uuid")
                ? tag.getString("escorted_reporter_uuid") : "";
        questDefenseWaveSpawned = tag.getBoolean("quest_defense_wave_spawned");
        questDefenseNextTick = tag.getLong("quest_defense_next_tick");
        questResolutionRoute = tag.contains("quest_resolution_route")
                ? tag.getString("quest_resolution_route") : "";
        questResolutionReady = tag.getBoolean("quest_resolution_ready");
        schemaVersion = CURRENT_SCHEMA_VERSION;

        completedCommissions.clear();
        ListTag completed = tag.getList("completed_commissions", Tag.TAG_STRING);
        for (int i = 0; i < completed.size(); i++) {
            String rawId = completed.getString(i);
            ResourceLocation id = ResourceLocation.tryParse(rawId);
            if (id != null) {
                completedCommissions.add(id);
            } else {
                addOrphan("completed_commissions", "invalid_id",
                        StringTag.valueOf(rawId));
            }
        }

        commissionCooldowns.clear();
        CompoundTag commissionCooldownTag = tag.getCompound("commission_cooldowns");
        for (String key : commissionCooldownTag.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id != null) {
                commissionCooldowns.put(id, commissionCooldownTag.getLong(key));
            } else {
                CompoundTag payload = new CompoundTag();
                payload.putString("id", key);
                Tag value = commissionCooldownTag.get(key);
                if (value != null) payload.put("value", value.copy());
                addOrphan("commission_cooldowns", "invalid_id", payload);
            }
        }

        caseDebriefs.clear();
        Tag rawCaseDebriefs = tag.get("case_debriefs");
        if (rawCaseDebriefs != null && !(rawCaseDebriefs instanceof CompoundTag)) {
            addOrphan("case_debriefs", "invalid_container", rawCaseDebriefs.copy());
        } else if (rawCaseDebriefs instanceof CompoundTag caseDebriefTag) {
            for (String key : caseDebriefTag.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(key);
                Tag rawRecord = caseDebriefTag.get(key);
                if (id != null && rawRecord instanceof CompoundTag recordTag
                        && CaseDebriefRecord.isValid(recordTag)) {
                    caseDebriefs.put(id, CaseDebriefRecord.load(recordTag));
                    continue;
                }
                CompoundTag payload = new CompoundTag();
                payload.putString("id", key);
                if (rawRecord != null) payload.put("value", rawRecord.copy());
                addOrphan("case_debriefs",
                        id == null ? "invalid_id" : "invalid_record", payload);
            }
        }

        caseHypotheses.clear();
        Tag rawCaseHypotheses = tag.get("case_hypotheses");
        if (rawCaseHypotheses != null
                && !(rawCaseHypotheses instanceof CompoundTag)) {
            addOrphan("case_hypotheses", "invalid_container",
                    rawCaseHypotheses.copy());
        } else if (rawCaseHypotheses instanceof CompoundTag caseHypothesisTag) {
            for (String key : caseHypothesisTag.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(key);
                Tag rawRecord = caseHypothesisTag.get(key);
                if (id != null && rawRecord instanceof CompoundTag recordTag
                        && CaseHypothesisRecord.isValid(recordTag)) {
                    caseHypotheses.put(id,
                            CaseHypothesisRecord.load(recordTag));
                    continue;
                }
                CompoundTag payload = new CompoundTag();
                payload.putString("id", key);
                if (rawRecord != null) payload.put("value", rawRecord.copy());
                addOrphan("case_hypotheses",
                        id == null ? "invalid_id" : "invalid_record", payload);
            }
        }

        dynamicCaseHistory.clear();
        Tag rawDynamicCaseHistory = tag.get("dynamic_case_history");
        if (rawDynamicCaseHistory != null
                && (!(rawDynamicCaseHistory instanceof ListTag historyTag)
                        || !historyTag.isEmpty()
                        && historyTag.getElementType() != Tag.TAG_COMPOUND)) {
            addOrphan("dynamic_case_history", "invalid_container",
                    rawDynamicCaseHistory.copy());
        } else if (rawDynamicCaseHistory instanceof ListTag historyTag) {
            for (int index = 0; index < historyTag.size(); index++) {
                Tag rawEntry = historyTag.get(index);
                if (rawEntry instanceof CompoundTag entryTag
                        && DynamicCaseHistoryEntry.isValid(entryTag)) {
                    dynamicCaseHistory.add(
                            DynamicCaseHistoryEntry.load(entryTag));
                    continue;
                }
                addOrphan("dynamic_case_history", "invalid_record",
                        rawEntry.copy());
            }
        }

        knownKnowledge.clear();
        ListTag known = tag.getList("known_knowledge", Tag.TAG_STRING);
        for (int i = 0; i < known.size(); i++) {
            ResourceLocation rl = ResourceLocation.tryParse(known.getString(i));
            if (rl != null) {
                knownKnowledge.add(rl);
            } else {
                addOrphan("known_knowledge", "invalid_id",
                        StringTag.valueOf(known.getString(i)));
            }
        }

        actingHistory.clear();
        CompoundTag acting = tag.getCompound("acting_history");
        for (String key : acting.getAllKeys()) actingHistory.put(key, acting.getLong(key));

        actingCounters.clear();
        CompoundTag counters = tag.getCompound("acting_counters");
        for (String key : counters.getAllKeys()) actingCounters.put(key, counters.getInt(key));
        principleInsight = tag.getFloat("principle_insight");
        roleOveridentification = tag.getFloat("role_overidentification");
        actingReflectionCount = tag.getInt("acting_reflection_count");
        lastActingReflectionDay = tag.contains("last_acting_reflection_day")
                ? tag.getLong("last_acting_reflection_day") : Long.MIN_VALUE;
        identityAnchored = tag.getBoolean("identity_anchored");

        orgReputation.clear();
        CompoundTag rep = tag.getCompound("org_reputation");
        for (String key : rep.getAllKeys()) {
            ResourceLocation rl = ResourceLocation.tryParse(key);
            if (rl != null) {
                orgReputation.put(rl, rep.getInt(key));
            } else {
                CompoundTag payload = new CompoundTag();
                payload.putString("id", key);
                Tag value = rep.get(key);
                if (value != null) payload.put("value", value.copy());
                addOrphan("org_reputation", "invalid_id", payload);
            }
        }

        characteristicBundles.clear();
        Tag rawCharacteristics = tag.get("characteristic_bundles");
        if (rawCharacteristics != null && !(rawCharacteristics instanceof ListTag)) {
            addOrphan("characteristic_bundles", "invalid_container",
                    rawCharacteristics);
        }
        ListTag characteristics = tag.getList(
                "characteristic_bundles", Tag.TAG_COMPOUND);
        for (int index = 0; index < characteristics.size(); index++) {
            CompoundTag rawBundle = characteristics.getCompound(index);
            try {
                characteristicBundles.add(CharacteristicBundle.load(rawBundle));
            } catch (IllegalArgumentException ignored) {
                addOrphan("characteristic_bundles", "invalid_bundle", rawBundle);
            }
        }
        captureInvalidActiveQuestState();
        sanitize();
        resetDirtyTracking();
    }

    /** Detects mutations made through the legacy public fields. */
    public int refreshDirtySections() {
        for (PlayerDataSection section : PlayerDataSection.values()) {
            long currentFingerprint = fingerprint(section);
            int index = section.ordinal();
            if (sectionFingerprints[index] != currentFingerprint) {
                sectionFingerprints[index] = currentFingerprint;
                dirtySectionMask |= section.mask();
            }
        }
        return dirtySectionMask;
    }

    public int dirtySectionMask() {
        return refreshDirtySections();
    }

    public boolean isDirty(PlayerDataSection section) {
        return (refreshDirtySections() & section.mask()) != 0;
    }

    public void markDirty(PlayerDataSection section) {
        dirtySectionMask |= section.mask();
    }

    public void acknowledgeDirty(PlayerDataSection section) {
        refreshDirtySections();
        dirtySectionMask &= ~section.mask();
    }

    public void acknowledgeAllDirty() {
        refreshDirtySections();
        dirtySectionMask = 0;
    }

    private void resetDirtyTracking() {
        for (PlayerDataSection section : PlayerDataSection.values()) {
            sectionFingerprints[section.ordinal()] = fingerprint(section);
        }
        dirtySectionMask = PlayerDataSection.ALL_MASK;
    }

    private long fingerprint(PlayerDataSection section) {
        return switch (section) {
            case CORE -> coreFingerprint();
            case KNOWLEDGE -> knowledgeFingerprint();
            case SOCIAL -> socialFingerprint();
            case ENDGAME -> endgameFingerprint();
        };
    }

    private long coreFingerprint() {
        long hash = fingerprintSeed();
        hash = mix(hash, pathway);
        hash = mix(hash, sequence);
        hash = mix(hash, spirituality);
        hash = mix(hash, spiritualityMax);
        hash = mix(hash, digestion);
        hash = mix(hash, pollution);
        hash = mix(hash, insanityPressure);
        hash = mix(hash, potionQuality);
        hash = mix(hash, characteristicBundles);
        hash = mix(hash, orphanedEntries);
        hash = mix(hash, migrationBackups);
        hash = mix(hash, migrationHistory);
        hash = mix(hash, futureSchemaDetected);
        hash = mix(hash, spiritVisionActive);
        hash = mix(hash, divinationCooldownEndTick);
        hash = mix(hash, dangerIntuitionCooldownEndTick);
        hash = mix(hash, breakdownCooldownEndTick);
        hash = mix(hash, mentalTraumaEndTick);
        hash = mix(hash, emotionReadActive);
        hash = mix(hash, behaviorPredictionCooldownEndTick);
        hash = mix(hash, surfaceReadCooldownEndTick);
        hash = mix(hash, mentalSuggestionCooldownEndTick);
        hash = mix(hash, hunterTrackedTarget);
        hash = mix(hash, hunterTrackingStartTick);
        hash = mix(hash, hunterTrackingEndTick);
        hash = mix(hash, provokeCooldownEndTick);
        hash = mix(hash, enrageCooldownEndTick);
        hash = mix(hash, battleWillCooldownEndTick);
        hash = mix(hash, battleWillEndTick);
        hash = mix(hash, cardBladeCooldownEndTick);
        hash = mix(hash, clownDodgeCooldownEndTick);
        hash = mix(hash, clownDodgeCount);
        hash = mix(hash, expressionControlCooldownEndTick);
        hash = mix(hash, flameLeapCooldownEndTick);
        hash = mix(hash, flameLeapStrikeEndTick);
        hash = mix(hash, paperSubstituteCooldownEndTick);
        hash = mix(hash, paperSubstituteArmedEndTick);
        hash = mix(hash, paperSubstituteDimension);
        hash = mix(hash, paperSubstituteX);
        hash = mix(hash, paperSubstituteY);
        hash = mix(hash, paperSubstituteZ);
        hash = mix(hash, airBulletCooldownEndTick);
        hash = mix(hash, stageIllusionCooldownEndTick);
        hash = mix(hash, thiefPilferCooldownEndTick);
        hash = mix(hash, thiefEscapeCooldownEndTick);
        hash = mix(hash, apprenticeTrickCooldownEndTick);
        hash = mix(hash, apprenticeCopyCooldownEndTick);
        hash = mix(hash, thiefSwapCooldownEndTick);
        hash = mix(hash, thiefDecoyCooldownEndTick);
        hash = mix(hash, thiefRuneCooldownEndTick);
        hash = mix(hash, thiefLockpickCooldownEndTick);
        hash = mix(hash, thiefEraseCooldownEndTick);
        hash = mix(hash, apprenticeRelocateCooldownEndTick);
        hash = mix(hash, apprenticeLinkCooldownEndTick);
        hash = mix(hash, apprenticeMirrorCooldownEndTick);
        hash = mix(hash, apprenticeDivinationCooldownEndTick);
        hash = mix(hash, apprenticeWardCooldownEndTick);
        hash = mix(hash, psychPacifyCooldownEndTick);
        hash = mix(hash, psychShockCooldownEndTick);
        hash = mix(hash, pyroSpearCooldownEndTick);
        hash = mix(hash, pyroRingCooldownEndTick);
        hash = mix(hash, lastRestRecoveryDay);
        hash = mix(hash, principleInsight);
        hash = mix(hash, roleOveridentification);
        hash = mix(hash, actingReflectionCount);
        hash = mix(hash, lastActingReflectionDay);
        hash = mix(hash, identityAnchored);
        hash = mix(hash, schemaVersion);
        return hash;
    }

    private long knowledgeFingerprint() {
        long hash = fingerprintSeed();
        hash = mix(hash, knownKnowledge);
        hash = mix(hash, actingHistory);
        hash = mix(hash, actingCounters);
        return hash;
    }

    private long socialFingerprint() {
        long hash = fingerprintSeed();
        hash = mix(hash, moneyPence);
        hash = mix(hash, lastCityWorkDay);
        hash = mix(hash, cityWorkShifts);
        hash = mix(hash, activeCommissionId);
        hash = mix(hash, activeQuestChainId);
        hash = mix(hash, activeQuestStep);
        hash = mix(hash, questObjectiveProgress);
        hash = mix(hash, commissionAcceptedTick);
        hash = mix(hash, escortedReporterUuid);
        hash = mix(hash, questDefenseWaveSpawned);
        hash = mix(hash, questDefenseNextTick);
        hash = mix(hash, questResolutionRoute);
        hash = mix(hash, questResolutionReady);
        hash = mix(hash, completedCommissions);
        hash = mix(hash, commissionCooldowns);
        hash = mix(hash, caseDebriefs);
        hash = mix(hash, caseHypotheses);
        hash = mix(hash, dynamicCaseHistory);
        hash = mix(hash, orgReputation);
        return hash;
    }

    private long endgameFingerprint() {
        long hash = fingerprintSeed();
        hash = mix(hash, m1TrialActive);
        hash = mix(hash, m1TrialStartTick);
        hash = mix(hash, m1TrialElapsedTicks);
        hash = mix(hash, m1TrialCampVisited);
        hash = mix(hash, m1TrialBestSequence);
        hash = mix(hash, m1TrialOccultKills);
        hash = mix(hash, m1TrialDeaths);
        hash = mix(hash, m1TrialRestRecoveries);
        hash = mix(hash, m1TrialCharmsConsumed);
        hash = mix(hash, m1TrialActingEvents);
        hash = mix(hash, m1TrialMaxPressure);
        hash = mix(hash, m1TrialMaxPollution);
        hash = mix(hash, m1TrialReconnects);
        hash = mix(hash, m1TrialServerRestarts);
        hash = mix(hash, m1TrialDimensionChanges);
        hash = mix(hash, m1TrialDeathRecoveries);
        hash = mix(hash, m1TrialPendingReconnect);
        hash = mix(hash, m1TrialSessionId);
        hash = mix(hash, m1TrialCampReachedTick);
        hash = mix(hash, m1TrialSequence9Tick);
        hash = mix(hash, m1TrialSequence8Tick);
        hash = mix(hash, m1TrialSequence7Tick);
        hash = mix(hash, m1TrialFirstOccultKillTick);
        hash = mix(hash, m1TrialFirstActingTick);
        hash = mix(hash, m1TrialRiskReachedTick);
        hash = mix(hash, m1TrialIdentityAnchoredTick);
        hash = mix(hash, m1TrialReflectionCompletedTick);
        hash = mix(hash, m1TrialStreetLifeCompletedTick);
        return hash;
    }

    private static long fingerprintSeed() {
        return 0xcbf29ce484222325L;
    }

    private static long mix(long hash, Object value) {
        return (hash ^ Objects.hashCode(value)) * 0x100000001b3L;
    }

    private void loadMigrationMetadata(
            CompoundTag tag, PlayerMysteryDataFixer.MigrationResult migration) {
        orphanedEntries = loadCompoundTags(tag, "orphaned_entries");
        migrationBackups = loadCompoundTags(tag, "migration_backups");
        migrationHistory = loadCompoundTags(tag, "migration_history");
        for (CompoundTag orphanedEntry : migration.orphanedEntries()) {
            orphanedEntries.add(orphanedEntry.copy());
        }
        if (migration.backup() != null) {
            migrationBackups.add(migration.backup().copy());
            trimOldest(migrationBackups, MAX_MIGRATION_BACKUPS);
        }
        for (PlayerMysteryDataFixer.MigrationStep step : migration.appliedSteps()) {
            migrationHistory.add(step.save());
            trimOldest(migrationHistory, MAX_MIGRATION_HISTORY);
        }
        futureSchemaDetected = migration.futureSchema();
    }

    private void captureInvalidActiveQuestState() {
        boolean commissionValid = activeCommissionId.isBlank()
                || ResourceLocation.tryParse(activeCommissionId) != null;
        boolean questValid = activeQuestChainId.isBlank()
                || ResourceLocation.tryParse(activeQuestChainId) != null;
        boolean incompletePair = activeCommissionId.isBlank()
                != activeQuestChainId.isBlank();
        if (commissionValid && questValid && !incompletePair) return;
        CompoundTag payload = new CompoundTag();
        payload.putString("active_commission", activeCommissionId);
        payload.putString("active_quest_chain", activeQuestChainId);
        payload.putInt("active_quest_step", activeQuestStep);
        payload.putInt("quest_objective_progress", questObjectiveProgress);
        payload.putLong("commission_accepted_tick", commissionAcceptedTick);
        payload.putString("escorted_reporter_uuid", escortedReporterUuid);
        payload.putBoolean("quest_defense_wave_spawned", questDefenseWaveSpawned);
        payload.putLong("quest_defense_next_tick", questDefenseNextTick);
        payload.putString("quest_resolution_route", questResolutionRoute);
        payload.putBoolean("quest_resolution_ready", questResolutionReady);
        addOrphan("active_quest", "invalid_or_incomplete_id_pair", payload);
    }

    private void addOrphan(String section, String reason, Tag payload) {
        orphanedEntries.add(PlayerMysteryDataFixer.orphan(section, reason, payload));
    }

    private static ListTag saveCompoundTags(List<CompoundTag> values) {
        ListTag tags = new ListTag();
        for (CompoundTag value : values) {
            if (value != null) tags.add(value.copy());
        }
        return tags;
    }

    private static List<CompoundTag> loadCompoundTags(CompoundTag source,
                                                       String key) {
        List<CompoundTag> values = new ArrayList<>();
        ListTag tags = source.getList(key, Tag.TAG_COMPOUND);
        for (int index = 0; index < tags.size(); index++) {
            values.add(tags.getCompound(index).copy());
        }
        return values;
    }

    private static List<CompoundTag> copyCompoundTags(List<CompoundTag> source) {
        List<CompoundTag> copy = new ArrayList<>();
        for (CompoundTag value : source) {
            if (value != null) copy.add(value.copy());
        }
        return copy;
    }

    private static void trimOldest(List<CompoundTag> values, int maximum) {
        while (values.size() > maximum) values.remove(0);
    }

    private static long optionalTrialTick(CompoundTag tag, String key) {
        return tag.contains(key) ? tag.getLong(key) : -1L;
    }

    public int sanitize() {
        return PlayerMysteryDataSanitizer.sanitize(this);
    }
}
