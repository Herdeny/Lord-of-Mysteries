package top.aurora.lordofmysteries.artifact;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import top.aurora.lordofmysteries.ProjectMystery;

public final class SealedArtifactDefinitionManager
        extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static volatile Map<ResourceLocation, SealedArtifactDefinition>
            definitions = Map.of();

    public SealedArtifactDefinitionManager() {
        super(GSON, "artifacts");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resources,
            ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, SealedArtifactDefinition> loaded =
                new LinkedHashMap<>();
        resources.forEach((fileId, element) -> {
            try {
                JsonObject json = element.getAsJsonObject();
                SealedArtifactDefinition definition =
                        SealedArtifactDefinition.parse(json, fileId);
                if (loaded.putIfAbsent(
                        definition.id(), definition) != null) {
                    throw new IllegalArgumentException(
                            "duplicate sealed artifact id "
                                    + definition.id());
                }
            } catch (RuntimeException exception) {
                ProjectMystery.LOGGER.error(
                        "Failed to load sealed artifact {}",
                        fileId, exception);
            }
        });
        definitions = Map.copyOf(loaded);
        ProjectMystery.LOGGER.info(
                "Loaded {} sealed artifact definitions",
                definitions.size());
    }

    public static Map<ResourceLocation, SealedArtifactDefinition> all() {
        return definitions;
    }

    public static SealedArtifactDefinition get(ResourceLocation id) {
        return definitions.get(id);
    }
}
