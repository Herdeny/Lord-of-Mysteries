package top.aurora.lordofmysteries.artifact;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.ProjectMystery;

public enum ManagedArtifactKind {
    ETERNAL_MATCHBOX("eternal_matchbox"),
    KINDLY_UMBRELLA("artifact_3_091_kindly_umbrella"),
    HONEST_MIRROR("artifact_3_207_honest_mirror"),
    SLEEPING_BELL("artifact_2_031_sleeping_bell"),
    GUEST_MASK("artifact_2_166_guest_mask"),
    MERCIFUL_CHAIN("artifact_2_203_merciful_chain"),
    CITY_WHISTLE("artifact_1_012_city_whistle");

    private final String path;
    private final ResourceLocation id;

    ManagedArtifactKind(String path) {
        this.path = path;
        this.id = ResourceLocation.fromNamespaceAndPath(
                ProjectMystery.MOD_ID, path);
    }

    public String path() {
        return path;
    }

    public ResourceLocation id() {
        return id;
    }
}
