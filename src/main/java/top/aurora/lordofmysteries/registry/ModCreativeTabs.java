package top.aurora.lordofmysteries.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import top.aurora.lordofmysteries.ProjectMystery;

/**
 * 创造模式物品栏（Forge 1.20.1）。收纳本 Mod 当前可用物品，便于开发与验收。
 *
 * <p>创造模式标签本身也是注册表对象。它引用的图标和展示物品都通过 RegistryObject
 * 延迟获取，这样不会在注册过早阶段强行初始化 Item。
 */
public final class ModCreativeTabs {

    /** 注册类不需要实例。 */
    private ModCreativeTabs() {}

    /** 创造模式标签注册表，命名空间仍使用 ProjectMystery.MOD_ID。 */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ProjectMystery.MOD_ID);

    /**
     * 主标签页：展示当前可用材料、魔药、封印物和功能方块。
     *
     * <p>翻译键为 {@code itemGroup.lord_of_mysteries.main}，对应 lang/*.json。
     */
    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ProjectMystery.MOD_ID + ".main"))
                    // 图标用占卜水晶，能在创造栏中快速识别本 Mod 标签页。
                    .icon(() -> new ItemStack(ModItems.DIVINATION_CRYSTAL.get()))
                    .displayItems((params, output) -> {
                        // 展示顺序按“材料 -> 封印物 -> 功能方块”排列，便于测试时扫描。
                        output.accept(ModItems.SPIRIT_HERB.get());
                        output.accept(ModItems.DIVINATION_CRYSTAL.get());
                        output.accept(ModItems.MOONWATER.get());
                        output.accept(ModItems.PURE_WATER.get());
                        output.accept(ModItems.WHITE_CANDLE.get());
                        output.accept(ModItems.DEEP_GRAY_SPIRIT_TEAR.get());
                        output.accept(ModItems.HEATHER.get());
                        output.accept(ModItems.SPIRIT_ALCOHOL.get());
                        output.accept(ModItems.SHAPESHIFTER_SERPENT_GLAND.get());
                        output.accept(ModItems.ASH_POWDER.get());
                        output.accept(ModItems.SILVER_FILINGS.get());
                        output.accept(ModItems.SPIRIT_SALT.get());
                        output.accept(ModItems.ASHEN_THREAD.get());
                        output.accept(ModItems.BLANK_MANUSCRIPT.get());
                        output.accept(ModItems.MYSTIC_INK.get());
                        output.accept(ModItems.FORMULA_FRAGMENT.get());
                        output.accept(ModItems.NEWSPAPER.get());
                        output.accept(ModItems.MYSTIC_PLAYING_CARDS.get());
                        output.accept(ModItems.PAPER_FIGURINE.get());
                        output.accept(ModItems.SHAPESHIFTER_SERPENT_SPAWN_EGG.get());
                        output.accept(ModItems.SPIRIT_WISP_SPAWN_EGG.get());
                        output.accept(ModItems.ASHEN_PUPPET_SPAWN_EGG.get());
                        output.accept(ModItems.CONTAMINATED_MIXTURE.get());
                        output.accept(ModItems.BROKEN_CHARACTERISTIC.get());
                        output.accept(ModItems.INVESTIGATOR_NOTES.get());
                        output.accept(ModItems.INVESTIGATOR_COMPASS.get());
                        output.accept(ModItems.CALMING_INCENSE.get());
                        output.accept(ModItems.SPIRIT_LANTERN.get());
                        output.accept(ModItems.PROTECTIVE_CHARM.get());
                        output.accept(ModItems.SEER_POTION_9.get());
                        output.accept(ModItems.SEER_POTION_8.get());
                        output.accept(ModItems.SEER_POTION_7.get());
                        output.accept(ModItems.SPECTATOR_POTION_9.get());
                        output.accept(ModItems.SPECTATOR_POTION_8.get());
                        output.accept(ModItems.HUNTER_POTION_9.get());
                        output.accept(ModItems.HUNTER_POTION_8.get());
                        output.accept(ModItems.HUNTER_SNARE_ITEM.get());
                        output.accept(ModItems.ETERNAL_MATCHBOX.get());
                        output.accept(ModItems.RITUAL_ALTAR_ITEM.get());
                        output.accept(ModItems.RITUAL_CHALK_MARK_ITEM.get());
                        output.accept(ModItems.CRUCIBLE_ITEM.get());
                    })
                    .build());
}
