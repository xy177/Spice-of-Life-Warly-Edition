package com.xy177.solwarly.spice;

import net.minecraft.world.item.Item;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class SpiceKind implements Comparable<SpiceKind> {
    private static final java.util.List<SpiceKind> BUILTINS = new java.util.ArrayList<>();
    public static final SpiceKind SUGAR = new SpiceKind("SUGAR", 0xEBC978, "solwarly:sweet", null);
    public static final SpiceKind SALT = new SpiceKind("SALT", 0xF3F1DD, "solwarly:tasty", null);
    public static final SpiceKind TOMATO = new SpiceKind("TOMATO", 0xD84C3F, "farmersdelight:comfort", "farmersdelight:tomato");
    public static final SpiceKind ONION = new SpiceKind("ONION", 0xCAA879, "solwarly:aromatic", "farmersdelight:onion");
    public static final SpiceKind HAM = new SpiceKind("HAM", 0xA35A4A, "farmersdelight:nourishment", "farmersdelight:smoked_ham");
    public static final SpiceKind RICE = new SpiceKind("RICE", 0xEADDBB, null, "farmersdelight:cooked_rice");
    public static final SpiceKind SPICY = new SpiceKind("SPICY", 0xE95D22, "solwarly:hot", "nethersdelight:propelpearl");
    public static final SpiceKind CHORUS = new SpiceKind("CHORUS", 0xB788B8, null, "ends_delight:chorus_fruit_grain");
    public static final SpiceKind DRAGON = new SpiceKind("DRAGON", 0xA66ACB, null, "ends_delight:raw_dragon_meat");
    public static final SpiceKind PICKLE = new SpiceKind("PICKLE", 0x91A84D, "solwarly:appetizing", "brewinandchewin:kimchi");
    public static final SpiceKind CHEESE = new SpiceKind("CHEESE", 0xE8CC6B, null, "brewinandchewin:flaxen_cheese_wedge");
    public static final SpiceKind CARROT = new SpiceKind("CARROT", 0xE1A352, "minecraft:night_vision", "miners_delight:cave_carrot");
    public static final SpiceKind SEARING = new SpiceKind("SEARING", 0xF5A747, "twilightdelight:fire_range", "twilightforest:fiery_ingot");
    public static final SpiceKind TOXIC = new SpiceKind("TOXIC", 0x87B340, "twilightdelight:poison_range", "twilightforest:steeleaf_ingot");
    public static final SpiceKind FROST = new SpiceKind("FROST", 0x8ADCE6, "twilightdelight:frozen_range", "twilightforest:ice_bomb");
    public static final SpiceKind MUSHROOM = new SpiceKind("MUSHROOM", 0xC34E48, "twilight_spark_delight:charge", "twilight_spark_delight:labyrinth_mushroom");
    public static final SpiceKind EXPERIMENT = new SpiceKind("EXPERIMENT", 0x945D76, "twilight_spark_delight:symbiosis", "twilight_spark_delight:experiment_000");
    public static final SpiceKind MYSTERY = new SpiceKind("MYSTERY", 0xE7E3CA, null, "twilight_spark_delight:quest_ram_cheese");
    public static final SpiceKind DEEP_TEAR = new SpiceKind("DEEP_TEAR", 0x4D7FB8, "twilight_spark_delight:sorrow", "twilight_spark_delight:experiment_234");
    public static final SpiceKind FRUIT_APPLE = new SpiceKind("FRUIT_APPLE", 0xC3833D); public static final SpiceKind FRUIT_BLUEBERRY = new SpiceKind("FRUIT_BLUEBERRY", 0x5A32BF); public static final SpiceKind FRUIT_GLOWBERRY = new SpiceKind("FRUIT_GLOWBERRY", 0xFAAB16);
    public static final SpiceKind FRUIT_HAMIMELON = new SpiceKind("FRUIT_HAMIMELON", 0xFFC14E); public static final SpiceKind FRUIT_MELON = new SpiceKind("FRUIT_MELON", 0xC83627); public static final SpiceKind FRUIT_HAWBERRY = new SpiceKind("FRUIT_HAWBERRY", 0xBF1D1D);
    public static final SpiceKind FRUIT_LYCHEE = new SpiceKind("FRUIT_LYCHEE", 0xFFF2FC); public static final SpiceKind FRUIT_MANGO = new SpiceKind("FRUIT_MANGO", 0xFFB229); public static final SpiceKind FRUIT_ORANGE = new SpiceKind("FRUIT_ORANGE", 0xF7970F);
    public static final SpiceKind FRUIT_PEACH = new SpiceKind("FRUIT_PEACH", 0xE78264); public static final SpiceKind FRUIT_PEAR = new SpiceKind("FRUIT_PEAR", 0xBECB32); public static final SpiceKind FRUIT_PERSIMMON = new SpiceKind("FRUIT_PERSIMMON", 0xE15F1E);
    public static final SpiceKind FRUIT_PINEAPPLE = new SpiceKind("FRUIT_PINEAPPLE", 0xFDDC28); public static final SpiceKind FRUIT_LEMON = new SpiceKind("FRUIT_LEMON", 0xFFE47A); public static final SpiceKind FRUIT_CRANBERRY = new SpiceKind("FRUIT_CRANBERRY", 0xCF2626);
    public static final SpiceKind FRUIT_MANGOSTEEN = new SpiceKind("FRUIT_MANGOSTEEN", 0xFBECD6); public static final SpiceKind FRUIT_SWEETBERRY = new SpiceKind("FRUIT_SWEETBERRY", 0xC41A4F); public static final SpiceKind FRUIT_CHORUS = new SpiceKind("FRUIT_CHORUS", 0xD0A9D7);
    public static final SpiceKind FRUIT_BAYBERRY = new SpiceKind("FRUIT_BAYBERRY", 0x541135); public static final SpiceKind FRUIT_KIWI = new SpiceKind("FRUIT_KIWI", 0x89D90D); public static final SpiceKind FRUIT_FIG = new SpiceKind("FRUIT_FIG", 0xE08517); public static final SpiceKind FRUIT_DURIAN = new SpiceKind("FRUIT_DURIAN", 0xEDD955);
    public static final SpiceKind BEEF = new SpiceKind("BEEF", 0x985643, "minecraft:strength", "minecraft:cooked_beef");

    public static final SpiceKind POISONOUS_POTATO = new SpiceKind("POISONOUS_POTATO", 0xA69E49, "immortalers_delight:resistance_to_illager", "minecraft:poisonous_potato");
    public static final SpiceKind SPIDER_EYE = new SpiceKind("SPIDER_EYE", 0x922C47, "immortalers_delight:resistance_to_arthropod", "minecraft:spider_eye");
    public static final SpiceKind PUFFERFISH = new SpiceKind("PUFFERFISH", 0xD5B04D, "immortalers_delight:resistance_to_abyssal", "minecraft:pufferfish");
    public static final SpiceKind CINEMA = new SpiceKind("CINEMA", 0xEDCA73, "immortalers_delight:incandescence", "immortalers_delight:popolutcorn");
    public static final SpiceKind MILK_TEA = new SpiceKind("MILK_TEA", 0xD9C686, null, "immortalers_delight:pearlipearl_milk_tea");
    public static final SpiceKind MILK_GREEN = new SpiceKind("MILK_GREEN", 0x7C9D6B, null, "immortalers_delight:pearlipearl_milk_green");
    public static final SpiceKind MUSTARD = new SpiceKind("MUSTARD", 0xD68A25, "immortalers_delight:warm_current_surges", "immortalers_delight:torchflower_mustard");
    public static final SpiceKind POD_HUSK = new SpiceKind("POD_HUSK", 0xB28C6B, "immortalers_delight:burn_the_boats", "immortalers_delight:pitcher_pod_petal");
    public static final SpiceKind ALFALFA = new SpiceKind("ALFALFA", 0x7D9A54, "immortalers_delight:keep_a_fast", "immortalers_delight:alfalfa");
    public static final SpiceKind EVOLUTCORN = new SpiceKind("EVOLUTCORN", 0xE5D89B, "immortalers_delight:cultural_legacy", "immortalers_delight:evolutcorn");
    public static final SpiceKind MIRACLE = new SpiceKind("MIRACLE", 0xE8BF4E, "immortalers_delight:magical_reverse", "minecraft:gold_ingot");
    public static final SpiceKind HIMEKAIDO = new SpiceKind("HIMEKAIDO", 0xDB6A72, "immortalers_delight:relieve_poison", "immortalers_delight:himekaido");
    public static final SpiceKind PEARLIPEARL = new SpiceKind("PEARLIPEARL", 0xD9C686, "immortalers_delight:vitality", "immortalers_delight:pearlipearl");
    public static final SpiceKind LEISAMBOO = new SpiceKind("LEISAMBOO", 0x7C9D6B, "immortalers_delight:lingering_infusion", "immortalers_delight:leisamboo_tea");
    public static final SpiceKind CHILLED_SUGAR = new SpiceKind("CHILLED_SUGAR", 0xA9DFEC, "immortalers_delight:cool", null);
    public static final SpiceKind ONSEN_EGG = new SpiceKind("ONSEN_EGG", 0xEADDBB, "immortalers_delight:prehistoric_powers", "immortalers_delight:ancient_onsen_tamago");
    public static final SpiceKind PURGATORY_ALE = new SpiceKind("PURGATORY_ALE", 0xCAA879, "immortalers_delight:prehistoric_powers", "immortalers_delight:purgatory_ale");
    public static final SpiceKind POWDERED_SOUL = new SpiceKind("POWDERED_SOUL", 0x527E86, "immortalers_delight:deepness", "immortalers_delight:warped_laurel");
    public static final SpiceKind CONQUEROR = new SpiceKind("CONQUEROR", 0x6B5B88, "immortalers_delight:gaixia", "minecraft:pitcher_plant");
    public static final SpiceKind GELPITAYA = new SpiceKind("GELPITAYA", 0x8CCBDC, "immortalers_delight:let_it_freeze", "immortalers_delight:gelpitaya_flesh");
    public static final SpiceKind ASH = new SpiceKind("ASH", 0x6B6258, "immortalers_delight:smoke_abstinence", "immortalers_delight:ab_ash");
    public static final SpiceKind TRAVARICE = new SpiceKind("TRAVARICE", 0xEADDBB, "immortalers_delight:unyielding", "immortalers_delight:cooked_travarice");
    public static final SpiceKind MOONLIT = new SpiceKind("MOONLIT", 0xE9DFAA, "immortalers_delight:moon_bright", "immortalers_delight:sextlotus_root");
    public static final SpiceKind WALNUT = new SpiceKind("WALNUT", 0x6B5246, "immortalers_delight:infernal_forging", "immortalers_delight:obsidian_walnut_kernel");

    public static final SpiceKind CREATIVE = new SpiceKind("CREATIVE", 0xEBC978, null, null);

    public boolean accompaniment() { return this == RICE || this == MILK_TEA || this == MILK_GREEN || this == ONSEN_EGG || this == PURGATORY_ALE || this == TRAVARICE; }
    public boolean nativeMaterial() { return accompaniment() || this == MUSTARD || this == ASH; }
    public boolean immortal() { return ordinal() >= POISONOUS_POTATO.ordinal() && ordinal() <= WALNUT.ordinal(); }

    private final String name;
    private final int ordinal;
    public final int defaultColor;
    public final String potionId, ingredientId;
    private SpiceKind(String name, int color, String potion, String ingredient) {
        this.name = name; this.ordinal = BUILTINS.size();
        defaultColor = color; potionId = potion; ingredientId = ingredient; BUILTINS.add(this);
    }
    private SpiceKind(String name, int color) { this(name, color, null, null); }
    protected SpiceKind(String id, int color, double multiplier) {
        name = id; ordinal = Integer.MAX_VALUE; defaultColor = color; potionId = null; ingredientId = null;
        this.customMultiplier = multiplier;
    }
    private double customMultiplier;
    public String name() { return name; }
    public int ordinal() { return ordinal; }
    public static SpiceKind[] values() { return BUILTINS.toArray(new SpiceKind[0]); }
    public static java.util.List<SpiceKind> all() {
        java.util.List<SpiceKind> result = new java.util.ArrayList<>(BUILTINS); result.addAll(CustomSpices.all()); return result;
    }
    public int color() { return SpiceConfig.colors.getOrDefault(this, defaultColor); }
    public double multiplier() { return SpiceConfig.multipliers.getOrDefault(this, customMultiplier); }
    @Override public int compareTo(SpiceKind other) {
        int order = Integer.compare(ordinal, other.ordinal); return order == 0 ? id().compareTo(other.id()) : order;
    }
    public String id() { return name().toLowerCase(java.util.Locale.ROOT); }
    public boolean fruit() { return name().startsWith("FRUIT_"); }
    public String fruitId() { return fruit() ? id().substring(6) : ""; }
    public MobEffect potion() { return potionId == null ? null : com.xy177.solwarly.registry.RegistryLookup.effect(new ResourceLocation(potionId)); }
    public static boolean usesMyNethersDelight() {
        return com.xy177.solwarly.registry.RegistryLookup.item(SPICY.ingredientId) == null
            && com.xy177.solwarly.registry.RegistryLookup.item("mynethersdelight:bullet_pepper") != null;
    }
    public static String spicyIngredientId() {
        return usesMyNethersDelight() ? "mynethersdelight:bullet_pepper" : SPICY.ingredientId;
    }
    public boolean available() {
        if (immortal()) {
            if (com.xy177.solwarly.registry.RegistryLookup.item("immortalers_delight:himekaido") == null) return false;
            if (potionId != null && potion() == null) return false;
            if (this == GELPITAYA) return com.xy177.solwarly.registry.RegistryLookup.item("immortalers_delight:gelpitaya") != null
                || com.xy177.solwarly.registry.RegistryLookup.item(ingredientId) != null;
        }
        if (fruit()) return com.xy177.solwarly.registry.RegistryLookup.item("fruitsdelight:" + fruitId() + "_jelly") != null;
        if (this == SPICY) return potion() != null && (com.xy177.solwarly.registry.RegistryLookup.item("minecraft:blaze_powder") != null || com.xy177.solwarly.registry.RegistryLookup.item(ingredientId) != null);
        if (this == CHORUS) return com.xy177.solwarly.registry.RegistryLookup.item("minecraft:chorus_fruit") != null || com.xy177.solwarly.registry.RegistryLookup.item(ingredientId) != null;
        if (this == DRAGON) return com.xy177.solwarly.registry.RegistryLookup.item("ends_delight:raw_dragon_meat") != null
            || com.xy177.solwarly.registry.RegistryLookup.item("ends_delight:roasted_dragon_meat") != null
            || com.xy177.solwarly.registry.RegistryLookup.item("ends_delight:raw_dragon_meat_cuts") != null
            || com.xy177.solwarly.registry.RegistryLookup.item("ends_delight:roasted_dragon_meat_cuts") != null;
        if (this == PICKLE) return com.xy177.solwarly.registry.RegistryLookup.item("brewinandchewin:kimchi") != null
            || com.xy177.solwarly.registry.RegistryLookup.item("brewinandchewin:pickled_pickles") != null;
        if (this == CHEESE) return com.xy177.solwarly.registry.RegistryLookup.item("brewinandchewin:flaxen_cheese_wedge") != null
            || com.xy177.solwarly.registry.RegistryLookup.item("brewinandchewin:scarlet_cheese_wedge") != null;
        if (this == SEARING || this == TOXIC || this == FROST) return potion() != null && com.xy177.solwarly.registry.RegistryLookup.item(ingredientId) != null;
        return ingredientId == null || com.xy177.solwarly.registry.RegistryLookup.item(ingredientId) != null;
    }
    public static SpiceKind from(String id) {
        for (SpiceKind kind : values()) if (kind.id().equals(id)) return kind;
        return CustomSpices.find(id);
    }
}
