package top.aurora.lordofmysteries.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.client.MysteryStatusScreen;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

public record PlayerMysteryStatusS2CPacket(
        String pathway,
        int sequence,
        float spirituality,
        float spiritualityMax,
        float digestion,
        float pollution,
        float insanityPressure,
        String potionQuality,
        float principleInsight,
        float roleOveridentification,
        List<String> knownKnowledge) {

    public static PlayerMysteryStatusS2CPacket from(PlayerMysteryData data, boolean showExactDigestion) {
        List<String> knowledge = data.knownKnowledge.stream()
                .map(Object::toString)
                .sorted()
                .toList();
        return new PlayerMysteryStatusS2CPacket(
                data.pathway == null ? "" : data.pathway.toString(),
                data.sequence,
                data.spirituality,
                data.spiritualityMax,
                showExactDigestion ? data.digestion : -1f,
                data.pollution,
                data.insanityPressure,
                data.potionQuality,
                data.principleInsight,
                data.roleOveridentification,
                knowledge);
    }

    public static void encode(PlayerMysteryStatusS2CPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.pathway);
        buffer.writeVarInt(packet.sequence);
        buffer.writeFloat(packet.spirituality);
        buffer.writeFloat(packet.spiritualityMax);
        buffer.writeFloat(packet.digestion);
        buffer.writeFloat(packet.pollution);
        buffer.writeFloat(packet.insanityPressure);
        buffer.writeUtf(packet.potionQuality);
        buffer.writeFloat(packet.principleInsight);
        buffer.writeFloat(packet.roleOveridentification);
        buffer.writeVarInt(packet.knownKnowledge.size());
        for (String id : packet.knownKnowledge) buffer.writeUtf(id);
    }

    public static PlayerMysteryStatusS2CPacket decode(FriendlyByteBuf buffer) {
        String pathway = buffer.readUtf();
        int sequence = buffer.readVarInt();
        float spirituality = buffer.readFloat();
        float spiritualityMax = buffer.readFloat();
        float digestion = buffer.readFloat();
        float pollution = buffer.readFloat();
        float pressure = buffer.readFloat();
        String quality = buffer.readUtf();
        float principleInsight = buffer.readFloat();
        float roleOveridentification = buffer.readFloat();
        int size = Math.min(256, Math.max(0, buffer.readVarInt()));
        List<String> knowledge = new ArrayList<>(size);
        for (int i = 0; i < size; i++) knowledge.add(buffer.readUtf());
        return new PlayerMysteryStatusS2CPacket(pathway, sequence, spirituality,
                spiritualityMax, digestion, pollution, pressure, quality,
                principleInsight, roleOveridentification, knowledge);
    }

    public static void handle(PlayerMysteryStatusS2CPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> MysteryStatusScreen.open(packet)));
        context.setPacketHandled(true);
    }
}
