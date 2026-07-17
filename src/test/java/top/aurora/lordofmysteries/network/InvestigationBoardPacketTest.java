package top.aurora.lordofmysteries.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.IntStream;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import net.minecraft.network.FriendlyByteBuf;

import top.aurora.lordofmysteries.commission.CommissionBoardState;
import top.aurora.lordofmysteries.commission.InvestigationBoardView;

class InvestigationBoardPacketTest {

    @Test
    void boardViewRoundTripsWithoutLosingServerState() {
        InvestigationBoardView view = new InvestigationBoardView(
                372L,
                "lord_of_mysteries:commission/test",
                "commission.test.title",
                "quest.test.step",
                2,
                6,
                1,
                3,
                List.of(new InvestigationBoardView.Entry(
                        "lord_of_mysteries:commission/test",
                        "commission.test.title",
                        "commission.test.summary",
                        144L,
                        CommissionBoardState.ACTIVE)));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        InvestigationBoardS2CPacket.encode(
                new InvestigationBoardS2CPacket(view), buffer);

        assertEquals(view, InvestigationBoardS2CPacket.decode(buffer).view());

        List<InvestigationBoardView.Entry> oversizedEntries = IntStream.range(0, 40)
                .mapToObj(index -> new InvestigationBoardView.Entry(
                        "lord_of_mysteries:commission/test_" + index,
                        "commission.test.title",
                        "commission.test.summary",
                        index,
                        CommissionBoardState.AVAILABLE))
                .toList();
        InvestigationBoardView oversizedView = new InvestigationBoardView(
                0L, "", "", "", 0, 0, 0, 0, oversizedEntries);
        FriendlyByteBuf oversizedBuffer = new FriendlyByteBuf(Unpooled.buffer());

        InvestigationBoardS2CPacket.encode(
                new InvestigationBoardS2CPacket(oversizedView), oversizedBuffer);

        assertEquals(32,
                InvestigationBoardS2CPacket.decode(oversizedBuffer).view().entries().size());
    }

    @Test
    void boardActionRoundTripsAsIntentOnly() {
        InvestigationBoardActionC2SPacket packet =
                new InvestigationBoardActionC2SPacket(
                        InvestigationBoardActionC2SPacket.Action.ACCEPT,
                        "lord_of_mysteries:commission/test");
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        InvestigationBoardActionC2SPacket.encode(packet, buffer);

        assertEquals(packet, InvestigationBoardActionC2SPacket.decode(buffer));
    }
}
