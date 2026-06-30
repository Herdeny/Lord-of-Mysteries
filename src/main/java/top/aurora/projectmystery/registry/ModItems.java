package top.aurora.projectmystery.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

import top.aurora.projectmystery.ProjectMystery;

/**
 * 物品注册。M0 放占位材料 + 方块物品，验证管线 + 创造模式标签。
 */
public final class ModItems {

    private ModItems() {}

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ProjectMystery.MOD_ID);

    // —— 占位材料（§4.1 ~40 材料的起点）——
    public static final DeferredItem<Item> SPIRIT_HERB =
            ITEMS.registerSimpleItem("spirit_herb");
    public static final DeferredItem<Item> DIVINATION_CRYSTAL =
            ITEMS.registerSimpleItem("divination_crystal");
    public static final DeferredItem<Item> MOONWATER =
            ITEMS.registerSimpleItem("moonwater");
    public static final DeferredItem<Item> CONTAMINATED_MIXTURE =
            ITEMS.registerSimpleItem("contaminated_mixture");

    // —— 封印物占位（§9.2 #1 永燃火柴盒）——
    public static final DeferredItem<Item> ETERNAL_MATCHBOX =
            ITEMS.registerSimpleItem("eternal_matchbox");

    // —— 方块物品 ——
    public static final DeferredItem<BlockItem> RITUAL_ALTAR_ITEM =
            ITEMS.registerSimpleBlockItem("ritual_altar", ModBlocks.RITUAL_ALTAR);
    public static final DeferredItem<BlockItem> CRUCIBLE_ITEM =
            ITEMS.registerSimpleBlockItem("crucible", ModBlocks.CRUCIBLE);
}
