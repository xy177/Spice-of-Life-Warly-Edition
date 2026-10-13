package com.xy177.solwarly.spice;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/** Sneaking with a powder must still reach the block's binding interaction. */
@EventBusSubscriber(modid = "solwarly")
public final class GrinderInteraction {
    @SubscribeEvent public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().isShiftKeyDown()
            && event.getLevel().getBlockEntity(event.getPos()) instanceof TileEntitySpiceGrinder
            && (event.getItemStack().isEmpty() || event.getItemStack().getItem() == SpiceRegistry.spice)) {
            event.setUseBlock(TriState.TRUE);
        }
    }
    private GrinderInteraction() {}
}
