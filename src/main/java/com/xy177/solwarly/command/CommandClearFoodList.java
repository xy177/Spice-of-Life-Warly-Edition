// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.command;

import com.xy177.solwarly.tracking.CapabilityHandler;
import com.xy177.solwarly.tracking.FoodList;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

final class CommandClearFoodList extends CommandFoodList.SubCommand {
	@Override
	public String getName() {
		return "clear";
	}
	
	@Override
	void execute(ICommandSender sender, EntityPlayer player, FoodList foodList) {
		foodList.clearFood();
		com.xy177.solwarly.diet.DietData diet = com.xy177.solwarly.diet.DietCapability.get(player);
		if (diet != null) diet.reset(false);
		CapabilityHandler.syncFoodList(player);
		
		showMessage(sender, localizedComponent("success"));
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}
}
