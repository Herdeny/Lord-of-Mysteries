package top.aurora.lordofmysteries.spirit;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpiritFieldJournalTest {
    @Test
    void studyAndMasteryAreIdempotentAndPlayerIsolated() {
        var journal = new SpiritFieldJournal();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        assertTrue(journal.record(first, SpiritEncounter.MEMORY_LEECH, false));
        assertFalse(journal.record(first, SpiritEncounter.MEMORY_LEECH, false));
        assertEquals(1, journal.entry(first).observedCount());
        assertEquals(0, journal.entry(first).masteredCount());
        assertEquals(0, journal.entry(second).observedCount());
        assertTrue(journal.record(first, SpiritEncounter.MEMORY_LEECH, true));
        assertFalse(journal.record(first, SpiritEncounter.MEMORY_LEECH, true));
        assertEquals(1, journal.entry(first).masteredCount());
    }

    @Test
    void allTwelveEntriesSurviveRestartWithoutChangingOtherPlayers() {
        var journal = new SpiritFieldJournal();
        UUID player = UUID.randomUUID();
        for (var encounter : SpiritEncounter.values()) journal.record(player, encounter, true);
        var saved = journal.save(new CompoundTag());
        var restored = SpiritFieldJournal.load(saved);
        assertEquals(12, restored.entry(player).observedCount());
        assertEquals(12, restored.entry(player).masteredCount());
        assertEquals(saved, restored.save(new CompoundTag()));
        assertEquals(0, restored.entry(UUID.randomUUID()).observedCount());
    }

    @Test
    void duplicateRowsMergeAndMalformedRowsArePreserved() {
        UUID player = UUID.randomUUID();
        ListTag rows = new ListTag();
        for (int bit : new int[] {1, 2}) {
            var row = new CompoundTag();
            row.putUUID("player", player);
            row.putInt("observed", bit);
            row.putInt("mastered", bit);
            rows.add(row);
        }
        rows.add(new CompoundTag());
        var tag = new CompoundTag();
        tag.put("entries", rows);
        var journal = SpiritFieldJournal.load(tag);
        assertEquals(2, journal.entry(player).masteredCount());
        assertEquals(1, journal.save(new CompoundTag()).getList("quarantined", 10).size());
    }

    @Test
    void futureSchemaRefusesWritesAndRoundTripsUntouched() {
        var tag = new CompoundTag();
        tag.putInt("schema", 77);
        tag.putString("future", "preserve");
        var journal = SpiritFieldJournal.load(tag);
        assertFalse(journal.record(UUID.randomUUID(), SpiritEncounter.ARCHIVE_SPIDER, true));
        assertEquals(tag, journal.save(new CompoundTag()));
    }

    @Test
    void invalidInputsAndBitsCannotCreateProgressBeyondTheRoster() {
        var journal = new SpiritFieldJournal();
        assertFalse(journal.record(null, SpiritEncounter.DOOR_WISP, false));
        assertFalse(journal.record(UUID.randomUUID(), null, true));
        assertEquals(0, new SpiritFieldJournal.Entry(1 << 25, 0).observedCount());
        assertEquals(1, new SpiritFieldJournal.Entry(0, 2).observedCount());
        assertEquals(12, new SpiritFieldJournal.Entry(-1, -1).masteredCount());
    }

    @Test
    void persistedBitPositionsKeepTheOriginalTwelveSpeciesOrder() {
        assertEquals("lantern_moth_swarm,memory_leech,prayer_echo,spirit_ferryman,color_eater,door_wisp,whisper_crow,compass_bird,archive_spider,gaslight_specter,theatre_maskling,grave_lantern",
                java.util.Arrays.stream(SpiritEncounter.values()).map(SpiritEncounter::id)
                        .collect(java.util.stream.Collectors.joining(",")));
    }
}
