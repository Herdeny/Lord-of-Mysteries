package top.aurora.lordofmysteries.acting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

class ActingIdentityServiceTest {

    @Test
    void repeatedHighPressurePracticeRaisesOveridentification() {
        PlayerMysteryData data = extraordinary();
        data.insanityPressure = 60f;

        ActingIdentityService.recordPractice(data, 0.1f, 12f);

        assertTrue(data.principleInsight > 0f);
        assertEquals(1.1f, data.roleOveridentification, 0.0001f);
    }

    @Test
    void reflectionIsDailyAndRestoresIdentitySeparation() {
        PlayerMysteryData data = extraordinary();
        data.principleInsight = 5f;
        data.roleOveridentification = 30f;
        data.insanityPressure = 20f;

        assertEquals(ActingIdentityService.ReflectionResult.SUCCESS,
                ActingIdentityService.reflect(data, 4L));
        assertEquals(6f, data.principleInsight);
        assertEquals(22f, data.roleOveridentification);
        assertEquals(18f, data.insanityPressure);
        assertEquals(ActingIdentityService.ReflectionResult.ALREADY_REFLECTED,
                ActingIdentityService.reflect(data, 4L));
        assertEquals(1, data.actingReflectionCount);
    }

    @Test
    void commonerCannotCreateActingInsight() {
        assertEquals(ActingIdentityService.ReflectionResult.COMMONER,
                ActingIdentityService.reflect(new PlayerMysteryData(), 0L));
    }

    private static PlayerMysteryData extraordinary() {
        PlayerMysteryData data = new PlayerMysteryData();
        data.pathway = ResourceLocation.fromNamespaceAndPath(
                "lord_of_mysteries", "seer");
        data.sequence = 9;
        return data;
    }
}
