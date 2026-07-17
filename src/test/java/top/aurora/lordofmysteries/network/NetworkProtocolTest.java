package top.aurora.lordofmysteries.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import org.junit.jupiter.api.Test;

class NetworkProtocolTest {

    @Test
    void packetIdsAreExplicitUniqueAndContiguous() {
        assertEquals(NetworkProtocol.PACKET_COUNT, NetworkProtocol.packetIds().size());
        assertEquals(NetworkProtocol.PACKET_COUNT,
                new HashSet<>(NetworkProtocol.packetIds()).size());
        assertEquals(0, NetworkProtocol.packetIds().stream().mapToInt(Integer::intValue).min()
                .orElseThrow());
        assertEquals(NetworkProtocol.PACKET_COUNT - 1,
                NetworkProtocol.packetIds().stream().mapToInt(Integer::intValue).max()
                        .orElseThrow());
    }

    @Test
    void onlyExactProtocolVersionIsAccepted() {
        assertTrue(NetworkProtocol.accepts("7"));
        assertFalse(NetworkProtocol.accepts("6"));
        assertFalse(NetworkProtocol.accepts("7.1"));
    }
}
