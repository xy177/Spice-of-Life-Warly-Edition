package com.xy177.solwarly.client;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
@Mod.EventBusSubscriber(modid="solwarly",value=Side.CLIENT)
public final class ClientConnectionEvents {
    @SubscribeEvent public static void disconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        com.xy177.solwarly.diet.FoodRules.clearClient();
    }
    private ClientConnectionEvents() {}
}
