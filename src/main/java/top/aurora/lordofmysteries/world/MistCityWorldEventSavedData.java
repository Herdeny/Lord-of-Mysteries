package top.aurora.lordofmysteries.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class MistCityWorldEventSavedData extends SavedData {

    private static final String DATA_NAME =
            "lord_of_mysteries_mist_city_world_event";
    private long currentDay = Long.MIN_VALUE;
    private MistCityWorldEvent currentEvent = MistCityWorldEvent.CLEAR;

    public static MistCityWorldEventSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                MistCityWorldEventSavedData::load,
                MistCityWorldEventSavedData::new,
                DATA_NAME);
    }

    public static MistCityWorldEventSavedData load(CompoundTag tag) {
        MistCityWorldEventSavedData data =
                new MistCityWorldEventSavedData();
        if (tag.contains("current_day", Tag.TAG_LONG)) {
            data.currentDay = tag.getLong("current_day");
        }
        data.currentEvent = MistCityWorldEvent.fromId(
                tag.getString("current_event"));
        return data;
    }

    public boolean update(long day, MistCityWorldEvent event) {
        if (day < 0L || event == null) {
            throw new IllegalArgumentException(
                    "world event state is invalid");
        }
        if (day == currentDay && event == currentEvent) return false;
        currentDay = day;
        currentEvent = event;
        setDirty();
        return true;
    }

    public long currentDay() {
        return currentDay;
    }

    public MistCityWorldEvent currentEvent() {
        return currentEvent;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("current_day", currentDay);
        tag.putString("current_event", currentEvent.id());
        return tag;
    }
}
