// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/command/FoodListCommand.java
// Earlier shared ancestry and notices are recorded in THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.command;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.lib.Localization;
import com.xy177.solwarly.tracking.CapabilityHandler;
import com.xy177.solwarly.tracking.FoodList;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Objects;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class FoodListCommand {
	private static final String name = "foodlist";

	@SubscribeEvent
	public static void register(RegisterCommandsEvent event) {
		event.getDispatcher().register(
			literal(name)
				.then(withPlayerArgumentOrSender(literal("size"), FoodListCommand::showFoodListSize))
				.then(withPlayerArgumentOrSender(literal("sync"), FoodListCommand::syncFoodList))
				.then(withPlayerArgumentOrSender(literal("clear"), FoodListCommand::clearFoodList))
		);
	}

	@FunctionalInterface
	private interface CommandWithPlayer {
		int run(CommandContext<CommandSourceStack> context, Player target) throws CommandSyntaxException;
	}

	static ArgumentBuilder<CommandSourceStack, ?> withPlayerArgumentOrSender(ArgumentBuilder<CommandSourceStack, ?> base, CommandWithPlayer command) {
		String target = "target";
		return base
			.executes((context) -> command.run(context, context.getSource().getPlayerOrException()))
			.then(argument(target, EntityArgument.player())
				.executes((context) -> command.run(context, EntityArgument.getPlayer(context, target)))
			);
	}

	static int showFoodListSize(CommandContext<CommandSourceStack> context, Player target) {
		sendFeedback(context.getSource(), localizedQuantityComponent("size.desc.foods_eaten", FoodList.get(target).getEatenFoods().size()));
		return Command.SINGLE_SUCCESS;
	}

	static int syncFoodList(CommandContext<CommandSourceStack> context, Player target) {
		CapabilityHandler.syncFoodList(target);

		sendFeedback(context.getSource(), localizedComponent("sync.success"));
		return Command.SINGLE_SUCCESS;
	}

	static int clearFoodList(CommandContext<CommandSourceStack> context, Player target) throws CommandSyntaxException {
		boolean isOp = context.getSource().hasPermission(2);
		boolean isTargetingSelf = isTargetingSelf(context, target);
		if (!isOp && !isTargetingSelf)
			throw new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(localizedComponent("no_permissions")).create();

		FoodList.get(target).clearFood();
        com.xy177.solwarly.diet.DietCapability.get(target).reset();
		CapabilityHandler.syncFoodList(target);

		var feedback = localizedComponent("clear.success");
		sendFeedback(context.getSource(), feedback);
		if (!isTargetingSelf) {
			target.displayClientMessage(applyFeedbackStyle(feedback), true);
		}

		return Command.SINGLE_SUCCESS;
	}

	static void sendFeedback(CommandSourceStack source, MutableComponent message) {
		source.sendSuccess(() -> applyFeedbackStyle(message), true);
	}

	private static MutableComponent applyFeedbackStyle(MutableComponent text) {
		return text.withStyle(ChatFormatting.DARK_AQUA);
	}

	static boolean isTargetingSelf(CommandContext<CommandSourceStack> context, Player target) {
		return target.is(Objects.requireNonNull(context.getSource().getEntity()));
	}

	static MutableComponent localizedComponent(String path, Object... args) {
		return Localization.localizedComponent("command", localizationPath(path), args);
	}

	static MutableComponent localizedQuantityComponent(String path, int number) {
		return Localization.localizedQuantityComponent("command", localizationPath(path), number);
	}

	static String localizationPath(String path) {
		return FoodListCommand.name + "." + path;
	}
}
