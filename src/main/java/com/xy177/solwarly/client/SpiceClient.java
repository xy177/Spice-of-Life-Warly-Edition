package com.xy177.solwarly.client;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.spice.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid=SOLWarly.MOD_ID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class SpiceClient {
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) { event.registerBlockEntityRenderer(SpiceRegistry.GRINDER_ENTITY.get(),SpiceGrinderRenderer::new); }
    @SubscribeEvent public static void colors(RegisterColorHandlersEvent.Item event) { event.register((stack,layer)->0xFF000000|ItemSpice.color(stack,layer),SpiceRegistry.spice); }
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional event) { event.register(SpiceGrinderRenderer.STAND);event.register(SpiceGrinderRenderer.JAR); }
    @SubscribeEvent public static void reload(RegisterClientReloadListenersEvent event) { event.registerReloadListener((net.minecraft.server.packs.resources.ResourceManagerReloadListener)SpiceGrinderRenderer::loadAnimation); }
    @SubscribeEvent public static void setup(RegisterMenuScreensEvent event) {
        event.register(com.xy177.solwarly.lunch.LunchRegistry.MENU.get(),LunchScreen::new);
    }
    @SubscribeEvent public static void effects(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event) {
        for(PotionSpice effect:java.util.List.of(SpiceRegistry.sweet,SpiceRegistry.tasty,SpiceRegistry.aromatic,SpiceRegistry.hot,SpiceRegistry.appetizing))
            event.registerMobEffect(new SpiceEffectIcons(effect),effect);
    }
    private SpiceClient() {}
}
