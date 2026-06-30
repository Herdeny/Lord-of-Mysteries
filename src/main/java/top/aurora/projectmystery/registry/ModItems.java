package top.aurora.projectmystery.registry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import top.aurora.projectmystery.ProjectMystery;

/**
 * 物品注册（Forge 1.20.1）。M0 放占位材料 + 方块物品，验证管线 + 创造模式标签。
 */
public final class ModItems {

    private ModItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ProjectMystery.MOD_ID);

    private static RegistryObject<Item> simple(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    // —— 占位材料（§4.1 ~40 材料的起点）——
    public static final RegistryObject<Item> SPIRIT_HERB = simple("spirit_herb");
    public static final RegistryObject<Item> DIVINATION_CRYSTAL = simple("divination_crystal");
    public static final RegistryObject<Item> MOONWATER = simple("moonwater");
    public static final RegistryObject<Item> CONTAMINATED_MIXTURE = simple("contaminated_mixture");

    // —— 封印物占位（§9.2 #1 永燃火柴盒）——
    public static final RegistryObject<Item> ETERNAL_MATCHBOX = simple("eternal_matchbox");

    // —— 方块物品 ——
    public static final RegistryObject<Item> RITUAL_ALTAR_ITEM = ITEMS.register("ritual_altar",
            () -> new BlockItem(ModBlocks.RITUAL_ALTAR.get(), new Item.Properties()));
    public static final RegistryObject<Item> CRUCIBLE_ITEM = ITEMS.register("crucible",
            () -> new BlockItem(ModBlocks.CRUCIBLE.get(), new Item.Properties()));
}
