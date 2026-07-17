package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class CityNewsLogicTest {

    @Test
    void dailyEditionIsDeterministicForTheSameWorldAndDay() {
        CityNewsLogic.Issue first = CityNewsLogic.issue(
                42L, 5L, CommissionService.MISSING_SQUAD.toString(), false);
        CityNewsLogic.Issue second = CityNewsLogic.issue(
                42L, 5L, CommissionService.MISSING_SQUAD.toString(), false);

        assertEquals(first, second);
        assertEquals("message.lord_of_mysteries.newspaper.case.missing_squad",
                first.caseBulletinKey());
    }

    @Test
    void bulletinReflectsWorkAndCaseState() {
        CityNewsLogic.Issue available = CityNewsLogic.issue(9L, 2L, "", false);
        CityNewsLogic.Issue completed = CityNewsLogic.issue(
                9L, 2L, CommissionService.COUNTERFEIT_FORMULA.toString(), true);

        assertNotEquals(available.caseBulletinKey(), completed.caseBulletinKey());
        assertEquals("message.lord_of_mysteries.newspaper.shift.available",
                available.shiftBulletinKey());
        assertEquals("message.lord_of_mysteries.newspaper.shift.done",
                completed.shiftBulletinKey());
    }
}
