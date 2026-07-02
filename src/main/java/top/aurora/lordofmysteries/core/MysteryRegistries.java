package top.aurora.lordofmysteries.core;

import net.minecraftforge.eventbus.api.IEventBus;

import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.registry.ModBlocks;
import top.aurora.lordofmysteries.registry.ModBlockEntities;
import top.aurora.lordofmysteries.registry.ModCreativeTabs;
import top.aurora.lordofmysteries.registry.ModEntities;

/**
 * 注册聚合入口（设计文档 §16.1 core 模块）。
 * 把各 DeferredRegister 挂到 mod 事件总线，集中管理注册顺序。
 *
 * <p>Forge 推荐使用 {@code DeferredRegister} 在加载期声明注册项。每个具体注册类
 * 只保存自己的注册表和 RegistryObject，本类负责把它们一次性接到入口类传入的
 * modEventBus 上。这样新增注册类别时，只需要在这里补一行 register 调用。
 */
public final class MysteryRegistries {

    /** 工具类不应该被实例化。 */
    private MysteryRegistries() {}

    /**
     * 注册所有本 Mod 的 DeferredRegister。
     *
     * @param modEventBus 当前 Mod 的加载期事件总线，来自 FMLJavaModLoadingContext
     */
    public static void registerAll(IEventBus modEventBus) {
        // 方块先于方块物品声明更直观；RegistryObject 会延迟解析，实际顺序由 Forge 管理。
        ModBlocks.BLOCKS.register(modEventBus);
        // 物品包含普通材料，也包含引用 ModBlocks 的 BlockItem。
        ModItems.ITEMS.register(modEventBus);
        // M0 只有占位注册表，M1 会在这里承载坩埚等方块实体类型。
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        // 创造模式标签会引用物品图标和展示列表，因此放在物品注册之后阅读更顺手。
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
    }
}
