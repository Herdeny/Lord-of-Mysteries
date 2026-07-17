package top.aurora.lordofmysteries;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.MinecraftForge;

import top.aurora.lordofmysteries.core.MysteryRegistries;
import top.aurora.lordofmysteries.core.config.ServerConfig;
import top.aurora.lordofmysteries.network.PMNetwork;
import top.aurora.lordofmysteries.registry.ModEntities;

/**
 * Project Mystery —《诡秘之主》Minecraft Mod 入口。
 *
 * 技术基线：Minecraft 1.20.1 · Forge 47.4.x · Java 17
 * 设计基线：docs/DESIGN_BASELINE.md（Project Mystery v0.8）
 *
 * 当前按 v0.8 路线推进 M1 占卜家序列 9-7 验收，并保留 M2 数据与内容预研；
 * 完整门禁由 roadmap.json 定义。
 *
 * 注：1.20.1 没有 NeoForge 的 Attachments API，玩家数据走 Forge Capability。
 *
 * <p>Forge 会在扫描到 {@link Mod} 注解后实例化本类。构造函数里的代码只负责
 * 注册“加载期”需要知道的内容，例如 DeferredRegister、配置和生命周期回调；真正会
 * 频繁运行的玩家 tick、Capability 事件等，放在各自的事件订阅类中，避免入口类变成
 * 上帝对象。
 */
@Mod(ProjectMystery.MOD_ID)
public class ProjectMystery {

    /**
     * Mod 的稳定命名空间。这个值必须与 gradle.properties 里的 mod_id 一致，
     * 同时也是资源路径 assets/lord_of_mysteries 与 data/lord_of_mysteries 的命名空间。
     */
    public static final String MOD_ID = "lord_of_mysteries";

    /**
     * 全项目共享日志器。Forge/Minecraft 会把这里的输出接入 run/logs/latest.log，
     * 后续调试注册流程、数据包读取、能力触发时优先使用它。
     */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Mod 加载入口。
     *
     * <p>这里有两个事件总线概念：
     * <ul>
     *   <li>modEventBus：加载期总线，用于注册物品、方块、配置、setup 等。</li>
     *   <li>MinecraftForge.EVENT_BUS：游戏期总线，用于玩家 tick、实体事件、交互事件等。</li>
     * </ul>
     */
    public ProjectMystery(FMLJavaModLoadingContext loadingContext) {
        LOGGER.info("[Project Mystery] 初始化中 —— 在未知中承担风险，逐步成为非凡者。");

        // 取得当前 Mod 专属的加载期事件总线；DeferredRegister 必须挂在这条总线上。
        IEventBus modEventBus = loadingContext.getModEventBus();

        // 注册 DeferredRegister（物品、方块、方块实体、创造模式标签等）。
        MysteryRegistries.registerAll(modEventBus);

        // 通用 setup 会在注册阶段之后触发，适合做网络包、能力系统等一次性初始化。
        modEventBus.addListener(this::commonSetup);

        // 服务端配置会生成在 serverconfig 或 world/serverconfig 中，随存档/服务器生效。
        loadingContext.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        // 把入口实例注册到游戏期事件总线。当前类暂未声明 @SubscribeEvent 方法，
        // 保留这行是为了后续需要入口级游戏事件时无需调整加载结构。
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Forge 通用初始化阶段回调。
     *
     * <p>注意：此时注册表已经建立，但部分跨线程任务应通过 event.enqueueWork(...)
     * 安排到主线程执行。当前在此注册网络协议并输出初始化状态。
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        // 网络包必须在主线程注册（SimpleChannel 内部会做线程检查）。
        event.enqueueWork(() -> {
            PMNetwork.register();
            ModEntities.registerSpawnPlacements();
        });
        LOGGER.info("[Project Mystery] commonSetup 完成。MOD_ID={}", MOD_ID);
    }
}
