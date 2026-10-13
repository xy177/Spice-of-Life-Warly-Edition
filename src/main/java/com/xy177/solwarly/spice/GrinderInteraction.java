package com.xy177.solwarly.spice;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Sneaking with a powder must still reach the block's binding interaction. */
@Mod.EventBusSubscriber(modid = "solwarly")
public final class GrinderInteraction {
    @SubscribeEvent public static void rightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntityPlayer().isSneaking()
            && event.getWorld().getTileEntity(event.getPos()) instanceof TileEntitySpiceGrinder
            && (event.getItemStack().isEmpty() || event.getItemStack().getItem() == SpiceRegistry.spice)) {
            event.setUseBlock(Event.Result.ALLOW);
        }
    }
    private GrinderInteraction() {}
}
