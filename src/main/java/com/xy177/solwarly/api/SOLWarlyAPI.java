// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/api/SOLSweetPotatoAPI.java
package com.xy177.solwarly.api;
import net.minecraft.entity.player.EntityPlayer;
import com.xy177.solwarly.tracking.FoodList;
import com.xy177.solwarly.tracking.CapabilityHandler;
public final class SOLWarlyAPI {
    public static FoodCapability getFoodCapability(EntityPlayer player) { return FoodList.get(player); }
    public static void syncFoodList(EntityPlayer player) { CapabilityHandler.syncFoodList(player); }
    private SOLWarlyAPI() {}
}
