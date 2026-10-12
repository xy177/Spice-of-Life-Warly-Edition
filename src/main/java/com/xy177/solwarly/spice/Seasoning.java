package com.xy177.solwarly.spice;

import com.xy177.solwarly.api.FlavorAPI;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import java.util.*;

/** Preserves actual seasoning components while all blends share one dietary flavor. */
public final class Seasoning {
    public static final String FOOD_KEY = "SolWarlySeasoning", COMPONENTS = "SpiceComponents";
    public static final String MIXED_FLAVOR = "solwarly:mixed";
    public static String canonicalFlavor(String id) {
        return id.startsWith(MIXED_FLAVOR + ":") ? MIXED_FLAVOR : id;
    }
    public static List<SpiceKind> components(ItemStack stack) {
        if (!stack.hasTagCompound()) return Collections.emptyList();
        NBTTagCompound tag = stack.getItem() == SpiceRegistry.spice ? stack.getTagCompound() : stack.getTagCompound().getCompoundTag(FOOD_KEY);
        NBTTagList list = tag.getTagList(COMPONENTS, 8);
        List<SpiceKind> result = new ArrayList<>();
        for (int i = 0; i < list.tagCount(); i++) {
            SpiceKind kind = SpiceKind.from(list.getStringTagAt(i));
            if (kind == null) return Collections.emptyList(); result.add(kind);
        }
        if (result.isEmpty() && stack.getItem() == SpiceRegistry.spice) {
            SpiceKind kind = SpiceKind.from(ItemSpice.flavor(stack)); if (kind != null) result.add(kind);
        }
        if (result.size() != 1 && result.size() != 3 || result.size() == 3 && result.contains(SpiceKind.RICE)) return Collections.emptyList();
        Collections.sort(result); return Collections.unmodifiableList(result);
    }
    public static boolean seasoned(ItemStack food) { return food.hasTagCompound() && food.getTagCompound().hasKey(FOOD_KEY); }
    public static boolean rice(ItemStack stack) { return !seasoned(stack) && stack.getItem().getRegistryName() != null
        && stack.getItem().getRegistryName().toString().equals("farmersdelight:cooked_rice"); }
    public static ItemStack plain(ItemStack stack) {
        if (!seasoned(stack)) return stack;
        ItemStack copy = stack.copy(); copy.getTagCompound().removeTag(FOOD_KEY);
        if (copy.getTagCompound().isEmpty()) copy.setTagCompound(null); return copy;
    }
    public static String identity(List<SpiceKind> components) {
        List<SpiceKind> sorted = new ArrayList<>(components); Collections.sort(sorted);
        StringJoiner id = new StringJoiner("+"); for (SpiceKind kind : sorted) id.add(kind.id());
        return (components.size() > 1 ? "mixed:" : "") + id;
    }
    public static int color(List<SpiceKind> components) {
        if (components.isEmpty()) return 0xFFFFFF;
        int r = 0, g = 0, b = 0;
        for (SpiceKind kind : components) { int c = kind.color(); r += c >> 16 & 255; g += c >> 8 & 255; b += c & 255; }
        return (r / components.size() << 16) | (g / components.size() << 8) | b / components.size();
    }
    private static NBTTagCompound data(List<SpiceKind> components) {
        List<SpiceKind> sorted = new ArrayList<>(components); Collections.sort(sorted);
        NBTTagCompound tag = new NBTTagCompound(); NBTTagList list = new NBTTagList();
        for (SpiceKind kind : sorted) list.appendTag(new NBTTagString(kind.id())); tag.setTag(COMPONENTS, list); return tag;
    }
    public static ItemStack powder(List<SpiceKind> components, int count) {
        if (components.isEmpty() || components.contains(SpiceKind.RICE) || components.size() != 1 && components.size() != 3)
            throw new IllegalArgumentException("Powder requires one or three powder components");
        ItemStack stack = ItemSpice.create(identity(components), color(components), count);
        stack.getTagCompound().setTag(COMPONENTS, data(components).getTagList(COMPONENTS, 8)); return stack;
    }
    public static ItemStack apply(ItemStack food, List<SpiceKind> components) {
        return apply(food, components, color(components));
    }
    public static ItemStack apply(ItemStack food, List<SpiceKind> components, int color) {
        if (seasoned(food) || components.isEmpty()) throw new IllegalArgumentException("Food must be unseasoned");
        ItemStack result = food.copy(); result.setCount(1);
        NBTTagCompound seasoning = data(components); seasoning.setInteger(ItemSpice.COLOR_KEY, color & 0xFFFFFF);
        if (!result.hasTagCompound()) result.setTagCompound(new NBTTagCompound()); result.getTagCompound().setTag(FOOD_KEY, seasoning); return result;
    }
    public static int color(ItemStack stack) {
        if (stack.getItem() == SpiceRegistry.spice) return ItemSpice.color(stack, 0);
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag(FOOD_KEY) : new NBTTagCompound();
        return tag.hasKey(ItemSpice.COLOR_KEY) ? tag.getInteger(ItemSpice.COLOR_KEY) & 0xFFFFFF : color(components(stack));
    }
    public static FlavorAPI.Flavor flavor(ItemStack food) {
        List<SpiceKind> kinds = components(food); if (!seasoned(food) || kinds.isEmpty()) return null;
        return new FlavorAPI.Flavor(kinds.size() == 3 ? MIXED_FLAVOR : "solwarly:" + identity(kinds),
            kinds.size() == 3 ? SpiceConfig.mixedMultiplier : kinds.get(0).multiplier());
    }
    public static double durationScale(int copies) { return copies >= 3 ? SpiceConfig.tripleDuration : copies == 2 ? SpiceConfig.doubleDuration : 1; }
    public static Collection<ItemStack> variants(ItemStack plainFood) {
        List<ItemStack> result = new ArrayList<>();
        List<SpiceKind> powders = new ArrayList<>();
        for (SpiceKind kind : SpiceKind.all()) if (kind.available()) {
            result.add(apply(plainFood, Collections.singletonList(kind)));
            if (kind != SpiceKind.RICE) powders.add(kind);
        }
        if (!powders.isEmpty()) result.add(apply(plainFood, Collections.nCopies(3, powders.get(0))));
        return result;
    }
    private Seasoning() {}
}
