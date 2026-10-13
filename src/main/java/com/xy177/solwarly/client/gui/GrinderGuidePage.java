package com.xy177.solwarly.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import java.awt.Rectangle;
import java.util.*;

final class GrinderGuidePage extends Page {
    private final List<FormattedCharSequence> lines;
    private GrinderGuidePage(Rectangle frame, List<FormattedCharSequence> lines) {
        super(frame, net.minecraft.client.resources.language.I18n.get("solwarly.book.grinder")); this.lines = lines;
    }
    static List<Page> pages(Rectangle frame) {
        var font = Minecraft.getInstance().font;
        var lines = new ArrayList<FormattedCharSequence>();
        for (int i = 1; i <= 5; i++) {
            lines.addAll(font.split(Component.translatable("solwarly.book.grinder." + i), frame.width - 62));
            lines.add(FormattedCharSequence.EMPTY);
        }
        var pages = new ArrayList<Page>();
        for (int first = 0; first < lines.size(); first += 9) pages.add(new GrinderGuidePage(frame, List.copyOf(lines.subList(first, Math.min(first + 9, lines.size())))));
        return pages;
    }
    @Override protected void render(GuiGraphics graphics) {
        super.render(graphics);
        int y = frame.y + 40;
        for (var line : lines) { graphics.drawString(Minecraft.getInstance().font, line, frame.x + 31, y, 0xFF000000, false); y += 10; }
    }
}
