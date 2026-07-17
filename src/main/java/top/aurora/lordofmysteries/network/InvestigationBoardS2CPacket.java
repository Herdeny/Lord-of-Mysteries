package top.aurora.lordofmysteries.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import top.aurora.lordofmysteries.client.InvestigationBoardScreen;
import top.aurora.lordofmysteries.commission.CaseEvidenceView;
import top.aurora.lordofmysteries.commission.CommissionBoardState;
import top.aurora.lordofmysteries.commission.EvidenceState;
import top.aurora.lordofmysteries.commission.InvestigationBoardView;

public record InvestigationBoardS2CPacket(InvestigationBoardView view) {

    private static final int MAX_ENTRIES = 32;
    private static final int MAX_EVIDENCE_ENTRIES = 32;

    public static void encode(InvestigationBoardS2CPacket packet,
                              FriendlyByteBuf buffer) {
        InvestigationBoardView view = packet.view();
        buffer.writeLong(view.balancePence());
        buffer.writeUtf(view.activeCommissionId());
        buffer.writeUtf(view.activeTitleKey());
        buffer.writeUtf(view.activeGuidanceKey());
        buffer.writeVarInt(view.activeStep());
        buffer.writeVarInt(view.activeStepCount());
        buffer.writeVarInt(view.activeProgress());
        buffer.writeVarInt(view.activeTarget());
        int size = Math.min(MAX_ENTRIES, view.entries().size());
        buffer.writeVarInt(size);
        for (InvestigationBoardView.Entry entry : view.entries().subList(0, size)) {
            buffer.writeUtf(entry.id());
            buffer.writeUtf(entry.titleKey());
            buffer.writeUtf(entry.summaryKey());
            buffer.writeLong(entry.rewardPence());
            buffer.writeEnum(entry.state());
        }
        encodeEvidence(view.evidence(), buffer);
    }

    public static InvestigationBoardS2CPacket decode(FriendlyByteBuf buffer) {
        long balancePence = buffer.readLong();
        String activeCommissionId = buffer.readUtf(256);
        String activeTitleKey = buffer.readUtf(256);
        String activeGuidanceKey = buffer.readUtf(256);
        int activeStep = buffer.readVarInt();
        int activeStepCount = buffer.readVarInt();
        int activeProgress = buffer.readVarInt();
        int activeTarget = buffer.readVarInt();
        int size = Math.min(MAX_ENTRIES, Math.max(0, buffer.readVarInt()));
        List<InvestigationBoardView.Entry> entries = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            entries.add(new InvestigationBoardView.Entry(
                    buffer.readUtf(256),
                    buffer.readUtf(256),
                    buffer.readUtf(256),
                    buffer.readLong(),
                    buffer.readEnum(CommissionBoardState.class)));
        }
        CaseEvidenceView evidence = decodeEvidence(buffer);
        return new InvestigationBoardS2CPacket(new InvestigationBoardView(
                balancePence,
                activeCommissionId,
                activeTitleKey,
                activeGuidanceKey,
                activeStep,
                activeStepCount,
                activeProgress,
                activeTarget,
                entries,
                evidence));
    }

    private static void encodeEvidence(
            CaseEvidenceView evidence, FriendlyByteBuf buffer) {
        buffer.writeUtf(evidence.commissionId());
        buffer.writeUtf(evidence.caseTitleKey());
        buffer.writeVarInt(evidence.discovered());
        buffer.writeVarInt(evidence.total());
        buffer.writeBoolean(evidence.conclusionReady());
        int size = Math.min(MAX_EVIDENCE_ENTRIES, evidence.entries().size());
        buffer.writeVarInt(size);
        for (CaseEvidenceView.Entry entry : evidence.entries().subList(0, size)) {
            buffer.writeUtf(entry.titleKey());
            buffer.writeUtf(entry.detailKey());
            buffer.writeEnum(entry.state());
        }
    }

    private static CaseEvidenceView decodeEvidence(FriendlyByteBuf buffer) {
        String commissionId = buffer.readUtf(256);
        String caseTitleKey = buffer.readUtf(256);
        int discovered = Math.max(0, buffer.readVarInt());
        int total = Math.min(MAX_EVIDENCE_ENTRIES,
                Math.max(0, buffer.readVarInt()));
        discovered = Math.min(discovered, total);
        boolean conclusionReady = buffer.readBoolean();
        int size = Math.min(MAX_EVIDENCE_ENTRIES,
                Math.max(0, buffer.readVarInt()));
        List<CaseEvidenceView.Entry> entries = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            entries.add(new CaseEvidenceView.Entry(
                    buffer.readUtf(256),
                    buffer.readUtf(256),
                    buffer.readEnum(EvidenceState.class)));
        }
        return new CaseEvidenceView(
                commissionId, caseTitleKey, discovered, total,
                conclusionReady, entries);
    }

    public static void handle(InvestigationBoardS2CPacket packet,
                              Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT, () -> () -> InvestigationBoardScreen.open(packet.view())));
        context.setPacketHandled(true);
    }
}
