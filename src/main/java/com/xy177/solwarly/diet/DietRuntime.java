package com.xy177.solwarly.diet;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.api.FlavorAPI;
import com.xy177.solwarly.tracking.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import squeek.applecore.api.food.FoodValues;
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
    public static AppetiteData appetite(World world) {
        if (world.isRemote) return clientAppetite;
        WorldServer overworld = world.getMinecraftServer().getWorld(0);
        AppetiteData data = (AppetiteData) overworld.getMapStorage().getOrLoadData(AppetiteData.class, "solwarly_appetite");
        if (data == null) { data = new AppetiteData(); overworld.getMapStorage().setData("solwarly_appetite", data); }
        return data;
    }
    public static void ensureCatalog() { if (!FoodCatalog.isReady()) FoodCatalog.build(); }
    public static void initialize(EntityPlayer player) {
        DietData data = DietCapability.get(player);
        if (data == null || data.initialized) return;
        ensureCatalog();
        FoodList legacy = FoodList.get(player);
        for (FoodInstance food : legacy.getEatenFoods()) {
            if (legacy.getConfigInfo().shouldCount(food.getItemStack()))
                data.migrateFood(food.encode(), FoodCatalog.coefficient(food.getItemStack()));
        }
        if (!legacy.getEatenFoods().isEmpty()) {
            data.legacyHealthFloor = legacy.getProgressInfo().milestonesAchieved() * 2.0 * SOLWarlyConfig.heartsPerMilestone;
        }
        net.minecraft.entity.ai.attributes.AttributeModifier old = player.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH)
            .getModifier(MaxHealthHandler.MILESTONE_HEALTH_MODIFIER_ID);
        if (old != null) data.legacyHealthFloor = Math.max(data.legacyHealthFloor, old.getAmount() - 2 * (SOLWarlyConfig.baseHearts - 10));
        data.initialized = true;
    }
    public static void eat(EntityPlayer player, net.minecraft.item.ItemStack food) {
        if (player.world.isRemote) return;
        initialize(player);
        String id = FoodCatalog.id(food);
        AppetiteData appetite = appetite(player.world);
        appetite.refresh(Math.floorDiv(player.getServer().getWorld(0).getWorldTime(), 24000),
            player.getServer().getCurrentPlayerCount(), FoodCatalog.candidates(), player.world.rand);
        long clock = currentClock(player.world);
        DietData data = DietCapability.get(player);
        boolean craved = appetite.state(id) > 0;
        double repeat = craved ? 1 : data.warlyMultiplier(id, clock);
        if (!craved) data.recordWarlyMeal(id, clock);
        if (!FoodList.get(player).getConfigInfo().shouldCount(food)) return;
        FlavorAPI.Flavor flavor = FlavorAPI.resolve(food);
        double base = FoodCatalog.coefficient(food);
        data.eat(id, flavor.id, base, base * appetite.multiplier(id) * flavor.diversityMultiplier * repeat, DietConfig.recentCount);
        appetite.eat(id);
    }

    public static long currentClock(World world) {
        if (world.isRemote) return clientWarlyClock + Math.max(0, world.getTotalWorldTime() - clientWarlyTotalTime);
        World overworld = world.getMinecraftServer().getWorld(0);
        return WarlyClockData.get(overworld).advance(overworld);
    }
    public static double warlyMultiplier(EntityPlayer player, net.minecraft.item.ItemStack food) {
        if (player == null) return 1;
        DietData data = DietCapability.get(player);
        if (data == null || (data.warlyOverride() < 0 ? !(player.world.isRemote ? clientWarlyMode : DietConfig.warlyMode)
            : data.warlyOverride() == 0)) return 1;
        String id = FoodCatalog.id(com.xy177.solwarly.spice.Seasoning.plain(food));
        AppetiteData appetite = appetite(player.world);
        if (!player.world.isRemote) {
            World overworld = player.world.getMinecraftServer().getWorld(0);
            long day = Math.floorDiv(overworld.getWorldTime(), 24000);
            if (day != appetite.lastDay)
                appetite.refresh(day, player.world.getMinecraftServer().getCurrentPlayerCount(), FoodCatalog.candidates(), overworld.rand);
        }
        if (appetite.state(id) > 0) return 1;
        if (player.world.isRemote)
            return data.warlyMultiplier(id, currentClock(player.world), clientWarlyMode,
                clientWarlyRepeatMultipliers, clientWarlyDurationDays);
        return data.warlyMultiplier(id, currentClock(player.world));
    }
    public static FoodValues applyWarlyValues(EntityPlayer player, net.minecraft.item.ItemStack food, FoodValues values) {
        if (values == null || player == null) return values;
        double multiplier = warlyMultiplier(player, food);
        return WarlyNutrition.scale(values, multiplier);
    }
    public static int useDuration(EntityPlayer player, net.minecraft.item.ItemStack food, int original) {
        double multiplier = warlyMultiplier(player, food);
        return scaledUseDuration(original, multiplier);
    }
    public static int scaledUseDuration(int original, double multiplier) {
        return original <= 0 || multiplier == 1 ? original : (int) Math.min(Integer.MAX_VALUE, Math.ceil(original / multiplier));
    }
    public static NBTTagCompound snapshot(EntityPlayer player) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("diet", DietCapability.get(player).serializeNBT());
        AppetiteData appetite = appetite(player.world);
        appetite.refresh(Math.floorDiv(player.getServer().getWorld(0).getWorldTime(), 24000),
            player.getServer().getCurrentPlayerCount(), FoodCatalog.candidates(), player.world.rand);
        tag.setTag("appetite", appetite.writeToNBT(new NBTTagCompound()));
        NBTTagCompound values = new NBTTagCompound();
        for (String id : FoodCatalog.ids()) values.setDouble(id, FoodCatalog.coefficient(FoodCatalog.stack(id)));
        tag.setTag("coefficients", values);
        List<Double> eligible = new ArrayList<>();
        ProgressInfo.ConfigInfo rules = FoodList.get(player).getConfigInfo();
        for (String id : FoodCatalog.ids()) if (rules.shouldCount(FoodCatalog.stack(id))) eligible.add(values.getDouble(id));
        double[] boundaries = FoodQuality.boundaries(eligible, DietConfig.snackBoundary, DietConfig.mealBoundary, DietConfig.cuisineBoundary);
        for (int i = 0; i < boundaries.length; i++) tag.setDouble("quality" + i, boundaries[i]);
        tag.setInteger("recentCount", DietConfig.recentCount); tag.setDouble("extraFlavor", DietConfig.extraFlavor);
        tag.setBoolean("warlyMode", DietConfig.warlyMode); tag.setInteger("warlyDurationDays", DietConfig.warlyDurationDays);
        tag.setLong("warlyClock", currentClock(player.world));
        net.minecraft.nbt.NBTTagList multipliers = new net.minecraft.nbt.NBTTagList();
        for (double value : DietConfig.warlyRepeatMultipliers) multipliers.appendTag(new net.minecraft.nbt.NBTTagDouble(value));
        tag.setTag("warlyRepeatMultipliers", multipliers);
        tag.setDouble("scale", DietConfig.scale);
        tag.setDouble("cravingMultiplier", DietConfig.cravingMultiplier);
        tag.setDouble("normalMultiplier", DietConfig.normalMultiplier);
        tag.setDouble("boringMultiplier", DietConfig.boringMultiplier);
        tag.setTag("cumulativeRewards", strings(DietConfig.cumulativeRewards));
        tag.setTag("attributes", strings(DietConfig.attributes)); tag.setTag("effects", strings(DietConfig.effects));
        tag.setTag("spices", com.xy177.solwarly.spice.SpiceConfig.snapshot());
        tag.setTag("customSpices", com.xy177.solwarly.spice.CustomSpices.snapshot());
        return tag;
    }
    public static void readSnapshot(EntityPlayer player, NBTTagCompound tag) {
        DietCapability.get(player).deserializeNBT(tag.getCompoundTag("diet"));
        clientAppetite.readFromNBT(tag.getCompoundTag("appetite"));
        clientCoefficients.clear();
        NBTTagCompound values = tag.getCompoundTag("coefficients");
        values.getKeySet().forEach(id -> clientCoefficients.put(id, values.getDouble(id)));
        for (int i = 0; i < 3; i++) clientQualityBoundaries[i] = tag.getDouble("quality" + i);
        clientRecentCount = tag.getInteger("recentCount"); clientExtraFlavor = tag.getDouble("extraFlavor"); clientScale = tag.getDouble("scale");
        clientWarlyMode = tag.getBoolean("warlyMode"); clientWarlyDurationDays = Math.max(1, tag.getInteger("warlyDurationDays"));
        clientWarlyClock = tag.getLong("warlyClock");
        clientWarlyTotalTime = player.world.getTotalWorldTime();
        net.minecraft.nbt.NBTTagList multipliers = tag.getTagList("warlyRepeatMultipliers", 6);
        if (multipliers.tagCount() == 6) {
            clientWarlyRepeatMultipliers = new double[6];
            for (int i = 0; i < 6; i++) clientWarlyRepeatMultipliers[i] = multipliers.getDoubleAt(i);
        }
        clientCravingMultiplier = tag.getDouble("cravingMultiplier"); clientNormalMultiplier = tag.getDouble("normalMultiplier"); clientBoringMultiplier = tag.getDouble("boringMultiplier");
        clientCumulativeRewards = readStrings(tag, "cumulativeRewards"); clientAttributes = readStrings(tag, "attributes"); clientEffects = readStrings(tag, "effects");
        if (tag.hasKey("spices")) com.xy177.solwarly.spice.SpiceConfig.readSnapshot(tag.getCompoundTag("spices"));
        com.xy177.solwarly.spice.CustomSpices.readSnapshot(tag.getTagList("customSpices", 8));
        clientRevision++;
    }
    private static net.minecraft.nbt.NBTTagList strings(String[] values) {
        net.minecraft.nbt.NBTTagList list = new net.minecraft.nbt.NBTTagList();
        for (String value : values) list.appendTag(new net.minecraft.nbt.NBTTagString(value)); return list;
    }
    private static String[] readStrings(NBTTagCompound tag, String key) {
        net.minecraft.nbt.NBTTagList list = tag.getTagList(key, 8); String[] values = new String[list.tagCount()];
        for (int i = 0; i < values.length; i++) values[i] = list.getStringTagAt(i); return values;
    }
    @SubscribeEvent public static void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote || event.world.provider.getDimension() != 0) return;
        WarlyClockData clock = WarlyClockData.get(event.world);
        if (event.phase == TickEvent.Phase.START) { clock.beginTick(event.world); return; }
        ensureCatalog();
        currentClock(event.world);
        AppetiteData appetite = appetite(event.world);
        appetite.refresh(Math.floorDiv(event.world.getWorldTime(), 24000),
            event.world.getMinecraftServer().getCurrentPlayerCount(), FoodCatalog.candidates(), event.world.rand);
        // A meal or login snapshot may already have refreshed this day before the world tick.
        if (appetite.consumePendingSync() | clock.consumePendingSync()) {
            for (EntityPlayerMP player : event.world.getMinecraftServer().getPlayerList().getPlayers()) CapabilityHandler.syncFoodList(player);
        }
    }
    @SubscribeEvent public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) return;
        if (event.player.ticksExisted % 40 == 0) DietRewards.refreshEffects(event.player);
    }
    @SubscribeEvent public static void onEffectAdded(net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent event) {
        DietRewards.onEffectAdded(event);
    }
    private DietRuntime() {}
}
