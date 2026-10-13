package com.xy177.solwarly.diet;
import net.minecraft.core.Holder;

import com.xy177.solwarly.SOLWarly;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.*;
import net.minecraft.resources.ResourceLocation;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class DietRewards {
    private static final String PREFIX = "solwarly:diversity:";
    private static final Map<Player, Map<Holder<MobEffect>, MobEffectInstance>> ownedEffects = new WeakHashMap<>();
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
                if (modifier.id().getNamespace().equals("solwarly") && modifier.id().getPath().startsWith("diversity/")) attribute.removeModifier(modifier);
        DietData data = DietCapability.get(player);
        double tier = tier(data.diversity(DietConfig.extraFlavor, DietConfig.scale), DietConfig.attributes, DietConfig.effects);
        for (String line : DietConfig.attributes) {
            try {
                String[] parts = line.split("\\|");
                if (Double.parseDouble(parts[0]) != tier) continue;
                AttributeInstance attribute = player.getAttribute(com.xy177.solwarly.registry.RegistryLookup.attribute(net.minecraft.resources.ResourceLocation.parse(attributeId(parts[1]))));
                if (attribute == null) continue;
                String name = PREFIX + parts[1];
                var id=ResourceLocation.fromNamespaceAndPath("solwarly","diversity/"+parts[1].replace(':','/'));
                attribute.addTransientModifier(new AttributeModifier(id, Double.parseDouble(parts[2]), AttributeModifier.Operation.values()[Integer.parseInt(parts[3])]));
            } catch (RuntimeException e) { SOLWarly.LOGGER.warn("Invalid diversity attribute: {}", line); }
        }
    }
    public static void refreshEffects(Player player) {
        DietData data = DietCapability.get(player);
        if (data == null || !data.initialized) return;
        double tier = tier(data.diversity(DietConfig.extraFlavor, DietConfig.scale), DietConfig.attributes, DietConfig.effects);
        Map<Holder<MobEffect>, Integer> wanted = new HashMap<>();
        for (String line : DietConfig.effects) {
            try {
                String[] parts = line.split("\\|");
                if (Double.parseDouble(parts[0]) != tier) continue;
                Holder<MobEffect> potion = com.xy177.solwarly.registry.RegistryLookup.effect(net.minecraft.resources.ResourceLocation.parse(parts[1]));
                if (potion != null) wanted.put(potion, Integer.parseInt(parts[2]));
            } catch (RuntimeException e) { SOLWarly.LOGGER.warn("Invalid diversity effect: {}", line); }
        }
        Map<Holder<MobEffect>, MobEffectInstance> owned = ownedEffects.computeIfAbsent(player, key -> new HashMap<>());
        for (Iterator<Map.Entry<Holder<MobEffect>, MobEffectInstance>> it = owned.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Holder<MobEffect>, MobEffectInstance> entry = it.next();
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
    public static void onEffectAdded(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player) || event.getEffectInstance() instanceof DietEffect) return;
        Map<Holder<MobEffect>, MobEffectInstance> owned = ownedEffects.get(event.getEntity());
        if (owned != null) owned.remove(event.getEffectInstance().getEffect());
    }
    private static final class DietEffect extends MobEffectInstance {
        DietEffect(Holder<MobEffect> potion, int amplifier) { super(potion, 60, amplifier, true, false); }
    }
    public static String attributeId(String id) {
        return id.startsWith("generic.") ? "minecraft:" + id : id;
    }
    private DietRewards() {}
}
