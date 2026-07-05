package top.aurora.lordofmysteries.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import top.aurora.lordofmysteries.network.PlayerMysteryStatusS2CPacket;
import top.aurora.lordofmysteries.knowledge.KnowledgeText;

public final class MysteryStatusScreen extends Screen {

    private final PlayerMysteryStatusS2CPacket status;

    private MysteryStatusScreen(PlayerMysteryStatusS2CPacket status) {
        super(Component.translatable("screen.lord_of_mysteries.status.title"));
        this.status = status;
    }

    public static void open(PlayerMysteryStatusS2CPacket status) {
        Minecraft.getInstance().setScreen(new MysteryStatusScreen(status));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        int panelWidth = Math.min(360, width - 32);
        int left = (width - panelWidth) / 2;
        int top = Math.max(24, (height - 260) / 2);
        graphics.fill(left, top, left + panelWidth, top + 260, 0xE6100D16);
        graphics.fill(left, top, left + panelWidth, top + 2, 0xFF8B5CF6);

        graphics.drawCenteredString(font, title, width / 2, top + 14, 0xFFD9C7FF);
        int x = left + 18;
        int y = top + 42;

        if (status.pathway().isBlank()) {
            graphics.drawString(font, Component.translatable(
                    "screen.lord_of_mysteries.status.commoner"), x, y, 0xFFB8B0C5, false);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        line(graphics, x, y, "screen.lord_of_mysteries.status.pathway",
                Component.translatable(KnowledgeText.pathwayTranslationKey(
                        status.pathway())).getString() + " · " + status.sequence());
        y += 18;
        line(graphics, x, y, "screen.lord_of_mysteries.status.spirituality",
                oneDecimal(status.spirituality()) + " / " + oneDecimal(status.spiritualityMax()));
        y += 18;
        line(graphics, x, y, "screen.lord_of_mysteries.status.digestion",
                status.digestion() < 0f
                        ? Component.translatable("screen.lord_of_mysteries.status.hidden").getString()
                        : oneDecimal(status.digestion()) + "%");
        y += 18;
        line(graphics, x, y, "screen.lord_of_mysteries.status.pollution",
                oneDecimal(status.pollution()));
        y += 18;
        line(graphics, x, y, "screen.lord_of_mysteries.status.pressure",
                oneDecimal(status.insanityPressure()));
        y += 18;
        line(graphics, x, y, "screen.lord_of_mysteries.status.quality",
                Component.translatable(
                        "quality.lord_of_mysteries." + status.potionQuality()).getString());
        y += 28;

        graphics.drawString(font, Component.translatable(
                "screen.lord_of_mysteries.status.knowledge"), x, y, 0xFFD9C7FF, false);
        y += 16;
        if (status.knownKnowledge().isEmpty()) {
            graphics.drawString(font, Component.translatable(
                    "screen.lord_of_mysteries.status.no_knowledge"), x + 8, y, 0xFF8E8798, false);
        } else {
            int limit = Math.min(5, status.knownKnowledge().size());
            for (int i = 0; i < limit; i++) {
                graphics.drawString(font, Component.literal("• ").append(
                                Component.translatable(KnowledgeText.translationKey(
                                        status.knownKnowledge().get(i)))),
                        x + 8, y + i * 14, 0xFFB8B0C5, false);
            }
        }
        graphics.drawCenteredString(font, Component.translatable(
                "screen.lord_of_mysteries.status.guide_hint"),
                width / 2, top + 240, 0xFF8E8798);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void line(GuiGraphics graphics, int x, int y, String labelKey, String value) {
        graphics.drawString(font, Component.translatable(labelKey), x, y, 0xFF8E8798, false);
        graphics.drawString(font, value, x + 112, y, 0xFFE8E3F0, false);
    }

    private static String oneDecimal(float value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
