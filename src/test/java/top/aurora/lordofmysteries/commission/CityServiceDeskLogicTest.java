package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CityServiceDeskLogicTest {

    @Test
    void fieldKitPurchaseRequiresEnoughPence() {
        assertEquals(CityServiceDeskLogic.TransactionStatus.INSUFFICIENT_FUNDS,
                CityServiceDeskLogic.purchase(
                        CityServiceDeskLogic.FIELD_KIT_COST - 1,
                        CityServiceDeskLogic.FIELD_KIT_COST));
        assertEquals(CityServiceDeskLogic.TransactionStatus.SUCCESS,
                CityServiceDeskLogic.purchase(
                        CityServiceDeskLogic.FIELD_KIT_COST,
                        CityServiceDeskLogic.FIELD_KIT_COST));
    }

    @Test
    void safeRoomChargesAndRecoversBothRiskTracks() {
        CityServiceDeskLogic.SafeRoomResult result =
                CityServiceDeskLogic.requestSafeRoom(60L, 55f, 12f);

        assertEquals(CityServiceDeskLogic.TransactionStatus.SUCCESS,
                result.status());
        assertEquals(32L, result.balance());
        assertEquals(35f, result.pressure());
        assertEquals(8f, result.pollution());
        assertEquals(20f, result.recoveredPressure());
        assertEquals(4f, result.recoveredPollution());
    }

    @Test
    void safeRoomNeverPushesRiskBelowZero() {
        CityServiceDeskLogic.SafeRoomResult result =
                CityServiceDeskLogic.requestSafeRoom(28L, 5f, 2f);

        assertEquals(0f, result.pressure());
        assertEquals(0f, result.pollution());
        assertEquals(5f, result.recoveredPressure());
        assertEquals(2f, result.recoveredPollution());
    }

    @Test
    void safeRoomDoesNotChargeHealthyOrPoorPlayers() {
        assertEquals(CityServiceDeskLogic.TransactionStatus.NOT_NEEDED,
                CityServiceDeskLogic.requestSafeRoom(28L, 0f, 0f).status());
        assertEquals(CityServiceDeskLogic.TransactionStatus.INSUFFICIENT_FUNDS,
                CityServiceDeskLogic.requestSafeRoom(27L, 10f, 1f).status());
    }
}
