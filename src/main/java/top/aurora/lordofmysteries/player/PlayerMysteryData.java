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

    // 知识系统。保存玩家已经解锁的知识条目 ID，供手册、仪式和配方门槛读取。
    public Set<ResourceLocation> knownKnowledge = new HashSet<>();

    // 扮演事件历史（事件ID → 最后触发的 gameTime）。
    // key 暂用 String 便于直接作为 CompoundTag 键，value 用服务器 gameTime 做冷却/衰减判断。
    public Map<String, Long> actingHistory = new HashMap<>();

    // 组织声望（组织ID → 声望值）。组织 ID 仍使用 ResourceLocation，支持数据包新增组织。
    public Map<ResourceLocation, Integer> orgReputation = new HashMap<>();

    // 序列化版本号（用于迁移）。未来字段改名或结构升级时，可根据旧版本补默认值。
    public int schemaVersion = 1;

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
        this.knownKnowledge = new HashSet<>(src.knownKnowledge);
        this.actingHistory = new HashMap<>(src.actingHistory);
        this.orgReputation = new HashMap<>(src.orgReputation);
        this.schemaVersion = src.schemaVersion;
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
        tag.putInt("schema_version", schemaVersion);

        ListTag known = new ListTag();
        // ListTag 只能存 Tag 对象，因此 ResourceLocation 统一序列化成字符串。
        for (ResourceLocation k : knownKnowledge) known.add(StringTag.valueOf(k.toString()));
        tag.put("known_knowledge", known);

        CompoundTag acting = new CompoundTag();
        // CompoundTag 的键天然是字符串，适合保存“事件 ID -> 最后触发时间”的稀疏表。
        actingHistory.forEach(acting::putLong);
        tag.put("acting_history", acting);

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
        schemaVersion = tag.contains("schema_version") ? tag.getInt("schema_version") : 1;

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

        orgReputation.clear();
        CompoundTag rep = tag.getCompound("org_reputation");
        for (String key : rep.getAllKeys()) {
            ResourceLocation rl = ResourceLocation.tryParse(key);
            // 只恢复合法组织 ID；非法键静默丢弃，避免污染运行期数据结构。
            if (rl != null) orgReputation.put(rl, rep.getInt(key));
        }
    }
}
