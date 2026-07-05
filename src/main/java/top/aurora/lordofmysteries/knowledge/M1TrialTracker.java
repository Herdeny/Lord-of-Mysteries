package top.aurora.lordofmysteries.knowledge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;
import top.aurora.lordofmysteries.player.PlayerMysteryData;
import top.aurora.lordofmysteries.potion.SeerPotionItem;
import top.aurora.lordofmysteries.world.CampGenerationSavedData;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class M1TrialTracker {

    private M1TrialTracker() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 20 != 0) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        if (!data.m1TrialActive) return;
        refresh(player, data);
    }

    public static void refresh(ServerPlayer player, PlayerMysteryData data) {
        data.m1TrialMaxPressure = Math.max(
                data.m1TrialMaxPressure, data.insanityPressure);
        data.m1TrialMaxPollution = Math.max(
                data.m1TrialMaxPollution, data.pollution);
        if (SeerPotionItem.SEER_PATHWAY.equals(data.pathway)
                && (data.m1TrialBestSequence < 0
                || data.sequence < data.m1TrialBestSequence)) {
            data.m1TrialBestSequence = data.sequence;
        }
        if (!data.m1TrialCampVisited && player.level().dimension() == Level.OVERWORLD) {
            CampGenerationSavedData.get(player.serverLevel())
                    .nearestCamp(player.blockPosition())
                    .filter(camp -> camp.distSqr(player.blockPosition()) <= 24d * 24d)
                    .ifPresent(camp -> data.m1TrialCampVisited = true);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.m1TrialActive) data.m1TrialDeaths++;
    }

    public static void recordOccultKill(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.m1TrialActive) data.m1TrialOccultKills++;
    }

    public static void recordRest(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.m1TrialActive) data.m1TrialRestRecoveries++;
    }

    public static void recordCharm(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.m1TrialActive) data.m1TrialCharmsConsumed++;
    }

    public static void recordActing(ServerPlayer player) {
        PlayerMysteryData data = MysteryCapability.get(player);
        if (data.m1TrialActive) data.m1TrialActingEvents++;
    }
}
