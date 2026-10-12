// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.command;

import com.xy177.solwarly.tracking.FoodList;
import com.xy177.solwarly.tracking.ProgressInfo;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;

final class CommandSizeFoodList extends CommandFoodList.SubCommand {
	@Override
	public String getName() {
		return "size";
	}
	
	@Override
	void execute(ICommandSender sender, EntityPlayer player, FoodList foodList) {
		ProgressInfo progressInfo = foodList.getProgressInfo();
		
		ITextComponent progressDesc = localizedQuantityComponent("desc.foods_eaten", progressInfo.foodsEaten);
		showMessage(sender, progressDesc);
		
        com.xy177.solwarly.diet.DietRuntime.initialize(player);
        com.xy177.solwarly.diet.DietData diet = com.xy177.solwarly.diet.DietCapability.get(player);
        showMessage(sender, new net.minecraft.util.text.TextComponentTranslation("solwarly.command.status", player.getName(),
            com.xy177.solwarly.lib.DisplayNumbers.format(diet.cumulative()), com.xy177.solwarly.lib.DisplayNumbers.format(diet.diversity(com.xy177.solwarly.diet.DietConfig.extraFlavor, com.xy177.solwarly.diet.DietConfig.scale)), diet.mealCount()));
	}
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}
}
