// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/tracking/FoodList.java
package com.xy177.solwarly.tracking;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import com.xy177.solwarly.api.FoodCapability;
import com.xy177.solwarly.diet.*;
import java.util.*;
/** Apple Pie-style queries backed by Warly's diet, with no parallel compatibility archive. */
public final class FoodList implements FoodCapability {
    private final EntityPlayer player;
    private final DietData data;
    private FoodList(EntityPlayer player) {
        this.player=player;
        this.data=Objects.requireNonNull(DietCapability.get(player), "Player diet is not attached");
    }
    public static FoodList get(EntityPlayer player) { return new FoodList(player); }
    public boolean addFood(ItemStack food) { return data.markTasted(FoodCatalog.id(food)) && com.xy177.solwarly.diet.FoodRules.current().shouldCount(food); }
    @Override public boolean hasEaten(ItemStack food) { return data.hasTastedFood(FoodCatalog.id(food)); }
    @Override public double foodDiversity() { return data.diversity(player.world.isRemote ? DietRuntime.clientExtraFlavor : DietConfig.extraFlavor,
        player.world.isRemote ? DietRuntime.clientScale : DietConfig.scale); }
    public Set<FoodInstance> getEatenFoods() {
        Set<FoodInstance> result=new HashSet<>();
        for(String id:data.tastedFoods()) { FoodInstance food=FoodInstance.decode(id); if(food!=null)result.add(food); }
        return result;
    }
    public void clearFood() { data.reset(); }
}
