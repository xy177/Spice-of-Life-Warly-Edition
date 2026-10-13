package com.xy177.solwarly.spice;

import com.xy177.solwarly.api.FlavorAPI;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import java.util.*;

/** Keeps dietary flavor separate from actual ingredient amounts and crafting applications. */
public final class Seasoning {
    public static final String FOOD_KEY = "SolWarlySeasoning", COMPONENTS = "SpiceComponents";
    public static final String MIXED_FLAVOR = "solwarly:mixed";
    private static final String AMOUNTS = "SpiceAmounts", USES = "PowderUses", MIXED = "MixedPowder", BADGES = "SideBadges";
    public static String canonicalFlavor(String id) { return id.startsWith(MIXED_FLAVOR + ":") ? MIXED_FLAVOR : id; }
    public static List<SpiceKind> components(ItemStack stack) {
        if (!stack.hasTagCompound()) return Collections.emptyList();
        NBTTagCompound tag = stack.getItem() == SpiceRegistry.spice ? stack.getTagCompound() : stack.getTagCompound().getCompoundTag(FOOD_KEY);
        try {
            if (tag.hasKey(AMOUNTS)) {
                NBTTagCompound counts = tag.getCompoundTag(AMOUNTS); Map<SpiceKind, Long> values = new TreeMap<>();
                for (String id : counts.getKeySet()) { SpiceKind kind = SpiceKind.from(id); if (kind == null) return Collections.emptyList(); values.put(kind, counts.getLong(id)); }
                List<SpiceKind> first = new ArrayList<>(); NBTTagList badges = tag.getTagList(BADGES, 8);
                for (int i = 0; i < Math.min(2, badges.tagCount()); i++) { SpiceKind kind = SpiceKind.from(badges.getStringTagAt(i)); if (kind != null && kind.accompaniment()) first.add(kind); }
                return new SeasoningAmounts(values, tag.getInteger(USES), tag.getBoolean(MIXED), first);
            }
            List<SpiceKind> result = new ArrayList<>(); NBTTagList list = tag.getTagList(COMPONENTS, 8);
            for (int i = 0; i < list.tagCount(); i++) { SpiceKind kind = SpiceKind.from(list.getStringTagAt(i)); if (kind == null) return Collections.emptyList(); result.add(kind); }
            if (result.isEmpty() && stack.getItem() == SpiceRegistry.spice) { SpiceKind kind = SpiceKind.from(ItemSpice.flavor(stack)); if (kind != null) result.add(kind); }
            return result.isEmpty() ? Collections.emptyList() : SeasoningAmounts.of(result);
        } catch (IllegalArgumentException | ArithmeticException invalid) { return Collections.emptyList(); }
    }
    public static int copies(List<SpiceKind> kinds, SpiceKind kind) { return (int) Math.min(Integer.MAX_VALUE, amount(kinds, kind)); }
    public static long amount(List<SpiceKind> kinds, SpiceKind kind) { return kinds instanceof SeasoningAmounts ? ((SeasoningAmounts) kinds).count(kind) : Collections.frequency(kinds, kind); }
    public static int powderUses(List<SpiceKind> kinds) { return kinds.isEmpty() ? 0 : SeasoningAmounts.of(kinds).powderUses; }
    public static long sideUses(List<SpiceKind> kinds) { return kinds.isEmpty() ? 0 : SeasoningAmounts.of(kinds).sideUses(); }
    public static boolean mixed(List<SpiceKind> kinds) { return !kinds.isEmpty() && SeasoningAmounts.of(kinds).mixed; }
    public static boolean mixed(ItemStack stack) { return mixed(components(stack)); }
    public static List<SpiceKind> powders(List<SpiceKind> kinds) { return filtered(kinds, false); }
    public static List<SpiceKind> sides(List<SpiceKind> kinds) { return filtered(kinds, true); }
    private static List<SpiceKind> filtered(List<SpiceKind> kinds, boolean sides) { if (kinds.isEmpty()) return Collections.emptyList(); SeasoningAmounts result = SeasoningAmounts.of(kinds).filter(sides); return result == null ? Collections.emptyList() : result; }
    public static List<SpiceKind> combine(List<SpiceKind> existing, List<SpiceKind> added) { return existing.isEmpty() ? SeasoningAmounts.of(added) : SeasoningAmounts.of(existing).merge(SeasoningAmounts.of(added)); }
    public static boolean canAdd(List<SpiceKind> existing, List<SpiceKind> added) {
        if (added.isEmpty()) return false;
        long powders = (long) powderUses(existing) + powderUses(added), sides = sideUses(existing) + sideUses(added);
        return powders <= SpiceConfig.maxSeasonings && sides <= SpiceConfig.maxAccompaniments
            && (SpiceConfig.allowSeasoningWithAccompaniments || powders == 0 || sides == 0);
    }
    public static List<SpiceKind> effectKinds(List<SpiceKind> kinds) {
        if (!kinds.contains(SpiceKind.CREATIVE)) return kinds;
        Map<SpiceKind, Long> values = new TreeMap<>();
        for (SpiceKind kind : new LinkedHashSet<>(kinds)) if (kind != SpiceKind.CREATIVE) values.put(kind, amount(kinds, kind));
        for (SpiceKind kind : SpiceKind.all()) if (kind != SpiceKind.CREATIVE && kind.available()) values.merge(kind, amount(kinds, SpiceKind.CREATIVE), Long::sum);
        return new SeasoningAmounts(values, powderUses(kinds), mixed(kinds), Collections.emptyList());
    }
    public static boolean creative(ItemStack stack) { return components(stack).contains(SpiceKind.CREATIVE); }
    public static boolean accompaniments(List<SpiceKind> kinds) { return !kinds.isEmpty() && kinds.stream().allMatch(SpiceKind::accompaniment); }
    public static boolean valid(List<SpiceKind> kinds) {
        if (kinds.isEmpty()) return false;
        if (kinds instanceof SeasoningAmounts) return true;
        if (kinds.contains(SpiceKind.CREATIVE)) return kinds.size() == 1;
        return accompaniments(kinds) ? kinds.size() <= SpiceConfig.maxAccompaniments
            : (kinds.size() == 1 || kinds.size() == SpiceConfig.mixedIngredients) && kinds.stream().noneMatch(SpiceKind::accompaniment);
    }
    public static List<SpiceKind> ingredient(ItemStack stack) {
        if (stack.isEmpty() || seasoned(stack)) return Collections.emptyList();
        if (stack.getItem() == SpiceRegistry.spice) return components(stack);
        for (CustomSpice custom : CustomSpices.all()) if (custom.matches(stack)) return Collections.singletonList(custom);
        return rice(stack) && SpiceKind.RICE.available() ? Collections.singletonList(SpiceKind.RICE) : Collections.emptyList();
    }
    public static ItemStack material(SpiceKind kind) {
        if (kind instanceof CustomSpice && kind.accompaniment()) return ((CustomSpice) kind).material();
        if (!kind.accompaniment()) return powder(Collections.singletonList(kind), 1);
        net.minecraft.item.Item item = net.minecraft.item.Item.getByNameOrId(kind.ingredientId); return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
    public static boolean seasoned(ItemStack food) { return food.hasTagCompound() && food.getTagCompound().hasKey(FOOD_KEY); }
    public static boolean rice(ItemStack stack) { return !seasoned(stack) && stack.getItem().getRegistryName() != null && stack.getItem().getRegistryName().toString().equals("farmersdelight:cooked_rice"); }
    public static ItemStack plain(ItemStack stack) { if (!seasoned(stack)) return stack; ItemStack copy = stack.copy(); copy.getTagCompound().removeTag(FOOD_KEY); if (copy.getTagCompound().isEmpty()) copy.setTagCompound(null); return copy; }
    public static String identity(List<SpiceKind> kinds) {
        if (kinds.isEmpty()) return "";
        SeasoningAmounts values = SeasoningAmounts.of(kinds); StringJoiner id = new StringJoiner("+");
        for (Map.Entry<SpiceKind, Long> entry : values.amounts.entrySet()) {
            long count = entry.getValue();
            if (count <= 8) for (int i = 0; i < count; i++) id.add(entry.getKey().id());
            else id.add(entry.getKey().id() + "*" + count);
        }
        return (values.mixed ? "mixed:" : values.sideUses() > 1 ? "meal:" : "") + id;
    }
    public static int color(List<SpiceKind> kinds) {
        if (kinds.isEmpty()) return 0xFFFFFF;
        long r = 0, g = 0, b = 0, total = 0;
        for (SpiceKind kind : new LinkedHashSet<>(kinds)) { long count = amount(kinds, kind); int c = kind.color(); total += count; r += (c >> 16 & 255) * count; g += (c >> 8 & 255) * count; b += (c & 255) * count; }
        return (int) ((r / total << 16) | (g / total << 8) | b / total);
    }
    private static NBTTagCompound data(List<SpiceKind> kinds) {
        SeasoningAmounts values = SeasoningAmounts.of(kinds); NBTTagCompound tag = new NBTTagCompound(), counts = new NBTTagCompound(); NBTTagList list = new NBTTagList(), badges = new NBTTagList();
        values.amounts.forEach((kind, count) -> counts.setLong(kind.id(), count));
        for (SpiceKind kind : values) list.appendTag(new NBTTagString(kind.id()));
        for (SpiceKind kind : values.badges) badges.appendTag(new NBTTagString(kind.id()));
        tag.setTag(AMOUNTS, counts); tag.setTag(COMPONENTS, list); tag.setInteger(USES, values.powderUses); tag.setBoolean(MIXED, values.mixed); tag.setTag(BADGES, badges); return tag;
    }
    public static ItemStack powder(List<SpiceKind> kinds, int count) { return makePowder(kinds, count, mixed(kinds)); }
    public static ItemStack mixedPowder(List<SpiceKind> kinds, int count) { return makePowder(kinds, count, true); }
    private static ItemStack makePowder(List<SpiceKind> kinds, int count, boolean mixed) {
        if (kinds.isEmpty() || kinds.stream().anyMatch(SpiceKind::accompaniment)) throw new IllegalArgumentException("Powder requires powder components");
        SeasoningAmounts values = SeasoningAmounts.of(kinds); values = new SeasoningAmounts(values.amounts, 1, mixed, Collections.emptyList());
        String id = identity(values); if (mixed && !id.startsWith("mixed:")) id = "mixed:" + id;
        ItemStack stack = ItemSpice.create(id, color(values), count); NBTTagCompound tag = data(values); tag.setString(ItemSpice.FLAVOR_KEY, id); tag.setInteger(ItemSpice.COLOR_KEY, color(values)); stack.setTagCompound(tag); return stack;
    }
    public static ItemStack apply(ItemStack food, List<SpiceKind> kinds) { return apply(food, kinds, color(kinds)); }
    public static ItemStack apply(ItemStack food, List<SpiceKind> kinds, int color) {
        if (seasoned(food) || !valid(kinds)) throw new IllegalArgumentException("Food must be unseasoned");
        ItemStack result = food.copy(); result.setCount(1); NBTTagCompound seasoning = data(kinds); seasoning.setInteger(ItemSpice.COLOR_KEY, color & 0xFFFFFF); if (!result.hasTagCompound()) result.setTagCompound(new NBTTagCompound()); result.getTagCompound().setTag(FOOD_KEY, seasoning); return result;
    }
    public static int color(ItemStack stack) { if (stack.getItem() == SpiceRegistry.spice) return ItemSpice.color(stack, 0); NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound().getCompoundTag(FOOD_KEY) : new NBTTagCompound(); return tag.hasKey(ItemSpice.COLOR_KEY) ? tag.getInteger(ItemSpice.COLOR_KEY) & 0xFFFFFF : color(components(stack)); }
    public static FlavorAPI.Flavor flavor(ItemStack food) {
        List<SpiceKind> kinds = components(food); if (!seasoned(food) || kinds.isEmpty()) return null;
        List<SpiceKind> powders = powders(kinds), sides = sides(kinds);
        String powderId = powders.isEmpty() ? "" : mixed(kinds) ? MIXED_FLAVOR : "solwarly:" + powders.get(0).id();
        if (powders.contains(SpiceKind.CREATIVE)) powderId = "solwarly:creative";
        double powderMultiplier = powders.isEmpty() ? 0 : mixed(kinds) ? SpiceConfig.mixedMultiplier : powders.get(0).multiplier();
        double sideMultiplier = sides.isEmpty() ? 0 : sideUses(sides) > 1 ? SpiceConfig.accompanimentMultiplier : sides.get(0).multiplier();
        String id = powders.isEmpty() ? "solwarly:" + identity(sides) : sides.isEmpty() ? powderId : powderId + "|" + identity(sides);
        return new FlavorAPI.Flavor(id, Math.max(powderMultiplier, sideMultiplier));
    }
    public static double durationScale(int copies) { return durationScale((long) copies); }
    public static double durationScale(long copies) { return copies >= 3 ? Math.max(0, SpiceConfig.tripleDuration + (copies - 3.0) * (SpiceConfig.tripleDuration - SpiceConfig.doubleDuration)) : copies == 2 ? SpiceConfig.doubleDuration : 1; }
    public static int hunger(double value) { return Math.min(Integer.MAX_VALUE - 20, ticks(value)); }
    public static int ticks(double value) { return (int) Math.max(0, Math.min(Integer.MAX_VALUE, Math.round(value))); }
    public static int amplifier(long value) { return (int) Math.max(0, Math.min(127, value)); }
    public static List<ItemStack> badges(ItemStack food) {
        List<SpiceKind> kinds = components(food); if (kinds.isEmpty()) return Collections.emptyList(); List<ItemStack> result = new ArrayList<>();
        List<SpiceKind> powders = powders(kinds); if (!powders.isEmpty()) { ItemStack badge = mixed(kinds) ? mixedPowder(powders, 1) : material(powders.get(0)); if (badge.getItem() == SpiceRegistry.spice) badge.getTagCompound().setInteger(ItemSpice.COLOR_KEY, color(food)); result.add(badge); }
        for (SpiceKind kind : SeasoningAmounts.of(kinds).badges) result.add(material(kind)); return result;
    }
    public static Collection<ItemStack> variants(ItemStack plainFood) {
        List<ItemStack> result = new ArrayList<>(); List<SpiceKind> sides = new ArrayList<>(), powders = new ArrayList<>();
        for (SpiceKind kind : SpiceKind.all()) if (kind != SpiceKind.CREATIVE && kind.available()) { result.add(apply(plainFood, Collections.singletonList(kind))); if (kind.accompaniment()) sides.add(kind); else powders.add(kind); }
        if (SpiceConfig.maxAccompaniments >= 2) for (int a = 0; a < sides.size(); a++) for (int b = a; b < sides.size(); b++) result.add(apply(plainFood, Arrays.asList(sides.get(a), sides.get(b))));
        if (!powders.isEmpty()) {
            SeasoningAmounts mixture=SeasoningAmounts.of(Collections.nCopies(SpiceConfig.mixedIngredients,powders.get(0)));
            result.add(apply(plainFood,new SeasoningAmounts(mixture.amounts,1,true,Collections.emptyList())));
        }
        if (SpiceConfig.allowSeasoningWithAccompaniments) {
            List<ItemStack> powderVariants = new ArrayList<>(), sideVariants = new ArrayList<>();
            for (ItemStack variant : result) {
                List<SpiceKind> composition = components(variant);
                if (accompaniments(composition)) sideVariants.add(variant); else powderVariants.add(variant);
            }
            for (ItemStack powder : powderVariants) for (ItemStack side : sideVariants)
                result.add(apply(plainFood, combine(components(powder), components(side))));
        }
        return result;
    }
    private Seasoning() {}
}
