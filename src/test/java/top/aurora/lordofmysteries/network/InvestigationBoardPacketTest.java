package top.aurora.lordofmysteries.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.IntStream;

import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import net.minecraft.network.FriendlyByteBuf;

import top.aurora.lordofmysteries.commission.CommissionBoardState;
import top.aurora.lordofmysteries.commission.CaseAnalysisStage;
import top.aurora.lordofmysteries.commission.CaseEvidenceView;
import top.aurora.lordofmysteries.commission.CaseHypothesisStance;
import top.aurora.lordofmysteries.commission.CaseHypothesisStatus;
import top.aurora.lordofmysteries.commission.CaseHypothesisView;
import top.aurora.lordofmysteries.commission.EvidenceState;
import top.aurora.lordofmysteries.commission.EvidenceRelationKind;
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
                        50,
                        0,
                        1,
                        1,
                        false,
                        CaseAnalysisStage.CORRELATING,
                        "analysis.test.theory",
                        "analysis.test.next",
                        new CaseHypothesisView(
                                "clue_conflict",
                                CaseHypothesisStance.CONTRADICTS,
                                "The ink conflicts with the registry.",
                                CaseHypothesisStatus.DRAFT, 1, 2),
                        List.of(new CaseEvidenceView.Entry(
                                "evidence.test.title",
                                "evidence.test.detail",
                                EvidenceState.SUSPICIOUS)),
                        List.of(new CaseEvidenceView.Relation(
                                "clue_conflict",
                                "relation.test.title",
                                "relation.test.detail",
                                EvidenceRelationKind.CONTRADICTS,
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
        List<CaseEvidenceView.Relation> oversizedRelations = IntStream.range(0, 40)
                .mapToObj(index -> new CaseEvidenceView.Relation(
                        "relation_" + index,
                        "relation.test." + index,
                        "relation.test.detail",
                        EvidenceRelationKind.SUPPORTS,
                        EvidenceState.CONFIRMED))
                .toList();
        InvestigationBoardView oversizedView = new InvestigationBoardView(
                0L, "", "", "", 0, 0, 0, 0, oversizedEntries,
                new CaseEvidenceView("", "", 40, 40, 100,
                        40, 0, 0, true, CaseAnalysisStage.READY,
                        "analysis.test.theory", "analysis.test.next",
                        CaseHypothesisView.EMPTY,
                        oversizedEvidence, oversizedRelations));
        FriendlyByteBuf oversizedBuffer = new FriendlyByteBuf(Unpooled.buffer());

        InvestigationBoardS2CPacket.encode(
                new InvestigationBoardS2CPacket(oversizedView), oversizedBuffer);

        InvestigationBoardView decodedOversized =
                InvestigationBoardS2CPacket.decode(oversizedBuffer).view();
        assertEquals(32, decodedOversized.entries().size());
        assertEquals(32, decodedOversized.evidence().entries().size());
        assertEquals(32, decodedOversized.evidence().relations().size());
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
