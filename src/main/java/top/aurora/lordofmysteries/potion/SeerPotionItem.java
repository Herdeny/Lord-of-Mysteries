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

    private final int targetSequence;

    public SeerPotionItem(Properties properties, int targetSequence) {
        super(properties.stacksTo(1));
        if (targetSequence < 7 || targetSequence > 9) {
            throw new IllegalArgumentException("Seer potion only supports sequences 9 through 7");
        }
        this.targetSequence = targetSequence;
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
        if (!level.isClientSide()) {
            PlayerMysteryData data = MysteryCapability.get(player);
            if (!canAdvance(data)) {
                String key = targetSequence < 9
                        && SEER_PATHWAY.equals(data.pathway)
                        && data.sequence == targetSequence + 1
                        && data.digestion < 100f
                        ? "message.lord_of_mysteries.potion.digestion_incomplete"
                        : "message.lord_of_mysteries.potion.incompatible";
                player.sendSystemMessage(Component.translatable(key, targetSequence));
                return InteractionResultHolder.fail(stack);
            }
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!(livingEntity instanceof ServerPlayer player)) return stack;

        PlayerMysteryData data = MysteryCapability.get(player);
        if (!canAdvance(data)) return stack;

        PotionQuality quality = getQuality(stack);
        data.pathway = SEER_PATHWAY;
        data.sequence = targetSequence;
        data.spiritualityMax = spiritualityFor(targetSequence);
        data.spirituality = data.spiritualityMax;
        data.digestion = 0f;
        data.insanityPressure = Math.min(100f,
                data.insanityPressure + Math.max(pressureFor(targetSequence),
                        quality.initialPressure()));
        data.pollution = Math.min(100f, data.pollution + quality.initialPollution());
        data.potionQuality = quality.id();
        data.paperSubstituteArmedEndTick = 0L;
        data.paperSubstituteDimension = "";
        unlockKnowledge(data);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1f,
                0.75f + (9 - targetSequence) * 0.18f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.potion.seer_advanced",
                targetSequence,
                Component.translatable("sequence.lord_of_mysteries.seer_" + targetSequence),
                Component.translatable("quality.lord_of_mysteries." + quality.id()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        if (!player.getAbilities().instabuild) stack.shrink(1);
        return stack;
    }

    private boolean canAdvance(PlayerMysteryData data) {
        return PotionAdvancementRules.canAdvance(
                data.pathway == null ? null : data.pathway.toString(),
                data.sequence,
                data.digestion,
                SEER_PATHWAY.toString(),
                targetSequence);
    }

    private void unlockKnowledge(PlayerMysteryData data) {
        data.knownKnowledge.add(id("knowledge/seer_" + targetSequence + "_acting"));
        data.knownKnowledge.add(id("knowledge/spirit_vision"));
        data.knownKnowledge.add(id("knowledge/danger_intuition"));
        data.knownKnowledge.add(id("knowledge/simple_divination"));
        if (targetSequence <= 8) {
            data.knownKnowledge.add(id("knowledge/card_blade"));
            data.knownKnowledge.add(id("knowledge/perfect_balance"));
            data.knownKnowledge.add(id("knowledge/expression_control"));
            data.knownKnowledge.add(id("knowledge/intuitive_dodge"));
        }
        if (targetSequence <= 7) {
            data.knownKnowledge.add(id("knowledge/flame_leap"));
            data.knownKnowledge.add(id("knowledge/paper_substitute"));
            data.knownKnowledge.add(id("knowledge/air_bullet"));
            data.knownKnowledge.add(id("knowledge/stage_illusion"));
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, path);
    }

    private static float spiritualityFor(int sequence) {
        return switch (sequence) {
            case 9 -> 122f;
            case 8 -> 144f;
            case 7 -> 175f;
            default -> throw new IllegalArgumentException("Unsupported Seer sequence");
        };
    }

    private static float pressureFor(int sequence) {
        return switch (sequence) {
            case 9 -> 10f;
            case 8 -> 12f;
            case 7 -> 15f;
            default -> 0f;
        };
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
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.potion.sequence",
                targetSequence,
                Component.translatable("sequence.lord_of_mysteries.seer_" + targetSequence))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.potion.quality",
                Component.translatable("quality.lord_of_mysteries." + quality.id()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.potion.warning")
                .withStyle(ChatFormatting.DARK_RED));
    }
}
