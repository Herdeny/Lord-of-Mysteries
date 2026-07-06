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

public final class SpectatorPotionItem extends Item {

    public static final ResourceLocation SPECTATOR_PATHWAY =
            ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, "spectator");

    private final int targetSequence;

    public SpectatorPotionItem(Properties properties, int targetSequence) {
        super(properties.stacksTo(1));
        if (targetSequence < 7 || targetSequence > 9) {
            throw new IllegalArgumentException(
                    "Spectator potion only supports sequences 9 through 7");
        }
        this.targetSequence = targetSequence;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            PlayerMysteryData data = MysteryCapability.get(player);
            if (!canAdvance(data)) {
                String key = targetSequence < 9
                        && SPECTATOR_PATHWAY.equals(data.pathway)
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

        PotionQuality quality = SeerPotionItem.getQuality(stack);
        data.pathway = SPECTATOR_PATHWAY;
        data.sequence = targetSequence;
        data.spiritualityMax = switch (targetSequence) {
            case 9 -> 112f;
            case 8 -> 138f;
            default -> 168f;
        };
        data.spirituality = data.spiritualityMax;
        data.digestion = 0f;
        float sequencePressure = switch (targetSequence) {
            case 8 -> 18f;
            case 7 -> 24f;
            default -> 0f;
        };
        data.insanityPressure = Math.min(100f,
                data.insanityPressure + Math.max(sequencePressure, quality.initialPressure()));
        data.pollution = Math.min(100f, data.pollution + quality.initialPollution());
        data.potionQuality = quality.id();
        data.emotionReadActive = false;
        unlockKnowledge(data);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1f,
                targetSequence == 9 ? 0.9f : 1.1f);
        player.sendSystemMessage(Component.translatable(
                "message.lord_of_mysteries.potion.spectator_advanced",
                targetSequence,
                Component.translatable("sequence.lord_of_mysteries.spectator_" + targetSequence),
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
                SPECTATOR_PATHWAY.toString(),
                targetSequence);
    }

    private void unlockKnowledge(PlayerMysteryData data) {
        data.knownKnowledge.add(id("knowledge/spectator_" + targetSequence + "_acting"));
        data.knownKnowledge.add(id("knowledge/emotion_read"));
        data.knownKnowledge.add(id("knowledge/behavior_prediction"));
        data.knownKnowledge.add(id("knowledge/composure"));
        if (targetSequence <= 8) {
            data.knownKnowledge.add(id("knowledge/surface_read"));
            data.knownKnowledge.add(id("knowledge/mental_suggestion"));
        }
        if (targetSequence <= 7) {
            data.knownKnowledge.add(id("knowledge/psychological_pacification"));
            data.knownKnowledge.add(id("knowledge/mind_shock"));
            data.knownKnowledge.add(id("knowledge/psychological_cloak"));
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
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        PotionQuality quality = SeerPotionItem.getQuality(stack);
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.potion.sequence",
                targetSequence,
                Component.translatable("sequence.lord_of_mysteries.spectator_" + targetSequence))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.potion.quality",
                Component.translatable("quality.lord_of_mysteries." + quality.id()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.potion.warning")
                .withStyle(ChatFormatting.DARK_RED));
    }
}

