package top.aurora.lordofmysteries.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.IntStream;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import net.minecraft.network.FriendlyByteBuf;

import top.aurora.lordofmysteries.commission.CommissionBoardState;
import top.aurora.lordofmysteries.commission.CaseEvidenceView;
import top.aurora.lordofmysteries.commission.EvidenceState;
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
                        CommissionBoardState.ACTIVE)),
                new CaseEvidenceView(
                        "lord_of_mysteries:commission/test",
                        "commission.test.title",
                        1,
                        2,
                        false,
                        List.of(new CaseEvidenceView.Entry(
                                "evidence.test.title",
                                "evidence.test.detail",
                                EvidenceState.SUSPICIOUS))));
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
        List<CaseEvidenceView.Entry> oversizedEvidence = IntStream.range(0, 40)
                .mapToObj(index -> new CaseEvidenceView.Entry(
                        "evidence.test." + index,
                        "evidence.test.detail",
                        EvidenceState.CONFIRMED))
                .toList();
        InvestigationBoardView oversizedView = new InvestigationBoardView(
                0L, "", "", "", 0, 0, 0, 0, oversizedEntries,
                new CaseEvidenceView("", "", 40, 40, true,
                        oversizedEvidence));
        FriendlyByteBuf oversizedBuffer = new FriendlyByteBuf(Unpooled.buffer());

        InvestigationBoardS2CPacket.encode(
                new InvestigationBoardS2CPacket(oversizedView), oversizedBuffer);

        InvestigationBoardView decodedOversized =
                InvestigationBoardS2CPacket.decode(oversizedBuffer).view();
        assertEquals(32, decodedOversized.entries().size());
        assertEquals(32, decodedOversized.evidence().entries().size());
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
