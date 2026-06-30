package top.aurora.projectmystery;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

import top.aurora.projectmystery.player.MysteryAttachments;
import top.aurora.projectmystery.core.MysteryRegistries;
import top.aurora.projectmystery.core.config.ServerConfig;

/**
 * Project Mystery —《诡秘之主》Minecraft Mod 入口。
 *
 * 技术基线：Minecraft 1.21.1 · NeoForge 21.1.x · Java 21
 * 设计文档：docs/Project_Mystery_Mod_Design_Doc_v0.4
 *
 * M0 阶段目标：建立工程基础，验证核心技术路线（Attachment 数据系统、
 * 灵性同步、灵视渲染）。后续模块按设计文档第 16 节逐步填充。
 */
@Mod(ProjectMystery.MOD_ID)
public class ProjectMystery {

    public static final String MOD_ID = "project_mystery";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ProjectMystery(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("[Project Mystery] 初始化中 —— 在未知中承担风险，逐步成为非凡者。");

        // 注册 DeferredRegister（附件、物品、方块、实体等）
        MysteryAttachments.ATTACHMENT_TYPES.register(modEventBus);
        MysteryRegistries.registerAll(modEventBus);

        // 配置（§19.1 server.toml）
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        // 通用 setup
        modEventBus.addListener(this::commonSetup);

        // 全局事件总线（玩家 tick、实体生成等 game 事件）
        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[Project Mystery] commonSetup 完成。MOD_ID={}", MOD_ID);
    }
}
