package top.aurora.lordofmysteries.player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SaveMigrationBackupService {

    private static final DateTimeFormatter SNAPSHOT_TIME = DateTimeFormatter
            .ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private SaveMigrationBackupService() {}

    public static BackupResult backupIfNeeded(Path worldRoot, int targetSchema)
            throws IOException {
        return backupIfNeeded(worldRoot, targetSchema, Instant.now());
    }

    static BackupResult backupIfNeeded(Path worldRoot, int targetSchema,
                                       Instant timestamp) throws IOException {
        if (worldRoot == null) throw new IllegalArgumentException("worldRoot is required");
        if (targetSchema < 1) throw new IllegalArgumentException("targetSchema must be positive");
        Path normalizedRoot = worldRoot.toAbsolutePath().normalize();
        Path backupRoot = normalizedRoot.resolve("project_mystery_backups");
        Files.createDirectories(backupRoot);
        Path marker = backupRoot.resolve("schema-" + targetSchema + ".complete");
        BackupResult existing = existingBackup(backupRoot, marker, targetSchema);
        if (existing != null) return existing;

        String baseName = "schema-" + targetSchema + "-"
                + SNAPSHOT_TIME.format(timestamp);
        Path snapshot = availableSnapshotPath(backupRoot, baseName);
        Path temporary = backupRoot.resolve("." + snapshot.getFileName() + ".tmp");
        deleteTree(temporary);
        Files.createDirectories(temporary);

        int fileCount = 0;
        long totalBytes = 0L;
        try {
            for (Path source : backupCandidates(normalizedRoot)) {
                Path relative = normalizedRoot.relativize(source);
                Path target = temporary.resolve(relative.toString());
                Files.createDirectories(target.getParent());
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                fileCount++;
                totalBytes += Files.size(source);
            }
            String manifest = "schema=" + targetSchema + "\n"
                    + "created_utc=" + timestamp + "\n"
                    + "files=" + fileCount + "\n"
                    + "bytes=" + totalBytes + "\n";
            Files.writeString(temporary.resolve("manifest.properties"), manifest,
                    StandardCharsets.UTF_8);
            moveAtomically(temporary, snapshot);

            Path markerTemporary = backupRoot.resolve(".schema-" + targetSchema
                    + ".complete.tmp");
            Files.writeString(markerTemporary, snapshot.getFileName().toString(),
                    StandardCharsets.UTF_8);
            moveAtomically(markerTemporary, marker);
            return new BackupResult(true, snapshot, targetSchema, fileCount, totalBytes);
        } catch (IOException exception) {
            deleteTree(temporary);
            throw exception;
        }
    }

    private static BackupResult existingBackup(Path backupRoot, Path marker,
                                               int targetSchema) throws IOException {
        if (!Files.isRegularFile(marker, LinkOption.NOFOLLOW_LINKS)) return null;
        String snapshotName = Files.readString(marker, StandardCharsets.UTF_8).trim();
        if (snapshotName.isBlank()) return null;
        Path snapshot = backupRoot.resolve(snapshotName).normalize();
        if (!snapshot.startsWith(backupRoot)
                || !Files.isDirectory(snapshot, LinkOption.NOFOLLOW_LINKS)) {
            return null;
        }
        return new BackupResult(false, snapshot, targetSchema, 0, 0L);
    }

    private static Path availableSnapshotPath(Path backupRoot, String baseName) {
        Path candidate = backupRoot.resolve(baseName);
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = backupRoot.resolve(baseName + "-" + suffix++);
        }
        return candidate;
    }

    private static List<Path> backupCandidates(Path worldRoot) throws IOException {
        List<Path> candidates = new ArrayList<>();
        addIfRegular(candidates, worldRoot.resolve("level.dat"));
        addIfRegular(candidates, worldRoot.resolve("level.dat_old"));

        Path playerData = worldRoot.resolve("playerdata");
        if (Files.isDirectory(playerData, LinkOption.NOFOLLOW_LINKS)) {
            try (var paths = Files.walk(playerData)) {
                paths.filter(path -> Files.isRegularFile(
                                path, LinkOption.NOFOLLOW_LINKS))
                        .filter(SaveMigrationBackupService::isDatFile)
                        .forEach(candidates::add);
            }
        }

        Path savedData = worldRoot.resolve("data");
        if (Files.isDirectory(savedData, LinkOption.NOFOLLOW_LINKS)) {
            try (var paths = Files.walk(savedData)) {
                paths.filter(path -> Files.isRegularFile(
                                path, LinkOption.NOFOLLOW_LINKS))
                        .filter(SaveMigrationBackupService::isProjectMysteryData)
                        .forEach(candidates::add);
            }
        }
        candidates.sort(Comparator.comparing(Path::toString));
        return candidates;
    }

    private static void addIfRegular(List<Path> candidates, Path path) {
        if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) candidates.add(path);
    }

    private static boolean isDatFile(Path path) {
        String name = path.getFileName().toString();
        return name.endsWith(".dat") || name.endsWith(".dat_old");
    }

    private static boolean isProjectMysteryData(Path path) {
        String name = path.getFileName().toString();
        return (name.startsWith("lord_of_mysteries")
                || name.startsWith("project_mystery")) && isDatFile(path);
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(source, target);
        }
    }

    private static void deleteTree(Path root) throws IOException {
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) return;
        try (var paths = Files.walk(root)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    public record BackupResult(
            boolean created,
            Path snapshotDirectory,
            int targetSchema,
            int fileCount,
            long totalBytes) {}
}
