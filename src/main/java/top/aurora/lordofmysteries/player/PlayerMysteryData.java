package top.aurora.lordofmysteries.player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

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

    public static final int CURRENT_SCHEMA_VERSION = 9;

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
    public long psychPacifyCooldownEndTick = 0L;
    public long psychShockCooldownEndTick = 0L;
    public long pyroSpearCooldownEndTick = 0L;
    public long pyroRingCooldownEndTick = 0L;
    public long lastRestRecoveryDay = Long.MIN_VALUE;
    public boolean m1TrialActive = false;
    public long m1TrialStartTick = 0L;
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

    // 知识系统。保存玩家已经解锁的知识条目 ID，供手册、仪式和配方门槛读取。
    public Set<ResourceLocation> knownKnowledge = new HashSet<>();

    // 扮演事件历史（事件ID → 最后触发的 gameTime）。
    // key 暂用 String 便于直接作为 CompoundTag 键，value 用服务器 gameTime 做冷却/衰减判断。
    public Map<String, Long> actingHistory = new HashMap<>();
    public Map<String, Integer> actingCounters = new HashMap<>();

    // 组织声望（组织ID → 声望值）。组织 ID 仍使用 ResourceLocation，支持数据包新增组织。
    public Map<ResourceLocation, Integer> orgReputation = new HashMap<>();

    // 序列化版本号（用于迁移）。未来字段改名或结构升级时，可根据旧版本补默认值。
    public int schemaVersion = CURRENT_SCHEMA_VERSION;

    /** 默认构造器需要保留，Capability Provider 和测试都会直接创建空数据。 */
    public PlayerMysteryData() {}

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
        this.knownKnowledge = new HashSet<>(src.knownKnowledge);
        this.actingHistory = new HashMap<>(src.actingHistory);
        this.actingCounters = new HashMap<>(src.actingCounters);
        this.orgReputation = new HashMap<>(src.orgReputation);
        this.schemaVersion = CURRENT_SCHEMA_VERSION;
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
        tag.putInt("schema_version", schemaVersion);

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

        CompoundTag rep = new CompoundTag();
        // ResourceLocation 不能直接作为 NBT 键，转成 namespace:path 字符串存储。
        orgReputation.forEach((id, v) -> rep.putInt(id.toString(), v));
        tag.put("org_reputation", rep);

        return tag;
    }

    /**
     * 从 NBT 读取玩家非凡者数据。
     *
     * <p>读取时尽量容错：ResourceLocation 使用 tryParse，遇到非法 ID 会跳过；新增字段
     * 使用 contains 判断并提供默认值，确保旧存档也能继续加载。
     */
    public void load(CompoundTag tag) {
        pathway = tag.contains("pathway") ? ResourceLocation.tryParse(tag.getString("pathway")) : null;
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
        psychPacifyCooldownEndTick = tag.getLong("psych_pacify_cd_end");
        psychShockCooldownEndTick = tag.getLong("psych_shock_cd_end");
        pyroSpearCooldownEndTick = tag.getLong("pyro_spear_cd_end");
        pyroRingCooldownEndTick = tag.getLong("pyro_ring_cd_end");
        lastRestRecoveryDay = tag.contains("last_rest_recovery_day")
                ? tag.getLong("last_rest_recovery_day") : Long.MIN_VALUE;
        m1TrialActive = tag.getBoolean("m1_trial_active");
        m1TrialStartTick = tag.getLong("m1_trial_start_tick");
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
        schemaVersion = CURRENT_SCHEMA_VERSION;

        knownKnowledge.clear();
        ListTag known = tag.getList("known_knowledge", Tag.TAG_STRING);
        for (int i = 0; i < known.size(); i++) {
            ResourceLocation rl = ResourceLocation.tryParse(known.getString(i));
            // 数据包或旧版本留下的非法 ID 不应阻止玩家进入世界。
            if (rl != null) knownKnowledge.add(rl);
        }

        actingHistory.clear();
        CompoundTag acting = tag.getCompound("acting_history");
        for (String key : acting.getAllKeys()) actingHistory.put(key, acting.getLong(key));

        actingCounters.clear();
        CompoundTag counters = tag.getCompound("acting_counters");
        for (String key : counters.getAllKeys()) actingCounters.put(key, counters.getInt(key));

        orgReputation.clear();
        CompoundTag rep = tag.getCompound("org_reputation");
        for (String key : rep.getAllKeys()) {
            ResourceLocation rl = ResourceLocation.tryParse(key);
            // 只恢复合法组织 ID；非法键静默丢弃，避免污染运行期数据结构。
            if (rl != null) orgReputation.put(rl, rep.getInt(key));
        }
    }
}
