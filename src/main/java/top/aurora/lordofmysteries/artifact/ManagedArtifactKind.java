package top.aurora.lordofmysteries.artifact;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.ProjectMystery;

public enum ManagedArtifactKind {
    ETERNAL_MATCHBOX("eternal_matchbox"),
    KINDLY_UMBRELLA("artifact_3_091_kindly_umbrella"),
    HONEST_MIRROR("artifact_3_207_honest_mirror"),
    WEATHERED_COIN("artifact_3_114_weathered_coin"),
    PILGRIM_CHALK("artifact_3_148_pilgrim_chalk"),
    HUSHGLASS_VIAL("artifact_3_221_hushglass_vial"),
    WAKEFUL_SNUFFBOX("artifact_3_233_wakeful_snuffbox"),
    WAYFINDER_CANDLE("artifact_3_244_wayfinder_candle"),
    SALVAGE_GLOVE("artifact_3_260_salvage_glove"),
    SLEEPING_BELL("artifact_2_031_sleeping_bell"),
    REVERSE_WATCH("artifact_2_081_reverse_watch"),
    STRINGLESS_VIOLIN("artifact_2_082_stringless_violin"),
    GLUTTONOUS_FORK("artifact_2_083_gluttonous_fork"),
    DEATH_LEDGER("artifact_2_084_death_ledger"),
    HUSH_BLADE("artifact_2_085_hush_blade"),
    GUEST_MASK("artifact_2_166_guest_mask"),
    MERCIFUL_CHAIN("artifact_2_203_merciful_chain"),
    CITY_WHISTLE("artifact_1_012_city_whistle"),
    THOUSAND_FACE_MIRROR("artifact_1_021_thousand_face_mirror"),
    STORM_ANCHOR("artifact_1_022_storm_anchor"),
    WHITE_NOISE_CANDELABRUM("artifact_1_023_white_noise_candelabrum"),
    PENITENT_IRON_SHOES("artifact_1_024_penitent_iron_shoes"),
    GAMBLER_DICE_CUP("artifact_1_025_gambler_dice_cup"),
    FERRYMAN_TICKET("artifact_1_026_ferryman_ticket");

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
