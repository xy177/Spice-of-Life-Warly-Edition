// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/communication/FoodListMessage.java
package com.xy177.solwarly.communication;

import com.xy177.solwarly.tracking.FoodList;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class FoodListMessage {
	private CompoundTag dietNBT;

	public FoodListMessage(Player player) {
		dietNBT=com.xy177.solwarly.diet.DietRuntime.snapshot(player);
	}

	public FoodListMessage(FriendlyByteBuf buffer) {
		this.dietNBT = buffer.readNbt();
	}

	public void write(FriendlyByteBuf buffer) {
		buffer.writeNbt(dietNBT);
	}

	public void handle(Supplier<NetworkEvent.Context> context) {
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Handler.handle(this, context));
	}

	private static class Handler {
		static void handle(FoodListMessage message, Supplier<NetworkEvent.Context> context) {
			context.get().enqueueWork(() -> {
				Player player = Minecraft.getInstance().player;
				if(player==null)return;
				if(!message.dietNBT.isEmpty())com.xy177.solwarly.diet.DietRuntime.readSnapshot(player,message.dietNBT);
			});
			context.get().setPacketHandled(true);
		}
	}
}
