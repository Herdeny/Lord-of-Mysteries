package top.aurora.projectmystery.player;

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
 */
public class PlayerMysteryData {

    // 途径 & 序列
    public ResourceLocation pathway = null; // null = 普通人
    public int sequence = -1;               // -1 = 未入途径

    // 核心数值（§5）
    public float spirituality = 0f;
    public float spiritualityMax = 100f;
    public float digestion = 0f;            // 0-100%
    public float pollution = 0f;            // 0-100
    public float insanityPressure = 0f;     // 0-100

    // 知识系统
    public Set<ResourceLocation> knownKnowledge = new HashSet<>();

    // 扮演事件历史（事件ID → 最后触发的 gameTime）
    public Map<String, Long> actingHistory = new HashMap<>();

    // 组织声望（组织ID → 声望值）
    public Map<ResourceLocation, Integer> orgReputation = new HashMap<>();

    // 序列化版本号（用于迁移）
    public int schemaVersion = 1;

    public PlayerMysteryData() {}

    public boolean isExtraordinary() {
        return pathway != null && sequence >= 0;
    }

    /** 死亡/跨维度时把数据拷贝到新实体（§5：默认保留非凡者身份）。 */
    public void copyFrom(PlayerMysteryData src) {
        this.pathway = src.pathway;
        this.sequence = src.sequence;
        this.spirituality = src.spirituality;
        this.spiritualityMax = src.spiritualityMax;
        this.digestion = src.digestion;
        this.pollution = src.pollution;
        this.insanityPressure = src.insanityPressure;
        this.knownKnowledge = new HashSet<>(src.knownKnowledge);
        this.actingHistory = new HashMap<>(src.actingHistory);
        this.orgReputation = new HashMap<>(src.orgReputation);
        this.schemaVersion = src.schemaVersion;
    }

    // —— NBT 序列化 ——

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (pathway != null) tag.putString("pathway", pathway.toString());
        tag.putInt("sequence", sequence);
        tag.putFloat("spirituality", spirituality);
        tag.putFloat("spirituality_max", spiritualityMax);
        tag.putFloat("digestion", digestion);
        tag.putFloat("pollution", pollution);
        tag.putFloat("insanity_pressure", insanityPressure);
        tag.putInt("schema_version", schemaVersion);

        ListTag known = new ListTag();
        for (ResourceLocation k : knownKnowledge) known.add(StringTag.valueOf(k.toString()));
        tag.put("known_knowledge", known);

        CompoundTag acting = new CompoundTag();
        actingHistory.forEach(acting::putLong);
        tag.put("acting_history", acting);

        CompoundTag rep = new CompoundTag();
        orgReputation.forEach((id, v) -> rep.putInt(id.toString(), v));
        tag.put("org_reputation", rep);

        return tag;
    }

    public void load(CompoundTag tag) {
        pathway = tag.contains("pathway") ? ResourceLocation.tryParse(tag.getString("pathway")) : null;
        sequence = tag.getInt("sequence");
        spirituality = tag.getFloat("spirituality");
        spiritualityMax = tag.contains("spirituality_max") ? tag.getFloat("spirituality_max") : 100f;
        digestion = tag.getFloat("digestion");
        pollution = tag.getFloat("pollution");
        insanityPressure = tag.getFloat("insanity_pressure");
        schemaVersion = tag.contains("schema_version") ? tag.getInt("schema_version") : 1;

        knownKnowledge.clear();
        ListTag known = tag.getList("known_knowledge", Tag.TAG_STRING);
        for (int i = 0; i < known.size(); i++) {
            ResourceLocation rl = ResourceLocation.tryParse(known.getString(i));
            if (rl != null) knownKnowledge.add(rl);
        }

        actingHistory.clear();
        CompoundTag acting = tag.getCompound("acting_history");
        for (String key : acting.getAllKeys()) actingHistory.put(key, acting.getLong(key));

        orgReputation.clear();
        CompoundTag rep = tag.getCompound("org_reputation");
        for (String key : rep.getAllKeys()) {
            ResourceLocation rl = ResourceLocation.tryParse(key);
            if (rl != null) orgReputation.put(rl, rep.getInt(key));
        }
    }
}
