package top.aurora.lordofmysteries.commission;

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

public final class CommissionDefinitionManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static volatile Map<ResourceLocation, CommissionDefinition> definitions = Map.of();

    public CommissionDefinitionManager() {
        super(GSON, "commissions");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources,
                         ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, CommissionDefinition> loaded = new LinkedHashMap<>();
        resources.forEach((fileId, element) -> {
            try {
                JsonObject json = element.getAsJsonObject();
                CommissionDefinition definition = CommissionDefinition.parse(json, fileId);
                if (loaded.putIfAbsent(definition.id(), definition) != null) {
                    throw new IllegalArgumentException("duplicate commission id " + definition.id());
                }
            } catch (RuntimeException exception) {
                ProjectMystery.LOGGER.error("Failed to load commission {}", fileId, exception);
            }
        });
        definitions = Map.copyOf(loaded);
        ProjectMystery.LOGGER.info("Loaded {} commission definitions", definitions.size());
    }

    public static Map<ResourceLocation, CommissionDefinition> all() {
        return definitions;
    }

    public static CommissionDefinition get(ResourceLocation id) {
        return definitions.get(id);
    }
}
