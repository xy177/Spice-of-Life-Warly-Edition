// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.communication;

import com.xy177.solwarly.SOLWarly;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class PacketHandler {
	public static final SimpleNetworkWrapper channel = NetworkRegistry.INSTANCE.newSimpleChannel(SOLWarly.MOD_ID);

	private static int packetID = 0;

	@SubscribeEvent
	public static void preInit(SOLWarly.PreInitializationEvent e) {
		channel.registerMessage(MessageFoodList.Handler.class, MessageFoodList.class, nextID(), Side.CLIENT);
	}

	private static int nextID() {
		return packetID++;
	}

	private PacketHandler() {}
}
