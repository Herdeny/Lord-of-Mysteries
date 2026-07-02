package top.aurora.lordofmysteries.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.potion.SeerPotionItem;
import top.aurora.lordofmysteries.artifact.EternalMatchboxItem;

/**
 * 物品注册（Forge 1.20.1）。M0 放占位材料 + 方块物品，验证管线 + 创造模式标签。
 *
 * <p>本类只负责“声明有哪些物品”。物品行为、右键逻辑、能力触发等应放到对应
 * Item 子类或事件处理器中，避免注册类承担过多业务逻辑。
 */
public final class ModItems {

    /** 注册类只提供静态 RegistryObject，不需要实例。 */
    private ModItems() {}

    /**
     * 本 Mod 的物品注册表。所有注册名都会自动落在 ProjectMystery.MOD_ID 命名空间下，
     * 例如 {@code spirit_herb} 会成为 {@code lord_of_mysteries:spirit_herb}。
     */
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ProjectMystery.MOD_ID);

    /**
     * 创建无特殊行为的普通物品。
     *
     * <p>M0 阶段很多材料只需要能注册、能显示、能被配方或数据 JSON 引用。等某个
     * 物品需要耐久、右键、NBT 或发光效果时，再替换为专门的 Item 子类。
     */
    private static RegistryObject<Item> simple(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    // —— 占位材料（§4.1 ~40 材料的起点）——
    // 基础灵性材料。后续可作为采集、掉落、交易、魔药制作的共同输入。
    public static final RegistryObject<Item> SPIRIT_HERB = simple("spirit_herb");

    // 占卜和序列 9 魔药用材料。M1/M2 可接入占卜系统或仪式聚焦逻辑。
    public static final RegistryObject<Item> DIVINATION_CRYSTAL = simple("divination_crystal");

    // 月相/夜晚相关材料，占位给魔药品质加成和仪式媒介使用。
    public static final RegistryObject<Item> MOONWATER = simple("moonwater");
    public static final RegistryObject<Item> PURE_WATER = simple("pure_water");
    public static final RegistryObject<Item> WHITE_CANDLE = simple("white_candle");

    // 制作失败或污染反应的产物，用于验证失败结果和污染系统链路。
    public static final RegistryObject<Item> CONTAMINATED_MIXTURE = simple("contaminated_mixture");
    public static final RegistryObject<Item> BROKEN_CHARACTERISTIC = ITEMS.register(
            "broken_characteristic",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> INVESTIGATOR_NOTES = ITEMS.register(
            "investigator_notes",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SEER_POTION_9 = ITEMS.register("seer_potion_9",
            () -> new SeerPotionItem(new Item.Properties()));

    // —— 封印物占位（§9.2 #1 永燃火柴盒）——
    // 目前只是普通物品；封印物主动效果、负面代价和封印状态会在 artifact 模块实现。
    public static final RegistryObject<Item> ETERNAL_MATCHBOX = ITEMS.register("eternal_matchbox",
            () -> new EternalMatchboxItem(new Item.Properties().durability(16).rarity(Rarity.RARE)));

    // —— 方块物品 ——
    // BlockItem 让方块可以以物品形式存在于背包和创造模式标签中。
    // 这里通过 RegistryObject.get() 关联方块，Forge 会在合适阶段解析真实 Block 实例。
    public static final RegistryObject<Item> RITUAL_ALTAR_ITEM = ITEMS.register("ritual_altar",
            () -> new BlockItem(ModBlocks.RITUAL_ALTAR.get(), new Item.Properties()));

    // 坩埚方块物品；未来打开 GUI、保存配方状态的是方块实体，不是这个 BlockItem。
    public static final RegistryObject<Item> CRUCIBLE_ITEM = ITEMS.register("crucible",
            () -> new BlockItem(ModBlocks.CRUCIBLE.get(), new Item.Properties()));
}
