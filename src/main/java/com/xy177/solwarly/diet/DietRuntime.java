package com.xy177.solwarly.diet;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.api.FlavorAPI;
import com.xy177.solwarly.tracking.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.event.TickEvent;
import java.util.*;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class DietRuntime {
    public static final Map<String, Double> clientCoefficients = new HashMap<>();
    public static final AppetiteData clientAppetite = new AppetiteData();
    public static double clientExtraFlavor = 0.25, clientScale = 1;
    public static String[] clientCumulativeRewards = {}, clientAttributes = {}, clientEffects = {};
    public static int clientRecentCount = 32;
    public static long clientRevision;
    public static boolean clientWarlyMode;
    public static int clientWarlyDurationDays = 2;
    public static double[] clientWarlyRepeatMultipliers = {1.0, 0.9, 0.8, 0.65, 0.5, 0.3};
    public static long clientWarlyClock;
    private static long clientWarlyTotalTime;
    public static double clientCravingMultiplier = 1.25, clientNormalMultiplier = 1, clientBoringMultiplier = 0.75;
    public static double[] clientQualityBoundaries = {2, 5, 8};
    public static AppetiteData appetite(net.minecraft.world.level.Level world) {
        if(world.isClientSide)return clientAppetite;
        return world.getServer().overworld().getDataStorage().computeIfAbsent(tag->{ AppetiteData data=new AppetiteData();data.readFromNBT(tag);return data; },AppetiteData::new,"solwarly_appetite");
    }
    public static void ensureCatalog() {
        if(!FoodCatalog.isReady())throw new IllegalStateException("Food catalog must be prepared during mod startup");
    }
    public static void initialize(Player player) {
        DietData data = DietCapability.get(player);
        if (data == null || data.initialized) return;
        ensureCatalog();
        FoodList legacy = FoodList.get(player);
        for (FoodInstance food : legacy.getEatenFoods()) {
            if (SOLWarlyConfig.shouldCount(food.item))
                data.migrateFood(food.encode(), FoodCatalog.coefficient(food.getItemStack()));
        }
        if (!legacy.getEatenFoods().isEmpty()) {
            data.legacyHealthFloor = legacy.getProgressInfo().milestonesAchieved() * 2.0 * SOLWarlyConfig.getHeartsPerMilestone();
        }
        net.minecraft.world.entity.ai.attributes.AttributeModifier old = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
            .getModifier(MaxHealthHandler.MILESTONE_HEALTH_MODIFIER_ID);
        if (old != null) data.legacyHealthFloor = Math.max(data.legacyHealthFloor, old.getAmount() - 2 * (SOLWarlyConfig.getBaseHearts() - 10));
        data.initialized = true;
    }
    public static void eat(Player player, net.minecraft.world.item.ItemStack food) {
        if (player.level().isClientSide) return;
        initialize(player);
        String id = FoodCatalog.id(food);
        AppetiteData appetite = appetite(player.level());
        appetite.refresh(Math.floorDiv(player.getServer().overworld().getDayTime(), 24000),
            player.getServer().getPlayerCount(), FoodCatalog.candidates(), new java.util.Random(player.level().random.nextLong()));
        long clock = currentClock(player.level());
        DietData data = DietCapability.get(player);
        boolean craved = appetite.state(id) > 0;
        double repeat = craved ? 1 : data.warlyMultiplier(id, clock);
        if (!craved) data.recordWarlyMeal(id, clock);
        if (!SOLWarlyConfig.shouldCount(food.getItem())) return;
        FlavorAPI.Flavor flavor = FlavorAPI.resolve(food);
        double base = FoodCatalog.coefficient(food);
        data.eat(id, flavor.id, base, contribution(player, food, base * appetite.multiplier(id) * flavor.diversityMultiplier * repeat), DietConfig.recentCount);
        appetite.eat(id);
    }

    public static double contribution(Player player, net.minecraft.world.item.ItemStack food, double normal) {
        if (!com.xy177.solwarly.spice.Seasoning.creative(food)) return normal;
        boolean client = player.level().isClientSide;
        return Math.max(0, DietRewards.tier(Double.MAX_VALUE, client ? clientAttributes : DietConfig.attributes,
            client ? clientEffects : DietConfig.effects));
    }

    public static long currentClock(Level world) {
        if (world.isClientSide) return clientWarlyClock + Math.max(0, world.getGameTime() - clientWarlyTotalTime);
        Level overworld = world.getServer().overworld();
        return WarlyClockData.get(overworld).advance(overworld);
    }
    public static double warlyMultiplier(Player player, net.minecraft.world.item.ItemStack food) {
        if (player == null) return 1;
        DietData data = DietCapability.get(player);
        if (data == null || (data.warlyOverride() < 0 ? !(player.level().isClientSide ? clientWarlyMode : DietConfig.warlyMode)
            : data.warlyOverride() == 0)) return 1;
        String id = FoodCatalog.id(com.xy177.solwarly.spice.Seasoning.plain(food));
        AppetiteData appetite = appetite(player.level());
        if (!player.level().isClientSide) {
            Level overworld = player.level().getServer().overworld();
            long day = Math.floorDiv(overworld.getDayTime(), 24000);
            if (day != appetite.lastDay)
                appetite.refresh(day, player.level().getServer().getPlayerCount(), FoodCatalog.candidates(), new java.util.Random(overworld.random.nextLong()));
        }
        if (appetite.state(id) > 0) return 1;
        if (player.level().isClientSide)
            return data.warlyMultiplier(id, currentClock(player.level()), clientWarlyMode,
                clientWarlyRepeatMultipliers, clientWarlyDurationDays);
        return data.warlyMultiplier(id, currentClock(player.level()));
    }
    public static int useDuration(Player player, net.minecraft.world.item.ItemStack food, int original) {
        double multiplier = warlyMultiplier(player, food);
        return scaledUseDuration(original, multiplier);
    }
    public static int scaledUseDuration(int original, double multiplier) {
        return original <= 0 || multiplier == 1 ? original : (int) Math.min(Integer.MAX_VALUE, Math.ceil(original / multiplier));
    }
    public static CompoundTag snapshot(Player player) {
        CompoundTag tag = new CompoundTag();
        tag.put("diet", DietCapability.get(player).serializeNBT());
        AppetiteData appetite = appetite(player.level());
        appetite.refresh(Math.floorDiv(player.getServer().overworld().getDayTime(), 24000),
            player.getServer().getPlayerCount(), FoodCatalog.candidates(), new java.util.Random(player.level().random.nextLong()));
        tag.put("appetite", appetite.save(new CompoundTag()));
        CompoundTag values = new CompoundTag();
        for (String id : FoodCatalog.ids()) values.putDouble(id, FoodCatalog.coefficient(FoodCatalog.stack(id)));
        tag.put("coefficients", values);
        List<Double> eligible = new ArrayList<>();
        for (String id : FoodCatalog.ids()) if (SOLWarlyConfig.shouldCount(FoodCatalog.stack(id).getItem())) eligible.add(values.getDouble(id));
        double[] boundaries = FoodQuality.boundaries(eligible, DietConfig.snackBoundary, DietConfig.mealBoundary, DietConfig.cuisineBoundary);
        for (int i = 0; i < boundaries.length; i++) tag.putDouble("quality" + i, boundaries[i]);
        tag.putInt("recentCount", DietConfig.recentCount); tag.putDouble("extraFlavor", DietConfig.extraFlavor);
        tag.putBoolean("warlyMode", DietConfig.warlyMode); tag.putInt("warlyDurationDays", DietConfig.warlyDurationDays);
        tag.putLong("warlyClock", currentClock(player.level()));
        net.minecraft.nbt.ListTag multipliers = new net.minecraft.nbt.ListTag();
        for (double value : DietConfig.warlyRepeatMultipliers) multipliers.add(net.minecraft.nbt.DoubleTag.valueOf(value));
        tag.put("warlyRepeatMultipliers", multipliers);
        tag.putDouble("scale", DietConfig.scale);
        tag.putDouble("cravingMultiplier", DietConfig.cravingMultiplier);
        tag.putDouble("normalMultiplier", DietConfig.normalMultiplier);
        tag.putDouble("boringMultiplier", DietConfig.boringMultiplier);
        tag.put("cumulativeRewards", strings(DietConfig.cumulativeRewards));
        tag.put("attributes", strings(DietConfig.attributes)); tag.put("effects", strings(DietConfig.effects));
        tag.put("legacyRules", com.xy177.solwarly.compat.LegacyRules.snapshot());
        tag.put("spices", com.xy177.solwarly.spice.SpiceConfig.snapshot());
        tag.put("customSpices", com.xy177.solwarly.spice.CustomSpices.snapshot());
        return tag;
    }
    public static void readSnapshot(Player player, CompoundTag tag) {
        com.xy177.solwarly.compat.LegacyRules.readSnapshot(tag.getCompound("legacyRules"));
        DietCapability.get(player).deserializeNBT(tag.getCompound("diet"));
        clientAppetite.readFromNBT(tag.getCompound("appetite"));
        clientCoefficients.clear();
        CompoundTag values = tag.getCompound("coefficients");
        values.getAllKeys().forEach(id -> clientCoefficients.put(id, values.getDouble(id)));
        for (int i = 0; i < 3; i++) clientQualityBoundaries[i] = tag.getDouble("quality" + i);
        clientRecentCount = tag.getInt("recentCount"); clientExtraFlavor = tag.getDouble("extraFlavor"); clientScale = tag.getDouble("scale");
        clientWarlyMode = tag.getBoolean("warlyMode"); clientWarlyDurationDays = Math.max(1, tag.getInt("warlyDurationDays"));
        clientWarlyClock = tag.getLong("warlyClock");
        clientWarlyTotalTime = player.level().getGameTime();
        net.minecraft.nbt.ListTag multipliers = tag.getList("warlyRepeatMultipliers", 6);
        if (multipliers.size() == 6) {
            clientWarlyRepeatMultipliers = new double[6];
            for (int i = 0; i < 6; i++) clientWarlyRepeatMultipliers[i] = multipliers.getDouble(i);
        }
        clientCravingMultiplier = tag.getDouble("cravingMultiplier"); clientNormalMultiplier = tag.getDouble("normalMultiplier"); clientBoringMultiplier = tag.getDouble("boringMultiplier");
        clientCumulativeRewards = readStrings(tag, "cumulativeRewards"); clientAttributes = readStrings(tag, "attributes"); clientEffects = readStrings(tag, "effects");
        if (tag.contains("spices")) com.xy177.solwarly.spice.SpiceConfig.readSnapshot(tag.getCompound("spices"));
        com.xy177.solwarly.spice.CustomSpices.readSnapshot(tag.getList("customSpices", 8));
        clientRevision++;
    }
    private static net.minecraft.nbt.ListTag strings(String[] values) {
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (String value : values) list.add(net.minecraft.nbt.StringTag.valueOf(value)); return list;
    }
    private static String[] readStrings(CompoundTag tag, String key) {
        net.minecraft.nbt.ListTag list = tag.getList(key, 8); String[] values = new String[list.size()];
        for (int i = 0; i < values.length; i++) values[i] = list.getString(i); return values;
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event) {
        if(event.phase!=TickEvent.Phase.END)return;
        var server=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();if(server==null)return;
        var world=server.overworld();if(world==null)return;
        WarlyClockData clock=WarlyClockData.get(world);clock.advance(world);
        AppetiteData appetite=appetite(world);
        appetite.refresh(Math.floorDiv(world.getDayTime(),24000),server.getPlayerCount(),FoodCatalog.candidates(),new java.util.Random(world.random.nextLong()));
        boolean sync=appetite.consumePendingSync()|clock.consumePendingSync()|com.xy177.solwarly.spice.ImmortalSpices.powerChanged();
        for(var player:server.getPlayerList().getPlayers()) {
            if(player.tickCount%20==0)DietRewards.refreshEffects(player);
            if(sync)com.xy177.solwarly.tracking.CapabilityHandler.syncFoodList(player);
        }
    }
    private DietRuntime() {}
}
