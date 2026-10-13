package com.xy177.solwarly.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.diet.*;
import com.xy177.solwarly.spice.*;
import com.xy177.solwarly.tracking.*;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import static net.minecraft.commands.Commands.*;

@Mod.EventBusSubscriber(modid=SOLWarly.MOD_ID)
public final class CommandWarly {
    @SubscribeEvent public static void register(RegisterCommandsEvent event) {
        var root=literal("solwarly");
        root.then(literal("status").executes(context->status(context.getSource().getPlayerOrException(),context.getSource()))
            .then(argument("player",EntityArgument.player()).requires(source->source.hasPermission(2)).executes(context->status(EntityArgument.getPlayer(context,"player"),context.getSource()))));
        root.then(literal("mode").requires(source->source.hasPermission(2)).then(argument("player",EntityArgument.player())
            .then(literal("true").executes(context->mode(context,1,"warly")))
            .then(literal("false").executes(context->mode(context,0,"normal")))
            .then(literal("default").executes(context->mode(context,-1,"default")))));
        root.then(literal("reload").requires(source->source.hasPermission(2)).executes(context-> {
            DietConfig.load(DietConfig.directory);SpiceConfig.load(DietConfig.directory);GrinderConfig.load(DietConfig.directory);
            BuiltinSpiceRecipes.reload();FoodCatalog.build(false,context.getSource().getServer());
            context.getSource().getServer().getPlayerList().getPlayers().forEach(player->{DietCapability.get(player).trim(DietConfig.recentCount);FoodList.get(player).invalidateProgressInfo();});
            context.getSource().getServer().getPlayerList().getPlayers().forEach(CapabilityHandler::syncFoodList);
            context.getSource().getServer().getPlayerList().getPlayers().forEach(player->{
                var effect=player.getEffect(SpiceRegistry.sweet);
                if(effect!=null) {
                    SpiceRegistry.sweet.removeAttributeModifiers(player,player.getAttributes(),effect.getAmplifier());
                    SpiceRegistry.sweet.addAttributeModifiers(player,player.getAttributes(),effect.getAmplifier());
                }
            });
            context.getSource().sendSuccess(()->Component.translatable("solwarly.command.done"),true);return 1;
        }));
        root.then(literal("regenerate").requires(source->source.hasPermission(2)).then(argument("food",StringArgumentType.string()).suggests((context,builder)->net.minecraft.commands.SharedSuggestionProvider.suggest(FoodCatalog.ids(),builder)).executes(context-> {
            String food=StringArgumentType.getString(context,"food");
            if(!FoodCatalog.ids().contains(food))throw new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(Component.translatable("solwarly.command.regenerate_usage")).create();
            FoodCatalog.regenerate(food,context.getSource().getServer());
            context.getSource().getServer().getPlayerList().getPlayers().forEach(CapabilityHandler::syncFoodList);
            context.getSource().sendSuccess(()->Component.translatable("solwarly.command.done"),true);return 1;
        })));
        root.then(literal("recalculate").requires(source->source.hasPermission(2)).then(argument("player",EntityArgument.player()).executes(context-> {
            var player=EntityArgument.getPlayer(context,"player");DietRuntime.initialize(player);
            DietCapability.get(player).recalculate(id->{var stack=FoodCatalog.stack(id);return stack.isEmpty()?Double.NaN:FoodCatalog.coefficient(stack);});
            CapabilityHandler.syncFoodList(player);context.getSource().sendSuccess(()->Component.translatable("solwarly.command.done"),true);return 1;
        })));
        root.then(literal("reset").requires(source->source.hasPermission(2)).then(argument("player",EntityArgument.player())
            .then(literal("keep-floor").executes(context->reset(context,false)))
            .then(literal("clear-floor").executes(context->reset(context,true)))));
        event.getDispatcher().register(root);
    }
    private static int mode(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> context,int value,String mode) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player=EntityArgument.getPlayer(context,"player");DietRuntime.initialize(player);DietCapability.get(player).setWarlyOverride(value);CapabilityHandler.syncFoodList(player);
        String key=mode.equals("warly")?"true":mode.equals("normal")?"false":"default";
        context.getSource().sendSuccess(()->Component.translatable("solwarly.command.mode_set",player.getName(),Component.translatable("solwarly.command.mode."+key)),true);return 1;
    }
    private static int reset(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> context,boolean floor) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player=EntityArgument.getPlayer(context,"player");FoodList.get(player).clearFood();DietCapability.get(player).reset(floor);CapabilityHandler.syncFoodList(player);
        context.getSource().sendSuccess(()->Component.translatable("solwarly.command.done"),true);return 1;
    }
    private static int status(net.minecraft.server.level.ServerPlayer player,net.minecraft.commands.CommandSourceStack source) {
        DietRuntime.initialize(player);var data=DietCapability.get(player);
        source.sendSuccess(()->Component.translatable("solwarly.command.status",player.getName(),com.xy177.solwarly.lib.DisplayNumbers.format(data.cumulative()),com.xy177.solwarly.lib.DisplayNumbers.format(data.diversity(DietConfig.extraFlavor,DietConfig.scale)),data.mealCount()),false);return 1;
    }
    private CommandWarly() {}
}
