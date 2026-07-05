package top.aurora.lordofmysteries.world;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class CampGenerationSavedData extends SavedData {

    private static final String DATA_NAME = "lord_of_mysteries_camp_generation";
    private final Set<Long> checkedChunks = new HashSet<>();
    private final Set<Long> campPositions = new HashSet<>();
    private boolean starterCampGenerated;

    public static CampGenerationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                CampGenerationSavedData::load,
                CampGenerationSavedData::new,
                DATA_NAME);
    }

    public static CampGenerationSavedData load(CompoundTag tag) {
        CampGenerationSavedData data = new CampGenerationSavedData();
        if (tag.contains("checked_chunks", Tag.TAG_LONG_ARRAY)) {
            for (long chunk : tag.getLongArray("checked_chunks")) data.checkedChunks.add(chunk);
        }
        if (tag.contains("camp_positions", Tag.TAG_LONG_ARRAY)) {
            for (long position : tag.getLongArray("camp_positions")) {
                data.campPositions.add(position);
            }
        }
        data.starterCampGenerated = tag.getBoolean("starter_camp_generated");
        return data;
    }

    public boolean markIfNew(long chunk) {
        boolean added = checkedChunks.add(chunk);
        if (added) setDirty();
        return added;
    }

    public boolean hasStarterCamp() {
        return starterCampGenerated;
    }

    public void recordCamp(BlockPos position, boolean starterCamp) {
        campPositions.add(position.asLong());
        starterCampGenerated |= starterCamp;
        setDirty();
    }

    public Optional<BlockPos> nearestCamp(BlockPos origin) {
        return campPositions.stream()
                .map(BlockPos::of)
                .min(java.util.Comparator.comparingDouble(position ->
                        position.distSqr(origin)));
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        long[] values = checkedChunks.stream().mapToLong(Long::longValue).toArray();
        tag.put("checked_chunks", new LongArrayTag(values));
        long[] positions = campPositions.stream().mapToLong(Long::longValue).toArray();
        tag.put("camp_positions", new LongArrayTag(positions));
        tag.putBoolean("starter_camp_generated", starterCampGenerated);
        return tag;
    }
}
