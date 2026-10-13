package com.xy177.solwarly.spice;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SpiceKind implements Comparable<SpiceKind> {
    private static final java.util.List<SpiceKind> BUILTINS = new java.util.ArrayList<>();
    public static final SpiceKind SUGAR = new SpiceKind("SUGAR", 0xEBC978, "solwarly:sweet", null);
    public static final SpiceKind SALT = new SpiceKind("SALT", 0xF3F1DD, "solwarly:tasty", null);
    public static final SpiceKind TOMATO = new SpiceKind("TOMATO", 0xD84C3F, "farmersdelight:comfort", "farmersdelight:tomato");
    public static final SpiceKind ONION = new SpiceKind("ONION", 0xCAA879, "solwarly:aromatic", "farmersdelight:onion");
    public static final SpiceKind HAM = new SpiceKind("HAM", 0xA35A4A, "farmersdelight:nourishment", "farmersdelight:smoked_ham");
    public static final SpiceKind RICE = new SpiceKind("RICE", 0xEADDBB, null, "farmersdelight:cooked_rice");
    public static final SpiceKind SPICY = new SpiceKind("SPICY", 0xE95D22, "solwarly:hot", "nethers_delight_legacy:propelpearl");
    public static final SpiceKind CHORUS = new SpiceKind("CHORUS", 0xB788B8, null, "ends_delight:chorus_fruit_grain");
    public static final SpiceKind DRAGON = new SpiceKind("DRAGON", 0xA66ACB, null, "ends_delight:raw_dragon_meat");
    public static final SpiceKind PICKLE = new SpiceKind("PICKLE", 0x91A84D, "solwarly:appetizing", "brewinandchewinlegacy:kimchi");
    public static final SpiceKind CHEESE = new SpiceKind("CHEESE", 0xE8CC6B, null, "brewinandchewinlegacy:flaxen_cheese_wedge");
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

    public static final SpiceKind CREATIVE = new SpiceKind("CREATIVE", 0xEBC978, null, null);
    public boolean accompaniment() { return this == RICE; }

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
    public Potion potion() { return potionId == null ? null : ForgeRegistries.POTIONS.getValue(new ResourceLocation(potionId)); }
    public boolean available() {
        if (fruit()) return Item.getByNameOrId("fruits_delight_legacy:" + fruitId() + "_jelly") != null;
        if (this == SPICY) return potion() != null && (Item.getByNameOrId("minecraft:blaze_powder") != null || Item.getByNameOrId(ingredientId) != null);
        if (this == CHORUS) return Item.getByNameOrId("minecraft:chorus_fruit") != null || Item.getByNameOrId(ingredientId) != null;
        if (this == DRAGON) return Item.getByNameOrId("ends_delight:raw_dragon_meat") != null
            || Item.getByNameOrId("ends_delight:roasted_dragon_meat") != null
            || Item.getByNameOrId("ends_delight:raw_dragon_meat_cuts") != null
            || Item.getByNameOrId("ends_delight:roasted_dragon_meat_cuts") != null;
        if (this == PICKLE) return Item.getByNameOrId("brewinandchewinlegacy:kimchi") != null
            || Item.getByNameOrId("brewinandchewinlegacy:pickled_pickles") != null;
        if (this == CHEESE) return Item.getByNameOrId("brewinandchewinlegacy:flaxen_cheese_wedge") != null
            || Item.getByNameOrId("brewinandchewinlegacy:scarlet_cheese_wedge") != null;
        if (this == SEARING || this == TOXIC || this == FROST) return potion() != null && Item.getByNameOrId(ingredientId) != null;
        return ingredientId == null || Item.getByNameOrId(ingredientId) != null;
    }
    public static SpiceKind from(String id) {
        for (SpiceKind kind : values()) if (kind.id().equals(id)) return kind;
        return CustomSpices.find(id);
    }
}
