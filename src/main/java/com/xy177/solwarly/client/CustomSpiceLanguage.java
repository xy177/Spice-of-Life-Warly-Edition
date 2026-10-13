package com.xy177.solwarly.client;

import net.minecraft.client.Minecraft;

public final class CustomSpiceLanguage {
    public static String current() { return Minecraft.getInstance().getLanguageManager().getSelected(); }
    private CustomSpiceLanguage() {}
}
