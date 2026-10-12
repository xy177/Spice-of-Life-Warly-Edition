package com.xy177.solwarly.command;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.diet.*;
import com.xy177.solwarly.tracking.*;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import javax.annotation.Nullable;
import java.util.*;

public final class CommandWarly extends CommandBase {
    public String getName() { return "solwarly"; }
    public String getUsage(ICommandSender sender) { return "solwarly.command.usage"; }
    public int getRequiredPermissionLevel() { return 0; }
    @Override public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        boolean admin = sender.canUseCommand(2, getName());
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, admin
            ? Arrays.asList("status", "reload", "regenerate", "recalculate", "reset", "mode") : Collections.singletonList("status"));
        if (args.length == 2 && isUsernameIndex(args, 1) && (admin || args[0].equals("status")))
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        if (!admin) return Collections.emptyList();
        if (args.length == 2 && args[0].equals("regenerate")) return getListOfStringsMatchingLastWord(args, FoodCatalog.ids());
        if (args.length == 3 && args[0].equals("mode")) return getListOfStringsMatchingLastWord(args, "true", "false", "default");
        if (args.length == 3 && args[0].equals("reset")) return getListOfStringsMatchingLastWord(args, "keep-floor", "clear-floor");
        return Collections.emptyList();
    }
    @Override public boolean isUsernameIndex(String[] args, int index) {
        return index == 1 && args.length > 0 && Arrays.asList("status", "recalculate", "reset", "mode").contains(args[0]);
    }
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) throw new WrongUsageException(getUsage(sender));
        String action = args[0];
        if (!action.equals("status") && !sender.canUseCommand(2, getName())) throw new CommandException("commands.generic.permission");
        if (action.equals("reload")) {
            if (args.length != 1) throw new WrongUsageException(getUsage(sender));
            net.minecraftforge.common.config.ConfigManager.sync(SOLWarly.MOD_ID, net.minecraftforge.common.config.Config.Type.INSTANCE);
            DietConfig.load(DietConfig.directory); FoodCatalog.rebuild();
            com.xy177.solwarly.spice.GrinderConfig.load(DietConfig.directory);
            com.xy177.solwarly.spice.SpiceConfig.load(DietConfig.directory);
            com.xy177.solwarly.spice.BuiltinSpiceRecipes.reload();
            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                net.minecraft.potion.PotionEffect sweet = player.getActivePotionEffect(com.xy177.solwarly.spice.SpiceRegistry.sweet);
                if (sweet != null) {
                    com.xy177.solwarly.spice.SpiceRegistry.sweet.removeAttributesModifiersFromEntity(player, player.getAttributeMap(), sweet.getAmplifier());
                    com.xy177.solwarly.spice.SpiceRegistry.sweet.applyAttributesModifiersToEntity(player, player.getAttributeMap(), sweet.getAmplifier());
                }
                DietCapability.get(player).trim(DietConfig.recentCount); FoodList.get(player).updateProgressInfo(); CapabilityHandler.syncFoodList(player);
            }
        } else if (action.equals("regenerate")) {
            if (args.length != 2 || !FoodCatalog.ids().contains(args[1])) throw new WrongUsageException("solwarly.command.regenerate_usage");
            net.minecraftforge.common.config.Configuration config = new net.minecraftforge.common.config.Configuration(new java.io.File(DietConfig.directory, "solwarly-foods.cfg"));
            config.load(); config.getCategory("foods").remove(args[1]); config.save(); FoodCatalog.build();
            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) CapabilityHandler.syncFoodList(player);
        } else if (action.equals("mode")) {
            if (args.length != 3) throw new WrongUsageException("solwarly.command.mode_usage");
            EntityPlayerMP player = getPlayer(server, sender, args[1]);
            int override;
            if (args[2].equalsIgnoreCase("true")) override = 1;
            else if (args[2].equalsIgnoreCase("false")) override = 0;
            else if (args[2].equalsIgnoreCase("default")) override = -1;
            else throw new WrongUsageException("solwarly.command.mode_usage");
            DietRuntime.initialize(player);
            DietCapability.get(player).setWarlyOverride(override);
            CapabilityHandler.syncFoodList(player);
            sender.sendMessage(new TextComponentTranslation("solwarly.command.mode_set", player.getName(),
                new TextComponentTranslation("solwarly.command.mode." + args[2].toLowerCase(Locale.ROOT))));
            return;
        } else {
            if (!Arrays.asList("status", "reset", "recalculate").contains(action)) throw new WrongUsageException(getUsage(sender));
            if (args.length > (action.equals("reset") ? 3 : 2)) throw new WrongUsageException(getUsage(sender));
            EntityPlayerMP player = args.length >= 2 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);
            if (player != sender.getCommandSenderEntity() && !sender.canUseCommand(2, getName())) throw new CommandException("commands.generic.permission");
            DietRuntime.initialize(player); DietData data = DietCapability.get(player);
            if (action.equals("reset")) {
                if (args.length != 3 || !(args[2].equals("keep-floor") || args[2].equals("clear-floor")))
                    throw new WrongUsageException("solwarly.command.reset_usage");
                data.reset(args[2].equals("clear-floor")); FoodList.get(player).clearFood();
            } else if (action.equals("recalculate")) data.recalculate(id -> {
                net.minecraft.item.ItemStack food = FoodCatalog.stack(id);
                return food.isEmpty() ? Double.NaN : FoodCatalog.coefficient(food);
            });
            CapabilityHandler.syncFoodList(player);
            sender.sendMessage(new TextComponentTranslation("solwarly.command.status", player.getName(), com.xy177.solwarly.lib.DisplayNumbers.format(data.cumulative()),
                com.xy177.solwarly.lib.DisplayNumbers.format(data.diversity(DietConfig.extraFlavor, DietConfig.scale)), data.mealCount()));
        }
        if (!action.equals("status")) sender.sendMessage(new TextComponentTranslation("solwarly.command.done"));
    }
}
