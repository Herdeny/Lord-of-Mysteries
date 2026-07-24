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
import top.aurora.lordofmysteries.acting.IdentityKitService;
import top.aurora.lordofmysteries.characteristic.CharacteristicLedger;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerFeedback;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public final class HunterPotionItem extends Item {

    public static final ResourceLocation HUNTER_PATHWAY =
            ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, "hunter");

    private final int targetSequence;

    public HunterPotionItem(Properties properties, int targetSequence) {
        super(properties.stacksTo(1));
        if (targetSequence < 5 || targetSequence > 9) {
            throw new IllegalArgumentException(
                    "Hunter potion only supports sequences 9 through 5");
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
                        && HUNTER_PATHWAY.equals(data.pathway)
                        && data.sequence == targetSequence + 1
                        && data.digestion < 100f
                        ? "message.lord_of_mysteries.potion.digestion_incomplete"
                        : "message.lord_of_mysteries.potion.incompatible";
                PlayerFeedback.send(player,
                        Component.translatable(key, targetSequence));
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

        boolean firstPotion = data.pathway == null;
        PotionQuality quality = SeerPotionItem.getQuality(stack);
        data.pathway = HUNTER_PATHWAY;
        data.sequence = targetSequence;
        data.spiritualityMax = switch (targetSequence) {
            case 9 -> 118f;
            case 8 -> 142f;
            case 7 -> 175f;
            case 6 -> 218f;
            case 5 -> 272f;
            default -> throw new IllegalArgumentException("Unsupported Hunter sequence");
        };
        data.spirituality = data.spiritualityMax;
        data.digestion = 0f;
        float sequencePressure = switch (targetSequence) {
            case 8 -> 12f;
            case 7 -> 20f;
            case 6 -> 20f;
            case 5 -> 26f;
            default -> 0f;
        };
        data.insanityPressure = Math.min(100f,
                data.insanityPressure + Math.max(sequencePressure, quality.initialPressure()));
        data.pollution = Math.min(100f, data.pollution + quality.initialPollution());
        data.potionQuality = quality.id();
        CharacteristicLedger.recordPotionAdvancement(
                data, HUNTER_PATHWAY, targetSequence, quality);
        IdentityKitService.onPotionAdvancement(
                player, data, firstPotion);
        data.hunterTrackedTarget = "";
        data.hunterTrackingStartTick = 0L;
        data.hunterTrackingEndTick = 0L;
        unlockKnowledge(data);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS, 1f,
                targetSequence == 9 ? 0.8f : 0.7f);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.potion.hunter_advanced",
                targetSequence,
                Component.translatable("sequence.lord_of_mysteries.hunter_" + targetSequence),
                Component.translatable("quality.lord_of_mysteries." + quality.id()))
                .withStyle(ChatFormatting.RED));

        if (!player.getAbilities().instabuild) stack.shrink(1);
        return stack;
    }

    private boolean canAdvance(PlayerMysteryData data) {
        return PotionAdvancementRules.canAdvance(
                data.pathway == null ? null : data.pathway.toString(),
                data.sequence,
                data.digestion,
                HUNTER_PATHWAY.toString(),
                targetSequence);
    }

    private void unlockKnowledge(PlayerMysteryData data) {
        data.knownKnowledge.add(id("knowledge/hunter_" + targetSequence + "_acting"));
        data.knownKnowledge.add(id("knowledge/tracking"));
        data.knownKnowledge.add(id("knowledge/trap_mastery"));
        data.knownKnowledge.add(id("knowledge/wilderness_sense"));
        if (targetSequence <= 8) {
            data.knownKnowledge.add(id("knowledge/provoke"));
            data.knownKnowledge.add(id("knowledge/enrage"));
            data.knownKnowledge.add(id("knowledge/battle_will"));
        }
        if (targetSequence <= 7) {
            data.knownKnowledge.add(id("knowledge/flame_spear"));
            data.knownKnowledge.add(id("knowledge/fire_ring"));
            data.knownKnowledge.add(id("knowledge/fire_affinity"));
        }
        if (targetSequence <= 6) {
            data.knownKnowledge.add(id("knowledge/battlefield_layout"));
            data.knownKnowledge.add(id("knowledge/instigate_conflict"));
        }
        if (targetSequence <= 5) {
            data.knownKnowledge.add(id("knowledge/flame_scythe"));
            data.knownKnowledge.add(id("knowledge/harvest_mark"));
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
                Component.translatable("sequence.lord_of_mysteries.hunter_" + targetSequence))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.potion.quality",
                Component.translatable("quality.lord_of_mysteries." + quality.id()))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.lord_of_mysteries.potion.warning")
                .withStyle(ChatFormatting.DARK_RED));
    }
}
