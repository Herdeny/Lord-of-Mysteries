package top.aurora.lordofmysteries.core.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 服务端配置（Forge 1.20.1，设计文档 §19.1 server.toml）。
 * M0 先声明核心平衡开关；随系统落地逐项接线到对应 Handler。
 *
 * <p>ForgeConfigSpec 会负责校验范围、写入默认值和生成注释。这里使用 SERVER 配置，
 * 意味着这些选项应由服务端或存档控制，客户端只读取服务端最终生效的规则。
 */
public final class ServerConfig {

    /** 配置声明类只提供静态字段，不保存运行期状态。 */
    private ServerConfig() {}

    /** Forge 注册配置时需要的完整 spec，由静态初始化块构建。 */
    public static final ForgeConfigSpec SPEC;

    /** 灵性自然恢复倍率；最终恢复值 = 基础恢复值 * 该倍率。 */
    public static final ForgeConfigSpec.DoubleValue SPIRITUALITY_REGEN_MULTIPLIER;

    /** 扮演消化倍率；后续 ActingTracker 计算消化收益时读取。 */
    public static final ForgeConfigSpec.DoubleValue DIGESTION_MULTIPLIER;

    /** 污染增长倍率；仪式失败、封印物代价、环境污染会统一乘以该倍率。 */
    public static final ForgeConfigSpec.DoubleValue POLLUTION_RATE_MULTIPLIER;

    /** 失控结局策略：recoverable 可恢复，permanent 永久惩罚，death 直接死亡。 */
    public static final ForgeConfigSpec.ConfigValue<String> BREAKDOWN_MODE; // recoverable / permanent / death

    /** 是否允许玩家对玩家使用精神类能力；服务器可用它控制 PVP 强度。 */
    public static final ForgeConfigSpec.BooleanValue PVP_MENTAL_ABILITIES;

    /** 仪式爆燃、封印物事故等爆炸效果是否破坏方块。 */
    public static final ForgeConfigSpec.BooleanValue EXPLOSION_BLOCK_DAMAGE;

    /** 灰雾空间总开关；关闭后相关维度/会话逻辑应整体禁用。 */
    public static final ForgeConfigSpec.BooleanValue GRAYFOG_ENABLED;

    /** 世界事件总开关；关闭后随机事件和大型阶段事件不应调度。 */
    public static final ForgeConfigSpec.BooleanValue WORLD_EVENTS_ENABLED;

    /** 调试用开关：是否向玩家展示精确消化数值，而不是模糊描述。 */
    public static final ForgeConfigSpec.BooleanValue SHOW_EXACT_DIGESTION;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        // push/pop 会在生成的 toml 中形成 [gameplay] 分组，便于服务器管理员查找。
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

        // 所有配置项必须在 build() 前定义；build() 后 spec 就会被冻结供 Forge 使用。
        SPEC = b.build();
    }
}
