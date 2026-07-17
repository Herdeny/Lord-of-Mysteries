package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.player.PlayerMysteryData;

class CommissionBoardStateTest {

    private static final ResourceLocation CASE = id("commission/case");
    private static final ResourceLocation INTRO = id("commission/intro");

    @Test
    void derivesPlayerFacingAvailabilityWithoutTrustingTheClient() {
        PlayerMysteryData data = new PlayerMysteryData();
        CommissionDefinition definition = definition(List.of(), false);

        assertEquals(CommissionBoardState.AVAILABLE,
                CommissionService.availability(data, definition, 100L));

        data.activeCommissionId = CASE.toString();
        assertEquals(CommissionBoardState.ACTIVE,
                CommissionService.availability(data, definition, 100L));

        data.activeCommissionId = INTRO.toString();
        assertEquals(CommissionBoardState.LOCKED,
                CommissionService.availability(data, definition, 100L));

        data.activeCommissionId = "";
        data.completedCommissions.add(CASE);
        assertEquals(CommissionBoardState.COMPLETED,
                CommissionService.availability(data, definition, 100L));
    }

    @Test
    void distinguishesPrerequisiteAndCooldownLocks() {
        PlayerMysteryData data = new PlayerMysteryData();
        CommissionDefinition gated = definition(List.of(INTRO), true);

        assertEquals(CommissionBoardState.LOCKED,
                CommissionService.availability(data, gated, 100L));

        data.completedCommissions.add(INTRO);
        data.commissionCooldowns.put(CASE, 120L);
        assertEquals(CommissionBoardState.COOLDOWN,
                CommissionService.availability(data, gated, 100L));
        assertEquals(CommissionBoardState.AVAILABLE,
                CommissionService.availability(data, gated, 120L));
    }

    @Test
    void boardViewSortsCasesAndExposesBalance() {
        PlayerMysteryData data = new PlayerMysteryData();
        data.moneyPence = 372L;
        ResourceLocation later = id("commission/z_case");
        ResourceLocation first = id("commission/a_case");
        CommissionDefinition zCase = definition(later, List.of(), false);
        CommissionDefinition aCase = definition(first, List.of(), false);

        InvestigationBoardView view = InvestigationBoardView.from(
                data, Map.of(later, zCase, first, aCase), 0L,
                CaseEvidenceView.EMPTY);

        assertEquals(372L, view.balancePence());
        assertEquals(first.toString(), view.entries().get(0).id());
        assertEquals(later.toString(), view.entries().get(1).id());
    }

    private static CommissionDefinition definition(
            List<ResourceLocation> prerequisites, boolean repeatable) {
        return definition(CASE, prerequisites, repeatable);
    }

    private static CommissionDefinition definition(
            ResourceLocation commissionId,
            List<ResourceLocation> prerequisites,
            boolean repeatable) {
        return new CommissionDefinition(
                commissionId,
                "commission.test.title",
                "commission.test.summary",
                List.of("mist_city_outpost"),
                0,
                3,
                List.of("tracking", "divination"),
                new CommissionDefinition.Reward(24L, Map.of()),
                id("quest/test"),
                prerequisites,
                1000L,
                repeatable);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("lord_of_mysteries", path);
    }
}
