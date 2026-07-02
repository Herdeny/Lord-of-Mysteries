package top.aurora.lordofmysteries.potion;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class SeerPotionItem extends Item {

    public static final ResourceLocation SEER_PATHWAY =
            ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, "seer");

    public SeerPotionItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack create(Item item, PotionQuality quality) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putString("potion_quality", quality.id());
        return stack;
    }

    public static PotionQuality getQuality(ItemStack stack) {
        return PotionQuality.fromId(stack.getOrCreateTag().getString("potion_quality"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && MysteryCapability.get(player).isExtraordinary()) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.potion.already_extraordinary"));
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof ServerPlayer player)) return stack;

        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.isExtraordinary()) return stack;

        PotionQuality quality = getQuality(stack);
        data.pathway = SEER_PATHWAY;
        data.sequence = 9;
        data.spiritualityMax = 122f;
        data.spirituality = 122f;
        data.digestion = 0f;
        data.insanityPressure = Math.min(100f, data.insanityPressure + quality.initialPressure());
        data.pollution = Math.min(100f, data.pollution + quality.initialPollution());
        data.potionQuality = quality.id();
        data.knownKnowledge.add(ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, "knowledge/seer_9_acting"));
        data.knownKnowledge.add(ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, "knowledge/spirit_vision"));
        data.knownKnowledge.add(ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, "knowledge/simple_divination"));

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1f, 0.8f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.potion.advanced",
                Component.translatable("quality.lord_of_mysteries." + quality.id()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        if (!player.getAbilities().instabuild) stack.shrink(1);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return getQuality(stack) == PotionQuality.PERFECT;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        PotionQuality quality = getQuality(stack);
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.potion.quality",
                Component.translatable("quality.lord_of_mysteries." + quality.id()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.potion.warning")
                .withStyle(ChatFormatting.DARK_RED));
    }
}
