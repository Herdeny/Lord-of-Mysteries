package top.aurora.lordofmysteries.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.client.ClientMysteryState;
import top.aurora.lordofmysteries.player.PlayerMysteryData;

/** Compact server-authoritative state used by HUD and lifecycle synchronization. */
public record PlayerMysterySummaryS2CPacket(
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
        boolean spiritVisionActive,
        boolean emotionReadActive) {

    public static PlayerMysterySummaryS2CPacket from(
            PlayerMysteryData data, boolean showExactDigestion) {
        return new PlayerMysterySummaryS2CPacket(
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
                data.spiritVisionActive,
                data.emotionReadActive);
    }

    public static void encode(PlayerMysterySummaryS2CPacket packet,
                              FriendlyByteBuf buffer) {
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
        buffer.writeBoolean(packet.spiritVisionActive);
        buffer.writeBoolean(packet.emotionReadActive);
    }

    public static PlayerMysterySummaryS2CPacket decode(FriendlyByteBuf buffer) {
        return new PlayerMysterySummaryS2CPacket(
                buffer.readUtf(),
                buffer.readVarInt(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readUtf(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readBoolean(),
                buffer.readBoolean());
    }

    public static void handle(PlayerMysterySummaryS2CPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> ClientMysteryState.update(packet)));
        context.setPacketHandled(true);
    }
}
