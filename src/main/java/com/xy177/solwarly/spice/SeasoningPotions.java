package com.xy177.solwarly.spice;
import net.minecraft.core.Holder;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.world.effect.*;
import net.minecraft.resources.ResourceLocation;
import java.util.*;

/** Shared effect descriptions used by consumption and tooltip rendering. */
public final class SeasoningPotions {
    public static final class Entry {
        public final MobEffectInstance effect;
        public final float chance;
        public Entry(MobEffectInstance effect, float chance) { this.effect = effect; this.chance = chance; }
    }
    private static final Set<String> SCALING = new HashSet<>(Arrays.asList(
        "solwarly:sweet", "solwarly:tasty", "solwarly:aromatic", "solwarly:hot", "solwarly:appetizing",
        "minecraft:absorption", "minecraft:health_boost", "minecraft:regeneration", "minecraft:strength",
        "minecraft:speed", "minecraft:haste", "fruitsdelight:lozenge", "fruitsdelight:heal_aura",
        "fruitsdelight:cycling", "twilightdelight:fire_range",
        "twilight_spark_delight:symbiosis", "twilight_spark_delight:sorrow",
        "immortalers_delight:resistance_to_illager",
        "immortalers_delight:resistance_to_undead",
        "immortalers_delight:resistance_to_arthropod",
        "immortalers_delight:resistance_to_abyssal",
        "immortalers_delight:incandescence",
        "immortalers_delight:warm_current_surges",
        "immortalers_delight:burn_the_boats",
        "immortalers_delight:keep_a_fast",
        "immortalers_delight:cultural_legacy",
        "immortalers_delight:magical_reverse",
        "immortalers_delight:relieve_poison",
        "immortalers_delight:vitality",
        "immortalers_delight:lingering_infusion",
        "immortalers_delight:cool",
        "immortalers_delight:prehistoric_powers",
        "immortalers_delight:deepness",
        "immortalers_delight:let_it_freeze",
        "immortalers_delight:smoke_abstinence",
        "immortalers_delight:unyielding",
        "immortalers_delight:moon_bright",
        "immortalers_delight:infernal_forging"));
    public static boolean scales(Holder<MobEffect> potion) {
        var id = com.xy177.solwarly.registry.RegistryLookup.id(potion);
        return id != null && SCALING.contains(id.toString());
    }
    public static List<Entry> additions(List<SpiceKind> kinds, Map<Holder<MobEffect>, MobEffectInstance> original) {
        return additions(kinds, original, ImmortalSpices.powered());
    }
    public static List<Entry> additions(List<SpiceKind> kinds, Map<Holder<MobEffect>, MobEffectInstance> original, boolean powered) {
        kinds = Seasoning.effectKinds(kinds);
        List<Entry> result = new ArrayList<>();
        for (SpiceKind kind : new LinkedHashSet<>(kinds)) {
            long copies = Seasoning.amount(kinds, kind);
            if (kind.immortal()) { result.addAll(ImmortalSpices.effects(kind, copies, powered)); continue; }
            if (kind instanceof CustomSpice) { result.addAll(((CustomSpice) kind).effects(copies)); continue; }
            if (kind.fruit()) { result.addAll(fruit(kind, copies)); continue; }
            Holder<MobEffect> potion = kind.potion(); if (potion == null) continue;
            boolean scaling = scales(potion);
            int duration = SpiceConfig.durations.get(kind);
            int amplifier = scaling ? Seasoning.amplifier((long) copies - 1) : 0;
            if (kind == SpiceKind.HAM) duration = original.containsKey(potion) ? SpiceConfig.hamExtension : SpiceConfig.hamDuration;
            if (!scaling) duration = Seasoning.ticks(duration * Seasoning.durationScale(copies));
            if (duration > 0) result.add(new Entry(new MobEffectInstance(potion, duration, amplifier), 1));
            if (kind == SpiceKind.SPICY && SpiceKind.usesMyNethersDelight()) {
                var pungent = com.xy177.solwarly.registry.RegistryLookup.effect(ResourceLocation.tryParse("mynethersdelight:g_pungent"));
                int ticks = Seasoning.ticks(SpiceConfig.spicyPungentDuration * Seasoning.durationScale(copies));
                if (pungent != null && ticks > 0) result.add(new Entry(new MobEffectInstance(pungent, ticks, 0), 1));
            }
        }
        return result;
    }
    public static MobEffectInstance combine(MobEffectInstance original, MobEffectInstance added) {
        return original == null ? new MobEffectInstance(added) : new MobEffectInstance(added.getEffect(),
            (int) Math.min(Integer.MAX_VALUE, (long) original.getDuration() + added.getDuration()),
            Math.max(original.getAmplifier(), added.getAmplifier()), original.isAmbient(), original.isVisible());
    }
    public static Map<Holder<MobEffect>, MobEffectInstance> merge(Map<Holder<MobEffect>, MobEffectInstance> original, Collection<Entry> additions) {
        Map<Holder<MobEffect>, MobEffectInstance> result = new LinkedHashMap<>();
        original.forEach((potion, effect) -> result.put(potion, new MobEffectInstance(effect)));
        for (Entry entry : additions) result.put(entry.effect.getEffect(), combine(result.get(entry.effect.getEffect()), entry.effect));
        return result;
    }
    private static final Map<SpiceKind, List<Entry>> fruitEffects = new LinkedHashMap<>();
    public static void loadFruits() {
        fruitEffects.clear();
        if (com.xy177.solwarly.registry.RegistryLookup.item("fruitsdelight:apple_jam") == null) return;
        try {
            Class<?> type = Class.forName("dev.xkmc.fruitsdelight.init.food.FruitType");
            for (Object fruit : type.getEnumConstants()) {
                SpiceKind kind = SpiceKind.from("fruit_" + ((Enum<?>) fruit).name().toLowerCase(java.util.Locale.ROOT));
                if (kind == null) continue;
                List<Entry> entries = new ArrayList<>();
                for (Object spec : (List<?>) type.getField("eff").get(fruit)) {
                    MobEffectInstance effect = (MobEffectInstance) spec.getClass().getMethod("getEffect", int.class).invoke(spec, 20);
                    float chance = ((Number) spec.getClass().getMethod("getChance", int.class).invoke(spec, 20)).floatValue();
                    if (effect != null) entries.add(new Entry(effect, chance));
                }
                fruitEffects.put(kind, entries);
            }
        } catch (ReflectiveOperationException e) {
            SOLWarly.LOGGER.error("Could not read Fruits Delight jam effects", e);
        }
    }
    private static List<Entry> fruit(SpiceKind kind, long copies) {
        List<Entry> result = new ArrayList<>();
        for (Entry entry : fruitEffects.getOrDefault(kind, Collections.emptyList())) {
            MobEffectInstance effect = entry.effect;
            boolean scaling = scales(effect.getEffect());
            int duration = Math.max(effect.getDuration(), SpiceConfig.fruitMinimumDuration);
            result.add(new Entry(new MobEffectInstance(effect.getEffect(), scaling ? duration
                : Seasoning.ticks(duration * Seasoning.durationScale(copies)),
                Seasoning.amplifier((long) effect.getAmplifier() + (scaling ? copies - 1L : 0))), entry.chance));
        }
        return result;
    }
    private SeasoningPotions() {}
}
