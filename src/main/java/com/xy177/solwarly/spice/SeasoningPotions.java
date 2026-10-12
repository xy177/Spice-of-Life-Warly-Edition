package com.xy177.solwarly.spice;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import java.util.*;

/** Shared effect descriptions used by consumption and tooltip rendering. */
public final class SeasoningPotions {
    public static final class Entry {
        public final PotionEffect effect;
        public final float chance;
        public Entry(PotionEffect effect, float chance) { this.effect = effect; this.chance = chance; }
    }
    private static final Set<String> SCALING = new HashSet<>(Arrays.asList(
        "solwarly:sweet", "solwarly:tasty", "solwarly:aromatic", "solwarly:hot", "solwarly:appetizing",
        "minecraft:absorption", "minecraft:health_boost", "minecraft:regeneration", "minecraft:strength",
        "minecraft:speed", "minecraft:haste", "fruits_delight_legacy:lozenge", "fruits_delight_legacy:heal_aura",
        "fruits_delight_legacy:cycling", "twilightdelight:fire_range",
        "twilight_spark_delight:symbiosis", "twilight_spark_delight:sorrow"));
    public static boolean scales(Potion potion) {
        return potion.getRegistryName() != null && SCALING.contains(potion.getRegistryName().toString());
    }
    public static List<Entry> additions(List<SpiceKind> kinds, Map<Potion, PotionEffect> original) {
        List<Entry> result = new ArrayList<>();
        for (SpiceKind kind : new LinkedHashSet<>(kinds)) {
            int copies = Collections.frequency(kinds, kind);
            if (kind instanceof CustomSpice) { result.addAll(((CustomSpice) kind).effects(copies)); continue; }
            if (kind.fruit()) { result.addAll(fruit(kind, copies)); continue; }
            Potion potion = kind.potion(); if (potion == null) continue;
            boolean scaling = scales(potion);
            int duration = SpiceConfig.durations.get(kind);
            int amplifier = scaling ? copies - 1 : 0;
            if (kind == SpiceKind.HAM) duration = original.containsKey(potion) ? SpiceConfig.hamExtension : SpiceConfig.hamDuration;
            if (!scaling) duration = (int) Math.round(duration * Seasoning.durationScale(copies));
            if (duration > 0) result.add(new Entry(new PotionEffect(potion, duration, amplifier), 1));
        }
        return result;
    }
    public static PotionEffect combine(PotionEffect original, PotionEffect added) {
        return original == null ? new PotionEffect(added) : new PotionEffect(added.getPotion(),
            (int) Math.min(Integer.MAX_VALUE, (long) original.getDuration() + added.getDuration()),
            Math.max(original.getAmplifier(), added.getAmplifier()), original.getIsAmbient(), original.doesShowParticles());
    }
    public static Map<Potion, PotionEffect> merge(Map<Potion, PotionEffect> original, Collection<Entry> additions) {
        Map<Potion, PotionEffect> result = new LinkedHashMap<>();
        original.forEach((potion, effect) -> result.put(potion, new PotionEffect(effect)));
        for (Entry entry : additions) result.put(entry.effect.getPotion(), combine(result.get(entry.effect.getPotion()), entry.effect));
        return result;
    }
    private static final Map<SpiceKind, List<Entry>> fruitEffects = new LinkedHashMap<>();
    public static void loadFruits() {
        fruitEffects.clear();
        if (net.minecraft.item.Item.getByNameOrId("fruits_delight_legacy:apple_jelly") == null) return;
        try {
            Class<?> type = Class.forName("xy177.fruitsdelightlegacy.common.food.FruitType");
            for (Object fruit : type.getEnumConstants()) {
                SpiceKind kind = SpiceKind.from("fruit_" + type.getMethod("getId").invoke(fruit));
                if (kind == null) continue;
                List<Entry> entries = new ArrayList<>();
                for (Object spec : (List<?>) type.getMethod("getEffects").invoke(fruit)) {
                    PotionEffect effect = (PotionEffect) spec.getClass().getMethod("create", int.class).invoke(spec, 20);
                    float chance = ((Number) spec.getClass().getMethod("getChance", int.class).invoke(spec, 20)).floatValue();
                    if (effect != null) entries.add(new Entry(effect, chance));
                }
                fruitEffects.put(kind, entries);
            }
        } catch (ReflectiveOperationException e) {
            SOLWarly.LOGGER.error("Could not read Fruits Delight Legacy jam effects", e);
        }
    }
    private static List<Entry> fruit(SpiceKind kind, int copies) {
        List<Entry> result = new ArrayList<>();
        for (Entry entry : fruitEffects.getOrDefault(kind, Collections.emptyList())) {
            PotionEffect effect = entry.effect;
            boolean scaling = scales(effect.getPotion());
            int duration = Math.max(effect.getDuration(), SpiceConfig.fruitMinimumDuration);
            result.add(new Entry(new PotionEffect(effect.getPotion(), scaling ? duration
                : (int) Math.round(duration * Seasoning.durationScale(copies)),
                effect.getAmplifier() + (scaling ? copies - 1 : 0)), entry.chance));
        }
        return result;
    }
    private SeasoningPotions() {}
}
