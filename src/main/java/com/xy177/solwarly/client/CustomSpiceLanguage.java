package com.xy177.solwarly.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "solwarly", value = Side.CLIENT)
public final class CustomSpiceLanguage {
    public static String current() { return Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode(); }
    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public static void disconnected(net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Minecraft.getMinecraft().addScheduledTask(com.xy177.solwarly.spice.CustomSpices::clearServerDefinitions);
    }
    private CustomSpiceLanguage() {}
}
