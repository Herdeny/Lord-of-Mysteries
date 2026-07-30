package top.aurora.lordofmysteries.organization;

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

public final class OrganizationDefinitionManager
        extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static volatile Map<ResourceLocation, OrganizationDefinition>
            definitions = Map.of();

    public OrganizationDefinitionManager() {
        super(GSON, "organizations");
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resources,
            ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, OrganizationDefinition> loaded =
                new LinkedHashMap<>();
        resources.forEach((fileId, element) -> {
            try {
                JsonObject json = element.getAsJsonObject();
                OrganizationDefinition definition =
                        OrganizationDefinition.parse(json, fileId);
                if (loaded.putIfAbsent(
                        definition.id(), definition) != null) {
                    throw new IllegalArgumentException(
                            "duplicate organization id " + definition.id());
                }
            } catch (RuntimeException exception) {
                ProjectMystery.LOGGER.error(
                        "Failed to load organization {}", fileId, exception);
            }
        });
        definitions = Map.copyOf(loaded);
        ProjectMystery.LOGGER.info(
                "Loaded {} organization definitions", definitions.size());
    }

    public static Map<ResourceLocation, OrganizationDefinition> all() {
        return definitions;
    }

    public static OrganizationDefinition get(ResourceLocation id) {
        return definitions.get(id);
    }
}
