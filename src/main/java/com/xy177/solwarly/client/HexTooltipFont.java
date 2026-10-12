package com.xy177.solwarly.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import java.util.*;

/** A tooltip-only font delegate; the RGB marker is invisible and survives line wrapping. */
public final class HexTooltipFont extends FontRenderer {
    private final FontRenderer original;
    public HexTooltipFont(FontRenderer original) {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().getTextureManager(), original.getUnicodeFlag());
        this.original = original; FONT_HEIGHT = original.FONT_HEIGHT;
    }
    private static final java.util.regex.Pattern MARKER = java.util.regex.Pattern.compile("§r((?:§[0-9a-fA-F]){6})§r");
    public static String mark(int color, String text) {
        StringBuilder marker = new StringBuilder("§r");
        for (char digit : String.format(Locale.ROOT, "%06X", color & 0xFFFFFF).toCharArray()) marker.append('§').append(digit);
        return marker.append("§r").append(text).toString();
    }
    public static boolean marked(String text) { return text != null && MARKER.matcher(text).find(); }
    public static String text(String value) {
        if (value == null) return null;
        java.util.regex.Matcher marker = MARKER.matcher(value);
        // Vanilla/JEI may prepend gray formatting. It must not override this line's RGB.
        return marker.find() ? value.substring(marker.end()) : value;
    }
    public static int color(String text, int fallback) {
        if (text == null) return fallback;
        java.util.regex.Matcher marker = MARKER.matcher(text);
        return marker.find() ? Integer.parseInt(marker.group(1).replace("§", ""), 16) : fallback;
    }
    @Override public int drawStringWithShadow(String text, float x, float y, int color) { return original.drawStringWithShadow(text(text), x, y, color(text, color)); }
    @Override public int drawString(String text, int x, int y, int color) { return original.drawString(text(text), x, y, color(text, color)); }
    @Override public int drawString(String text, float x, float y, int color, boolean shadow) { return original.drawString(text(text), x, y, color(text, color), shadow); }
    @Override public int getStringWidth(String text) { return original.getStringWidth(text(text)); }
    @Override public int getCharWidth(char c) { return original.getCharWidth(c); }
    @Override public boolean getUnicodeFlag() { return original == null ? super.getUnicodeFlag() : original.getUnicodeFlag(); }
    @Override public boolean getBidiFlag() { return original == null ? super.getBidiFlag() : original.getBidiFlag(); }
    @Override public List<String> listFormattedStringToWidth(String text, int width) {
        List<String> result = new ArrayList<>(original.listFormattedStringToWidth(text(text), width));
        if (marked(text)) for (int i = 0; i < result.size(); i++) result.set(i, mark(color(text, 0xFFFFFF), result.get(i)));
        return result;
    }
    @Override public String trimStringToWidth(String text, int width) { return trimStringToWidth(text, width, false); }
    @Override public String trimStringToWidth(String text, int width, boolean reverse) {
        String trimmed = original.trimStringToWidth(text(text), width, reverse);
        return marked(text) ? mark(color(text, 0xFFFFFF), trimmed) : trimmed;
    }
}
