package top.aurora.lordofmysteries.world;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class MistCityOutpostSavedData extends SavedData {

    private static final String DATA_NAME = "lord_of_mysteries_mist_city_outpost";
    private long outpostPosition;
    private boolean generated;

    public static MistCityOutpostSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                MistCityOutpostSavedData::load,
                MistCityOutpostSavedData::new,
                DATA_NAME);
    }

    public static MistCityOutpostSavedData load(CompoundTag tag) {
        MistCityOutpostSavedData data = new MistCityOutpostSavedData();
        data.generated = tag.getBoolean("generated");
        if (tag.contains("outpost_position", Tag.TAG_LONG)) {
            data.outpostPosition = tag.getLong("outpost_position");
        }
        return data;
    }

    public boolean hasOutpost() {
        return generated;
    }

    public void recordOutpost(BlockPos position) {
        generated = true;
        outpostPosition = position.asLong();
        setDirty();
    }

    public Optional<BlockPos> outpost() {
        return generated ? Optional.of(BlockPos.of(outpostPosition)) : Optional.empty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("generated", generated);
        if (generated) tag.putLong("outpost_position", outpostPosition);
        return tag;
    }
}
