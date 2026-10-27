package com.xy177.solwarly.spice;

import com.xy177.solwarly.api.FlavorAPI;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.*;
import java.util.*;

/** Keeps dietary flavor separate from actual ingredient amounts and crafting applications. */
public final class Seasoning {
    public static final String FOOD_KEY = "SolWarlySeasoning", COMPONENTS = "SpiceComponents";
    public static final String MIXED_FLAVOR = "solwarly:mixed";
    public static final String COMBINED_ACCOMPANIMENT_FLAVOR = "solwarly:combined_accompaniments";
    private static final String AMOUNTS = "SpiceAmounts", USES = "PowderUses", MIXED = "MixedPowder", BADGES = "SideBadges";
    private static boolean hasData(ItemStack stack) { return stack.has(net.minecraft.core.component.DataComponents.CUSTOM_DATA); }
    /** Merge statistical flavors without changing the food's actual ingredients. */
    public static String canonicalFlavor(String id) {
        int separator = id.indexOf('|');
        String primary = separator < 0 ? id : id.substring(0, separator);
        if (primary.startsWith(MIXED_FLAVOR + ":")) primary = MIXED_FLAVOR;
        else if (primary.startsWith("solwarly:meal:")) primary = COMBINED_ACCOMPANIMENT_FLAVOR;
        if (separator < 0) return primary;
        String side = id.substring(separator + 1);
        if (side.startsWith("meal:")) side = "combined_accompaniments";
        return primary + "|" + side;
    }
    public static List<SpiceKind> components(ItemStack stack) {
        if (!hasData(stack)) return Collections.emptyList();
        CompoundTag tag = stack.getItem() == SpiceRegistry.spice ? com.xy177.solwarly.item.StackData.get(stack) : com.xy177.solwarly.item.StackData.get(stack).getCompound(FOOD_KEY);
        try {
            if (tag.contains(AMOUNTS)) {
                CompoundTag counts = tag.getCompound(AMOUNTS); Map<SpiceKind, Long> values = new TreeMap<>();
                for (String id : counts.getAllKeys()) { SpiceKind kind = SpiceKind.from(id); if (kind == null) return Collections.emptyList(); values.put(kind, counts.getLong(id)); }
                List<SpiceKind> first = new ArrayList<>(); ListTag badges = tag.getList(BADGES, 8);
                for (int i = 0; i < Math.min(2, badges.size()); i++) { SpiceKind kind = SpiceKind.from(badges.getString(i)); if (kind != null && kind.accompaniment()) first.add(kind); }
                return new SeasoningAmounts(values, tag.getInt(USES), tag.getBoolean(MIXED), first);
            }
            List<SpiceKind> result = new ArrayList<>(); ListTag list = tag.getList(COMPONENTS, 8);
            for (int i = 0; i < list.size(); i++) { SpiceKind kind = SpiceKind.from(list.getString(i)); if (kind == null) return Collections.emptyList(); result.add(kind); }
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
        String id = com.xy177.solwarly.registry.RegistryLookup.id(stack.getItem()).toString();
        for (SpiceKind kind : SpiceKind.values()) if (kind.nativeMaterial() && kind.available() && id.equals(kind.ingredientId)) return List.of(kind);
        return List.of();
    }
    public static ItemStack material(SpiceKind kind) {
        if (kind instanceof CustomSpice && kind.accompaniment()) return ((CustomSpice) kind).material();
        if (!kind.nativeMaterial()) return powder(List.of(kind), 1);
        var item = com.xy177.solwarly.registry.RegistryLookup.item(kind.ingredientId);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }
    public static boolean seasoned(ItemStack food) { return hasData(food) && com.xy177.solwarly.item.StackData.get(food).contains(FOOD_KEY); }
    public static boolean rice(ItemStack stack) { return !seasoned(stack) && com.xy177.solwarly.registry.RegistryLookup.id(stack.getItem()) != null && com.xy177.solwarly.registry.RegistryLookup.id(stack.getItem()).toString().equals("farmersdelight:cooked_rice"); }
    public static ItemStack plain(ItemStack stack) { if (!seasoned(stack)) return stack; ItemStack copy=stack.copy(); com.xy177.solwarly.item.StackData.update(copy, tag -> tag.remove(FOOD_KEY)); return copy; }
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
    private static CompoundTag data(List<SpiceKind> kinds) {
        SeasoningAmounts values = SeasoningAmounts.of(kinds); CompoundTag tag = new CompoundTag(), counts = new CompoundTag(); ListTag list = new ListTag(), badges = new ListTag();
        values.amounts.forEach((kind, count) -> counts.putLong(kind.id(), count));
        for (SpiceKind kind : values) list.add(StringTag.valueOf(kind.id()));
        for (SpiceKind kind : values.badges) badges.add(StringTag.valueOf(kind.id()));
        tag.put(AMOUNTS, counts); tag.put(COMPONENTS, list); tag.putInt(USES, values.powderUses); tag.putBoolean(MIXED, values.mixed); tag.put(BADGES, badges); return tag;
    }
    public static ItemStack powder(List<SpiceKind> kinds, int count) { return makePowder(kinds, count, mixed(kinds)); }
    public static ItemStack mixedPowder(List<SpiceKind> kinds, int count) { return makePowder(kinds, count, true); }
    private static ItemStack makePowder(List<SpiceKind> kinds, int count, boolean mixed) {
        if (kinds.isEmpty() || kinds.stream().anyMatch(SpiceKind::accompaniment)) throw new IllegalArgumentException("Powder requires powder components");
        SeasoningAmounts values = SeasoningAmounts.of(kinds); values = new SeasoningAmounts(values.amounts, 1, mixed, Collections.emptyList());
        String id = identity(values); if (mixed && !id.startsWith("mixed:")) id = "mixed:" + id;
        ItemStack stack = ItemSpice.create(id, color(values), count); CompoundTag tag = data(values); tag.putString(ItemSpice.FLAVOR_KEY, id); tag.putInt(ItemSpice.COLOR_KEY, color(values)); com.xy177.solwarly.item.StackData.set(stack, tag); return stack;
    }
    public static ItemStack apply(ItemStack food, List<SpiceKind> kinds) { return apply(food, kinds, color(kinds)); }
    public static ItemStack apply(ItemStack food, List<SpiceKind> kinds, int color) {
        if (seasoned(food) || !valid(kinds)) throw new IllegalArgumentException("Food must be unseasoned");
        ItemStack result = food.copy(); result.setCount(1); CompoundTag seasoning = data(kinds); seasoning.putInt(ItemSpice.COLOR_KEY, color & 0xFFFFFF); com.xy177.solwarly.item.StackData.update(result, tag -> tag.put(FOOD_KEY, seasoning)); return result;
    }
    public static int color(ItemStack stack) { if (stack.getItem() == SpiceRegistry.spice) return ItemSpice.color(stack, 0); CompoundTag tag = hasData(stack) ? com.xy177.solwarly.item.StackData.get(stack).getCompound(FOOD_KEY) : new CompoundTag(); return tag.contains(ItemSpice.COLOR_KEY) ? tag.getInt(ItemSpice.COLOR_KEY) & 0xFFFFFF : color(components(stack)); }
    public static FlavorAPI.Flavor flavor(ItemStack food) {
        List<SpiceKind> kinds = components(food); if (!seasoned(food) || kinds.isEmpty()) return null;
        List<SpiceKind> powders = powders(kinds), sides = sides(kinds);
        String powderId = powders.isEmpty() ? "" : mixed(kinds) ? MIXED_FLAVOR : "solwarly:" + powders.get(0).id();
        if (powders.contains(SpiceKind.CREATIVE)) powderId = "solwarly:creative";
        double powderMultiplier = powders.isEmpty() ? 0 : mixed(kinds) ? SpiceConfig.mixedMultiplier : powders.get(0).multiplier();
        double sideMultiplier = sides.isEmpty() ? 0 : sideUses(sides) > 1 ? SpiceConfig.accompanimentMultiplier : sides.get(0).multiplier();
        String sideId = sides.isEmpty() ? "" : sideUses(sides) > 1 ? "combined_accompaniments" : identity(sides);
        String id = powders.isEmpty() ? "solwarly:" + sideId : sides.isEmpty() ? powderId : powderId + "|" + sideId;
        return new FlavorAPI.Flavor(id, Math.max(powderMultiplier, sideMultiplier));
    }
    public static double durationScale(int copies) { return durationScale((long) copies); }
    public static double durationScale(long copies) { return copies >= 3 ? Math.max(0, SpiceConfig.tripleDuration + (copies - 3.0) * (SpiceConfig.tripleDuration - SpiceConfig.doubleDuration)) : copies == 2 ? SpiceConfig.doubleDuration : 1; }
    public static int ticks(double value) { return (int) Math.max(0, Math.min(Integer.MAX_VALUE, Math.round(value))); }
    public static int amplifier(long value) { return (int) Math.max(0, Math.min(255, value)); }
    public static List<ItemStack> badges(ItemStack food) {
        List<SpiceKind> kinds = components(food); if (kinds.isEmpty()) return Collections.emptyList(); List<ItemStack> result = new ArrayList<>();
        List<SpiceKind> powders = powders(kinds); if (!powders.isEmpty()) { ItemStack badge = mixed(kinds) ? mixedPowder(powders, 1) : material(powders.get(0)); if (badge.getItem() == SpiceRegistry.spice) com.xy177.solwarly.item.StackData.update(badge, tag -> tag.putInt(ItemSpice.COLOR_KEY, color(food))); result.add(badge); }
        for (SpiceKind kind : SeasoningAmounts.of(kinds).badges) result.add(material(kind)); return result;
    }
    public static Collection<ItemStack> variants(ItemStack plainFood) {
        List<ItemStack> result = new ArrayList<>(); List<SpiceKind> sides = new ArrayList<>(), powders = new ArrayList<>();
        for (SpiceKind kind : SpiceKind.all()) if (kind != SpiceKind.CREATIVE && kind.available()) { result.add(apply(plainFood, Collections.singletonList(kind))); if (kind.accompaniment()) sides.add(kind); else powders.add(kind); }
        if (SpiceConfig.maxAccompaniments >= 2 && !sides.isEmpty())
            result.add(apply(plainFood, Arrays.asList(sides.get(0), sides.get(0))));
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
