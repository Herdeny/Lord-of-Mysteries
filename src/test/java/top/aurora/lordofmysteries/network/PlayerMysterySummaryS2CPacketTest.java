package top.aurora.lordofmysteries.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

class PlayerMysterySummaryS2CPacketTest {

    @Test
    void summaryContainsCoreStateWithoutKnowledgePayload() {
        PlayerMysteryData data = new PlayerMysteryData();
        data.pathway = ResourceLocation.fromNamespaceAndPath(
                "lord_of_mysteries", "seer");
        data.sequence = 8;
        data.spirituality = 55f;
        data.digestion = 37f;
        data.spiritVisionActive = true;
        data.emotionReadActive = false;
        data.knownKnowledge.add(ResourceLocation.fromNamespaceAndPath(
                "lord_of_mysteries", "secret/gray_fog"));

        PlayerMysterySummaryS2CPacket hidden =
                PlayerMysterySummaryS2CPacket.from(data, false);
        PlayerMysterySummaryS2CPacket exact =
                PlayerMysterySummaryS2CPacket.from(data, true);

        assertEquals("lord_of_mysteries:seer", hidden.pathway());
        assertEquals(8, hidden.sequence());
        assertEquals(55f, hidden.spirituality());
        assertEquals(-1f, hidden.digestion());
        assertEquals(37f, exact.digestion());
        assertTrue(hidden.spiritVisionActive());
        assertFalse(hidden.emotionReadActive());
        assertEquals(12, PlayerMysterySummaryS2CPacket.class
                .getRecordComponents().length);
    }
}
