package com.xy177.solwarly.client;

import com.xy177.solwarly.spice.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemFood;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.relauncher.Side;
import java.time.LocalDate;
import java.util.*;

@Mod.EventBusSubscriber(modid = "solwarly", value = Side.CLIENT)
public final class SpiceTooltips {
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack(); List<SpiceKind> kinds = Seasoning.components(stack);
        boolean nativeMaterial = kinds.isEmpty();
        if (nativeMaterial) kinds = Seasoning.ingredient(stack);
        if (kinds.isEmpty()) return;
        boolean powder = stack.getItem() == SpiceRegistry.spice;
        List<String> lines = event.getToolTip();
        if (Seasoning.seasoned(stack)) {
            addPotionTooltips(stack, lines);
            if (!lines.isEmpty()) lines.set(0, foodName(lines.get(0), kinds, LocalDate.now()));
        }
        if (nativeMaterial && Seasoning.accompaniments(kinds)) lines.add(TextFormatting.AQUA + I18n.format("solwarly.spice.accompaniment"));
        kinds = Seasoning.effectKinds(kinds);
        for (SpiceKind kind : new LinkedHashSet<>(kinds)) {
            int copies = Math.min(3, Seasoning.copies(kinds, kind));
            int color = nativeMaterial || kinds.size() > 1 ? kind.color() : Seasoning.color(stack);
            String description;
            if (kind instanceof CustomSpice) {
                CustomSpice custom = (CustomSpice) kind;
                description = custom.text("tooltip");
                if (description.trim().isEmpty()) {
                    description = custom.text("name");
                    List<String> effects = new ArrayList<>();
                    for (SeasoningPotions.Entry entry : custom.effects(copies)) {
                        PotionEffect effect = entry.effect;
                        String effectName = I18n.format(effect.getEffectName());
                        if (effect.getAmplifier() > 0) effectName += " " + potency(effect.getAmplifier());
                        effects.add(effectName + " (" + Potion.getPotionDurationString(effect, 1.0F) + ")");
                    }
                    if (!effects.isEmpty()) description += ": " + String.join(", ", effects);
                }
                if (copies > 1 && !kind.accompaniment()) description += " " + I18n.format("solwarly.spice." + (copies == 3 ? "too_much" : "lots"));
            } else if (kind == SpiceKind.MYSTERY) {
                description = I18n.format("solwarly.spice.mystery.tooltip",
                    SpiceEffects.mysteryCureCount(copies));
            } else {
                description = kind.fruit() ? fruitDescription(kind) : I18n.format("solwarly.spice." + kind.id() + ".tooltip");
                if (copies > 1 && !kind.accompaniment()) description += " " + I18n.format("solwarly.spice." + (copies == 3 ? "too_much" : "lots"));
            }
            for (String line : description.split("\\r?\\n", -1)) lines.add(HexTooltipFont.mark(color, line));
        }
    }

    private static String fruitDescription(SpiceKind kind) {
        List<SeasoningPotions.Entry> effects = SeasoningPotions.additions(
            Collections.singletonList(kind), Collections.<Potion, PotionEffect>emptyMap());
        String fruit = I18n.format("solwarly.fruit." + kind.fruitId());
        if (effects.isEmpty()) return I18n.format("solwarly.spice.fruit.tooltip", fruit);
        List<String> names = new ArrayList<>();
        for (SeasoningPotions.Entry entry : effects)
            names.add(new net.minecraft.util.text.TextComponentTranslation(entry.effect.getEffectName()).getUnformattedText());
        return I18n.format("solwarly.spice.fruit.tooltip", String.join(", ", names));
    }

    /** Reads optional food-effect APIs without linking any integration at compile time. */
    private static void addPotionTooltips(ItemStack stack, List<String> lines) {
        Map<Potion, PotionEffect> original = readFoodEffects(stack);
        List<SeasoningPotions.Entry> additions = SeasoningPotions.additions(Seasoning.components(stack), original);
        if (additions.isEmpty()) return;
        Map<Potion, SeasoningPotions.Entry> added = new LinkedHashMap<>();
        for (SeasoningPotions.Entry entry : additions) added.put(entry.effect.getPotion(), entry);
        Map<Potion, PotionEffect> merged = SeasoningPotions.merge(original, additions);
        for (Potion potion : added.keySet()) removeExistingEffectLines(lines, potion);
        int index = Math.min(1, lines.size());
        for (Map.Entry<Potion, SeasoningPotions.Entry> addedEntry : added.entrySet()) {
            Map.Entry<Potion, PotionEffect> entry = new AbstractMap.SimpleEntry<>(addedEntry.getKey(), merged.get(addedEntry.getKey()));
            float chance = addedEntry.getValue().chance;
            PotionEffect effect = entry.getValue();
            String name = new TextComponentTranslation(effect.getEffectName()).getFormattedText();
            if (effect.getAmplifier() > 0) name += TextFormatting.BLUE + " " + potency(effect.getAmplifier());
            String duration = Potion.getPotionDurationString(effect, 1.0F);
            String key = net.minecraftforge.fml.common.Loader.isModLoaded("farmersdelight")
                ? "farmersdelight.tooltip.food.effect" : "solwarly.tooltip.food.effect";
            // Potion names carry a trailing reset code; restore blue for both duration delimiters.
            String line = TextFormatting.BLUE + I18n.format(key,
                name + TextFormatting.BLUE, duration + TextFormatting.BLUE);
            lines.add(index++, line);
            if (chance < .999F) lines.add(index++, TextFormatting.BLUE + I18n.format(
                net.minecraftforge.fml.common.Loader.isModLoaded("farmersdelight")
                    ? "farmersdelight.tooltip.food.effect_chance" : "solwarly.tooltip.food.effect_chance", Math.round(chance * 100))); 
        }
    }

    private static void removeExistingEffectLines(List<String> lines, Potion potion) {
        String name = new TextComponentTranslation(potion.getName()).getFormattedText();
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).contains(name)) continue;
            lines.remove(i--);
            if (i + 1 < lines.size() && lines.get(i + 1).startsWith(TextFormatting.BLUE.toString())) lines.remove(i + 1);
        }
    }

    private static Map<Potion, PotionEffect> readFoodEffects(ItemStack stack) {
        Map<Potion, PotionEffect> result = new LinkedHashMap<>();
        ItemFood food = stack.getItem() instanceof ItemFood ? (ItemFood) stack.getItem() : null;
        if (food == null) return result;
        try {
            java.lang.reflect.Field effect = findField(ItemFood.class, "potionId", "potionEffect", "field_77853_b");
            Object value = effect == null ? null : effect.get(food);
            if (value instanceof PotionEffect) {
                PotionEffect e = (PotionEffect) value;
                result.put(e.getPotion(), new PotionEffect(e));
            }
        } catch (ReflectiveOperationException ignored) { }
        for (Class<?> type = food.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
            for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                if (!List.class.isAssignableFrom(field.getType())) continue;
                try {
                    field.setAccessible(true); Object value = field.get(food);
                    if (!(value instanceof Iterable)) continue;
                    for (Object entry : (Iterable<?>) value) {
                        PotionEffect parsed = parseEntry(entry); if (parsed != null) result.put(parsed.getPotion(), parsed);
                    }
                } catch (ReflectiveOperationException ignored) { }
            }
        }
        return result;
    }

    private static java.lang.reflect.Field findField(Class<?> type, String... names) {
        for (String name : names) try {
            java.lang.reflect.Field field = type.getDeclaredField(name); field.setAccessible(true); return field;
        } catch (NoSuchFieldException ignored) { }
        return null;
    }

    private static PotionEffect parseEntry(Object entry) {
        try {
            Object id = value(entry, "getEffectId", "effectId", "id");
            Potion potion = id instanceof Potion ? (Potion) id : id instanceof ResourceLocation
                ? ForgeRegistries.POTIONS.getValue((ResourceLocation) id) : id instanceof String
                    ? ForgeRegistries.POTIONS.getValue(new ResourceLocation((String) id)) : null;
            if (potion == null) return null;
            int duration = number(value(entry, "getDuration", "duration"), 0);
            int amplifier = number(value(entry, "getAmplifier", "amplifier"), 0);
            return new PotionEffect(potion, duration, amplifier);
        } catch (ReflectiveOperationException ignored) { return null; }
    }

    private static Object value(Object object, String... names) throws ReflectiveOperationException {
        for (String name : names) {
            try {
                java.lang.reflect.Method method = object.getClass().getMethod(name);
                method.setAccessible(true); return method.invoke(object);
            } catch (NoSuchMethodException ignored) { }
            for (Class<?> type = object.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
                try {
                    java.lang.reflect.Field field = type.getDeclaredField(name);
                    field.setAccessible(true); return field.get(object);
                } catch (NoSuchFieldException ignored) { }
            }
        }
        return null;
    }

    private static int number(Object value, int fallback) { return value instanceof Number ? ((Number) value).intValue() : fallback; }
    public static String foodName(String original, List<SpiceKind> kinds, LocalDate date) {
        java.util.regex.Matcher debug = java.util.regex.Pattern.compile("(\\s+(?:§.)*\\(#\\d+(?:/\\d+)?\\).*)$").matcher(original);
        String ending = "";
        if (debug.find()) { ending = debug.group(1); original = original.substring(0, debug.start()); }
        kinds = Seasoning.effectKinds(kinds);
        List<SpiceKind> unique = new ArrayList<>(new LinkedHashSet<>(kinds));
        for (int i = unique.size() - 1; i >= 0; i--) {
            SpiceKind kind = unique.get(i); int copies = Math.min(3, Seasoning.copies(kinds, kind));
            String id = kind.id();
            if (kind == SpiceKind.HAM && (date.getMonthValue() == 4 && date.getDayOfMonth() == 1
                || date.getMonthValue() == 11 && date.getDayOfMonth() == 18)) id = "ham_holiday";
            if (kind instanceof CustomSpice) {
                CustomSpice custom = (CustomSpice) kind;
                String prefix = custom.text("prefix").trim(), suffix = custom.text("suffix").trim();
                original = (prefix.isEmpty() ? "" : prefix + " ") + original + (suffix.isEmpty() ? "" : " " + suffix);
            } else original = I18n.format("solwarly.seasoning." + id, original);
            if (copies > 1 && !kind.accompaniment()) original = I18n.format("solwarly.seasoning." + (copies == 3 ? "extreme" : "super"), original);
        }
        return original + ending;
    }
    private static final Map<net.minecraft.client.gui.FontRenderer, HexTooltipFont> fonts = new WeakHashMap<>();
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void font(RenderTooltipEvent.Pre event) {
        for (String line : event.getLines()) if (HexTooltipFont.marked(line)) {
            net.minecraft.client.gui.FontRenderer original = event.getFontRenderer();
            event.setFontRenderer(fonts.computeIfAbsent(original, HexTooltipFont::new)); break;
        }
    }
    private static String potency(int amplifier) {
        String key="potion.potency."+amplifier, text=I18n.format(key);
        if (!text.equals(key)) return text;
        int level=amplifier+1; StringBuilder roman=new StringBuilder();
        int[] values={100,90,50,40,10,9,5,4,1}; String[] symbols={"C","XC","L","XL","X","IX","V","IV","I"};
        for(int i=0;i<values.length;i++)while(level>=values[i]) {roman.append(symbols[i]);level-=values[i];}
        return roman.toString();
    }
    private SpiceTooltips() {}
}
