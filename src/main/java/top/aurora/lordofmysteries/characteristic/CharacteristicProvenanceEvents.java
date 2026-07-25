package top.aurora.lordofmysteries.characteristic;

import net.minecraft.server.level.ServerPlayer;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.player.MysteryCapability;

@Mod.EventBusSubscriber(
        modid = ProjectMystery.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CharacteristicProvenanceEvents {

    private CharacteristicProvenanceEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CharacteristicLedger.ensurePlayerProvenance(
                    MysteryCapability.get(player));
        }
    }
}
