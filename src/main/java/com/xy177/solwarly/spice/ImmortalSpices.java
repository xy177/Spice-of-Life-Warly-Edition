package com.xy177.solwarly.spice;

import com.xy177.solwarly.config.Configuration;
import com.xy177.solwarly.registry.RegistryLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.*;
import java.util.*;
import java.lang.reflect.Method;

/** Optional Immortalers Delight behavior, without loading its classes when absent. */
public final class ImmortalSpices {
    private static final Map<SpiceKind, Integer> levels = new LinkedHashMap<>(), poweredDurations = new LinkedHashMap<>(), poweredLevels = new LinkedHashMap<>();
    public static int travariceHunger = 6, aleResistanceDuration = 1000, aleResistanceLevel = 2;
    public static double travariceSaturation = 6;
    public static boolean clientPowered;
    private static Method powerMode;
    private static boolean checkedPower, lastPower;
    public static int defaultDuration(SpiceKind kind) {
        return switch (kind.id()) {
            case "spider_eye", "alfalfa" -> 3600;
            case "evolutcorn", "onsen_egg", "purgatory_ale" -> 1200;
            case "miracle", "himekaido" -> 20;
            case "travarice" -> 600;
            default -> 2400;
        };
    }
    private static int defaultLevel(SpiceKind kind) {
        return kind == SpiceKind.PURGATORY_ALE ? 3 : kind == SpiceKind.SPIDER_EYE || kind == SpiceKind.MIRACLE || kind == SpiceKind.PEARLIPEARL ? 2 : 1;
    }
    private static int defaultPoweredDuration(SpiceKind kind) {
        return kind == SpiceKind.POISONOUS_POTATO ? 4200 : kind == SpiceKind.SPIDER_EYE ? 7200
            : kind == SpiceKind.PUFFERFISH || kind == SpiceKind.PEARLIPEARL ? 4800 : defaultDuration(kind);
    }
    public static void load(Configuration config) {
        for (SpiceKind kind : SpiceKind.values()) if (kind.immortal()) {
            levels.put(kind, config.getInt("level", kind.id(), defaultLevel(kind), 1, 128, "基础药水等级，1表示Ⅰ级。\nBase potion level; 1 means level I."));
            poweredDurations.put(kind, config.getInt("poweredDurationTicks", kind.id(), defaultPoweredDuration(kind), 1, 72000,
                "千古乐事超凡模式下的基础时长，之后再应用混合增强。\nBase duration in Immortalers Delight Power Battle Mode, before mixed enhancement."));
            poweredLevels.put(kind, config.getInt("poweredExtraLevels", kind.id(), kind == SpiceKind.POISONOUS_POTATO || kind == SpiceKind.CHILLED_SUGAR ? 1 : 0, 0, 127,
                "超凡模式额外增加的等级，之后再应用混合增强。\nExtra potion levels in Power Battle Mode, before mixed enhancement."));
        }
        travariceHunger = config.getInt("hunger", "travarice", 6, 0, 100, "每份菅米饭额外恢复的饥饿点数。\nAdditional hunger points per Travarice serving.");
        travariceSaturation = config.get("travarice", "saturation", 6.0, "每份菅米饭额外恢复的实际饱和度点数。\nAdditional actual saturation points per Travarice serving.", 0, 100).getDouble();
        aleResistanceDuration = config.getInt("resistanceDurationTicks", "purgatory_ale", 1000, 1, 72000, "佐麦酒的抗性提升时长。\nResistance duration from an Ale serving.");
        aleResistanceLevel = config.getInt("resistanceLevel", "purgatory_ale", 2, 1, 128, "佐麦酒的抗性提升等级，1表示Ⅰ级。\nResistance level from an Ale serving; 1 means level I.");
    }
    public static boolean powered() {
        if (!checkedPower) {
            checkedPower = true;
            if (RegistryLookup.item("immortalers_delight:himekaido") != null) try {
                powerMode = Class.forName("com.renyigesai.immortalers_delight.util.DifficultyModeUtil").getMethod("isPowerBattleMode");
            } catch (ReflectiveOperationException e) { com.xy177.solwarly.SOLWarly.LOGGER.error("Cannot read Immortalers Delight Power Battle Mode", e); }
        }
        if (powerMode == null) return false;
        try { return (Boolean) powerMode.invoke(null); }
        catch (ReflectiveOperationException e) { throw new IllegalStateException("Cannot read Immortalers Delight Power Battle Mode", e); }
    }
    public static boolean powerChanged() {
        boolean now = powered();
        boolean changed = lastPower != now; lastPower = now; return changed;
    }
    public static List<SeasoningPotions.Entry> effects(SpiceKind kind, long copies, boolean powered) {
        List<SeasoningPotions.Entry> result = new ArrayList<>();
        if (kind == SpiceKind.MILK_TEA || kind == SpiceKind.MILK_GREEN) {
            var item = RegistryLookup.item(kind.ingredientId);
            if (item != null) {
                var food = new net.minecraft.world.item.ItemStack(item).getFoodProperties(null);
                if (food != null) for (var spec : food.effects()) {
                    var effect = spec.effect();
                    if (RegistryLookup.id(effect.getEffect()).toString().equals("immortalers_delight:satiated")) continue;
                    result.add(new SeasoningPotions.Entry(new MobEffectInstance(effect.getEffect(),
                        Seasoning.ticks((double) Math.max(SpiceConfig.durations.get(kind), effect.getDuration()) * copies), effect.getAmplifier()), spec.probability()));
                }
            }
            return result;
        }
        int duration = powered ? poweredDurations.getOrDefault(kind, defaultPoweredDuration(kind)) : SpiceConfig.durations.get(kind);
        int level = levels.getOrDefault(kind, defaultLevel(kind)) + (powered ? poweredLevels.getOrDefault(kind, 0) : 0);
        add(result, kind.potion(), duration, level, copies, kind.accompaniment());
        if (kind == SpiceKind.POISONOUS_POTATO) add(result, effect("resistance_to_undead"), duration, level, copies, false);
        if (kind == SpiceKind.PURGATORY_ALE) add(result, MobEffects.DAMAGE_RESISTANCE, aleResistanceDuration, aleResistanceLevel, copies, true);
        return result;
    }
    private static net.minecraft.core.Holder<MobEffect> effect(String id) { return RegistryLookup.effect(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("immortalers_delight", id)); }
    private static void add(List<SeasoningPotions.Entry> entries, net.minecraft.core.Holder<MobEffect> effect, int duration, int level, long copies, boolean side) {
        if (effect == null) return;
        boolean scaling = SeasoningPotions.scales(effect);
        int ticks = side ? Seasoning.ticks((double) duration * copies) : scaling ? duration : Seasoning.ticks(duration * Seasoning.durationScale(copies));
        entries.add(new SeasoningPotions.Entry(new MobEffectInstance(effect, ticks, Seasoning.amplifier((long) level - 1 + (!side && scaling ? copies - 1L : 0))), 1));
    }
    public static CompoundTag snapshot() {
        CompoundTag tag = new CompoundTag(); tag.putBoolean("powered", powered());
        for (SpiceKind kind : SpiceKind.values()) if (kind.immortal()) {
            tag.putInt(kind.id() + "Level", levels.getOrDefault(kind, defaultLevel(kind)));
            tag.putInt(kind.id() + "PoweredDuration", poweredDurations.getOrDefault(kind, defaultPoweredDuration(kind)));
            tag.putInt(kind.id() + "PoweredLevels", poweredLevels.getOrDefault(kind, 0));
        }
        tag.putInt("travariceHunger", travariceHunger); tag.putDouble("travariceSaturation", travariceSaturation);
        tag.putInt("aleResistanceDuration", aleResistanceDuration); tag.putInt("aleResistanceLevel", aleResistanceLevel); return tag;
    }
    public static void readSnapshot(CompoundTag tag) {
        clientPowered = tag.getBoolean("powered");
        for (SpiceKind kind : SpiceKind.values()) if (kind.immortal()) {
            levels.put(kind, tag.getInt(kind.id() + "Level"));
            poweredDurations.put(kind, tag.getInt(kind.id() + "PoweredDuration"));
            poweredLevels.put(kind, tag.getInt(kind.id() + "PoweredLevels"));
        }
        travariceHunger = tag.getInt("travariceHunger"); travariceSaturation = tag.getDouble("travariceSaturation");
        aleResistanceDuration = tag.getInt("aleResistanceDuration"); aleResistanceLevel = tag.getInt("aleResistanceLevel");
    }
    private ImmortalSpices() {}
}
