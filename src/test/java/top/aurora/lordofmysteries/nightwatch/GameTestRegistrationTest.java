package top.aurora.lordofmysteries.nightwatch;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameTestRegistrationTest {
    @Test
    void everyGameTestClassIsExplicitlyRegisteredWithForge() throws Exception {
        var directory = Path.of("src/main/java/top/aurora/lordofmysteries/gametest");
        String registry = Files.readString(directory.resolve("PlayerPersistenceGameTests.java"));
        try (var files = Files.list(directory)) {
            for (Path path : files.filter(file -> file.toString().endsWith(".java")).toList()) {
                String source = Files.readString(path);
                if (source.contains("@GameTest(")) {
                    String name = path.getFileName().toString().replace(".java", "");
                    assertTrue(registry.contains("event.register(" + name + ".class)"), name + " would silently not run");
                }
            }
        }
    }
}
