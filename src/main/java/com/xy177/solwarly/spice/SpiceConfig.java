package com.xy177.solwarly.spice;

import com.xy177.solwarly.config.Configuration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import java.io.File;
import java.util.Map;
import java.util.LinkedHashMap;

public final class SpiceConfig {
    private static Configuration config;
    private static String lastMixingSnapshot = "";
    public static final Map<SpiceKind, Integer> colors = new LinkedHashMap<>();
    public static final Map<SpiceKind, Double> multipliers = new LinkedHashMap<>();
    public static final Map<SpiceKind, Integer> durations = new LinkedHashMap<>();
    public static int mixedCount = 2, duration = 2400, hamExtension = 2400, hamDuration = 1800, riceHunger = 7;
    public static int mixedIngredients = 3, maxSeasonings = 1, maxAccompaniments = 2;
    public static boolean allowSeasoningWithAccompaniments = false;
    public static int fruitMinimumDuration = 1800;
    public static int spicyPungentDuration = 2400;
    public static String mixedCatalystItem = "minecraft:echo_shard";
    public static int mixedCatalystMetadata = 0;
    public static boolean consumeMixedCatalyst = false;
    public static double doubleDuration = 1.5, tripleDuration = 2, mixedMultiplier = 1.2,
        miningPerLevel = .2, attackSpeedPerLevel = .07, healingPerLevel = .1,
        reductionBase = .1, reductionPerLevel = .05, riceSaturation = 7,
        hotBase = .1, hotPerLevel = .1, cheeseHealing = 6, sharingPerCopy = .4, sharingRadius = 5, appetiteBase = .1, appetitePerLevel = .1;
    public static int fireSeconds = 5;
    public static double accompanimentMultiplier = 1.2;
    static { for (SpiceKind kind : SpiceKind.values()) { colors.put(kind, kind.defaultColor); multipliers.put(kind, 1.2); durations.put(kind, defaultDuration(kind)); } }
    private static int defaultDuration(SpiceKind kind) { return kind.immortal() ? ImmortalSpices.defaultDuration(kind) : kind == SpiceKind.PICKLE || kind == SpiceKind.CARROT ? 6000 : 2400; }

    public static void load(File directory) {
        config = new Configuration(new File(directory, "solwarly-spices.toml")); config.load();
        mixedCount = integer("mix", "output", 2, 1, 64, "每次合成混合调味粉的产量。\nMixed spice output per craft.");
        mixedIngredients = integer("mix", "inputCount", 3, 1, 8,
            "混合调味料所需的单粉数量，另需一个配置的额外物品。\nNumber of single powders required for a blend, plus one configured catalyst.");
        maxSeasonings = integer("seasoning", "maxApplications", 1, 1, Integer.MAX_VALUE,
            "每份食物累计使用调味粉的次数上限；一份单粉或混合粉均算一次，可重复同种。\nMaximum powder applications per food; a single powder or a blend each counts once, including repeats.");
        maxAccompaniments = integer("accompaniments", "maxApplications", 2, 1, Integer.MAX_VALUE,
            "每份食物使用佐料的次数上限，独立于调味粉；仅前两份显示佐料角标。\nMaximum accompaniment servings per food, separate from powders; only the first two have badges.");
        allowSeasoningWithAccompaniments = config.getBoolean("allowWithAccompaniments", "seasoning", false,
            "是否允许同一食物同时具有佐料和调味粉，开启后可同时加入或先后追加。\nAllow accompaniments and powders on the same food, together or added sequentially.");
        mixedCatalystItem = config.getString("catalystItem", "mix", "minecraft:echo_shard",
            "混合调味粉所需的额外物品注册名，每次需要一个，默认回响碎片；无效物品会禁用混合配方，不能使用调味粉本身。\nRegistry ID of the extra item required for mixing, one per craft; defaults to an Echo Shard. Invalid items disable mixing; spice powder itself is not supported.").trim();
        mixedCatalystMetadata = integer("mix", "catalystMetadata", 0, 0, 32766,
            "额外物品的损伤值，默认0；不限制其NBT。\nDamage value of the extra item, default 0; NBT is not restricted.");
        consumeMixedCatalyst = config.getBoolean("consumeCatalyst", "mix", false,
            "是否每次消耗一个额外物品；false原样返还一个，true按普通合成消耗并返还物品自身的容器。\nWhether to consume one extra item per craft; false returns it unchanged, true consumes it with normal container-item handling.");
        doubleDuration = number("mix", "doubleDuration", 1.5, 0, 100, "同种两份不增强等级的效果时长倍率。\nDuration multiplier for two copies of a non-scaling effect.");
        tripleDuration = number("mix", "tripleDuration", 2, 0, 100, "同种三份不增强等级的效果时长倍率。\nDuration multiplier for three copies of a non-scaling effect.");
        mixedMultiplier = number("mix", "diversityMultiplier", 1.2, 0, 100, "混合粉整体多样性倍率，不累乘成分。\nOverall mixed spice diversity multiplier; components do not multiply it.");
        duration = integer("effects", "durationTicks", 2400, 1, 72000, "普通调味效果持续游戏刻数，20刻为一秒。\nRegular effect duration in ticks; 20 ticks equal one second.");
        fruitMinimumDuration = integer("effects", "fruitMinimumDurationTicks", 1800, 0, 72000,
            "果香粉单份效果最低持续刻数，默认1800刻为90秒；更长的果酱效果保留，之后再应用混合时长倍率。\nMinimum single-copy fruit spice effect duration in ticks; 1800 ticks equal 90 seconds. Longer jam effects are preserved, then mixed duration multipliers apply.");
        hamExtension = integer("effects", "hamExtensionTicks", 2400, 0, 72000, "原食物滋养效果额外延长刻数。\nExtra ticks added to the food's original nourishment.");
        spicyPungentDuration = integer("spicy", "pungentDurationTicks", 2400, 0, 72000,
            "仅使用My Nether's Delight替代联动时，火辣调味额外给予的正面呛鼻Ⅰ基础时长；默认2400刻为两分钟，重复只延长时长，0禁用。\nBase duration of the extra beneficial Pungent I from spicy seasoning when using My Nether's Delight as a fallback; 2400 ticks equal two minutes, repeats extend duration only, and 0 disables it.");
        hamDuration = integer("effects", "hamDurationTicks", 1800, 1, 72000, "原食物无滋养时火腿粉给予的刻数。\nNourishment ticks from ham when the original food provides none.");
        miningPerLevel = number("effects", "miningPerLevel", .2, 0, 100, "甜蜜每级挖掘速度加成，0.2为20%。\nMining speed bonus per Sweet level; 0.2 means 20%.");
        attackSpeedPerLevel = number("effects", "attackSpeedPerLevel", .07, 0, 100, "甜蜜每级攻击速度加成。\nAttack speed bonus per Sweet level.");
        healingPerLevel = number("effects", "healingPerLevel", .1, 0, 100, "好滋味每级恢复生命值的比例加成。\nHealing amount bonus per Tasty level.");
        reductionBase = number("effects", "reductionBase", .1, 0, 1, "辛香基础减伤比例。\nBase Aromatic damage reduction.");
        reductionPerLevel = number("effects", "reductionPerLevel", .05, 0, 1, "辛香每级额外减伤比例，合计最多100%。\nAdditional damage reduction per Aromatic level; total is capped at 100%.");
        accompanimentMultiplier = number("accompaniments", "diversityMultiplier", 1.2, 0, 100, "两份配餐的整体多样性倍率，不逐份累乘。\nOverall diversity multiplier for two accompaniments, without multiplying per serving.");
        riceHunger = integer("rice", "hunger", 7, 0, 100, "佐米饭额外恢复的饥饿点数。\nAdditional hunger points from a rice serving.");
        riceSaturation = number("rice", "saturation", 7, 0, 100, "佐米饭额外恢复的实际饱和度点数，不是饱和度倍率。\nAdditional actual saturation points from rice, not a saturation modifier.");
        hotBase = number("effects", "hotBase", .1, 0, 100, "火热基础伤害加成比例。\nBase Hot damage bonus.");
        hotPerLevel = number("effects", "hotPerLevel", .1, 0, 100, "火热每级额外伤害加成比例。\nAdditional Hot damage bonus per level.");
        fireSeconds = integer("effects", "fireSeconds", 5, 0, 3600, "火热攻击点燃目标的秒数。\nSeconds a Hot attack ignites its target.");
        appetiteBase = number("effects", "appetiteBase", .1, 0, 100, "开胃基础营养收益加成。\nBase Appetizing nutrition bonus.");
        appetitePerLevel = number("effects", "appetitePerLevel", .1, 0, 100, "开胃每级额外饥饿值和实际饱和度收益加成；饥饿值四舍五入。\nAdditional hunger and actual saturation bonus per Appetizing level; hunger is rounded to the nearest integer.");
        cheeseHealing = number("effects", "cheeseHealing", 6, 0, 100, "没有微醺时奶酪粉恢复的生命点数。\nHealth points restored by cheese when not Tipsy.");
        sharingPerCopy = number("effects", "sharingPerCopy", .4, 0, 100, "每份猎龙人粉共享本次实际饱和度和非负面药水时长的比例，不共享饥饿或已有状态。\nFraction of this meal's actual saturation and non-negative effect duration shared per dragon powder copy; excludes hunger and existing buffs.");
        sharingRadius = number("effects", "sharingRadius", 5, 0, 128, "猎龙人粉共享给其他玩家的球形半径（格）。\nSpherical radius in blocks for sharing with other players.");
        for (SpiceKind kind : SpiceKind.values()) {
            colors.put(kind, integer(kind.id(), "color", kind.defaultColor, 0, 0xFFFFFF, "粉末及说明文字的RGB十六进制颜色，以十进制整数存储。\nRGB hex color of powder and tooltip text, stored as a decimal integer."));
            multipliers.put(kind, number(kind.id(), "diversityMultiplier", 1.2, 0, 100, "该调味的多样性贡献倍率，不改变累计基础系数。\nDiversity contribution multiplier; cumulative base coefficient is unchanged."));
            durations.put(kind, integer(kind.id(), "durationTicks", kind.ordinal() <= SpiceKind.RICE.ordinal() ? duration : defaultDuration(kind), 1, 72000,
                "该调味基础药水时长（游戏刻），果香粉改用果酱原效果，此项不作用于即时功能。\nBase seasoning potion duration in ticks; fruit spices use original jam effects, and this does not affect instant actions."));
        }
        ImmortalSpices.load(config);
        save();
    }
    public static int recipeCount(String recipe, boolean input, int fallback, int max) {
        return integer("recipes." + recipe, input ? "inputCount" : "outputCount", fallback, 1, max,
            input ? "此原料每批消耗数量；双原料配方分别配置两种原料。\nAmount of this ingredient consumed per batch; two-ingredient recipes configure each material separately."
                  : "每批产出的调味粉数量。\nSpice items produced per batch.");
    }
    public static void save() { if (config != null) config.save(); }
    public static ItemStack mixedCatalyst() {
        try {
            ResourceLocation id = net.minecraft.resources.ResourceLocation.parse(mixedCatalystItem);
            Item item = com.xy177.solwarly.registry.RegistryLookup.item(id.toString());
            if (item == null || item == SpiceRegistry.spice) return ItemStack.EMPTY;
            ItemStack result = new ItemStack(item);
            if (mixedCatalystMetadata != 0) result.setDamageValue(mixedCatalystMetadata);
            return result;
        } catch (IllegalArgumentException exception) { return ItemStack.EMPTY; }
    }
    public static boolean matchesMixedCatalyst(ItemStack stack) {
        ItemStack catalyst = mixedCatalyst();
        return !stack.isEmpty() && !catalyst.isEmpty() && ItemStack.isSameItem(stack, catalyst)
            && stack.getDamageValue() == catalyst.getDamageValue();
    }
    private static int integer(String category, String key, int value, int min, int max, String text) {
        return config.getInt(key, category, value, min, max, text);
    }
    private static double number(String category, String key, double value, double min, double max, String text) {
        double result = config.get(category, key, value, text, min, max).getDouble();
        return Double.isFinite(result) ? Math.max(min, Math.min(max, result)) : value;
    }
    public static CompoundTag snapshot() {
        CompoundTag tag = new CompoundTag();
        tag.put("immortal", ImmortalSpices.snapshot()); tag.putDouble("accompanimentMultiplier", accompanimentMultiplier);
        for (SpiceKind kind : SpiceKind.values()) { tag.putDouble(kind.id(), multipliers.get(kind)); tag.putInt(kind.id() + "Color", colors.get(kind)); }
        tag.putDouble("mixed", mixedMultiplier); tag.putInt("mixedCount", mixedCount);
        tag.putString("mixedCatalystItem", mixedCatalystItem);
        tag.putInt("mixedCatalystMetadata", mixedCatalystMetadata);
        tag.putBoolean("consumeMixedCatalyst", consumeMixedCatalyst);
        tag.putDouble("mining", miningPerLevel); tag.putDouble("attackSpeed", attackSpeedPerLevel);
        tag.putInt("riceHunger", riceHunger); tag.putDouble("riceSaturation", riceSaturation);
        for (SpiceKind kind : SpiceKind.values()) tag.putInt(kind.id() + "Duration", durations.get(kind));
        tag.putInt("duration", duration); tag.putInt("hamExtension", hamExtension); tag.putInt("hamDuration", hamDuration);
        tag.putInt("fruitMinimumDuration", fruitMinimumDuration);
        tag.putInt("spicyPungentDuration", spicyPungentDuration);
        tag.putDouble("doubleDuration", doubleDuration); tag.putDouble("tripleDuration", tripleDuration);
        tag.putDouble("sharingPerCopy", sharingPerCopy); tag.putDouble("sharingRadius", sharingRadius);
        tag.putDouble("appetiteBase", appetiteBase); tag.putDouble("appetitePerLevel", appetitePerLevel);
        tag.putInt("mixedIngredients", mixedIngredients); tag.putInt("maxSeasonings", maxSeasonings); tag.putInt("maxAccompaniments", maxAccompaniments);
        tag.putBoolean("allowSeasoningWithAccompaniments", allowSeasoningWithAccompaniments);
        return tag;
    }
    public static void readSnapshot(CompoundTag tag) {
        mixedIngredients = tag.contains("mixedIngredients") ? Math.max(1, Math.min(8, tag.getInt("mixedIngredients"))) : 3;
        maxSeasonings = tag.contains("maxSeasonings") ? Math.max(1, tag.getInt("maxSeasonings")) : 1;
        maxAccompaniments = tag.contains("maxAccompaniments") ? Math.max(1, tag.getInt("maxAccompaniments")) : 2;
        allowSeasoningWithAccompaniments = tag.getBoolean("allowSeasoningWithAccompaniments");
        if (tag.contains("immortal")) ImmortalSpices.readSnapshot(tag.getCompound("immortal"));
        if (tag.contains("accompanimentMultiplier")) accompanimentMultiplier = tag.getDouble("accompanimentMultiplier");
        for (SpiceKind kind : SpiceKind.values()) { multipliers.put(kind, tag.getDouble(kind.id())); colors.put(kind, tag.getInt(kind.id() + "Color")); }
        mixedMultiplier = tag.getDouble("mixed"); mixedCount = tag.getInt("mixedCount");
        if (tag.contains("mixedCatalystItem")) {
            mixedCatalystItem = tag.getString("mixedCatalystItem");
            mixedCatalystMetadata = tag.getInt("mixedCatalystMetadata");
            consumeMixedCatalyst = tag.getBoolean("consumeMixedCatalyst");
        }
        miningPerLevel = tag.getDouble("mining"); attackSpeedPerLevel = tag.getDouble("attackSpeed");
        riceHunger = tag.getInt("riceHunger"); riceSaturation = tag.getDouble("riceSaturation");
        for (SpiceKind kind : SpiceKind.values()) if (tag.contains(kind.id() + "Duration")) durations.put(kind, tag.getInt(kind.id() + "Duration"));
        if (tag.contains("duration")) {
            duration = tag.getInt("duration"); hamExtension = tag.getInt("hamExtension"); hamDuration = tag.getInt("hamDuration");
            doubleDuration = tag.getDouble("doubleDuration"); tripleDuration = tag.getDouble("tripleDuration");
            sharingPerCopy = tag.getDouble("sharingPerCopy"); sharingRadius = tag.getDouble("sharingRadius");
            appetiteBase = tag.getDouble("appetiteBase"); appetitePerLevel = tag.getDouble("appetitePerLevel");
        }
        if (tag.contains("fruitMinimumDuration")) fruitMinimumDuration = tag.getInt("fruitMinimumDuration");
        if (tag.contains("spicyPungentDuration")) spicyPungentDuration = tag.getInt("spicyPungentDuration");
        // Compare received settings, not shared static fields already changed by an integrated-server reload.
        String mixingSnapshot = mixedCatalystItem + "@" + mixedCatalystMetadata + ":" + consumeMixedCatalyst + ":" + mixedCount + ":" + mixedIngredients;
        if (!lastMixingSnapshot.equals(mixingSnapshot)) {
            lastMixingSnapshot = mixingSnapshot;
            net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new MixingRecipeChanged());
        }
    }
    public static final class MixingRecipeChanged extends net.neoforged.bus.api.Event {}
    private SpiceConfig() {}
}
