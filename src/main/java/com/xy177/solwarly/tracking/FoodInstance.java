// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/412fa94a07d93f09378fc0e3393de38eb0099824/src/main/java/com/cazsius/solcarrot/tracking/FoodInstance.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.Optional;

public final class FoodInstance {
	public final Item item;
	
	public FoodInstance(Item item) {
		this.item = item;
	}
	public FoodInstance(net.minecraft.world.item.ItemStack stack) { this(stack.getItem()); }
	public net.minecraft.world.item.ItemStack getItemStack() { return new net.minecraft.world.item.ItemStack(item); }
	
	@Nullable
	public static FoodInstance decode(String encoded) {
		ResourceLocation name = net.minecraft.resources.ResourceLocation.parse(encoded);
		
		// TODO it'd be nice to store (and maybe even count) references to missing items, in case the mod is added back in later
		Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(name);
		if (item == null) {
			SOLWarly.LOGGER.warn("attempting to load item into food list that is no longer registered: " + encoded + " (removing from list)");
			return null;
		}
		
		if (!(item.getDefaultInstance().getFoodProperties(null)!=null)) {
			SOLWarly.LOGGER.warn("attempting to load item into food list that is no longer edible: " + encoded + " (ignoring in case it becomes edible again later)");
		}
		
		return new FoodInstance(item);
	}
	
	@Nullable
	public String encode() {
		return Optional.ofNullable(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item))
			.map(ResourceLocation::toString)
			.orElse(null);
	}
	
	@Override
	public int hashCode() {
		return item.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FoodInstance other)) return false;
		
		return item.equals(other.item);
	}
	
	public Item getItem() {
		return item;
	}
}
