package top.aurora.lordofmysteries.organization;

import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;
import top.aurora.lordofmysteries.artifact.SealedArtifactDefinitionManager;

@Mod.EventBusSubscriber(
        modid = ProjectMystery.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class OrganizationDataReloadHandler {

    private OrganizationDataReloadHandler() {}

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new OrganizationDefinitionManager());
        event.addListener(new SealedArtifactDefinitionManager());
    }
}
