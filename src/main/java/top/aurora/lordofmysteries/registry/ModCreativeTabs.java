package top.aurora.lordofmysteries.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import top.aurora.lordofmysteries.ProjectMystery;

/**
 * 创造模式物品栏（Forge 1.20.1）。把本 mod 的占位物品收纳到一个标签页，便于开发期测试。
 */
public final class ModCreativeTabs {

    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ProjectMystery.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + ProjectMystery.MOD_ID + ".main"))
                    .icon(() -> new ItemStack(ModItems.DIVINATION_CRYSTAL.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.SPIRIT_HERB.get());
                        output.accept(ModItems.DIVINATION_CRYSTAL.get());
                        output.accept(ModItems.MOONWATER.get());
                        output.accept(ModItems.CONTAMINATED_MIXTURE.get());
                        output.accept(ModItems.ETERNAL_MATCHBOX.get());
                        output.accept(ModItems.RITUAL_ALTAR_ITEM.get());
                        output.accept(ModItems.CRUCIBLE_ITEM.get());
                    })
                    .build());
}
