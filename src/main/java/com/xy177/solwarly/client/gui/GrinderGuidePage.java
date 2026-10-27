package com.xy177.solwarly.client.gui;
import net.minecraft.client.resources.I18n;
import java.awt.Rectangle;
import java.util.*;
final class GrinderGuidePage extends Page {
    private final List<String> lines;
    private GrinderGuidePage(Rectangle frame, List<String> lines) {
        super(frame, I18n.format("solwarly.book.grinder")); this.lines = lines;
    }
    static List<Page> pages(Rectangle frame) {
        List<String> lines = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            lines.addAll(fontRenderer.listFormattedStringToWidth(I18n.format("solwarly.book.grinder." + i), Math.max(1, frame.width - 70)));
            lines.add("");
        }
        List<Page> pages = new ArrayList<>();
        for (int i = 0; i < lines.size(); i += 9) pages.add(new GrinderGuidePage(frame, new ArrayList<>(lines.subList(i, Math.min(i + 9, lines.size())))));
        return pages;
    }
    @Override protected void render() {
        super.render(); int y = frame.y + 40;
        for (String line : lines) { fontRenderer.drawString(line, frame.x + (frame.width - contentWidth) / 2, y, 0); y += 10; }
    }
}
