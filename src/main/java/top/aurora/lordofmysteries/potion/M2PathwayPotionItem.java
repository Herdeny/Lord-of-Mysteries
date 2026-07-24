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

public final class M2PathwayPotionItem extends Item {

    public enum Pathway {
        THIEF("thief"),
        APPRENTICE("apprentice");

        private final ResourceLocation id;

        Pathway(String path) {
            this.id = ResourceLocation.fromNamespaceAndPath(ProjectMystery.MOD_ID, path);
        }

        public ResourceLocation id() {
            return id;
        }

        public String path() {
            return id.getPath();
        }

        public float spiritualityMax(int sequence) {
            return switch (this) {
                case THIEF -> switch (sequence) {
                    case 9 -> 108f;
                    case 8 -> 130f;
                    case 7 -> 158f;
                    case 6 -> 198f;
                    case 5 -> 246f;
                    default -> throw new IllegalArgumentException("Unsupported Thief sequence");
                };
                case APPRENTICE -> switch (sequence) {
                    case 9 -> 115f;
                    case 8 -> 138f;
                    case 7 -> 165f;
                    case 6 -> 200f;
                    case 5 -> 250f;
                    default -> throw new IllegalArgumentException(
                            "Unsupported Apprentice sequence");
                };
            };
        }

        public float drinkingPressure(int sequence) {
            return switch (this) {
                case THIEF -> switch (sequence) {
                    case 9 -> 10f;
                    case 8 -> 14f;
                    case 7 -> 15f;
                    case 6 -> 26f;
                    case 5 -> 32f;
                    default -> throw new IllegalArgumentException("Unsupported Thief sequence");
                };
                case APPRENTICE -> switch (sequence) {
                    case 9 -> 9f;
                    case 8 -> 12f;
                    case 7 -> 14f;
                    case 6 -> 18f;
                    case 5 -> 24f;
                    default -> throw new IllegalArgumentException(
                            "Unsupported Apprentice sequence");
                };
            };
        }
    }

    private final Pathway pathway;
    private final int targetSequence;

    public M2PathwayPotionItem(Properties properties, Pathway pathway,
                               int targetSequence) {
        super(properties.stacksTo(1));
        if (targetSequence < 5 || targetSequence > 9) {
            throw new IllegalArgumentException(
                    "M3 pathway potion only supports sequences 9 through 5");
        }
        this.pathway = pathway;
        this.targetSequence = targetSequence;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                   InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            PlayerMysteryData data = MysteryCapability.get(player);
            if (!canAdvance(data)) {
                String key = targetSequence < 9
                        && pathway.id().equals(data.pathway)
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
    public ItemStack finishUsingItem(ItemStack stack, Level level,
                                      LivingEntity livingEntity) {
        if (!(livingEntity instanceof ServerPlayer player)) return stack;

        PlayerMysteryData data = MysteryCapability.get(player);
        if (!canAdvance(data)) return stack;

        boolean firstPotion = data.pathway == null;
        PotionQuality quality = SeerPotionItem.getQuality(stack);
        data.pathway = pathway.id();
        data.sequence = targetSequence;
        data.spiritualityMax = pathway.spiritualityMax(targetSequence);
        data.spirituality = data.spiritualityMax;
        data.digestion = 0f;
        data.insanityPressure = Math.min(100f,
                data.insanityPressure
                        + Math.max(pathway.drinkingPressure(targetSequence),
                                   quality.initialPressure()));
        data.pollution = Math.min(100f,
                data.pollution + quality.initialPollution());
        data.potionQuality = quality.id();
        CharacteristicLedger.recordPotionAdvancement(
                data, pathway.id(), targetSequence, quality);
        IdentityKitService.onPotionAdvancement(
                player, data, firstPotion);
        data.spiritVisionActive = false;
        unlockKnowledge(data);

        level.playSound(null, player.blockPosition(),
                SoundEvents.BREWING_STAND_BREW, SoundSource.PLAYERS,
                1f, pathway == Pathway.THIEF ? 0.8f : 1.2f);
        PlayerFeedback.send(player, Component.translatable(
                "message.lord_of_mysteries.potion.m2_pathway_advanced",
                Component.translatable(
                        "sequence.lord_of_mysteries." + pathway.path()
                                + "_" + targetSequence),
                Component.translatable(
                        "quality.lord_of_mysteries." + quality.id()))
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        if (!player.getAbilities().instabuild) stack.shrink(1);
        return stack;
    }

    private void unlockKnowledge(PlayerMysteryData data) {
        String base = "knowledge/" + pathway.path() + "_" + targetSequence + "_";
        data.knownKnowledge.add(id(base + "acting"));
        if (pathway == Pathway.THIEF) {
            data.knownKnowledge.add(id("knowledge/sleight_of_hand"));
            data.knownKnowledge.add(id("knowledge/shadow_step"));
            data.knownKnowledge.add(id("knowledge/emergency_escape"));
            if (targetSequence <= 8) {
                data.knownKnowledge.add(id("knowledge/position_swap"));
                data.knownKnowledge.add(id("knowledge/decoy_escape"));
                data.knownKnowledge.add(id("knowledge/swindler_patter"));
            }
            if (targetSequence <= 7) {
                data.knownKnowledge.add(id("knowledge/rune_analysis"));
                data.knownKnowledge.add(id("knowledge/master_lockpick"));
                data.knownKnowledge.add(id("knowledge/trace_erasure"));
            }
            if (targetSequence <= 6) {
                data.knownKnowledge.add(id("knowledge/ability_theft"));
                data.knownKnowledge.add(id("knowledge/unowned_retrieval"));
            }
            if (targetSequence <= 5) {
                data.knownKnowledge.add(id("knowledge/dream_theft"));
                data.knownKnowledge.add(id("knowledge/reality_dislocation"));
            }
        } else {
            data.knownKnowledge.add(id("knowledge/quick_learning"));
            data.knownKnowledge.add(id("knowledge/spiritual_sight"));
            data.knownKnowledge.add(id("knowledge/stable_ink"));
            data.knownKnowledge.add(id("knowledge/space_trick"));
            if (targetSequence <= 8) {
                data.knownKnowledge.add(id("knowledge/spatial_relay"));
                data.knownKnowledge.add(id("knowledge/knowledge_link"));
                data.knownKnowledge.add(id("knowledge/mirror_door"));
            }
            if (targetSequence <= 7) {
                data.knownKnowledge.add(id("knowledge/stellar_divination"));
                data.knownKnowledge.add(id("knowledge/starlight_ward"));
                data.knownKnowledge.add(id("knowledge/star_atlas"));
            }
            if (targetSequence <= 6) {
                data.knownKnowledge.add(id("knowledge/perfect_copy"));
                data.knownKnowledge.add(id("knowledge/archive_focus"));
            }
            if (targetSequence <= 5) {
                data.knownKnowledge.add(id("knowledge/traveler_door"));
                data.knownKnowledge.add(id("knowledge/outpost_return"));
            }
        }
    }

    private boolean canAdvance(PlayerMysteryData data) {
        return PotionAdvancementRules.canAdvance(
                data.pathway == null ? null : data.pathway.toString(),
                data.sequence,
                data.digestion,
                pathway.id().toString(),
                targetSequence);
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
                targetSequence,
                Component.translatable(
                        "sequence.lord_of_mysteries." + pathway.path()
                                + "_" + targetSequence))
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
