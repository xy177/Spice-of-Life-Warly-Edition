// Adapted from Spice of Life: Apple Pie Edition FoodList; upstream contributors retain copyright.
// Ported and modified by xy177 on 2026-10-27; LGPL-2.1.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/tracking/FoodList.java
// Warly lifetime tracking, legacy API, save formats and progress adapter are retained.
// Earlier Carrot-based compatibility work remains acknowledged in THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.tracking;

import com.xy177.solwarly.SOLWarlyConfig;
import com.cazsius.solcarrot.api.FoodCapability;
import com.cazsius.solcarrot.api.SOLCarrotAPI;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@ParametersAreNonnullByDefault
public final class FoodList implements FoodCapability {
	private static final String NBT_KEY_FOOD_LIST = "foodList";

	public static FoodList get(Player player) {
		return player.getData(com.xy177.solwarly.diet.DietCapability.FOOD);
	}

	// Apple Pie's keyed food archive, kept for lifetime/legacy compatibility.
    // Recency and flavors are indexed by FoodHistory; the legacy archive must never expire.
    private final Map<FoodInstance, FoodInstance> uniqueFoods = new HashMap<>();

	@Nullable
	private ProgressInfo cachedProgressInfo;

	public FoodList() {}
    @Override public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) { return serializeNBT(); }
    @Override public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider,CompoundTag tag) { deserializeNBT(tag); }
    @Override public boolean hasEaten(net.minecraft.world.item.ItemStack stack) { return hasEaten(stack.getItem()); }


	/** used for persistent storage */
	public CompoundTag serializeNBT() {
		var tag = new CompoundTag();

		var list = new ListTag();
		uniqueFoods.values().stream()
			.map(FoodInstance::encode)
			.filter(Objects::nonNull)
			.map(StringTag::valueOf)
			.forEach(list::add);
		tag.put(NBT_KEY_FOOD_LIST, list);

		return tag;
	}

	/** used for persistent storage */
	public void deserializeNBT(CompoundTag tag) {
		var list = tag.getList(NBT_KEY_FOOD_LIST, Tag.TAG_STRING);

		uniqueFoods.clear();
		list.stream()
			.map(nbt -> (StringTag) nbt)
			.map(StringTag::getAsString)
			.map(FoodInstance::decode)
			.filter(Objects::nonNull)
			.forEach(food -> uniqueFoods.put(food, food));

		invalidateProgressInfo();
	}

	/** @return true if the food was not previously known, i.e. if a new food has been tried */
	public boolean addFood(Item food) {
		FoodInstance record = new FoodInstance(food);
		boolean wasAdded = uniqueFoods.put(record, record) == null && SOLWarlyConfig.shouldCount(food);
		invalidateProgressInfo();
		return wasAdded;
	}

	@Override
	public boolean hasEaten(Item food) {
		if (!(food.getDefaultInstance().getFoodProperties(null)!=null)) return false;
		return uniqueFoods.containsKey(new FoodInstance(food));
	}

	public void clearFood() {
		uniqueFoods.clear();
		invalidateProgressInfo();
	}

	public Set<FoodInstance> getEatenFoods() {
		return new HashSet<>(uniqueFoods.keySet());
	}

	// TODO: is this actually desirable? it doesn't filter at all
	@Override
	public int getEatenFoodCount() {
		return uniqueFoods.size();
	}

	public ProgressInfo getProgressInfo() {
		if (cachedProgressInfo == null) {
			cachedProgressInfo = new ProgressInfo(this);
		}
		return cachedProgressInfo;
	}

	public void invalidateProgressInfo() {
		cachedProgressInfo = null;
	}

	public static class FoodListNotFoundException extends RuntimeException {
		public FoodListNotFoundException() {
			super("Player must have food capability attached, but none was found.");
		}
	}
}
