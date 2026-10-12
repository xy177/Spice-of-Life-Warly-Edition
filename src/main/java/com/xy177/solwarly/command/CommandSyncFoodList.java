// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.command;

import com.xy177.solwarly.tracking.CapabilityHandler;
import com.xy177.solwarly.tracking.FoodList;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

final class CommandSyncFoodList extends CommandFoodList.SubCommand {
	@Override
	public String getName() {
		return "sync";
	}
	
	@Override
	void execute(ICommandSender sender, EntityPlayer player, FoodList foodList) {
		CapabilityHandler.syncFoodList(player);
		
		showMessage(sender, localizedComponent("success"));
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}
}
