package top.aurora.lordofmysteries.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import top.aurora.lordofmysteries.commission.CommissionBoardState;
import top.aurora.lordofmysteries.commission.CommissionCurrency;
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
        int start = page * visibleRows;
        int end = Math.min(view.entries().size(), start + visibleRows);
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
        int pageCount = Math.max(1,
                (view.entries().size() + visibleRows - 1) / visibleRows);
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
        renderActiveCase(graphics);
        int start = page * visibleRows;
        int end = Math.min(view.entries().size(), start + visibleRows);
        for (int index = start; index < end; index++) {
            renderEntry(graphics, view.entries().get(index),
                    top + 88 + (index - start) * ROW_HEIGHT);
        }
        if (view.entries().isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable(
                            "screen.lord_of_mysteries.investigation_board.empty"),
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
