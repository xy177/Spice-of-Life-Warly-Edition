// Adapted from Spice of Life: Apple Pie Edition; upstream contributors retain copyright.
// Ported and modified by xy177 on 2026-10-27; LGPL-2.1.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/tracking/FoodInstance.java
// Warly rules are retained; upstream credits are recorded in THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public final class FoodInstance {
	public final Item item;
	public final int metadata;

	public FoodInstance(Item item, int metadata) {
		this.item = item;
		this.metadata = item.getHasSubtypes() ? metadata : 0;
	}

	public FoodInstance(net.minecraft.item.ItemStack stack) { this(stack.getItem(), stack.getMetadata()); }
	public net.minecraft.item.ItemStack getItemStack() { return new net.minecraft.item.ItemStack(item, 1, metadata); }

	@Nullable
	public static FoodInstance decode(String encoded) {
		String[] parts = encoded.split("@");
		ResourceLocation name = new ResourceLocation(parts[0]);
		int metadata = parts.length > 1 ? Integer.decode(parts[1]) : 0;

		// TODO it'd be nice to store (and maybe even count) references to missing items, in case the mod is added back in later
		Item item = ForgeRegistries.ITEMS.getValue(name);
		if (item == null) {
			SOLWarly.LOGGER.warn("attempting to load item into food list that is no longer registered: " + encoded + " (removing from list)");
			return null;
		}

		if (!squeek.applecore.api.AppleCoreAPI.accessor.isFood(new net.minecraft.item.ItemStack(item, 1, metadata))) {
			SOLWarly.LOGGER.warn("attempting to load item into food list that is no longer edible: " + encoded + " (ignoring in case it becomes edible again later)");
		}

		return new FoodInstance(item, metadata);
	}

	@Nullable
	public String encode() {
		return Optional.ofNullable(ForgeRegistries.ITEMS.getKey(item))
			.map(location -> location + "@" + metadata)
			.orElse(null);
	}

	@Override
	public int hashCode() {
		return 31 * (31 + (item == null ? 0 : item.hashCode())) + metadata;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FoodInstance)) return false;
		FoodInstance other = (FoodInstance) obj;

		return net.minecraft.item.ItemStack.areItemStacksEqual(getItemStack(), other.getItemStack());
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
