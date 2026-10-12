package com.xy177.solwarly.diet;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public final class DietConfig {
    public static int recentCount = 32, frequencyPerPlayer = 20, frequencyMaximum = 4096, cravingCount = 5, boringCount = 5;
    public static boolean warlyMode = false;
    public static int warlyDurationDays = 2;
    public static double[] warlyRepeatMultipliers = {1.0, 0.9, 0.8, 0.65, 0.5, 0.3};
    public static double extraFlavor = 0.25, scale = 1, hungerWeight = 0.25, saturationWeight = 0.095,
        cumulativeFlavorFraction = 0.5,
        difficultyWeight = 0.65, minimum = 0.5, maximum = 12, craftWeight = 1, furnaceWeight = 1,
        cuttingWeight = 1, cookingWeight = 1.5, servingWeight = 0.25,
        cravingMultiplier = 1.25, normalMultiplier = 1, boringMultiplier = 0.75, uniformChance = 0.2,
        frequencyWeight = -1;
    public static double snackBoundary, mealBoundary, cuisineBoundary;
    public static final java.util.Map<String, Double> teaWeights = new java.util.LinkedHashMap<>();
    public static String[] cumulativeRewards = {"20|4", "50|8", "90|12", "140|16", "200|20"};
    public static String[] attributes = {
        "5|generic.maxHealth|2|0", "10|generic.maxHealth|4|0",
        "15|generic.maxHealth|4|0", "15|generic.movementSpeed|0.05|2",
        "25|generic.maxHealth|6|0", "25|generic.movementSpeed|0.05|2",
        "40|generic.maxHealth|8|0", "40|generic.movementSpeed|0.10|2", "40|generic.attackDamage|1|0"};
    public static String[] effects = {}, excludedInterest = {};
    public static String[] alwaysCraved = {}, alwaysBoring = {};
    public static String[] rawSources = {"minecraft:wheat", "minecraft:melon", "minecraft:iron_ore", "minecraft:gold_ore",
        "minecraft:apple", "minecraft:carrot", "minecraft:potato", "minecraft:beetroot", "minecraft:porkchop", "minecraft:beef",
        "minecraft:chicken", "minecraft:mutton", "minecraft:rabbit", "minecraft:fish@0", "minecraft:fish@1", "minecraft:fish@2", "minecraft:fish@3",
        "minecraft:egg", "minecraft:milk_bucket", "minecraft:reeds", "minecraft:dye@3", "farmersdelight:onion",
        "farmersdelight:cabbage", "farmersdelight:tomato", "farmersdelight:rice_panicle", "minecraft:pumpkin",
        "minecraft:diamond", "minecraft:emerald", "minecraft:coal", "minecraft:redstone", "minecraft:quartz",
        "minecraft:dye@4", "minecraft:slime_ball", "minecraft:bone", "minecraft:blaze_rod", "minecraft:leather", "teastory:tea_leaf", "teastory:xian_rice",
        "minecraft:rotten_flesh", "minecraft:string", "minecraft:prismarine_shard", "minecraft:prismarine_crystals"};
    public static File directory;

    public static void load(File directory) {
        DietConfig.directory = directory;
        Configuration config = new Configuration(new File(directory, "solwarly.cfg"));
        config.load();
        config.setCategoryComment("statistics", "饮食统计。原配置 solcarrot.cfg 的筛选与死亡规则仍生效。\nDietary statistics. Legacy filters and death settings remain in solcarrot.cfg.");
        warlyMode = config.getBoolean("warlyMode", "warlyMode", false,
            "是否全局启用沃利模式；玩家可用指令单独覆盖。\nWhether Warly Mode is enabled globally; players may override it with a command.");
        warlyDurationDays = config.getInt("repeatDurationDays", "warlyMode", 2, 1, 100,
            "同一种料理重复递减的持续天数；睡眠推进天数，time add 不推进。\nDays during which repeating one dish is reduced; sleeping advances days, while time add does not.");
        warlyRepeatMultipliers = config.get("warlyMode", "repeatMultipliers", new double[] {1.0, 0.9, 0.8, 0.65, 0.5, 0.3},
            "首次食用及随后五档重复食用的饥饿值、饱和度和本次完整多样性贡献倍率，范围大于0至1；进食耗时除以该倍率，美食阅历不变。第五次及之后重复使用最后一档。垂涎食物不递减、不计次数、不重置计时；原味和调味共用次数。\nMultipliers greater than 0 and up to 1 for the first meal and five repeat tiers' hunger, saturation and full diversity contribution; eating duration is divided by the multiplier and Culinary Experience is unchanged. The fifth and later repeats use the last tier. Craved meals have no penalty, do not count and do not restart the timer; plain and seasoned meals share repeats.", 0.001, 1).getDoubleList();
        if (warlyRepeatMultipliers.length != 6) {
            warlyRepeatMultipliers = new double[] {1.0, 0.9, 0.8, 0.65, 0.5, 0.3};
            config.getCategory("warlyMode").get("repeatMultipliers").set(warlyRepeatMultipliers);
        }
        for (int i = 0; i < warlyRepeatMultipliers.length; i++)
            if (!Double.isFinite(warlyRepeatMultipliers[i]) || warlyRepeatMultipliers[i] <= 0 || warlyRepeatMultipliers[i] > 1)
                warlyRepeatMultipliers[i] = new double[] {1.0, 0.9, 0.8, 0.65, 0.5, 0.3}[i];
        config.getCategory("warlyMode").get("repeatMultipliers").set(warlyRepeatMultipliers);
        recentCount = integer(config, "recentMeals", 32, 2, 4096, "用于计算个人多样性的最近进食次数。\nNumber of recent meals used for personal diversity.");
        frequencyPerPlayer = integer(config, "serverMealsPerPlayer", 20, 2, 4096, "日出在线人数乘以此值，得到服务器最近统计的进食次数。\nMultiply online players at sunrise by this value to determine recent server meals.");
        frequencyMaximum = integer(config, "serverMealLimit", 4096, 2, 4096, "服务器最近统计进食次数的上限，下限为2。\nMaximum recent server meals; minimum is 2.");
        cravingCount = integer(config, "cravedFoods", 5, 0, 1000, "每天垂涎欲滴的食物种类数。\nNumber of craved foods per day.");
        boringCount = integer(config, "boringFoods", 5, 0, 1000, "每天索然乏味的食物种类数。\nNumber of boring foods per day.");
        extraFlavor = number(config, "extraFlavor", 0.25, 0, 1, "同一道食物额外口味折算的种类数，原味也是口味。\nVariety counted per extra flavor of one food, including plain flavor.");
        cumulativeFlavorFraction = number(config, "cumulativeFlavorFraction", 0.5, 0, 1, "同一道食物首个口味全额，后来新口味的累计收益比例；首次尝试该食物时保存。\nCumulative gain fraction for later new flavors; first flavor gains full value. Saved on first taste of the food.");
        scale = number(config, "diversityScale", 1, 0, 100, "多样性总分倍率。\nOverall diversity score multiplier.");
        uniformChance = number(config, "uniformSelectionChance", 0.2, 0, 1, "兴趣抽选采用均匀随机的概率，其余采用频次权重。\nProbability of uniform appetite selection; remaining draws use frequency weights.");
        frequencyWeight = number(config, "frequencyWeightMultiplier", -1, -1, 1000000,
            "食用频次的额外权重倍率：-1自动使用0.43×实际每日候选食物数÷本日服务器最多统计的进食次数（排除固定兴趣食物）；其他值须非负。索然无味权重为1+次数×倍率，垂涎欲滴为其倒数。默认单人最近20次全吃同一种、约1000种候选时，每日索然无味概率约75%；0不偏向频次，1沿用原权重。\nExtra frequency weight multiplier: -1 uses 0.43 times eligible daily candidates divided by today's server meal retention limit (excluding fixed interests); other values must be nonnegative. Boring weight is 1 + count times multiplier; craved weight is its reciprocal. With default settings, 20 identical recent meals and about 1000 candidates give roughly a 75% daily boring chance; 0 ignores frequency and 1 preserves the original weights.");
        if (frequencyWeight < 0 && frequencyWeight != -1) {
            com.xy177.solwarly.SOLWarly.LOGGER.warn("Invalid frequencyWeightMultiplier {}; using automatic -1", frequencyWeight);
            frequencyWeight = -1;
            config.getCategory("statistics").get("frequencyWeightMultiplier").set(-1.0);
        }
        hungerWeight = coefficient(config, "hungerWeight", 0.25, "饥饿恢复值权重，2点为一格图标。\nHunger weight; 2 points equal one hunger icon.");
        saturationWeight = coefficient(config, "saturationWeight", 0.095, "实际饱和度恢复值权重。\nWeight of actual saturation restored.");
        difficultyWeight = coefficient(config, "difficultyWeight", 0.65, "制作难度权重。\nPreparation difficulty weight.");
        minimum = coefficient(config, "minimumCoefficient", 0.5, "基础系数下限。\nMinimum base coefficient.");
        maximum = coefficient(config, "maximumCoefficient", 12, "基础系数上限，不得小于下限。\nMaximum base coefficient, no lower than minimum.");
        maximum = Math.max(minimum, maximum);
        craftWeight = coefficient(config, "craftWeight", 1, "工作台工序权重。\nCrafting process weight.");
        furnaceWeight = coefficient(config, "furnaceWeight", 1, "熔炉工序权重。\nFurnace process weight.");
        cuttingWeight = coefficient(config, "cuttingWeight", 1, "砧板工序权重。\nCutting board process weight.");
        cookingWeight = coefficient(config, "cookingWeight", 1.5, "厨锅工序权重。\nCooking pot process weight.");
        servingWeight = coefficient(config, "servingWeight", 0.25, "分食工序权重，继承整道菜难度。\nServing process weight; inherits the whole dish difficulty.");
        for (String process : new String[] {"cookingPan", "dryingPan", "sunDrying", "indoorAging", "wetting", "stoveDrying", "stoveSteam", "teaTable", "riceWashing", "waterHeating", "mortarGrinding"})
            teaWeights.put(process, config.get("preparation.teastory", process, 1.75,
                "茶风工序 " + process + " 的权重（炒制、烘干、日晒、陈化、浸湿、焙火、蒸青、泡茶、洗米、烧水、研钵研磨）。\nWeight of Tea the Story operation " + process + " (pan cooking/drying, sun drying, aging, wetting, stove drying/steam, brewing, washing rice, boiling water, mortar grinding).", 0, 100).getDouble(1.75));
        snackBoundary = config.get("foodQuality", "mealMinimum", 0.0,
            "正餐最低原味分数，0表示按当前允许计入阅历的食物四等分自动确定；同分归同档。\nMinimum plain score for Meals; 0 uses current eligible food quartiles. Equal scores share a tier.", 0, 100000).getDouble();
        mealBoundary = config.get("foodQuality", "cuisineMinimum", 0.0,
            "佳肴最低原味分数，0自动确定。\nMinimum plain score for Fine Cuisine; 0 selects automatically.", 0, 100000).getDouble();
        cuisineBoundary = config.get("foodQuality", "delicacyMinimum", 0.0,
            "珍馐最低原味分数，0自动确定。\nMinimum plain score for Delicacies; 0 selects automatically.", 0, 100000).getDouble();
        cravingMultiplier = coefficient(config, "cravingMultiplier", 1.25, "垂涎欲滴时食用贡献倍率。\nContribution multiplier for craved food.");
        normalMultiplier = coefficient(config, "normalMultiplier", 1, "平平无奇时食用贡献倍率。\nContribution multiplier for ordinary food.");
        boringMultiplier = coefficient(config, "boringMultiplier", 0.75, "索然乏味时食用贡献倍率。\nContribution multiplier for boring food.");
        cumulativeRewards = config.get("rewards", "cumulativeHealth", cumulativeRewards,
            "累计阈值|额外生命总点数，2点为1颗心；只应用最高档。\nCumulative threshold|total bonus health points; 2 points = 1 heart. Highest tier only.").getStringList();
        attributes = config.get("rewards", "diversityAttributes", attributes,
            "分数阈值|属性名|数值|操作(0加值,1基础比例,2乘算)。所有条目按最高分数档整组应用。\nScore threshold|attribute name|amount|operation (0 add,1 base fraction,2 multiply). Apply all entries at the highest tier.").getStringList();
        effects = config.get("rewards", "diversityEffects", effects,
            "分数阈值|药水效果注册名|等级(0为I)，与属性共享最高档。\nScore threshold|potion registry name|amplifier (0=I); shares highest tier with attributes.").getStringList();
        cumulativeRewards = validRewards(cumulativeRewards, 2);
        attributes = validRewards(attributes, 4);
        effects = validRewards(effects, 3);
        excludedInterest = config.get("statistics", "excludedDailyFoods", excludedInterest,
            "只排除每日兴趣候选，不排除饮食统计；食物标识为modid:item@子类型。\nExclude from daily appetite selection only, not dietary statistics; IDs use modid:item@metadata.").getStringList();
        alwaysCraved = fixedFoods(config.get("statistics", "alwaysCravedFoods", new String[0],
            "始终垂涎欲滴的食物，modid:item@子类型（省略@时为0）；不参与每日抽选，不占每日名额，不绕过统计黑名单。\nAlways-craved foods, using modid:item@metadata (omitted metadata means 0); excluded from daily draws and daily quotas, without bypassing dietary filters.").getStringList());
        alwaysBoring = fixedFoods(config.get("statistics", "alwaysBoringFoods", new String[0],
            "始终索然无味的食物，格式同上；与垂涎名单冲突时以此名单为准，不参与每日抽选和名额。\nAlways-boring foods, using the same ID format; take precedence over always-craved foods and do not participate in daily draws or quotas.").getStringList());
        for (String food : alwaysCraved) if (java.util.Arrays.asList(alwaysBoring).contains(food))
            com.xy177.solwarly.SOLWarly.LOGGER.warn("Food appears in both fixed appetite lists; always-boring wins: {}", food);
        rawSources = config.get("preparation", "rawSources", rawSources,
            "自然获取的原料入口，避免压缩/拆分配方循环。modid:item 或 modid:item@子类型；仅对无子类型物品可省略@0。\nNaturally obtained preparation roots, preventing packing/unpacking cycles. Use modid:item or modid:item@metadata.").getStringList();
        config.save(); // Forge does not mark comment-only updates as changed.
    }
    public static String[] validRewards(String[] entries, int fields) {
        java.util.List<String> valid = new java.util.ArrayList<>();
        java.util.Set<String> unique = new java.util.HashSet<>();
        for (String entry : entries) {
            try {
                String[] parts = entry.split("\\|", -1);
                if (parts.length != fields || !nonnegative(parts[0])) throw new IllegalArgumentException();
                if (fields == 2 && !nonnegative(parts[1])) throw new IllegalArgumentException();
                if (fields >= 3 && parts[1].isEmpty()) throw new IllegalArgumentException();
                if (fields == 3) {
                    new net.minecraft.util.ResourceLocation(parts[1]);
                    int amplifier = Integer.parseInt(parts[2]);
                    if (amplifier < 0 || amplifier > 255) throw new IllegalArgumentException();
                }
                if (fields == 4 && (!Double.isFinite(Double.parseDouble(parts[2]))
                    || Integer.parseInt(parts[3]) < 0 || Integer.parseInt(parts[3]) > 2)) throw new IllegalArgumentException();
                String key = Double.parseDouble(parts[0]) + (fields == 2 ? "" : "|" + parts[1]);
                if (!unique.add(key)) throw new IllegalArgumentException();
                valid.add(entry);
            } catch (RuntimeException e) {
                com.xy177.solwarly.SOLWarly.LOGGER.warn("Ignoring invalid or duplicate reward entry: {}", entry);
            }
        }
        return valid.toArray(new String[0]);
    }
    private static String[] fixedFoods(String[] entries) {
        java.util.Set<String> result = new java.util.LinkedHashSet<>();
        for (String entry : entries) {
            String id = entry.trim();
            if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+(?:@[0-9]+)?")) {
                com.xy177.solwarly.SOLWarly.LOGGER.warn("Ignoring invalid fixed appetite food ID: {}", entry);
                continue;
            }
            result.add(id.contains("@") ? id : id + "@0");
        }
        return result.toArray(new String[0]);
    }
    private static boolean nonnegative(String value) {
        double number = Double.parseDouble(value);
        return Double.isFinite(number) && number >= 0;
    }
    private static int integer(Configuration config, String key, int value, int min, int max, String comment) {
        return config.getInt(key, "statistics", value, min, max, comment);
    }
    private static double number(Configuration config, String key, double value, double min, double max, String comment) {
        return config.getFloat(key, "statistics", (float) value, (float) min, (float) max, comment);
    }
    private static double coefficient(Configuration config, String key, double value, String comment) {
        return config.getFloat(key, "preparation", (float) value, 0, 1000, comment);
    }
    private DietConfig() {}
}
