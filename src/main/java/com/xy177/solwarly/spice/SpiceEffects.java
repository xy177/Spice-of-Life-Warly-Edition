package com.xy177.solwarly.spice;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.diet.DietRuntime;
import com.xy177.solwarly.diet.WarlyClockData;
import com.xy177.solwarly.diet.DietRewards;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.event.TickEvent;

import java.util.*;

@Mod.EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class SpiceEffects {
    private static final Map<Player, List<Meal>> pending = new WeakHashMap<>();
    private static final class Meal {
        final net.minecraft.world.item.ItemStack food;
        final List<SpiceKind> kinds;
        float saturation;
        final Map<MobEffect,MobEffectInstance> original=new LinkedHashMap<>();
        Meal(net.minecraft.world.item.ItemStack food) { this.food=food.copy();kinds=Seasoning.effectKinds(Seasoning.components(food)); }
    }
    public static void begin(Player player,net.minecraft.world.item.ItemStack food) {
        if(player.level().isClientSide)return;
        pending.computeIfAbsent(player,key->new ArrayList<>()).add(new Meal(food));
    }
    public static void ensureMeal(Player player,net.minecraft.world.item.ItemStack food) {
        if(player.level().isClientSide)return;
        List<Meal> meals=pending.get(player);
        if(meals==null||meals.isEmpty())begin(player,food);
    }
    public static void saturation(Player player,float added) {
        List<Meal> meals=pending.get(player);if(meals!=null&&!meals.isEmpty())meals.get(meals.size()-1).saturation+=added;
    }
    @SubscribeEvent public static void added(MobEffectEvent.Added event) {
        if(!(event.getEntity() instanceof Player player))return;
        DietRewards.onEffectAdded(event);
        List<Meal> meals=pending.get(player);if(meals==null||meals.isEmpty())return;
        var effect=event.getEffectInstance();meals.get(meals.size()-1).original.put(effect.getEffect(),new MobEffectInstance(effect));
    }
    @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if(event.phase==net.minecraftforge.event.TickEvent.Phase.END&&!event.player.level().isClientSide)finish(event.player);
    }
    public static void finish(Player player) {
        List<Meal> meals=pending.remove(player);if(meals==null)return;
        for(Meal meal:meals) {
            Map<MobEffect,MobEffectInstance> effects=apply(player,meal.kinds,meal.original);
            if(meal.kinds.contains(SpiceKind.CHEESE))cheese(player);
            if(meal.kinds.contains(SpiceKind.MYSTERY))cure(player,mysteryCureCount(Seasoning.amount(meal.kinds, SpiceKind.MYSTERY)));
            if(meal.kinds.contains(SpiceKind.DRAGON))share(player,effects,meal.saturation,Seasoning.amount(meal.kinds, SpiceKind.DRAGON));
            if(meal.kinds.contains(SpiceKind.CHORUS)&&player.isShiftKeyDown())teleport(player);
            com.xy177.solwarly.tracking.FoodTracker.complete(player,meal.food);
        }
    }
    @SubscribeEvent(priority=EventPriority.LOW) public static void useStarted(LivingEntityUseItemEvent.Start event) {
        if(event.getEntity() instanceof Player player) {
            var food=event.getItem();
            if(food.getItem() instanceof com.xy177.solwarly.lunch.ItemLunchContainer) {
                var inventory=new com.xy177.solwarly.lunch.LunchInventory(food);
                int slot=com.xy177.solwarly.lunch.ItemLunchContainer.bestFood(inventory,player);
                if(slot>=0)food=inventory.getStackInSlot(slot);
            }
            if(food.isEdible())event.setDuration(DietRuntime.useDuration(player,food,event.getDuration()));
        }
    }
    /** Compatibility entry point used by earlier integrations and tests. */
    public static void apply(Player player, List<SpiceKind> kinds, int originalNourishment, int originalAmplifier) {
        Map<MobEffect, MobEffectInstance> original = new LinkedHashMap<>();
        if (originalNourishment > 0 && SpiceKind.HAM.potion() != null) original.put(SpiceKind.HAM.potion(),
            new MobEffectInstance(SpiceKind.HAM.potion(), originalNourishment, originalAmplifier));
        apply(player, kinds, original);
    }
    public static Map<MobEffect, MobEffectInstance> apply(Player player, List<SpiceKind> kinds, Map<MobEffect, MobEffectInstance> original) {
        List<SeasoningPotions.Entry> additions = new ArrayList<>();
        for (SeasoningPotions.Entry entry : SeasoningPotions.additions(kinds, original))
            if (entry.chance >= 1 || player.getRandom().nextFloat() < entry.chance) additions.add(entry);
        Map<MobEffect, MobEffectInstance> combined = SeasoningPotions.merge(original, additions);
        Set<MobEffect> added = new LinkedHashSet<>();
        for (SeasoningPotions.Entry entry : additions) added.add(entry.effect.getEffect());
        for (MobEffect potion : added) refresh(player, combined.get(potion));
        return combined;
    }
    public static void refresh(Player player, MobEffectInstance incoming) {
        MobEffectInstance old = player.getEffect(incoming.getEffect());
        player.addEffect(new MobEffectInstance(incoming.getEffect(), Math.max(incoming.getDuration(), old == null ? 0 : old.getDuration()),
            Math.max(incoming.getAmplifier(), old == null ? 0 : old.getAmplifier()), incoming.isAmbient(), incoming.isVisible()));
    }
    public static void cheese(Player player) {
        MobEffect tipsy = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(new net.minecraft.resources.ResourceLocation("brewinandchewin:tipsy"));
        if (tipsy != null && player.hasEffect(tipsy)) player.removeEffect(tipsy);
        else player.heal((float) SpiceConfig.cheeseHealing);
    }
    public static void cure(Player player, int count) {
        List<MobEffect> effects = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) if (effect.getEffect().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL) effects.add(effect.getEffect());
        Collections.shuffle(effects, new java.util.Random(player.getRandom().nextLong()));
        for (int i = 0; i < Math.min(count, effects.size()); i++) player.removeEffect(effects.get(i));
    }
    public static int mysteryCureCount(int copies) { return mysteryCureCount((long) copies); }
    public static int mysteryCureCount(long copies) {
        return copies >= 3 ? (int) Math.min(Integer.MAX_VALUE, 2L * copies - 2) : (int) Math.max(0, copies);
    }
    public static void share(Player eater, Map<MobEffect, MobEffectInstance> effects, float saturation, int copies) { share(eater, effects, saturation, (long) copies); }
    public static void share(Player eater, Map<MobEffect, MobEffectInstance> effects, float saturation, long copies) {
        double scale = SpiceConfig.sharingPerCopy * copies, radius = SpiceConfig.sharingRadius;
        if (radius <= 0 || scale <= 0) return;
        List<Player> players = eater.level().getEntitiesOfClass(Player.class, eater.getBoundingBox().inflate(radius));
        for (Player recipient : players) {
            if (recipient == eater || !recipient.isAlive() || recipient.distanceToSqr(eater) > radius * radius) continue;
            addSaturation(recipient, (float) Math.max(0, saturation * scale));
            for (MobEffectInstance effect : effects.values()) if (effect.getEffect().getCategory() != net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
                int ticks = Seasoning.ticks(effect.getDuration() * scale);
                if (ticks > 0) refresh(recipient, new MobEffectInstance(effect.getEffect(), ticks, effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
            }
        }
    }
    public static void addSaturation(Player player, float amount) {
        net.minecraft.world.food.FoodData food = player.getFoodData();
        float before = food.getSaturationLevel();
        food.setSaturation(Math.min(food.getFoodLevel(), before + amount));
        if (food.getSaturationLevel() != before && player instanceof net.minecraft.server.level.ServerPlayer recipient)
            recipient.connection.send(new net.minecraft.network.protocol.game.ClientboundSetHealthPacket(
                recipient.getHealth(), food.getFoodLevel(), food.getSaturationLevel()));
    }
    public static boolean teleport(Player player) {
        double startX = player.getX(), startY = player.getY(), startZ = player.getZ();
        for (int i = 0; i < 16; i++) {
            double x = startX + (player.getRandom().nextDouble() - .5) * 16;
            double y = startY + player.getRandom().nextInt(16) - 8;
            double z = startZ + (player.getRandom().nextDouble() - .5) * 16;
            if (player.isPassenger()) player.stopRiding();
            if (player.randomTeleport(x, y, z, true)) {
                player.level().playSound(null, startX, startY, startZ, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1, 1);
                player.playSound(SoundEvents.CHORUS_FRUIT_TELEPORT, 1, 1); return true;
            }
        }
        return false;
    }
    private static int level(net.minecraft.world.entity.LivingEntity entity, MobEffect potion) {
        MobEffectInstance effect = potion == null ? null : entity.getEffect(potion); return effect == null ? 0 : effect.getAmplifier() + 1;
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void mining(PlayerEvent.BreakSpeed event) {
        int level = level(event.getEntity(), SpiceRegistry.sweet);
        if (level > 0) event.setNewSpeed((float) (event.getNewSpeed() * (1 + SpiceConfig.miningPerLevel * level)));
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void healing(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        int level = level(event.getEntity(), SpiceRegistry.tasty);
        if (level > 0) event.setAmount((float) (event.getAmount() * (1 + SpiceConfig.healingPerLevel * level)));
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void damage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        int level = level(event.getEntity(), SpiceRegistry.aromatic);
        if (level > 0) event.setAmount((float) (event.getAmount() * Math.max(0, 1 - SpiceConfig.reductionBase - SpiceConfig.reductionPerLevel * level)));
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void hotAttack(LivingHurtEvent event) {
        if (event.getAmount() <= 0 || !(event.getSource().getEntity() instanceof Player)) return;
        net.minecraft.world.entity.Entity immediate = event.getSource().getDirectEntity();
        if (!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK) && (!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE) || immediate == null)) return;
        Player player = (Player) event.getSource().getEntity();
        int level = level(player, SpiceRegistry.hot);
        if (level == 0) return;
        event.setAmount((float) (event.getAmount() * (1 + SpiceConfig.hotBase + SpiceConfig.hotPerLevel * level)));
        if (SpiceConfig.fireSeconds > 0) event.getEntity().setSecondsOnFire(SpiceConfig.fireSeconds);
    }
    private SpiceEffects() {}
}
