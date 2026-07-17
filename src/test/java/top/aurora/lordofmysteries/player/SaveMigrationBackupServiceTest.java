package top.aurora.lordofmysteries.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SaveMigrationBackupServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void createsOneAtomicSchemaBackupAndSkipsUnrelatedSavedData()
            throws Exception {
        Path world = temporaryDirectory.resolve("world");
        Files.createDirectories(world.resolve("playerdata"));
        Files.createDirectories(world.resolve("data"));
        Files.writeString(world.resolve("level.dat"), "level", StandardCharsets.UTF_8);
        Files.writeString(world.resolve("playerdata/player.dat"), "player",
                StandardCharsets.UTF_8);
        Files.writeString(world.resolve("data/lord_of_mysteries_parties.dat"),
                "party", StandardCharsets.UTF_8);
        Files.writeString(world.resolve("data/unrelated_mod.dat"), "ignore",
                StandardCharsets.UTF_8);
        Instant timestamp = Instant.parse("2026-07-17T18:00:00Z");

        SaveMigrationBackupService.BackupResult first =
                SaveMigrationBackupService.backupIfNeeded(world, 16, timestamp);

        assertTrue(first.created());
        assertEquals(3, first.fileCount());
        assertTrue(Files.isRegularFile(first.snapshotDirectory()
                .resolve("level.dat")));
        assertTrue(Files.isRegularFile(first.snapshotDirectory()
                .resolve("playerdata/player.dat")));
        assertTrue(Files.isRegularFile(first.snapshotDirectory()
                .resolve("data/lord_of_mysteries_parties.dat")));
        assertFalse(Files.exists(first.snapshotDirectory()
                .resolve("data/unrelated_mod.dat")));
        String manifest = Files.readString(first.snapshotDirectory()
                .resolve("manifest.properties"), StandardCharsets.UTF_8);
        assertTrue(manifest.contains("schema=16"));
        assertTrue(manifest.contains("files=3"));

        SaveMigrationBackupService.BackupResult second =
                SaveMigrationBackupService.backupIfNeeded(world, 16, timestamp);

        assertFalse(second.created());
        assertEquals(first.snapshotDirectory(), second.snapshotDirectory());
        try (var backups = Files.list(world.resolve("project_mystery_backups"))) {
            assertEquals(1, backups.filter(Files::isDirectory).count());
        }
    }
}
