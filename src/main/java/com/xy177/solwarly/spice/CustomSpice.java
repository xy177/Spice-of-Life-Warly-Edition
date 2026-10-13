package com.xy177.solwarly.spice;
import net.minecraft.core.Holder;

import com.google.gson.*;
import net.minecraft.world.effect.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.util.thread.EffectiveSide;
import java.util.*;

/** A user-defined flavor uses the same item/NBT and processing paths as built-in powders. */
public final class CustomSpice extends SpiceKind {
    final JsonObject definition;
    private final SortedMap<String, Map<String, String>> translations = new TreeMap<>();
    private final Map<Integer, List<SeasoningPotions.Entry>> effects = new TreeMap<>();

    private final net.minecraft.world.item.ItemStack material, returned;
    public final int hungerBonus;
    public final double saturationBonus;

    public CustomSpice(JsonObject json) { this(json, false); }
    public CustomSpice(JsonObject json, boolean accompaniment) {
        super(required(json, "id"), color(json), multiplier(json));
        if (!id().matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) throw new IllegalArgumentException("id must be namespace:path");
        definition = new JsonParser().parse(json.toString()).getAsJsonObject();
        material = accompaniment ? CustomSpices.stack(json.getAsJsonObject("material")) : net.minecraft.world.item.ItemStack.EMPTY;
        if (accompaniment && (material.getCount() != 1 || material.getItem() == SpiceRegistry.spice || Seasoning.seasoned(material)))
            throw new IllegalArgumentException("Accompaniment material must be one unseasoned native item");
        returned = !accompaniment ? net.minecraft.world.item.ItemStack.EMPTY : json.has("return")
            ? (json.get("return").isJsonNull() ? net.minecraft.world.item.ItemStack.EMPTY : CustomSpices.stack(json.getAsJsonObject("return")))
            : CustomSpices.container(material);
        hungerBonus = accompaniment ? integer(json, "hunger", 0, Integer.MAX_VALUE / 4) : 0;
        saturationBonus = accompaniment ? number(json, "saturation") : 0;
        if (!Double.isFinite(saturationBonus) || saturationBonus < 0 || saturationBonus > Float.MAX_VALUE / 4)
            throw new IllegalArgumentException("saturation must be a finite nonnegative actual saturation amount");
        definition.addProperty("_accompaniment", accompaniment);
        JsonObject languages = json.getAsJsonObject("translations");
        if (languages == null || languages.entrySet().isEmpty()) throw new IllegalArgumentException("translations must contain a language and name");
        for (Map.Entry<String, JsonElement> language : languages.entrySet()) {
            if (!language.getKey().matches("[a-z]{2}_[a-z]{2}")) throw new IllegalArgumentException("Invalid language: " + language.getKey());
            JsonObject text = language.getValue().getAsJsonObject();
            Map<String, String> values = new HashMap<>(); values.put("name", required(text, "name"));
            for (String field : new String[] {"prefix", "suffix"}) values.put(field, text.has(field) ? text.get(field).getAsString() : "");
            if (text.has("tooltip") && (!text.get("tooltip").isJsonPrimitive()
                    || !text.get("tooltip").getAsJsonPrimitive().isString()))
                throw new IllegalArgumentException("tooltip must be a string: " + language.getKey());
            values.put("tooltip", text.has("tooltip") ? text.get("tooltip").getAsString() : "");
            translations.put(language.getKey(), values);
        }
        JsonObject tiers = accompaniment ? null : json.getAsJsonObject("effects");
        if (!accompaniment && tiers == null) throw new IllegalArgumentException("effects must define arrays for 1, 2 and 3 copies");
        Set<Integer> counts = new TreeSet<>(Arrays.asList(1, 2, 3));
        if (!accompaniment) for (String key : tiers.keySet()) {
            if (!key.matches("[1-9][0-9]*")) throw new IllegalArgumentException("Effect tier must be a positive integer: " + key);
            try { counts.add(Integer.parseInt(key)); } catch (NumberFormatException invalid) { throw new IllegalArgumentException("Effect tier exceeds the integer range: " + key); }
        }
        for (int copies : counts) {
            JsonArray specs = accompaniment ? json.getAsJsonArray("effects") : tiers.getAsJsonArray(Integer.toString(copies));
            if (specs == null) throw new IllegalArgumentException("Missing effects for " + copies + " copies (use [] for no effects)");
            List<SeasoningPotions.Entry> entries = new ArrayList<>(); Set<Holder<MobEffect>> seen = new HashSet<>();
            for (JsonElement element : specs) {
                JsonObject spec = element.getAsJsonObject();
                Holder<MobEffect> potion = com.xy177.solwarly.registry.RegistryLookup.effect(net.minecraft.resources.ResourceLocation.parse(required(spec, "potion")));
                if (potion == null) throw new IllegalArgumentException("Unknown potion: " + spec.get("potion"));
                if (!seen.add(potion)) throw new IllegalArgumentException("Duplicate potion within the same copy tier: " + com.xy177.solwarly.registry.RegistryLookup.id(potion));
                int level = integer(spec, "level", 1, 256);
                double seconds = number(spec, "duration_seconds");
                if (!Double.isFinite(seconds) || seconds < .05 || seconds > Integer.MAX_VALUE / (20.0 * (accompaniment ? copies : 1)))
                    throw new IllegalArgumentException("duration_seconds must be positive and within the tick range");
                entries.add(new SeasoningPotions.Entry(new MobEffectInstance(potion, (int) Math.round(seconds * 20 * (accompaniment ? copies : 1)), Seasoning.amplifier(level - 1L)), 1));
            }
            effects.put(copies, Collections.unmodifiableList(entries));
        }
    }
    public List<SeasoningPotions.Entry> effects(int copies) { return effects((long) copies); }
    public List<SeasoningPotions.Entry> effects(long copies) {
        copies = Math.max(1, copies);
        boolean side = accompaniment(), exact = !side && copies <= Integer.MAX_VALUE && effects.containsKey((int) copies);
        List<SeasoningPotions.Entry> result = new ArrayList<>();
        for (SeasoningPotions.Entry entry : effects.get(side ? 1 : exact ? (int) copies : 3)) {
            MobEffectInstance effect = entry.effect;
            boolean scaling = !side && !exact && SeasoningPotions.scales(effect.getEffect());
            double scale = side ? copies : exact ? 1 : Seasoning.durationScale(copies) / Math.max(.000001, Seasoning.durationScale(3));
            result.add(new SeasoningPotions.Entry(new MobEffectInstance(effect.getEffect(),
                Seasoning.ticks(effect.getDuration() * (scaling ? 1 : scale)),
                Seasoning.amplifier((long) effect.getAmplifier() + (scaling ? copies - 3L : 0))), entry.chance));
        }
        return result;
    }
    @Override public boolean accompaniment() { return !material.isEmpty(); }
    public net.minecraft.world.item.ItemStack material() { return material.copy(); }
    public net.minecraft.world.item.ItemStack returned() { return returned.copy(); }
    public boolean matches(net.minecraft.world.item.ItemStack stack) { return accompaniment() && SpiceRecipe.same(material, stack); }
    @Override public boolean nativeMaterial() { return accompaniment(); }
    @Override public boolean available() { return true; }
    public String text(String field) {
        String language = net.neoforged.fml.loading.FMLEnvironment.dist.isClient()
            ? com.xy177.solwarly.client.CustomSpiceLanguage.current() : "en_us";
        return text(language, field);
    }
    public String text(String language, String field) {
        Map<String, String> values = translations.getOrDefault(language,
            translations.getOrDefault("en_us", translations.get(translations.firstKey())));
        return values.getOrDefault(field, "");
    }
    static String required(JsonObject json, String field) {
        if (json == null || !json.has(field) || !json.get(field).isJsonPrimitive() || !json.get(field).getAsJsonPrimitive().isString()
            || json.get(field).getAsString().trim().isEmpty()) throw new IllegalArgumentException("Missing string: " + field);
        return json.get(field).getAsString();
    }
    static int integer(JsonObject json, String field, int min, int max) {
        double number = number(json, field);
        if (!Double.isFinite(number) || number != Math.rint(number) || number < min || number > max)
            throw new IllegalArgumentException(field + " must be an integer between " + min + " and " + max);
        return (int) number;
    }
    private static double number(JsonObject json, String field) {
        if (json == null || !json.has(field) || !json.get(field).isJsonPrimitive() || !json.get(field).getAsJsonPrimitive().isNumber())
            throw new IllegalArgumentException("Missing number: " + field);
        return json.get(field).getAsDouble();
    }
    private static int color(JsonObject json) {
        String value = required(json, "color");
        if (!value.matches("#[0-9a-fA-F]{6}")) throw new IllegalArgumentException("color must be #RRGGBB");
        return Integer.parseInt(value.substring(1), 16);
    }
    private static double multiplier(JsonObject json) {
        double value = json.has("diversity_multiplier") ? number(json, "diversity_multiplier") : 1.2;
        if (!Double.isFinite(value) || value < 0 || value > 100) throw new IllegalArgumentException("Invalid diversity_multiplier (0..100)");
        return value;
    }
}
