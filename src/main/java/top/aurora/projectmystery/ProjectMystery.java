package top.aurora.projectmystery;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.common.MinecraftForge;

import top.aurora.projectmystery.core.MysteryRegistries;
import top.aurora.projectmystery.core.config.ServerConfig;

/**
 * Project Mystery —《诡秘之主》Minecraft Mod 入口。
 *
 * 技术基线：Minecraft 1.20.1 · Forge 47.4.x · Java 17
 * 设计文档：docs/Project_Mystery_Mod_Design_Doc_v0.4
 *
 * M0 阶段目标：建立工程基础，验证核心技术路线（玩家数据 Capability、
 * 灵性同步、灵视渲染）。后续模块按设计文档第 16 节逐步填充。
 *
 * 注：1.20.1 没有 NeoForge 的 Attachments API，玩家数据走 Forge Capability。
 */
@Mod(ProjectMystery.MOD_ID)
public class ProjectMystery {

    public static final String MOD_ID = "project_mystery";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ProjectMystery() {
        LOGGER.info("[Project Mystery] 初始化中 —— 在未知中承担风险，逐步成为非凡者。");

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册 DeferredRegister（物品、方块、实体、方块实体等）
        MysteryRegistries.registerAll(modEventBus);

        // 通用 setup
        modEventBus.addListener(this::commonSetup);

        // 配置（§19.1 server.toml）
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        // 全局事件总线（玩家 tick、能力检定、Capability 附着等 game 事件）
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("[Project Mystery] commonSetup 完成。MOD_ID={}", MOD_ID);
    }
}
