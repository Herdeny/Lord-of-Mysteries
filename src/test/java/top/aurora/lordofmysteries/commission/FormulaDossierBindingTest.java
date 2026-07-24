package top.aurora.lordofmysteries.commission;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class FormulaDossierBindingTest {

    @Test
    void dossierSeedSeparatesPlayersAndAcceptanceInstances() {
        UUID first = UUID.fromString(
                "00000000-0000-0000-0000-000000000001");
        UUID second = UUID.fromString(
                "00000000-0000-0000-0000-000000000002");
        long seed = FormulaAppraisalLogic.dossierSeed(77219L, first, 240L);
        CompoundTag tag = new CompoundTag();
        tag.putLong("lom_formula_seed", seed);

        assertTrue(FormulaAppraisalService.matchesSeed(tag, seed));
        assertFalse(FormulaAppraisalService.matchesSeed(tag,
                FormulaAppraisalLogic.dossierSeed(77219L, second, 240L)));
        assertFalse(FormulaAppraisalService.matchesSeed(tag,
                FormulaAppraisalLogic.dossierSeed(77219L, first, 24_240L)));
        assertFalse(FormulaAppraisalService.matchesSeed(
                new CompoundTag(), seed));
    }
}
