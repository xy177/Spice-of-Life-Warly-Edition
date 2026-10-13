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
import java.util.Objects;
import java.util.Set;

@ParametersAreNonnullByDefault
public final class FoodList implements FoodCapability {
	private static final String NBT_KEY_FOOD_LIST = "foodList";
	
	public static FoodList get(Player player) {
		return player.getData(com.xy177.solwarly.diet.DietCapability.FOOD);
	}
	
	private final Set<FoodInstance> foods = new HashSet<>();
	
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
		foods.stream()
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
		
		foods.clear();
		list.stream()
			.map(nbt -> (StringTag) nbt)
			.map(StringTag::getAsString)
			.map(FoodInstance::decode)
			.filter(Objects::nonNull)
			.forEach(foods::add);
		
		invalidateProgressInfo();
	}
	
	/** @return true if the food was not previously known, i.e. if a new food has been tried */
	public boolean addFood(Item food) {
		boolean wasAdded = foods.add(new FoodInstance(food)) && SOLWarlyConfig.shouldCount(food);
		invalidateProgressInfo();
		return wasAdded;
	}
	
	@Override
	public boolean hasEaten(Item food) {
		if (!(food.getDefaultInstance().getFoodProperties(null)!=null)) return false;
		return foods.contains(new FoodInstance(food));
	}
	
	public void clearFood() {
		foods.clear();
		invalidateProgressInfo();
	}
	
	public Set<FoodInstance> getEatenFoods() {
		return new HashSet<>(foods);
	}
	
	// TODO: is this actually desirable? it doesn't filter at all
	@Override
	public int getEatenFoodCount() {
		return foods.size();
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
