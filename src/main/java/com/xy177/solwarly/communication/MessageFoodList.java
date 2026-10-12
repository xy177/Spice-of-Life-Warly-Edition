// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.communication;

import com.xy177.solwarly.tracking.FoodList;
import com.xy177.solwarly.diet.DietRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public final class MessageFoodList extends NBTMessage {
	private NBTTagCompound capabilityNBT;
	
	public MessageFoodList(FoodList foodList) {
		this.capabilityNBT = foodList.serializeFullNBT();
	}
	public MessageFoodList(EntityPlayer player) {
		this.capabilityNBT = FoodList.get(player).serializeFullNBT();
		this.capabilityNBT.setTag("warly", DietRuntime.snapshot(player));
	}
	
	/** this is used when receiving, followed by a call to deserializeNBT */
	public MessageFoodList() {}
	
	@Override
	public NBTBase serializeNBT() {
		return capabilityNBT;
	}
	
	@Override
	public void deserializeNBT(NBTBase tag) {
		capabilityNBT = (NBTTagCompound) tag;
	}
	
	// message is only ever sent from server to client, so everything inside can use client-only methods just fine.
	public static final class Handler implements IMessageHandler<MessageFoodList, IMessage> {
		@Nullable
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(MessageFoodList message, MessageContext ctx) {
			// Always use a construct like this to actually handle your message.
			// This ensures that your 'handle' code is run on the main Minecraft
			// thread.
			// 'onMessage' itself is called on the networking thread so it is
			// not safe to do a lot of things here.
			FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
			return null;
		}
		
		@SideOnly(Side.CLIENT)
		private void handle(MessageFoodList message, MessageContext ctx) {
			EntityPlayer player = Minecraft.getMinecraft().player;
			if (player == null) return;
			FoodList.get(player).deserializeFullNBT(message.capabilityNBT);
            com.xy177.solwarly.client.FoodItemStacks.applyServerRules(FoodList.get(player).getConfigInfo());
			if (message.capabilityNBT.hasKey("warly")) DietRuntime.readSnapshot(player, message.capabilityNBT.getCompoundTag("warly"));
		}
	}
}
