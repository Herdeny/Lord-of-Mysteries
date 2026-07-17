package top.aurora.lordofmysteries.player;

import java.io.IOException;
import java.nio.file.Path;

import net.minecraft.world.level.storage.LevelResource;

import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import top.aurora.lordofmysteries.ProjectMystery;

@Mod.EventBusSubscriber(modid = ProjectMystery.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SaveMigrationEvents {

    private SaveMigrationEvents() {}

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        Path worldRoot = event.getServer().getWorldPath(LevelResource.ROOT);
        try {
            SaveMigrationBackupService.BackupResult result =
                    SaveMigrationBackupService.backupIfNeeded(worldRoot,
                            PlayerMysteryData.CURRENT_SCHEMA_VERSION);
            if (result.created()) {
                ProjectMystery.LOGGER.info(
                        "[Project Mystery] 已创建 schema {} 迁移前备份：{}（{} 文件，{} bytes）",
                        result.targetSchema(), result.snapshotDirectory(),
                        result.fileCount(), result.totalBytes());
            } else {
                ProjectMystery.LOGGER.debug(
                        "[Project Mystery] schema {} 迁移备份已存在：{}",
                        result.targetSchema(), result.snapshotDirectory());
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Project Mystery 无法创建迁移前备份，已阻止服务器继续加载存档",
                    exception);
        }
    }
}
