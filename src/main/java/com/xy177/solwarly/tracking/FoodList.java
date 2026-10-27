// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/tracking/FoodList.java
package com.xy177.solwarly.tracking;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import com.xy177.solwarly.api.FoodCapability;
import com.xy177.solwarly.diet.*;
import java.util.*;
/** Apple Pie-style queries backed by Warly's diet, with no parallel compatibility archive. */
public final class FoodList implements FoodCapability {
    private final Player player;
    private final DietData data;
    private FoodList(Player player) {
        this.player=player;
        this.data=Objects.requireNonNull(DietCapability.get(player), "Player diet is not attached");
    }
    public static FoodList get(Player player) { return new FoodList(player); }
    public boolean addFood(Item food) { return data.markTasted(FoodCatalog.id(new net.minecraft.world.item.ItemStack(food))) && com.xy177.solwarly.SOLWarlyConfig.shouldCount(food); }
    @Override public boolean hasEaten(Item food) { return data.hasTastedFood(FoodCatalog.id(new net.minecraft.world.item.ItemStack(food))); }
    @Override public double foodDiversity() { return data.diversity(player.level().isClientSide ? DietRuntime.clientExtraFlavor : DietConfig.extraFlavor,
        player.level().isClientSide ? DietRuntime.clientScale : DietConfig.scale); }
    public Set<FoodInstance> getEatenFoods() {
        Set<FoodInstance> result=new HashSet<>();
        for(String id:data.tastedFoods()) { FoodInstance food=FoodInstance.decode(id); if(food!=null)result.add(food); }
        return result;
    }
    public void clearFood() { data.reset(); }
}
