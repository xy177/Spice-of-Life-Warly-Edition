package com.xy177.solwarly.spice;
import net.minecraft.core.Holder;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.diet.DietRuntime;
import com.xy177.solwarly.diet.WarlyClockData;
import com.xy177.solwarly.diet.DietRewards;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.*;


import java.util.*;

@EventBusSubscriber(modid = SOLWarly.MOD_ID)
public final class SpiceEffects {
    private static final Map<Player, List<Meal>> pending = new WeakHashMap<>();
    private static final class Meal {
        final net.minecraft.world.item.ItemStack food;
        final List<SpiceKind> kinds;
        float saturation;
        final Map<Holder<MobEffect>,MobEffectInstance> original=new LinkedHashMap<>();
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
    @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        if(!event.getEntity().level().isClientSide)finish(event.getEntity());
    }
    public static void finish(Player player) {
        List<Meal> meals=pending.remove(player);if(meals==null)return;
        for(Meal meal:meals) {
            Map<Holder<MobEffect>,MobEffectInstance> effects=apply(player,meal.kinds,meal.original);
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
                var inventory=new com.xy177.solwarly.lunch.LunchInventory(food,player.registryAccess());
                int slot=com.xy177.solwarly.lunch.ItemLunchContainer.bestFood(inventory,player);
                if(slot>=0)food=inventory.getStackInSlot(slot);
            }
            if((food.getFoodProperties(null)!=null))event.setDuration(DietRuntime.useDuration(player,food,event.getDuration()));
        }
    }
    /** Compatibility entry point used by earlier integrations and tests. */
    public static void apply(Player player, List<SpiceKind> kinds, int originalNourishment, int originalAmplifier) {
        Map<Holder<MobEffect>, MobEffectInstance> original = new LinkedHashMap<>();
        if (originalNourishment > 0 && SpiceKind.HAM.potion() != null) original.put(SpiceKind.HAM.potion(),
            new MobEffectInstance(SpiceKind.HAM.potion(), originalNourishment, originalAmplifier));
        apply(player, kinds, original);
    }
    public static Map<Holder<MobEffect>, MobEffectInstance> apply(Player player, List<SpiceKind> kinds, Map<Holder<MobEffect>, MobEffectInstance> original) {
        List<SeasoningPotions.Entry> additions = new ArrayList<>();
        for (SeasoningPotions.Entry entry : SeasoningPotions.additions(kinds, original))
            if (entry.chance >= 1 || player.getRandom().nextFloat() < entry.chance) additions.add(entry);
        Map<Holder<MobEffect>, MobEffectInstance> combined = SeasoningPotions.merge(original, additions);
        Set<Holder<MobEffect>> added = new LinkedHashSet<>();
        for (SeasoningPotions.Entry entry : additions) added.add(entry.effect.getEffect());
        for (Holder<MobEffect> potion : added) refresh(player, combined.get(potion));
        return combined;
    }
    public static void refresh(Player player, MobEffectInstance incoming) {
        MobEffectInstance old = player.getEffect(incoming.getEffect());
        player.addEffect(new MobEffectInstance(incoming.getEffect(), Math.max(incoming.getDuration(), old == null ? 0 : old.getDuration()),
            Math.max(incoming.getAmplifier(), old == null ? 0 : old.getAmplifier()), incoming.isAmbient(), incoming.isVisible()));
    }
    public static void cheese(Player player) {
        Holder<MobEffect> tipsy = com.xy177.solwarly.registry.RegistryLookup.effect(net.minecraft.resources.ResourceLocation.parse("brewinandchewin:tipsy"));
        if (tipsy != null && player.hasEffect(tipsy)) player.removeEffect(tipsy);
        else player.heal((float) SpiceConfig.cheeseHealing);
    }
    public static void cure(Player player, int count) {
        List<Holder<MobEffect>> effects = new ArrayList<>();
        for (MobEffectInstance effect : player.getActiveEffects()) if (effect.getEffect().value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL) effects.add(effect.getEffect());
        Collections.shuffle(effects, new java.util.Random(player.getRandom().nextLong()));
        for (int i = 0; i < Math.min(count, effects.size()); i++) player.removeEffect(effects.get(i));
    }
    public static int mysteryCureCount(int copies) { return mysteryCureCount((long) copies); }
    public static int mysteryCureCount(long copies) {
        return copies >= 3 ? (int) Math.min(Integer.MAX_VALUE, 2L * copies - 2) : (int) Math.max(0, copies);
    }
    public static void share(Player eater, Map<Holder<MobEffect>, MobEffectInstance> effects, float saturation, int copies) { share(eater, effects, saturation, (long) copies); }
    public static void share(Player eater, Map<Holder<MobEffect>, MobEffectInstance> effects, float saturation, long copies) {
        double scale = SpiceConfig.sharingPerCopy * copies, radius = SpiceConfig.sharingRadius;
        if (radius <= 0 || scale <= 0) return;
        List<Player> players = eater.level().getEntitiesOfClass(Player.class, eater.getBoundingBox().inflate(radius));
        for (Player recipient : players) {
            if (recipient == eater || !recipient.isAlive() || recipient.distanceToSqr(eater) > radius * radius) continue;
            addSaturation(recipient, (float) Math.max(0, saturation * scale));
            for (MobEffectInstance effect : effects.values()) if (effect.getEffect().value().getCategory() != net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
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
    private static int level(net.minecraft.world.entity.LivingEntity entity, Holder<MobEffect> potion) {
        MobEffectInstance effect = potion == null ? null : entity.getEffect(potion); return effect == null ? 0 : effect.getAmplifier() + 1;
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void mining(PlayerEvent.BreakSpeed event) {
        int level = level(event.getEntity(), SpiceKind.SUGAR.potion());
        if (level > 0) event.setNewSpeed((float) (event.getNewSpeed() * (1 + SpiceConfig.miningPerLevel * level)));
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void healing(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        int level = level(event.getEntity(), SpiceKind.SALT.potion());
        if (level > 0) event.setAmount((float) (event.getAmount() * (1 + SpiceConfig.healingPerLevel * level)));
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void damage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player)) return;
        int level = level(event.getEntity(), SpiceKind.ONION.potion());
        if (level > 0) event.setNewDamage((float) (event.getNewDamage() * Math.max(0, 1 - SpiceConfig.reductionBase - SpiceConfig.reductionPerLevel * level)));
    }
    @SubscribeEvent(priority = EventPriority.LOW) public static void hotAttack(LivingIncomingDamageEvent event) {
        if (event.getAmount() <= 0 || !(event.getSource().getEntity() instanceof Player)) return;
        net.minecraft.world.entity.Entity immediate = event.getSource().getDirectEntity();
        if (!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK) && (!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE) || immediate == null)) return;
        Player player = (Player) event.getSource().getEntity();
        int level = level(player, SpiceKind.SPICY.potion());
        if (level == 0) return;
        event.setAmount((float) (event.getAmount() * (1 + SpiceConfig.hotBase + SpiceConfig.hotPerLevel * level)));
        if (SpiceConfig.fireSeconds > 0) event.getEntity().igniteForSeconds(SpiceConfig.fireSeconds);
    }
    private SpiceEffects() {}
}
