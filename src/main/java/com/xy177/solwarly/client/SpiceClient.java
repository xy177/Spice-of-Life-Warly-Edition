package com.xy177.solwarly.client;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.spice.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=SOLWarly.MOD_ID,value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class SpiceClient {
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) { event.registerBlockEntityRenderer(SpiceRegistry.GRINDER_ENTITY.get(),SpiceGrinderRenderer::new); }
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Item event) { event.register(ItemSpice::color,SpiceRegistry.spice); }
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional event) { event.register(SpiceGrinderRenderer.STAND);event.register(SpiceGrinderRenderer.JAR); }
    @SubscribeEvent public static void reload(RegisterClientReloadListenersEvent event) { event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)SpiceGrinderRenderer::loadAnimation); }
    @SubscribeEvent public static void setup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(()->net.minecraft.client.gui.screens.MenuScreens.register(com.xy177.solwarly.lunch.LunchRegistry.MENU.get(),LunchScreen::new));
    }
    private SpiceClient() {}
}
