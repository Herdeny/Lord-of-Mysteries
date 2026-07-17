package top.aurora.lordofmysteries.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PlayerMysteryDirtySectionsTest {

    @Test
    void newLoadedAndCopiedDataRequireInitialSynchronization() {
        PlayerMysteryData original = new PlayerMysteryData();
        assertEquals(PlayerDataSection.ALL_MASK, original.dirtySectionMask());

        original.acknowledgeAllDirty();
        PlayerMysteryData loaded = new PlayerMysteryData();
        loaded.load(original.save());
        assertEquals(PlayerDataSection.ALL_MASK, loaded.dirtySectionMask());

        loaded.acknowledgeAllDirty();
        PlayerMysteryData copied = new PlayerMysteryData();
        copied.copyFrom(loaded);
        assertEquals(PlayerDataSection.ALL_MASK, copied.dirtySectionMask());
    }

    @Test
    void publicFieldAndCollectionMutationsDirtyOnlyTheirSection() {
        PlayerMysteryData data = new PlayerMysteryData();
        data.acknowledgeAllDirty();

        data.spirituality = 42f;
        assertTrue(data.isDirty(PlayerDataSection.CORE));
        assertFalse(data.isDirty(PlayerDataSection.KNOWLEDGE));
        assertFalse(data.isDirty(PlayerDataSection.SOCIAL));
        assertFalse(data.isDirty(PlayerDataSection.ENDGAME));

        data.acknowledgeDirty(PlayerDataSection.CORE);
        data.knownKnowledge.add(ResourceLocation.fromNamespaceAndPath(
                "lord_of_mysteries", "guide/awakening"));
        assertTrue(data.isDirty(PlayerDataSection.KNOWLEDGE));
        assertFalse(data.isDirty(PlayerDataSection.CORE));

        data.acknowledgeDirty(PlayerDataSection.KNOWLEDGE);
        data.orgReputation.put(ResourceLocation.fromNamespaceAndPath(
                "lord_of_mysteries", "nighthawks"), 10);
        assertTrue(data.isDirty(PlayerDataSection.SOCIAL));
        assertFalse(data.isDirty(PlayerDataSection.KNOWLEDGE));

        data.acknowledgeDirty(PlayerDataSection.SOCIAL);
        data.m1TrialOccultKills++;
        assertTrue(data.isDirty(PlayerDataSection.ENDGAME));
        assertFalse(data.isDirty(PlayerDataSection.SOCIAL));
    }

    @Test
    void acknowledgingOneSectionPreservesOtherPendingSections() {
        PlayerMysteryData data = new PlayerMysteryData();
        data.acknowledgeAllDirty();
        data.pollution = 12f;
        data.moneyPence = 100L;

        data.acknowledgeDirty(PlayerDataSection.CORE);

        assertFalse(data.isDirty(PlayerDataSection.CORE));
        assertTrue(data.isDirty(PlayerDataSection.SOCIAL));
    }
}
