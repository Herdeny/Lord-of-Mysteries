package top.aurora.lordofmysteries.characteristic;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import top.aurora.lordofmysteries.knowledge.KnowledgeText;

public final class BrokenCharacteristicItem extends Item {

    public BrokenCharacteristicItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        CharacteristicConservationService.readStack(stack).ifPresentOrElse(
                bundle -> {
                    tooltip.add(Component.translatable(
                            "tooltip.lord_of_mysteries.characteristic.pathway",
                            Component.translatable(
                                    KnowledgeText.pathwayTranslationKey(
                                            bundle.pathway().toString())))
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                    tooltip.add(Component.translatable(
                            "tooltip.lord_of_mysteries.characteristic.layers",
                            bundle.highestSequence(), bundle.layers().size())
                            .withStyle(ChatFormatting.GRAY));
                    tooltip.add(Component.translatable(
                            "tooltip.lord_of_mysteries.characteristic.corruption",
                            Math.round(bundle.corruption()))
                            .withStyle(ChatFormatting.DARK_RED));
                },
                () -> tooltip.add(Component.translatable(
                        "tooltip.lord_of_mysteries.characteristic.fragment")
                        .withStyle(ChatFormatting.DARK_GRAY)));
    }
}
