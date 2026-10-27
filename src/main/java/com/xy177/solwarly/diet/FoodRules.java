// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/SOLSweetPotatoConfig.java
package com.xy177.solwarly.diet;
import com.xy177.solwarly.SOLWarlyConfig;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import java.util.*;
import java.util.regex.Pattern;
/** Warly food filters, synchronized independently of dietary progress. */
public final class FoodRules {
    private static FoodRules client;
    public final int minimumFoodValue;
    public final boolean shouldShowUneatenFoods;
    private final List<String> blacklist,whitelist;
    private FoodRules(int minimum,boolean show,List<String> blacklist,List<String> whitelist) {
        this.minimumFoodValue=minimum;this.shouldShowUneatenFoods=show;this.blacklist=blacklist;this.whitelist=whitelist;
    }
    public static FoodRules local() { return new FoodRules(SOLWarlyConfig.minimumFoodValue,SOLWarlyConfig.shouldShowUneatenFoods,
        Arrays.asList(SOLWarlyConfig.foodBlacklist),Arrays.asList(SOLWarlyConfig.foodWhitelist)); }
    public static FoodRules current() { return client==null?local():client; }
    public static void clearClient() { client=null; }
    public boolean hasWhitelist() { return !whitelist.isEmpty(); }
    public boolean isAllowed(ItemStack food) {
        String id=String.valueOf(food.getItem().getRegistryName());
        boolean listed=matches(id,hasWhitelist()?whitelist:blacklist)||matches(id+":"+food.getMetadata(),hasWhitelist()?whitelist:blacklist);
        return hasWhitelist()?listed:!listed;
    }
    private static boolean matches(String id,List<String> patterns) {
        for(String glob:patterns) {
            String[] parts=glob.split("\\*",-1);StringJoiner regex=new StringJoiner(".*");
            for(String part:parts)regex.add(Pattern.quote(part));
            if(id.matches(regex.toString()))return true;
        }
        return false;
    }
    public boolean isHearty(ItemStack food) {
        squeek.applecore.api.food.FoodValues values=squeek.applecore.api.AppleCoreAPI.accessor.getFoodValues(food);
        return values!=null&&values.hunger>=minimumFoodValue;
    }
    public boolean shouldCount(ItemStack food) { return isAllowed(food)&&isHearty(food); }
    public static NBTTagCompound snapshot() {
        FoodRules rules=local();NBTTagCompound tag=new NBTTagCompound();
        tag.setInteger("minimum",rules.minimumFoodValue);tag.setBoolean("uneaten",rules.shouldShowUneatenFoods);
        tag.setTag("blacklist",strings(rules.blacklist));tag.setTag("whitelist",strings(rules.whitelist));return tag;
    }
    private static NBTTagList strings(List<String> values) { NBTTagList list=new NBTTagList();for(String value:values)list.appendTag(new NBTTagString(value));return list; }
    private static List<String> strings(NBTTagList list) { List<String> values=new ArrayList<>();for(int i=0;i<list.tagCount();i++)values.add(list.getStringTagAt(i));return values; }
    public static void readSnapshot(NBTTagCompound tag) { client=new FoodRules(tag.getInteger("minimum"),tag.getBoolean("uneaten"),strings(tag.getTagList("blacklist",8)),strings(tag.getTagList("whitelist",8))); }
}
