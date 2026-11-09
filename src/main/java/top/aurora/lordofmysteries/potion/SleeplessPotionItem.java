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
    public SleeplessPotionItem(Properties properties) { super(properties.stacksTo(1)); }

    private static boolean eligible(Player player) {
        var data = MysteryCapability.get(player);
        return player.isAlive() && !player.isSpectator()
                && data.pathway == null && data.sequence < 0
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
        data.sequence = 9;
        data.spiritualityMax = 114;
        data.spirituality = 114;
        data.digestion = 0;
        data.insanityPressure = Math.min(100, data.insanityPressure + Math.max(10, quality.initialPressure()));
        data.pollution = Math.min(100, data.pollution + quality.initialPollution());
        data.potionQuality = quality.id();
        data.spiritVisionActive = false;
        data.knownKnowledge.add(NightwatchService.id("knowledge/sleepless_9_acting"));
        CharacteristicLedger.recordPotionAdvancement(data, data.pathway, 9, quality, level.getGameTime());
        IdentityKitService.onPotionAdvancement(player, data, true);
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
