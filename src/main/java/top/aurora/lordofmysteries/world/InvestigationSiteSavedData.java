package top.aurora.lordofmysteries.world;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class InvestigationSiteSavedData extends SavedData {

    private static final String DATA_NAME = "lord_of_mysteries_investigation_sites";
    private long churchPosition;
    private long cultistCampPosition;
    private boolean churchGenerated;
    private boolean cultistCampGenerated;
    private UUID reporterId;

    public static InvestigationSiteSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                InvestigationSiteSavedData::load,
                InvestigationSiteSavedData::new,
                DATA_NAME);
    }

    public static InvestigationSiteSavedData load(CompoundTag tag) {
        InvestigationSiteSavedData data = new InvestigationSiteSavedData();
        data.churchGenerated = tag.getBoolean("church_generated");
        data.cultistCampGenerated = tag.getBoolean("cultist_camp_generated");
        if (tag.contains("church_position", Tag.TAG_LONG)) {
            data.churchPosition = tag.getLong("church_position");
        }
        if (tag.contains("cultist_camp_position", Tag.TAG_LONG)) {
            data.cultistCampPosition = tag.getLong("cultist_camp_position");
        }
        if (tag.hasUUID("reporter_id")) data.reporterId = tag.getUUID("reporter_id");
        return data;
    }

    public boolean hasChurch() {
        return churchGenerated;
    }

    public boolean hasCultistCamp() {
        return cultistCampGenerated;
    }

    public void recordChurch(BlockPos position) {
        churchGenerated = true;
        churchPosition = position.asLong();
        setDirty();
    }

    public void recordCultistCamp(BlockPos position) {
        cultistCampGenerated = true;
        cultistCampPosition = position.asLong();
        setDirty();
    }

    public void recordReporter(UUID id) {
        reporterId = id;
        setDirty();
    }

    public Optional<BlockPos> church() {
        return churchGenerated ? Optional.of(BlockPos.of(churchPosition)) : Optional.empty();
    }

    public Optional<BlockPos> cultistCamp() {
        return cultistCampGenerated
                ? Optional.of(BlockPos.of(cultistCampPosition)) : Optional.empty();
    }

    public Optional<UUID> reporterId() {
        return Optional.ofNullable(reporterId);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("church_generated", churchGenerated);
        tag.putBoolean("cultist_camp_generated", cultistCampGenerated);
        if (churchGenerated) tag.putLong("church_position", churchPosition);
        if (cultistCampGenerated) tag.putLong("cultist_camp_position", cultistCampPosition);
        if (reporterId != null) tag.putUUID("reporter_id", reporterId);
        return tag;
    }
}
