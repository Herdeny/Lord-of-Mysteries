package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RescueApproachPolicyTest {

    @Test
    void thiefAndApprenticeCanUseStealth() {
        assertTrue(RescueApproachPolicy.stealthAllowed(
                "lord_of_mysteries:thief", 9));
        assertTrue(RescueApproachPolicy.stealthAllowed(
                "lord_of_mysteries:apprentice", 7));
    }

    @Test
    void unrelatedPathwaysCannotUseStealth() {
        assertFalse(RescueApproachPolicy.stealthAllowed(
                "lord_of_mysteries:hunter", 9));
        assertFalse(RescueApproachPolicy.stealthAllowed("", -1));
    }

    @Test
    void divinationRequiresSeerAndTwelveSpirituality() {
        assertTrue(RescueApproachPolicy.divinationAllowed(
                "lord_of_mysteries:seer", 9, 12f));
        assertFalse(RescueApproachPolicy.divinationAllowed(
                "lord_of_mysteries:seer", 9, 11.9f));
        assertFalse(RescueApproachPolicy.divinationAllowed(
                "lord_of_mysteries:apprentice", 7, 100f));
    }
}
