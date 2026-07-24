package top.aurora.lordofmysteries.player;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CityEconomyPersistenceTest {

    @Test
    void jobsAndExposureSurviveCapabilitySerialization() {
        PlayerMysteryData source = new PlayerMysteryData();
        source.moneyPence = 83L;
        source.lastCityWorkDay = 14L;
        source.cityWorkShifts = 7;
        source.pressWorkShifts = 3;
        source.agencyWorkShifts = 2;
        source.patrolWorkShifts = 2;
        source.mysticalExposure = 37.5f;

        PlayerMysteryData restored = new PlayerMysteryData();
        restored.load(source.save());

        assertEquals(83L, restored.moneyPence);
        assertEquals(14L, restored.lastCityWorkDay);
        assertEquals(7, restored.cityWorkShifts);
        assertEquals(3, restored.pressWorkShifts);
        assertEquals(2, restored.agencyWorkShifts);
        assertEquals(2, restored.patrolWorkShifts);
        assertEquals(37.5f, restored.mysticalExposure);
        assertEquals(PlayerMysteryData.CURRENT_SCHEMA_VERSION,
                restored.schemaVersion);
    }
}
