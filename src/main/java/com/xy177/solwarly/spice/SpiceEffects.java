package com.xy177.solwarly.spice;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.diet.DietRuntime;
import com.xy177.solwarly.diet.WarlyClockData;
import com.xy177.solwarly.lunch.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.*;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import squeek.applecore.api.food.*;
import java.util.*;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class SpiceEffects {
    private static final Map<EntityPlayer, List<Meal>> pending = new WeakHashMap<>();
    private static final class Meal {
        final List<SpiceKind> kinds;
        final float saturation;
        final Map<Potion, PotionEffect> original = new LinkedHashMap<>();
        Meal(List<SpiceKind> kinds, float saturation) { this.kinds = kinds; this.saturation = saturation; }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void foodValues(FoodEvent.GetFoodValues event) {
        if (event.foodValues == null) return;
        if (Seasoning.seasoned(event.food) && Seasoning.components(event.food).contains(SpiceKind.RICE)) event.foodValues = riceValues(event.foodValues);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void appetizingValues(FoodEvent.GetPlayerFoodValues event) {
        if (event.foodValues == null || event.player == null) return;
        int level = level(event.player, SpiceRegistry.appetizing);
        if (level > 0) event.foodValues = appetizingValues(event.foodValues, level);
        event.foodValues = DietRuntime.applyWarlyValues(event.player, event.food, event.foodValues);
    }
    public static FoodValues appetizingValues(FoodValues original, int level) {
        double scale = 1 + SpiceConfig.appetiteBase + SpiceConfig.appetitePerLevel * level;
        int hunger = (int) Math.round(original.hunger * scale);
        float saturation = (float) (original.getUnboundedSaturationIncrement() * scale);
        return new FoodValues(hunger, hunger > 0 ? saturation / (2 * hunger) : 0);
    }
    public static FoodValues riceValues(FoodValues original) {
        int hunger = original.hunger + SpiceConfig.riceHunger;
        float saturation = original.getUnboundedSaturationIncrement() + (float) SpiceConfig.riceSaturation;
        return new FoodValues(hunger, hunger > 0 ? saturation / (2 * hunger) : 0);
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void useStarted(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        net.minecraft.item.ItemStack food = event.getItem();
        if (food.getItem() instanceof ItemLunchContainer) {
            LunchInventory inventory = new LunchInventory(food);
            int slot = ItemLunchContainer.bestFood(inventory, player);
            if (slot >= 0) food = inventory.getStackInSlot(slot);
        }
        if (!squeek.applecore.api.AppleCoreAPI.accessor.isFood(food)) return;
        event.setDuration(DietRuntime.useDuration(player, food, event.getDuration()));
    }
    @SubscribeEvent public static void wake(net.minecraftforge.event.entity.player.PlayerWakeUpEvent event) {
        if (!event.getEntityPlayer().world.isRemote && event.getEntityPlayer().world.provider.getDimension() == 0
            && !event.wakeImmediately() && !event.updateWorld() && event.shouldSetSpawn())
            WarlyClockData.get(event.getEntityPlayer().world).recordSleep(event.getEntityPlayer().world);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST) public static void zeroHungerSaturation(FoodEvent.FoodEaten event) {
        com.xy177.solwarly.diet.WarlyNutrition.restoreZeroHungerSaturation(event);
    }
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void eaten(FoodEvent.FoodEaten event) {
        if (event.player.world.isRemote || !Seasoning.seasoned(event.food)) return;
        List<SpiceKind> kinds = Seasoning.components(event.food); if (kinds.isEmpty()) return;
        pending.computeIfAbsent(event.player, player -> new ArrayList<>()).add(new Meal(kinds, com.xy177.solwarly.diet.WarlyNutrition.saturationAdded(event)));
    }
    /** Capture only newly applied food effects; never add a player's previous buff duration. */
    @SubscribeEvent public static void added(PotionEvent.PotionAddedEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        List<Meal> meals = pending.get((EntityPlayer) event.getEntityLiving());
        if (meals == null || meals.isEmpty()) return;
        PotionEffect effect = event.getPotionEffect();
        meals.get(meals.size() - 1).original.put(effect.getPotion(), new PotionEffect(effect));
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.world.isRemote) flush(event.player);
    }
    private static void flush(EntityPlayer player) {
        List<Meal> meals = pending.remove(player); if (meals == null) return;
        for (Meal meal : meals) {
            Map<Potion, PotionEffect> effects = apply(player, meal.kinds, meal.original);
            if (meal.kinds.contains(SpiceKind.CHEESE)) cheese(player);
            if (meal.kinds.contains(SpiceKind.MYSTERY))
                cure(player, mysteryCureCount(Collections.frequency(meal.kinds, SpiceKind.MYSTERY)));
            if (meal.kinds.contains(SpiceKind.DRAGON)) share(player, effects, meal.saturation, Collections.frequency(meal.kinds, SpiceKind.DRAGON));
            if (meal.kinds.contains(SpiceKind.CHORUS) && player.isSneaking()) teleport(player);
        }
    }
    /** Compatibility entry point used by earlier integrations and tests. */
    public static void apply(EntityPlayer player, List<SpiceKind> kinds, int originalNourishment, int originalAmplifier) {
        Map<Potion, PotionEffect> original = new LinkedHashMap<>();
        if (originalNourishment > 0 && SpiceKind.HAM.potion() != null) original.put(SpiceKind.HAM.potion(),
            new PotionEffect(SpiceKind.HAM.potion(), originalNourishment, originalAmplifier));
        apply(player, kinds, original);
    }
    public static Map<Potion, PotionEffect> apply(EntityPlayer player, List<SpiceKind> kinds, Map<Potion, PotionEffect> original) {
        List<SeasoningPotions.Entry> additions = new ArrayList<>();
        for (SeasoningPotions.Entry entry : SeasoningPotions.additions(kinds, original))
            if (entry.chance >= 1 || player.getRNG().nextFloat() < entry.chance) additions.add(entry);
        Map<Potion, PotionEffect> combined = SeasoningPotions.merge(original, additions);
        Set<Potion> added = new LinkedHashSet<>();
        for (SeasoningPotions.Entry entry : additions) added.add(entry.effect.getPotion());
        for (Potion potion : added) refresh(player, combined.get(potion));
        return combined;
    }
    public static void refresh(EntityPlayer player, PotionEffect incoming) {
        PotionEffect old = player.getActivePotionEffect(incoming.getPotion());
        player.addPotionEffect(new PotionEffect(incoming.getPotion(), Math.max(incoming.getDuration(), old == null ? 0 : old.getDuration()),
            Math.max(incoming.getAmplifier(), old == null ? 0 : old.getAmplifier()), incoming.getIsAmbient(), incoming.doesShowParticles()));
    }
    public static void cheese(EntityPlayer player) {
        Potion tipsy = net.minecraftforge.fml.common.registry.ForgeRegistries.POTIONS.getValue(new net.minecraft.util.ResourceLocation("brewinandchewinlegacy:tipsy"));
        if (tipsy != null && player.isPotionActive(tipsy)) player.removePotionEffect(tipsy);
        else player.heal((float) SpiceConfig.cheeseHealing);
    }
    public static void cure(EntityPlayer player, int count) {
        List<Potion> effects = new ArrayList<>();
        for (PotionEffect effect : player.getActivePotionEffects()) if (effect.getPotion().isBadEffect()) effects.add(effect.getPotion());
        Collections.shuffle(effects, player.getRNG());
        for (int i = 0; i < Math.min(count, effects.size()); i++) player.removePotionEffect(effects.get(i));
    }
    public static int mysteryCureCount(int copies) {
        return copies >= 3 ? 4 : Math.max(0, copies);
    }
    public static void share(EntityPlayer eater, Map<Potion, PotionEffect> effects, float saturation, int copies) {
        double scale = SpiceConfig.sharingPerCopy * copies, radius = SpiceConfig.sharingRadius;
        if (radius <= 0 || scale <= 0) return;
        List<EntityPlayer> players = eater.world.getEntitiesWithinAABB(EntityPlayer.class, eater.getEntityBoundingBox().grow(radius));
        for (EntityPlayer recipient : players) {
            if (recipient == eater || recipient.isDead || recipient.getDistanceSq(eater) > radius * radius) continue;
            addSaturation(recipient, (float) Math.max(0, saturation * scale));
            for (PotionEffect effect : effects.values()) if (!effect.getPotion().isBadEffect()) {
                int ticks = (int) Math.round(effect.getDuration() * scale);
                if (ticks > 0) refresh(recipient, new PotionEffect(effect.getPotion(), ticks, effect.getAmplifier(), effect.getIsAmbient(), effect.doesShowParticles()));
            }
        }
    }
    public static void addSaturation(EntityPlayer player, float amount) {
        net.minecraft.util.FoodStats food = player.getFoodStats();
        food.setFoodSaturationLevel(Math.min(food.getFoodLevel(), food.getSaturationLevel() + amount));
    }
    public static boolean teleport(EntityPlayer player) {
        double startX = player.posX, startY = player.posY, startZ = player.posZ;
        for (int i = 0; i < 16; i++) {
            double x = startX + (player.getRNG().nextDouble() - .5) * 16;
            double y = startY + player.getRNG().nextInt(16) - 8;
            double z = startZ + (player.getRNG().nextDouble() - .5) * 16;
            if (player.isRiding()) player.dismountRidingEntity();
            if (player.attemptTeleport(x, y, z)) {
                player.world.playSound(null, startX, startY, startZ, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 1, 1);
                player.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, 1, 1); return true;
            }
        }
        return false;
    }
    private static int level(net.minecraft.entity.EntityLivingBase entity, Potion potion) {
        PotionEffect effect = potion == null ? null : entity.getActivePotionEffect(potion); return effect == null ? 0 : effect.getAmplifier() + 1;
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void mining(PlayerEvent.BreakSpeed event) {
        int level = level(event.getEntityPlayer(), SpiceRegistry.sweet);
        if (level > 0) event.setNewSpeed((float) (event.getNewSpeed() * (1 + SpiceConfig.miningPerLevel * level)));
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void healing(LivingHealEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        int level = level(event.getEntityLiving(), SpiceRegistry.tasty);
        if (level > 0) event.setAmount((float) (event.getAmount() * (1 + SpiceConfig.healingPerLevel * level)));
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void damage(LivingDamageEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        int level = level(event.getEntityLiving(), SpiceRegistry.aromatic);
        if (level > 0) event.setAmount((float) (event.getAmount() * Math.max(0, 1 - SpiceConfig.reductionBase - SpiceConfig.reductionPerLevel * level)));
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void hotAttack(LivingHurtEvent event) {
        if (event.getAmount() <= 0 || !(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        net.minecraft.entity.Entity immediate = event.getSource().getImmediateSource();
        if (!event.getSource().damageType.equals("player") && (!event.getSource().isProjectile() || immediate == null)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        int level = level(player, SpiceRegistry.hot);
        if (level == 0) return;
        event.setAmount((float) (event.getAmount() * (1 + SpiceConfig.hotBase + SpiceConfig.hotPerLevel * level)));
        if (SpiceConfig.fireSeconds > 0) event.getEntityLiving().setFire(SpiceConfig.fireSeconds);
    }
    private SpiceEffects() {}
}
