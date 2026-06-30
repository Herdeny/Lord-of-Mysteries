package top.aurora.projectmystery.player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;

/**
 * 玩家非凡者数据（设计文档 §5.1）。
 *
 * NeoForge 1.21.1 通过 Attachments 存储玩家自定义数据；本类即附着的载荷。
 * 注意：Attachment 推荐使用不可变/可序列化结构 + Codec。此处为 M0 骨架，
 * 字段保持可变以便快速迭代，序列化通过下方 CODEC 完成。
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

    /**
     * M0 简化 CODEC：先序列化最核心的数值与途径/序列字段，
     * 知识/历史/声望等集合字段待 M1 数据系统完善后补全。
     */
    public static final Codec<PlayerMysteryData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.optionalFieldOf("pathway").forGetter(d ->
                    java.util.Optional.ofNullable(d.pathway)),
            Codec.INT.fieldOf("sequence").forGetter(d -> d.sequence),
            Codec.FLOAT.fieldOf("spirituality").forGetter(d -> d.spirituality),
            Codec.FLOAT.fieldOf("spirituality_max").forGetter(d -> d.spiritualityMax),
            Codec.FLOAT.fieldOf("digestion").forGetter(d -> d.digestion),
            Codec.FLOAT.fieldOf("pollution").forGetter(d -> d.pollution),
            Codec.FLOAT.fieldOf("insanity_pressure").forGetter(d -> d.insanityPressure),
            Codec.INT.fieldOf("schema_version").forGetter(d -> d.schemaVersion)
    ).apply(inst, (pathway, sequence, spi, spiMax, dig, pol, ins, ver) -> {
        PlayerMysteryData d = new PlayerMysteryData();
        d.pathway = pathway.orElse(null);
        d.sequence = sequence;
        d.spirituality = spi;
        d.spiritualityMax = spiMax;
        d.digestion = dig;
        d.pollution = pol;
        d.insanityPressure = ins;
        d.schemaVersion = ver;
        return d;
    }));
}
