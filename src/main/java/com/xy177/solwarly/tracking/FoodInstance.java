// Modified by xy177 on 2026-10-10 for Spice of Life Warly Edition; upstream LGPL 2.1.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import squeek.applecore.api.AppleCoreAPI;

import javax.annotation.Nullable;
import java.util.Optional;

public final class FoodInstance {
	public final Item item;
	public final int metadata;
	
	public FoodInstance(ItemStack itemStack) {
		this(itemStack.getItem(), itemStack.getMetadata());
	}
	
	public FoodInstance(Item item, int metadata) {
		this.item = item;
		// e.g. actually additions coffee has metadata for how empty it is, but should only register once.
		this.metadata = item.getHasSubtypes() ? metadata : 0;
	}
	
	@Nullable
	public static FoodInstance decode(String encoded) {
		String[] parts = encoded.split("@");
		ResourceLocation name = new ResourceLocation(parts[0]);
		int meta;
		if (parts.length > 1) {
			meta = Integer.decode(parts[1]);
		} else {
			meta = 0;
		}
		
		// Match the canonical classes in the Carrot Edition 1.8.4 release.
		Item item = ForgeRegistries.ITEMS.getValue(name);
		if (item == null) {
			SOLWarly.LOGGER.warn("attempting to load item into food list that is no longer registered: " + encoded + " (ignoring)");
			return null;
		}
		ItemStack stack = new ItemStack(item, 1, meta);
		if (!AppleCoreAPI.accessor.isFood(stack)) {
			SOLWarly.LOGGER.warn("attempting to load item into food list that is no longer edible: " + encoded + " (ignoring in case it becomes edible again later)");
		}
		return new FoodInstance(stack);
	}
	
	@Nullable
	public String encode() {
		return Optional.ofNullable(ForgeRegistries.ITEMS.getKey(item))
			.map(location -> location + "@" + metadata)
			.orElse(null);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (item == null ? 0 : item.hashCode());
		result = prime * result + metadata;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FoodInstance)) return false;
		FoodInstance other = (FoodInstance) obj;
		
		return ItemStack.areItemStacksEqual(getItemStack(), other.getItemStack());
	}
	
	public ItemStack getItemStack() {
		return new ItemStack(item, 1, metadata);
	}
}
