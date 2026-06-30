package top.aurora.projectmystery.core;

import net.neoforged.bus.api.IEventBus;

import top.aurora.projectmystery.registry.ModItems;
import top.aurora.projectmystery.registry.ModBlocks;
import top.aurora.projectmystery.registry.ModBlockEntities;
import top.aurora.projectmystery.registry.ModCreativeTabs;

/**
 * 注册聚合入口（设计文档 §16.1 core 模块）。
 * 把各 DeferredRegister 挂到 mod 事件总线，集中管理注册顺序。
 */
public final class MysteryRegistries {

    private MysteryRegistries() {}

    public static void registerAll(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
    }
}
