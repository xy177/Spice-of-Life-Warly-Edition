// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.communication;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.client.gui.GuiFoodBook;
import com.xy177.solwarly.item.ItemFoodBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class GuiHandler implements IGuiHandler {
	@SubscribeEvent
	public static void preInit(SOLWarly.PreInitializationEvent e) {
		NetworkRegistry.INSTANCE.registerGuiHandler(SOLWarly.instance, new GuiHandler());
	}
	
	@Nullable
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case com.xy177.solwarly.lunch.ItemLunchContainer.GUI_ID:
				return com.xy177.solwarly.lunch.ContainerLunch.validSlot(player, x)
					? new com.xy177.solwarly.lunch.ContainerLunch(player, x) : null;
			default:
				return null;
		}
	}
	
	@Nullable
	@Override
	@SideOnly(Side.CLIENT)
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
			case ItemFoodBook.GUI_ID:
				return new GuiFoodBook(player);
			case com.xy177.solwarly.lunch.ItemLunchContainer.GUI_ID:
				return com.xy177.solwarly.lunch.ContainerLunch.validSlot(player, x)
					? new com.xy177.solwarly.client.gui.GuiLunch(player, x) : null;
			default:
				return null;
		}
	}
}
