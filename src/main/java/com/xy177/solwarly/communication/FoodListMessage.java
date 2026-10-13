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
	private CompoundTag capabilityNBT;
	private CompoundTag dietNBT;
	
	public FoodListMessage(FoodList foodList) {
		this.capabilityNBT = foodList.serializeNBT();
		this.dietNBT = new CompoundTag();
	}
	public FoodListMessage(Player player) {
		capabilityNBT=FoodList.get(player).serializeNBT();
		dietNBT=com.xy177.solwarly.diet.DietRuntime.snapshot(player);
	}
	
	public FoodListMessage(FriendlyByteBuf buffer) {
		this.capabilityNBT = buffer.readNbt();
		this.dietNBT = buffer.readNbt();
	}
	
	public void write(FriendlyByteBuf buffer) {
		buffer.writeNbt(capabilityNBT);
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
				FoodList.get(player).deserializeNBT(message.capabilityNBT);
				if(!message.dietNBT.isEmpty())com.xy177.solwarly.diet.DietRuntime.readSnapshot(player,message.dietNBT);
			});
			context.get().setPacketHandled(true);
		}
	}
}
