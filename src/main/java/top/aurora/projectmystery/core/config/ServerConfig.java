package top.aurora.projectmystery.core.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 服务端配置（设计文档 §19.1 server.toml）。
 * M0 先声明核心平衡开关；随系统落地逐项接线到对应 Handler。
 */
public final class ServerConfig {

    private ServerConfig() {}

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue SPIRITUALITY_REGEN_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue DIGESTION_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue POLLUTION_RATE_MULTIPLIER;
    public static final ModConfigSpec.ConfigValue<String> BREAKDOWN_MODE; // recoverable / permanent / death
    public static final ModConfigSpec.BooleanValue PVP_MENTAL_ABILITIES;
    public static final ModConfigSpec.BooleanValue EXPLOSION_BLOCK_DAMAGE;
    public static final ModConfigSpec.BooleanValue GRAYFOG_ENABLED;
    public static final ModConfigSpec.BooleanValue WORLD_EVENTS_ENABLED;
    public static final ModConfigSpec.BooleanValue SHOW_EXACT_DIGESTION;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("gameplay");

        SPIRITUALITY_REGEN_MULTIPLIER = b.comment("灵性恢复倍率（默认 1.0）")
                .defineInRange("spirituality_regen_multiplier", 1.0, 0.0, 10.0);
        DIGESTION_MULTIPLIER = b.comment("消化速度倍率（默认 1.0）")
                .defineInRange("digestion_multiplier", 1.0, 0.0, 10.0);
        POLLUTION_RATE_MULTIPLIER = b.comment("污染积累速度倍率（默认 1.0）")
                .defineInRange("pollution_rate_multiplier", 1.0, 0.0, 10.0);
        BREAKDOWN_MODE = b.comment("失控结局模式：recoverable / permanent / death")
                .define("breakdown_mode", "recoverable");
        PVP_MENTAL_ABILITIES = b.comment("是否允许 PvP 精神能力")
                .define("pvp_mental_abilities", true);
        EXPLOSION_BLOCK_DAMAGE = b.comment("爆燃陷阱是否破坏方块")
                .define("explosion_block_damage", false);
        GRAYFOG_ENABLED = b.comment("灰雾空间开关")
                .define("grayfog_enabled", true);
        WORLD_EVENTS_ENABLED = b.comment("世界事件开关")
                .define("world_events_enabled", true);
        SHOW_EXACT_DIGESTION = b.comment("是否显示精确消化数值（调试用）")
                .define("show_exact_digestion", false);

        b.pop();
        SPEC = b.build();
    }
}
