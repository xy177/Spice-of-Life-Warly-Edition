package com.xy177.solwarly.client;

import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.spice.CustomSpices;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid="solwarly",value=Dist.CLIENT)
public final class ClientConnectionEvents {
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        SOLWarlyConfig.clearClientRules();CustomSpices.clearServerDefinitions();
        com.xy177.solwarly.diet.DietRuntime.clientCoefficients.clear();
    }
    private ClientConnectionEvents() {}
}
