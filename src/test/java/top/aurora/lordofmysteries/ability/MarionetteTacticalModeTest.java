package top.aurora.lordofmysteries.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.CompoundTag;

class MarionetteTacticalModeTest {

    @Test
    void modesHaveSafeFallbackAndExplicitCombatRules() {
        assertTrue(MarionetteTacticalMode.isValidId("guard"));
        assertTrue(MarionetteTacticalMode.isValidId(" PASSIVE "));
        assertFalse(MarionetteTacticalMode.isValidId("attack_players"));
        assertEquals(
                MarionetteTacticalMode.FOLLOW,
                MarionetteTacticalMode.fromId("invalid"));
        assertTrue(MarionetteTacticalMode.GUARD.allowsCombat());
        assertFalse(MarionetteTacticalMode.PASSIVE.allowsCombat());
    }

    @Test
    void storedEntityPayloadKeepsTacticalMode() {
        UUID entityId = UUID.fromString(
                "00000000-0000-0000-0000-000000000031");
        CompoundTag payload = new CompoundTag();
        payload.putUUID("UUID", entityId);
        payload.putString("id", "minecraft:zombie");
        CompoundTag forgeData = new CompoundTag();
        forgeData.putString(
                MarionetteService.TACTICAL_MODE_TAG, "guard");
        payload.put("ForgeData", forgeData);
        CompoundTag record = MarionetteStoragePolicy.createRecord(
                UUID.fromString(
                        "00000000-0000-0000-0000-000000000032"),
                payload);

        assertEquals(
                MarionetteTacticalMode.GUARD,
                MarionetteService.storedTacticalMode(record));
    }
}
