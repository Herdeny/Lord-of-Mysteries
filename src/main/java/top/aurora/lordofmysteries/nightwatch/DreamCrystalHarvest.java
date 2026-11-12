package top.aurora.lordofmysteries.nightwatch;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.OccultActivityService;
import top.aurora.lordofmysteries.registry.ModItems;
import top.aurora.lordofmysteries.registry.ModParticles;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID)
public final class DreamCrystalHarvest {
    private static final String DAY = ProjectMystery.MOD_ID + ":dream_harvest_day";
    private static final String COOLDOWN = ProjectMystery.MOD_ID + ":dream_harvest_cooldown";

    private DreamCrystalHarvest() {}

    public static boolean harvest(ServerPlayer player, Villager villager, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown() || !player.isAlive()
                || player.isSpectator() || !player.getAbilities().mayBuild
                || !OccultActivityService.isAvailable(player) || !villager.isAlive() || villager.isBaby()
                || !villager.isSleeping() || villager.level() != player.level()
                || !player.level().dimension().equals(Level.OVERWORLD)
                || player.distanceToSqr(villager) > 9 || !player.hasLineOfSight(villager)
                || !player.getItemInHand(hand).is(Items.AMETHYST_SHARD)) return false;
        var tag = villager.getPersistentData();
        long now = player.level().getGameTime();
        long time = player.level().getDayTime();
        long last = tag.contains(DAY) ? tag.getLong(DAY) : -1;
        if (!PoetRules.canHarvest(time, now, last, tag.getLong(COOLDOWN))) return false;
        tag.putLong(DAY, Math.floorDiv(time, 24000));
        tag.putLong(COOLDOWN, now + 24000);
        if (!player.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
        ItemStack result = new ItemStack(ModItems.DREAM_MURMUR_CRYSTAL.get());
        if (!player.getInventory().add(result)) player.drop(result, false);
        player.serverLevel().sendParticles(ModParticles.DREAM_BUBBLE.get(),
                villager.getX(), villager.getY() + 0.7, villager.getZ(), 6, 0.2, 0.15, 0.2, 0.01);
        player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.poet.harvest"));
        return true;
    }

    @SubscribeEvent
    public static void interact(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager) || !event.getEntity().isShiftKeyDown()
                || !event.getItemStack().is(Items.AMETHYST_SHARD)) return;
        event.setCanceled(true);
        if (event.getEntity() instanceof ServerPlayer player) {
            boolean harvested = harvest(player, villager, event.getHand());
            event.setCancellationResult(harvested ? InteractionResult.SUCCESS : InteractionResult.FAIL);
            if (!harvested) player.sendSystemMessage(Component.translatable("message.lord_of_mysteries.poet.harvest_denied"));
        } else {
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }
}
