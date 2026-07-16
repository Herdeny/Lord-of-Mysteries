package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QuestPartyPolicyTest {

    @Test
    void sharingRequiresEnabledPartyWithinLimit() {
        assertTrue(QuestPartyPolicy.sharingAllowed(true, 4, 4));
        assertFalse(QuestPartyPolicy.sharingAllowed(false, 4, 2));
        assertFalse(QuestPartyPolicy.sharingAllowed(true, 4, 5));
        assertFalse(QuestPartyPolicy.sharingAllowed(true, 1, 1));
    }

    @Test
    void coordinatorSelectionIsDeterministic() {
        UUID first = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID second = UUID.fromString("00000000-0000-0000-0000-000000000002");
        assertEquals(first, QuestPartyPolicy.coordinator(List.of(second, first)));
    }
}
