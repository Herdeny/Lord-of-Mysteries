package top.aurora.lordofmysteries.client.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.client.ClientMysteryState;
import top.aurora.lordofmysteries.nightwatch.NightwatchProgress;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, value = Dist.CLIENT)
public final class NightwatchClientSounds {
    private NightwatchClientSounds() {}

    @SubscribeEvent
    public static void onStep(PlayLevelSoundEvent.AtEntity event) {
        var player = Minecraft.getInstance().player;
        if (player == null || event.getEntity() != player || player.isSpectator()
                || !player.level().dimension().equals(Level.OVERWORLD) || event.getSound() == null
                || !NightwatchProgress.isNight(player.level().getDayTime())) return;
        var summary = ClientMysteryState.latest().orElse(null);
        if (summary == null || (summary.sequence() != 9 && summary.sequence() != 8)
                || !"lord_of_mysteries:sleepless".equals(summary.pathway())) return;
        var sound = ForgeRegistries.SOUND_EVENTS.getKey(event.getSound().value());
        if (sound != null && sound.getPath().endsWith(".step")) event.setNewVolume(event.getNewVolume() * 0.3f);
    }
}
