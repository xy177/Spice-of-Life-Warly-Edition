package com.xy177.solwarly.client;

import com.xy177.solwarly.compat.LegacyRules;
import com.xy177.solwarly.spice.CustomSpices;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="solwarly",value=Dist.CLIENT)
public final class ClientConnectionEvents {
    @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        LegacyRules.clearClient();CustomSpices.clearServerDefinitions();
        com.xy177.solwarly.diet.DietRuntime.clientCoefficients.clear();
    }
    private ClientConnectionEvents() {}
}
