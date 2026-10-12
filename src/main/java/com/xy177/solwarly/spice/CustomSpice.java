package com.xy177.solwarly.spice;

import com.google.gson.*;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.*;

/** A user-defined flavor uses the same item/NBT and processing paths as built-in powders. */
public final class CustomSpice extends SpiceKind {
    final JsonObject definition;
    private final SortedMap<String, Map<String, String>> translations = new TreeMap<>();
    private final List<List<SeasoningPotions.Entry>> effects = new ArrayList<>();

    public CustomSpice(JsonObject json) {
        super(required(json, "id"), color(json), multiplier(json));
        if (!id().matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) throw new IllegalArgumentException("id must be namespace:path");
        definition = new JsonParser().parse(json.toString()).getAsJsonObject();
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
        JsonObject tiers = json.getAsJsonObject("effects");
        if (tiers == null) throw new IllegalArgumentException("effects must define arrays for 1, 2 and 3 copies");
        for (int copies = 1; copies <= 3; copies++) {
            JsonArray specs = tiers.getAsJsonArray(Integer.toString(copies));
            if (specs == null) throw new IllegalArgumentException("Missing effects for " + copies + " copies (use [] for no effects)");
            List<SeasoningPotions.Entry> entries = new ArrayList<>(); Set<Potion> seen = new HashSet<>();
            for (JsonElement element : specs) {
                JsonObject spec = element.getAsJsonObject();
                Potion potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(required(spec, "potion")));
                if (potion == null) throw new IllegalArgumentException("Unknown potion: " + spec.get("potion"));
                if (!seen.add(potion)) throw new IllegalArgumentException("Duplicate potion within the same copy tier: " + potion.getRegistryName());
                int level = integer(spec, "level", 1, 256);
                double seconds = number(spec, "duration_seconds");
                if (!Double.isFinite(seconds) || seconds < .05 || seconds > Integer.MAX_VALUE / 20.0)
                    throw new IllegalArgumentException("duration_seconds must be positive and within the tick range");
                entries.add(new SeasoningPotions.Entry(new PotionEffect(potion, (int) Math.round(seconds * 20), level - 1), 1));
            }
            effects.add(Collections.unmodifiableList(entries));
        }
    }
    public List<SeasoningPotions.Entry> effects(int copies) {
        List<SeasoningPotions.Entry> result = new ArrayList<>();
        for (SeasoningPotions.Entry entry : effects.get(Math.max(1, Math.min(3, copies)) - 1))
            result.add(new SeasoningPotions.Entry(new PotionEffect(entry.effect), entry.chance));
        return result;
    }
    @Override public boolean available() { return true; }
    public String text(String field) {
        String language = FMLCommonHandler.instance().getSide().isClient()
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
