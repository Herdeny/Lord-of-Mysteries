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

public final class M2PathwayPotionItem extends Item {

    public enum Pathway {
        THIEF("thief", 108f, 10f),
        APPRENTICE("apprentice", 115f, 9f);

        private final ResourceLocation id;
        private final float spiritualityMax;
        private final float drinkingPressure;

        Pathway(String path, float spiritualityMax, float drinkingPressure) {
            this.id = ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, path);
            this.spiritualityMax = spiritualityMax;
            this.drinkingPressure = drinkingPressure;
        }

        public ResourceLocation id() {
            return id;
        }

        public String path() {
            return id.getPath();
        }
    }

    private final Pathway pathway;

    public M2PathwayPotionItem(Properties properties, Pathway pathway) {
        super(properties.stacksTo(1));
        this.pathway = pathway;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                   InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()
                && !PotionAdvancementRules.canAdvance(
                        MysteryCapability.get(player).pathway == null
                                ? null : MysteryCapability.get(player).pathway.toString(),
                        MysteryCapability.get(player).sequence,
                        MysteryCapability.get(player).digestion,
                        pathway.id().toString(),
                        9)) {
            player.sendSystemMessage(Component.translatable(
                    "message.lord_of_mysteries.potion.incompatible", 9));
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level,
                                      LivingEntity livingEntity) {
        if (!(livingEntity instanceof ServerPlayer player)) return stack;

        PlayerMysteryData data = MysteryCapability.get(player);
        if (!PotionAdvancementRules.canAdvance(
                data.pathway == null ? null : data.pathway.toString(),
                data.sequence,
                data.digestion,
                pathway.id().toString(),
                9)) {
            return stack;
        }

        PotionQuality quality = SeerPotionItem.getQuality(stack);
        data.pathway = pathway.id();
        data.sequence = 9;
        data.spiritualityMax = pathway.spiritualityMax;
        data.spirituality = data.spiritualityMax;
        data.digestion = 0f;
        data.insanityPressure = Math.min(100f,
                data.insanityPressure
                        + Math.max(pathway.drinkingPressure, quality.initialPressure()));
        data.pollution = Math.min(100f,
                data.pollution + quality.initialPollution());
        data.potionQuality = quality.id();
        data.spiritVisionActive = false;
        unlockKnowledge(data);

        level.playSound(null, player.blockPosition(),
                SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS,
                1f, pathway == Pathway.THIEF ? 0.8f : 1.2f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.potion.m2_pathway_advanced",
                Component.translatable(
                        "sequence.lord_of_mysteries." + pathway.path() + "_9"),
                Component.translatable(
                        "quality.lord_of_mysteries." + quality.id()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        if (!player.getAbilities().instabuild) stack.shrink(1);
        return stack;
    }

    private void unlockKnowledge(PlayerMysteryData data) {
        String base = "knowledge/" + pathway.path() + "_9_";
        data.knownKnowledge.add(id(base + "acting"));
        if (pathway == Pathway.THIEF) {
            data.knownKnowledge.add(id("knowledge/sleight_of_hand"));
            data.knownKnowledge.add(id("knowledge/shadow_step"));
            data.knownKnowledge.add(id("knowledge/emergency_escape"));
        } else {
            data.knownKnowledge.add(id("knowledge/quick_learning"));
            data.knownKnowledge.add(id("knowledge/spiritual_sight"));
            data.knownKnowledge.add(id("knowledge/stable_ink"));
            data.knownKnowledge.add(id("knowledge/space_trick"));
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, path);
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
        return SeerPotionItem.getQuality(stack) == PotionQuality.PERFECT;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        PotionQuality quality = SeerPotionItem.getQuality(stack);
        tooltip.add(Component.translatable(
                "tooltip.lord_of_mysteries.potion.sequence",
                9,
                Component.translatable(
                        "sequence.lord_of_mysteries." + pathway.path() + "_9"))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                "tooltip.lord_of_mysteries.potion.quality",
                Component.translatable(
                        "quality.lord_of_mysteries." + quality.id()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                "tooltip.lord_of_mysteries.potion.warning")
                .withStyle(ChatFormatting.DARK_RED));
    }
}
