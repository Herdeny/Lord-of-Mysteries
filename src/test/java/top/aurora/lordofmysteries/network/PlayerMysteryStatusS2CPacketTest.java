package top.aurora.lordofmysteries.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import top.aurora.lordofmysteries.characteristic.CharacteristicBundle;
import top.aurora.lordofmysteries.characteristic.CharacteristicLoadLogic;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

class PlayerMysteryStatusS2CPacketTest {

    @Test
    void statusIncludesDerivedExtraCharacteristicLoad() {
        ResourceLocation seer = ResourceLocation.fromNamespaceAndPath(
                "lord_of_mysteries", "seer");
        PlayerMysteryData data = new PlayerMysteryData();
        data.pathway = seer;
        data.sequence = 7;
        data.characteristicBundles.add(new CharacteristicBundle(
                seer,
                7,
                List.of(
                        new CharacteristicBundle.Layer(9, 1, 0.95f),
                        new CharacteristicBundle.Layer(8, 1, 0.9f),
                        new CharacteristicBundle.Layer(7, 2, 0.8f)),
                CharacteristicBundle.Imprint.fresh(),
                0f,
                "a".repeat(64)));

        PlayerMysteryStatusS2CPacket packet =
                PlayerMysteryStatusS2CPacket.from(data, true);

        assertEquals(CharacteristicLoadLogic.extraLoad(data),
                packet.extraCharacteristicLoad());
        assertEquals(1, packet.extraCharacteristicLoad());
    }

    @Test
    void statusPacketRoundTripPreservesLoadAndKnowledge() {
        PlayerMysteryStatusS2CPacket expected =
                new PlayerMysteryStatusS2CPacket(
                        "lord_of_mysteries:seer",
                        7,
                        90f,
                        180f,
                        42f,
                        12f,
                        18f,
                        "complete",
                        30f,
                        15f,
                        2,
                        List.of("lord_of_mysteries:knowledge/example"));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        PlayerMysteryStatusS2CPacket.encode(expected, buffer);
        PlayerMysteryStatusS2CPacket restored =
                PlayerMysteryStatusS2CPacket.decode(buffer);

        assertEquals(expected, restored);
    }
}
