package com.xy177.solwarly.diet;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.*;
import net.minecraft.resources.ResourceLocation;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class DietRewards {
    private static final UUID EXPERIENCE_HEALTH=UUID.nameUUIDFromBytes("solwarly:experience_health".getBytes(StandardCharsets.UTF_8));
    public static void update(Player player) {
        if(player.level().isClientSide)return;
        DietRuntime.initialize(player);
        DietData data=DietCapability.get(player);
        if(data==null)return;
        AttributeInstance attribute=player.getAttribute(Attributes.MAX_HEALTH);
        if(attribute==null)return;
        AttributeModifier previous=attribute.getModifier(EXPERIENCE_HEALTH);
        double amount=2.0*(com.xy177.solwarly.SOLWarlyConfig.getBaseHearts()-10)+cumulativeHealth(data.cumulative());
        if(previous==null || previous.getAmount()!=amount) {
            if(previous!=null)attribute.removeModifier(previous);
            AttributeModifier modifier=new AttributeModifier(EXPERIENCE_HEALTH,"solwarly:experience_health",amount,AttributeModifier.Operation.ADDITION);
            attribute.addTransientModifier(modifier);
        }
        updateAttributes(player);refreshEffects(player);
        if(player.getHealth()>player.getMaxHealth())player.setHealth(player.getMaxHealth());
    }

    private static final String PREFIX = "solwarly:diversity:";
    private static final Map<Player, Map<MobEffect, MobEffectInstance>> ownedEffects = new WeakHashMap<>();
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
    public static void updateAttributes(Player player) {
        for (AttributeInstance attribute : player.getAttributes().getSyncableAttributes())
            for (AttributeModifier modifier : new ArrayList<>(attribute.getModifiers()))
                if (modifier.getName().startsWith(PREFIX)) attribute.removeModifier(modifier);
        DietData data = DietCapability.get(player);
        double tier = tier(data.diversity(DietConfig.extraFlavor, DietConfig.scale), DietConfig.attributes, DietConfig.effects);
        for (String line : DietConfig.attributes) {
            try {
                String[] parts = line.split("\\|");
                if (Double.parseDouble(parts[0]) != tier) continue;
                AttributeInstance attribute = player.getAttribute(net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(attributeId(parts[1]))));
                if (attribute == null) continue;
                String name = PREFIX + parts[1];
                UUID id = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
                attribute.addTransientModifier(new AttributeModifier(id, name, Double.parseDouble(parts[2]), AttributeModifier.Operation.fromValue(Integer.parseInt(parts[3]))));
            } catch (RuntimeException e) { SOLWarly.LOGGER.warn("Invalid diversity attribute: {}", line); }
        }
    }
    public static void refreshEffects(Player player) {
        DietData data = DietCapability.get(player);
        if (data == null || !data.initialized) return;
        double tier = tier(data.diversity(DietConfig.extraFlavor, DietConfig.scale), DietConfig.attributes, DietConfig.effects);
        Map<MobEffect, Integer> wanted = new HashMap<>();
        for (String line : DietConfig.effects) {
            try {
                String[] parts = line.split("\\|");
                if (Double.parseDouble(parts[0]) != tier) continue;
                MobEffect potion = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(parts[1]));
                if (potion != null) wanted.put(potion, Integer.parseInt(parts[2]));
            } catch (RuntimeException e) { SOLWarly.LOGGER.warn("Invalid diversity effect: {}", line); }
        }
        Map<MobEffect, MobEffectInstance> owned = ownedEffects.computeIfAbsent(player, key -> new HashMap<>());
        for (Iterator<Map.Entry<MobEffect, MobEffectInstance>> it = owned.entrySet().iterator(); it.hasNext();) {
            Map.Entry<MobEffect, MobEffectInstance> entry = it.next();
            if (player.getEffect(entry.getKey()) != entry.getValue()) { it.remove(); continue; }
            if (!wanted.containsKey(entry.getKey()) || wanted.get(entry.getKey()) != entry.getValue().getAmplifier()) {
                player.removeEffect(entry.getKey()); it.remove();
            }
        }
        wanted.forEach((potion, amplifier) -> {
            MobEffectInstance active = player.getEffect(potion);
            if (active == null || active == owned.get(potion)) {
                MobEffectInstance effect = new DietEffect(potion, amplifier);
                player.addEffect(effect);
                MobEffectInstance applied = player.getEffect(potion);
                if (applied instanceof DietEffect) owned.put(potion, applied);
            }
        });
    }
    public static void onEffectAdded(net.minecraftforge.event.entity.living.MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player) || event.getEffectInstance() instanceof DietEffect) return;
        Map<MobEffect, MobEffectInstance> owned = ownedEffects.get(event.getEntity());
        if (owned != null) owned.remove(event.getEffectInstance().getEffect());
    }
    private static final class DietEffect extends MobEffectInstance {
        DietEffect(MobEffect potion, int amplifier) { super(potion, 60, amplifier, true, false); }
    }
    public static String attributeId(String id) {
        return id.startsWith("generic.") ? "minecraft:" + id : id;
    }
    private DietRewards() {}
}
