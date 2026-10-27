// Adapted from Spice of Life: Apple Pie Edition; upstream contributors retain copyright.
// Ported and modified by xy177 on 2026-10-27; LGPL-2.1.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/tracking/FoodInstance.java
// Warly rules and legacy save/API compatibility are retained; see THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.world.item.*;
import net.minecraft.resources.ResourceLocation;


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
		ResourceLocation name = ResourceLocation.parse(encoded);

		// TODO it'd be nice to store (and maybe even count) references to missing items, in case the mod is added back in later
		Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(name);
		if (item == null) {
			SOLWarly.LOGGER.warn("attempting to load item into food list that is no longer registered: " + encoded + " (removing from list)");
			return null;
		}

		if (item.getDefaultInstance().getFoodProperties(null) == null) {
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
		if (!(obj instanceof FoodInstance)) return false;
		FoodInstance other = (FoodInstance) obj;

		return item.equals(other.item);
	}

	public Item getItem() {
		return item;
	}

	@Override
	public String toString() {
		String enc = encode();
		if (enc == null)
			return "null";
		else
			return enc;
	}
}
