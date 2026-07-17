package top.aurora.lordofmysteries.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import top.aurora.lordofmysteries.commission.CommissionBoardState;
import top.aurora.lordofmysteries.commission.CommissionCurrency;
import top.aurora.lordofmysteries.commission.CaseEvidenceView;
import top.aurora.lordofmysteries.commission.InvestigationBoardView;
import top.aurora.lordofmysteries.network.InvestigationBoardActionC2SPacket;
import top.aurora.lordofmysteries.network.PMNetwork;

public final class InvestigationBoardScreen extends Screen {

    private static final int ROW_HEIGHT = 54;
    private final InvestigationBoardView view;
    private int page;
    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    private int visibleRows;
    private boolean evidenceMode;

    private InvestigationBoardScreen(InvestigationBoardView view) {
        super(Component.translatable(
                "screen.lord_of_mysteries.investigation_board.title"));
        this.view = view;
    }

    public static void open(InvestigationBoardView view) {
        Minecraft.getInstance().setScreen(new InvestigationBoardScreen(view));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(540, width - 24);
        panelHeight = Math.min(338, height - 20);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        visibleRows = Math.max(1, Math.min(4, (panelHeight - 120) / ROW_HEIGHT));
        buildButtons();
    }

    private void buildButtons() {
        clearWidgets();
        int itemCount = evidenceMode
                ? view.evidence().entries().size() : view.entries().size();
        int start = page * visibleRows;
        int end = Math.min(itemCount, start + visibleRows);
        if (!evidenceMode) {
            for (int index = start; index < end; index++) {
                InvestigationBoardView.Entry entry = view.entries().get(index);
                int rowY = top + 88 + (index - start) * ROW_HEIGHT;
                Button button = Button.builder(stateLabel(entry.state()), pressed -> {
                            PMNetwork.CHANNEL.sendToServer(
                                    new InvestigationBoardActionC2SPacket(
                                            InvestigationBoardActionC2SPacket.Action.ACCEPT,
                                            entry.id()));
                        })
                        .bounds(left + panelWidth - 104, rowY + 14, 86, 20)
                        .build();
                button.active = entry.state() == CommissionBoardState.AVAILABLE;
                addRenderableWidget(button);
            }
        }
        int pageCount = Math.max(1,
                (itemCount + visibleRows - 1) / visibleRows);
        if (pageCount > 1) {
            addRenderableWidget(Button.builder(Component.literal("<"), pressed -> {
                        page = Math.floorMod(page - 1, pageCount);
                        buildButtons();
                    }).bounds(left + 18, top + panelHeight - 26, 24, 20).build());
            addRenderableWidget(Button.builder(Component.literal(">"), pressed -> {
                        page = (page + 1) % pageCount;
                        buildButtons();
                    }).bounds(left + 46, top + panelHeight - 26, 24, 20).build());
        }
        if (!view.activeCommissionId().isBlank()) {
            addRenderableWidget(Button.builder(Component.translatable(
                            "screen.lord_of_mysteries.investigation_board.abandon"), pressed ->
                            PMNetwork.CHANNEL.sendToServer(
                                    new InvestigationBoardActionC2SPacket(
                                            InvestigationBoardActionC2SPacket.Action.ABANDON,
                                            view.activeCommissionId())))
                    .bounds(left + panelWidth - 202, top + 44, 86, 20)
                    .build());
        }
        addRenderableWidget(Button.builder(Component.translatable(
                        evidenceMode
                                ? "screen.lord_of_mysteries.investigation_board.cases"
                                : "screen.lord_of_mysteries.investigation_board.evidence"),
                pressed -> {
                    evidenceMode = !evidenceMode;
                    page = 0;
                    buildButtons();
                })
                .bounds(left + panelWidth - 104, top + 44, 86, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable(
                        "screen.lord_of_mysteries.investigation_board.close"),
                pressed -> onClose())
                .bounds(left + panelWidth - 104, top + panelHeight - 26, 86, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xF214111A);
        graphics.fill(left, top, left + panelWidth, top + 3, 0xFFB78A45);
        graphics.drawCenteredString(font, title, width / 2, top + 12, 0xFFF3DFC1);
        graphics.drawString(font, Component.translatable(
                        "screen.lord_of_mysteries.investigation_board.balance",
                        CommissionCurrency.format(view.balancePence())),
                left + 18, top + 30, 0xFFE0C48F, false);
        if (evidenceMode) {
            renderEvidenceHeader(graphics);
        } else {
            renderActiveCase(graphics);
        }
        int start = page * visibleRows;
        int itemCount = evidenceMode
                ? view.evidence().entries().size() : view.entries().size();
        int end = Math.min(itemCount, start + visibleRows);
        for (int index = start; index < end; index++) {
            int rowY = top + 88 + (index - start) * ROW_HEIGHT;
            if (evidenceMode) {
                renderEvidenceEntry(
                        graphics, view.evidence().entries().get(index), rowY);
            } else {
                renderEntry(graphics, view.entries().get(index), rowY);
            }
        }
        if (itemCount == 0) {
            graphics.drawCenteredString(font, Component.translatable(
                            evidenceMode
                                    ? "screen.lord_of_mysteries.evidence.empty"
                                    : "screen.lord_of_mysteries.investigation_board.empty"),
                    width / 2, top + 116, 0xFF9A909F);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderActiveCase(GuiGraphics graphics) {
        if (view.activeCommissionId().isBlank()) {
            graphics.drawString(font, Component.translatable(
                            "screen.lord_of_mysteries.investigation_board.no_active"),
                    left + 18, top + 50, 0xFF9A909F, false);
            return;
        }
        graphics.drawString(font, Component.translatable(
                        "screen.lord_of_mysteries.investigation_board.active",
                        Component.translatable(view.activeTitleKey())),
                left + 18, top + 48, 0xFFC6A7F7, false);
        graphics.drawString(font, Component.translatable(
                        "screen.lord_of_mysteries.investigation_board.progress",
                        view.activeStep(), view.activeStepCount(),
                        view.activeProgress(), view.activeTarget()),
                left + 18, top + 62, 0xFFAEA3B7, false);
        if (!view.activeGuidanceKey().isBlank()) {
            String guidance = font.plainSubstrByWidth(
                    Component.translatable(view.activeGuidanceKey()).getString(),
                    panelWidth - 36);
            graphics.drawString(font, guidance, left + 18, top + 74,
                    0xFF7F7588, false);
        }
    }

    private void renderEvidenceHeader(GuiGraphics graphics) {
        CaseEvidenceView evidence = view.evidence();
        if (evidence.commissionId().isBlank()) {
            graphics.drawString(font, Component.translatable(
                            "screen.lord_of_mysteries.evidence.no_active"),
                    left + 18, top + 50, 0xFF9A909F, false);
            return;
        }
        graphics.drawString(font, Component.translatable(
                        "screen.lord_of_mysteries.evidence.case",
                        Component.translatable(evidence.caseTitleKey())),
                left + 18, top + 48, 0xFFC6A7F7, false);
        graphics.drawString(font, Component.translatable(
                        "screen.lord_of_mysteries.evidence.count",
                        evidence.discovered(), evidence.total()),
                left + 18, top + 62, 0xFFAEA3B7, false);
        graphics.drawString(font, Component.translatable(
                        evidence.conclusionReady()
                                ? "screen.lord_of_mysteries.evidence.ready"
                                : "screen.lord_of_mysteries.evidence.incomplete"),
                left + 18, top + 74,
                evidence.conclusionReady() ? 0xFF8BC99A : 0xFF7F7588, false);
    }

    private void renderEntry(GuiGraphics graphics,
                             InvestigationBoardView.Entry entry, int rowY) {
        int color = entry.state() == CommissionBoardState.AVAILABLE
                ? 0xD42C2433 : 0xD41D1A22;
        graphics.fill(left + 12, rowY, left + panelWidth - 12,
                rowY + ROW_HEIGHT - 4, color);
        graphics.drawString(font, Component.translatable(entry.titleKey()),
                left + 20, rowY + 8, 0xFFE8D9F0, false);
        String summary = font.plainSubstrByWidth(
                Component.translatable(entry.summaryKey()).getString(), panelWidth - 154);
        graphics.drawString(font, summary, left + 20, rowY + 22, 0xFFA79EAD, false);
        graphics.drawString(font, Component.translatable(
                        "screen.lord_of_mysteries.investigation_board.reward",
                        CommissionCurrency.format(entry.rewardPence())),
                left + 20, rowY + 36, 0xFFD7B56D, false);
    }

    private void renderEvidenceEntry(
            GuiGraphics graphics,
            CaseEvidenceView.Entry entry,
            int rowY) {
        int color = switch (entry.state()) {
            case CONFIRMED -> 0xD4243028;
            case SUSPICIOUS -> 0xD43B2528;
            case MISSING -> 0xD41D1A22;
        };
        int stateColor = switch (entry.state()) {
            case CONFIRMED -> 0xFF8BC99A;
            case SUSPICIOUS -> 0xFFE08B78;
            case MISSING -> 0xFF807785;
        };
        graphics.fill(left + 12, rowY, left + panelWidth - 12,
                rowY + ROW_HEIGHT - 4, color);
        graphics.drawString(font, Component.translatable(entry.titleKey()),
                left + 20, rowY + 8, 0xFFE8D9F0, false);
        String detail = font.plainSubstrByWidth(
                Component.translatable(entry.detailKey()).getString(),
                panelWidth - 154);
        graphics.drawString(font, detail,
                left + 20, rowY + 23, 0xFFA79EAD, false);
        graphics.drawString(font, Component.translatable(
                        "screen.lord_of_mysteries.evidence.state."
                                + entry.state().name().toLowerCase(
                                        java.util.Locale.ROOT)),
                left + 20, rowY + 37, stateColor, false);
    }

    private static Component stateLabel(CommissionBoardState state) {
        return Component.translatable(
                "screen.lord_of_mysteries.investigation_board.state."
                        + state.name().toLowerCase(java.util.Locale.ROOT));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
