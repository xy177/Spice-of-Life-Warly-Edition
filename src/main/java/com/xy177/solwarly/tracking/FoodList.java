// Adapted from Spice of Life: Apple Pie Edition FoodList; upstream contributors retain copyright.
// Ported and modified by xy177 on 2026-10-27; LGPL-2.1.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/tracking/FoodList.java
// Warly lifetime tracking, legacy API, save formats and progress adapter are retained.
// Earlier Carrot-based compatibility work remains acknowledged in THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.tracking;

import com.cazsius.solcarrot.api.FoodCapability;
import com.cazsius.solcarrot.api.SOLCarrotAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.StreamSupport;

@ParametersAreNonnullByDefault
public final class FoodList implements FoodCapability {
	private static final String NBT_KEY_FOOD_LIST = "foodList";
	private static final String NBT_KEY_PROGRESS_INFO = "progressInfo";

	public static FoodList get(EntityPlayer player) {
		FoodList foodList = (FoodList) player.getCapability(SOLCarrotAPI.foodCapability, null);
		assert foodList != null;
		return foodList;
	}

	// Apple Pie's keyed food archive, kept for lifetime/legacy compatibility.
    // Recency and flavors are indexed by FoodHistory; the legacy archive must never expire.
    private final Map<FoodInstance, FoodInstance> uniqueFoods = new HashMap<>();
	private final Set<String> missingFoods = new HashSet<>();
	private ProgressInfo progressInfo = new ProgressInfo(this);

	public FoodList() {}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == SOLCarrotAPI.foodCapability;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		return capability == SOLCarrotAPI.foodCapability ? (T) this : null;
	}

	/** used for persistent storage */
	@Override
	public NBTBase serializeNBT() {
		NBTTagList list = new NBTTagList();
		missingFoods.forEach(food -> list.appendTag(new NBTTagString(food)));
		uniqueFoods.values().stream()
			.map(FoodInstance::encode)
			.filter(Objects::nonNull)
			.map(NBTTagString::new)
			.forEach(list::appendTag);

		return list;
	}

	/** used for persistent storage */
	@Override
	public void deserializeNBT(NBTBase nbt) {
		NBTTagList list = (NBTTagList) nbt;

		uniqueFoods.clear();
		missingFoods.clear();
		for (int i = 0; i < list.tagCount(); i++) {
			String encoded = list.getStringTagAt(i);
			FoodInstance food = FoodInstance.decode(encoded);
			if (food == null) missingFoods.add(encoded); else uniqueFoods.put(food, food);
		}

		updateProgressInfo();
	}

	/** serializes everything, including the progress info, for sending to clients */
	public NBTTagCompound serializeFullNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag(NBT_KEY_FOOD_LIST, serializeNBT());
		tag.setTag(NBT_KEY_PROGRESS_INFO, progressInfo.getNBT());
		return tag;
	}

	/** deserializes everything, including the progress info, after receiving it from the server */
	public void deserializeFullNBT(NBTTagCompound tag) {
		deserializeNBT(tag.getTag(NBT_KEY_FOOD_LIST));
		progressInfo = new ProgressInfo(tag.getCompoundTag(NBT_KEY_PROGRESS_INFO));
	}

	/** @return true if the food was not previously known, i.e. if a new food has been tried */
	public boolean addFood(ItemStack food) {
		FoodInstance record = new FoodInstance(food);
		boolean wasAdded = uniqueFoods.put(record, record) == null && getConfigInfo().shouldCount(food);
		updateProgressInfo();
		return wasAdded;
	}

	@Override
	public boolean hasEaten(ItemStack itemStack) {
		return uniqueFoods.containsKey(new FoodInstance(itemStack));
	}

	public void clearFood() {
		uniqueFoods.clear();
		missingFoods.clear();
		updateProgressInfo();
	}

	public Set<FoodInstance> getEatenFoods() {
		return new HashSet<>(uniqueFoods.keySet());
	}

	public ProgressInfo getProgressInfo() {
		return progressInfo;
	}

	public ProgressInfo.ConfigInfo getConfigInfo() {
		return progressInfo.configInfo;
	}

	/** don't use this client-side! it'll overwrite it with client-side config values */
	public void updateProgressInfo() {
		progressInfo = new ProgressInfo(this);
	}

	@Override
	public int getEatenFoodCount() {
		return uniqueFoods.size();
	}

	public static final class Storage implements Capability.IStorage<FoodCapability> {
		@Override
		public NBTBase writeNBT(Capability<FoodCapability> capability, FoodCapability instance, EnumFacing side) {
			return instance.serializeNBT();
		}

		@Override
		public void readNBT(Capability<FoodCapability> capability, FoodCapability instance, EnumFacing side, NBTBase nbt) {
			instance.deserializeNBT(nbt);
		}
	}
}
