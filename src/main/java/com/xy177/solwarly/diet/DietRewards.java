package com.xy177.solwarly.diet;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.potion.*;
import net.minecraft.util.ResourceLocation;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class DietRewards {
    private static final UUID EXPERIENCE_HEALTH=UUID.nameUUIDFromBytes("solwarly:experience_health".getBytes(StandardCharsets.UTF_8));
    public static void update(EntityPlayer player) {
        if(player.world.isRemote)return;
        DietRuntime.initialize(player);
        DietData data=DietCapability.get(player);
        if(data==null)return;
        IAttributeInstance attribute=player.getEntityAttribute(net.minecraft.entity.SharedMonsterAttributes.MAX_HEALTH);
        if(attribute==null)return;
        AttributeModifier previous=attribute.getModifier(EXPERIENCE_HEALTH);
        double amount=2.0*(com.xy177.solwarly.SOLWarlyConfig.baseHearts-10)+cumulativeHealth(data.cumulative());
        if(previous==null || previous.getAmount()!=amount) {
            if(previous!=null)attribute.removeModifier(previous);
            AttributeModifier modifier=new AttributeModifier(EXPERIENCE_HEALTH,"solwarly:experience_health",amount,0);
            attribute.applyModifier(modifier);
        }
        updateAttributes(player);refreshEffects(player);
        if(player.getHealth()>player.getMaxHealth())player.setHealth(player.getMaxHealth());
    }

    private static final String PREFIX = "solwarly:diversity:";
    private static final Map<EntityPlayer, Map<Potion, PotionEffect>> ownedEffects = new WeakHashMap<>();
    public static boolean reachedFinalCumulativeTier(double score) {
        double last = Arrays.stream(DietConfig.cumulativeRewards).mapToDouble(line -> Double.parseDouble(line.split("\\|")[0])).max().orElse(Double.POSITIVE_INFINITY);
        return score >= last;
    }
    public static double cumulativeHealth(double score) {
        double threshold = -1, health = 0;
        for (String line : DietConfig.cumulativeRewards) {
            try {
                String[] parts = line.split("\\|"); double candidate = Double.parseDouble(parts[0]);
                if (candidate <= score && candidate >= threshold) { threshold = candidate; health = Double.parseDouble(parts[1]); }
            } catch (RuntimeException e) { SOLWarly.LOGGER.warn("Invalid cumulative reward: {}", line); }
        }
        return health;
    }
    public static double tier(double score, String[] attributes, String[] effects) {
        double result = -1;
        for (String[] entries : new String[][] {attributes, effects}) for (String entry : entries) {
            try { double value = Double.parseDouble(entry.split("\\|")[0]); if (value <= score) result = Math.max(result, value); }
            catch (RuntimeException ignored) { }
        }
        return result;
    }
    public static void updateAttributes(EntityPlayer player) {
        for (IAttributeInstance attribute : player.getAttributeMap().getAllAttributes())
            for (AttributeModifier modifier : new ArrayList<>(attribute.getModifiers()))
                if (modifier.getName().startsWith(PREFIX)) attribute.removeModifier(modifier);
        DietData data = DietCapability.get(player);
        double tier = tier(data.diversity(DietConfig.extraFlavor, DietConfig.scale), DietConfig.attributes, DietConfig.effects);
        for (String line : DietConfig.attributes) {
            try {
                String[] parts = line.split("\\|");
                if (Double.parseDouble(parts[0]) != tier) continue;
                IAttributeInstance attribute = player.getAttributeMap().getAttributeInstanceByName(parts[1]);
                if (attribute == null) continue;
                String name = PREFIX + parts[1];
                UUID id = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
                attribute.applyModifier(new AttributeModifier(id, name, Double.parseDouble(parts[2]), Integer.parseInt(parts[3])));
            } catch (RuntimeException e) { SOLWarly.LOGGER.warn("Invalid diversity attribute: {}", line); }
        }
    }
    public static void refreshEffects(EntityPlayer player) {
        DietData data = DietCapability.get(player);
        if (data == null || !data.initialized) return;
        double tier = tier(data.diversity(DietConfig.extraFlavor, DietConfig.scale), DietConfig.attributes, DietConfig.effects);
        Map<Potion, Integer> wanted = new HashMap<>();
        for (String line : DietConfig.effects) {
            try {
                String[] parts = line.split("\\|");
                if (Double.parseDouble(parts[0]) != tier) continue;
                Potion potion = Potion.REGISTRY.getObject(new ResourceLocation(parts[1]));
                if (potion != null) wanted.put(potion, Integer.parseInt(parts[2]));
            } catch (RuntimeException e) { SOLWarly.LOGGER.warn("Invalid diversity effect: {}", line); }
        }
        Map<Potion, PotionEffect> owned = ownedEffects.computeIfAbsent(player, key -> new HashMap<>());
        for (Iterator<Map.Entry<Potion, PotionEffect>> it = owned.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Potion, PotionEffect> entry = it.next();
            if (player.getActivePotionEffect(entry.getKey()) != entry.getValue()) { it.remove(); continue; }
            if (!wanted.containsKey(entry.getKey()) || wanted.get(entry.getKey()) != entry.getValue().getAmplifier()) {
                player.removePotionEffect(entry.getKey()); it.remove();
            }
        }
        wanted.forEach((potion, amplifier) -> {
            PotionEffect active = player.getActivePotionEffect(potion);
            if (active == null || active == owned.get(potion)) {
                PotionEffect effect = new DietEffect(potion, amplifier);
                player.addPotionEffect(effect);
                PotionEffect applied = player.getActivePotionEffect(potion);
                if (applied instanceof DietEffect) owned.put(potion, applied);
            }
        });
    }
    public static void onEffectAdded(net.minecraftforge.event.entity.living.PotionEvent.PotionAddedEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || event.getPotionEffect() instanceof DietEffect) return;
        Map<Potion, PotionEffect> owned = ownedEffects.get(event.getEntityLiving());
        if (owned != null) owned.remove(event.getPotionEffect().getPotion());
    }
    private static final class DietEffect extends PotionEffect {
        DietEffect(Potion potion, int amplifier) { super(potion, 60, amplifier, true, false); }
    }
    private DietRewards() {}
}
