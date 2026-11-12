package top.aurora.lordofmysteries.potion;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import top.aurora.lordofmysteries.acting.IdentityKitService;
import top.aurora.lordofmysteries.characteristic.CharacteristicLedger;
import top.aurora.lordofmysteries.nightwatch.NightwatchService;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.OccultActivityService;

public final class SleeplessPotionItem extends Item {
    private final int targetSequence;

    public SleeplessPotionItem(Properties properties) { this(properties, 9); }

    public SleeplessPotionItem(Properties properties, int targetSequence) {
        super(properties.stacksTo(1));
        if (targetSequence != 8 && targetSequence != 9) throw new IllegalArgumentException("Unsupported Sleepless sequence");
        this.targetSequence = targetSequence;
    }

    private boolean eligible(Player player) {
        var data = MysteryCapability.get(player);
        return player.isAlive() && !player.isSpectator()
                && Float.isFinite(data.digestion)
                && PotionAdvancementRules.canAdvance(data.pathway == null ? null : data.pathway.getPath(),
                        data.sequence, data.digestion, "sleepless", targetSequence)
                && (!(player instanceof ServerPlayer server) || OccultActivityService.isAvailable(server));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && !eligible(player)) {
            player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.nightwatch.potion_denied"));
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (!(consumer instanceof ServerPlayer player) || !eligible(player)) return stack;
        var data = MysteryCapability.get(player);
        PotionQuality quality = SeerPotionItem.getQuality(stack);
        data.pathway = NightwatchService.PATHWAY;
        boolean firstPotion = data.sequence < 0;
        data.sequence = targetSequence;
        data.spiritualityMax = targetSequence == 9 ? 114 : 140;
        data.spirituality = data.spiritualityMax;
        data.digestion = 0;
        data.insanityPressure = Math.min(100, data.insanityPressure + Math.max(targetSequence == 9 ? 10 : 14, quality.initialPressure()));
        data.pollution = Math.min(100, data.pollution + quality.initialPollution());
        data.potionQuality = quality.id();
        data.spiritVisionActive = false;
        data.knownKnowledge.add(NightwatchService.id("knowledge/sleepless_" + targetSequence + "_acting"));
        CharacteristicLedger.recordPotionAdvancement(data, data.pathway, targetSequence, quality, level.getGameTime());
        IdentityKitService.onPotionAdvancement(player, data, firstPotion);
        NightwatchService.pulse(player);
        NightwatchService.guide(player);
        if (!player.getAbilities().instabuild) stack.shrink(1);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) { return 32; }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }
}
