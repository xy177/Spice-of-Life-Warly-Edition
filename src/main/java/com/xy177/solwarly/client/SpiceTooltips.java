package com.xy177.solwarly.client;
import net.minecraft.core.Holder;

import com.xy177.solwarly.spice.*;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.effect.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.*;
import net.neoforged.api.distmarker.Dist;
import java.time.LocalDate;
import java.util.*;

@EventBusSubscriber(modid = "solwarly", value = Dist.CLIENT)
public final class SpiceTooltips {
    @SubscribeEvent(priority = EventPriority.LOWEST) public static void tooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack(); List<SpiceKind> kinds = Seasoning.components(stack);
        boolean nativeMaterial = kinds.isEmpty();
        if (nativeMaterial) kinds = Seasoning.ingredient(stack);
        if (kinds.isEmpty()) return;
        kinds = Seasoning.effectKinds(kinds);
        boolean powder = stack.getItem() == SpiceRegistry.spice;
        boolean seasoned = Seasoning.seasoned(stack);
        List<Component> lines = event.getToolTip();
        if (seasoned) {
            addPotionTooltips(stack, lines);
            if (!lines.isEmpty()) lines.set(0, Component.literal(foodName(lines.get(0).getString(), kinds, LocalDate.now())).withStyle(lines.get(0).getStyle()));
        }
        if (nativeMaterial && Seasoning.accompaniments(kinds)) lines.add(Component.translatable("solwarly.spice.accompaniment").withStyle(ChatFormatting.AQUA));
        for (SpiceKind kind : new LinkedHashSet<>(kinds)) {
            int copies = Math.min(3, Seasoning.copies(kinds, kind));
            int color = nativeMaterial || kinds.size() > 1 ? kind.color() : Seasoning.color(stack);
            String description;
            if (kind instanceof CustomSpice) {
                CustomSpice custom = (CustomSpice) kind;
                description = custom.text("tooltip");
                if (description.trim().isEmpty()) {
                    description = custom.text("name");
                    List<String> effects = new ArrayList<>();
                    for (SeasoningPotions.Entry entry : custom.effects(copies)) {
                        MobEffectInstance effect = entry.effect;
                        String effectName = I18n.get(effect.getDescriptionId());
                        if (effect.getAmplifier() > 0) effectName += " " + potency(effect.getAmplifier());
                        effects.add(effectName + " (" + MobEffectUtil.formatDuration(effect, 1.0F,20).getString() + ")");
                    }
                    if (!effects.isEmpty()) description += ": " + String.join(", ", effects);
                }
                if (copies > 1 && !kind.accompaniment()) description += " " + I18n.get("solwarly.spice." + (copies == 3 ? "too_much" : "lots"));
            } else if (kind == SpiceKind.MYSTERY) {
                description = I18n.get("solwarly.spice.mystery.tooltip",
                    SpiceEffects.mysteryCureCount(copies));
            } else {
                description = kind.fruit() ? fruitDescription(kind) : I18n.get("solwarly.spice." + kind.id() + ".tooltip");
                if (copies > 1 && !kind.accompaniment()) description += " " + I18n.get("solwarly.spice." + (copies == 3 ? "too_much" : "lots"));
            }
            for (String line : description.split("\\r?\\n", -1)) lines.add(Component.literal(line).withStyle(style->style.withColor(color)));
        }
    }

    private static String fruitDescription(SpiceKind kind) {
        List<SeasoningPotions.Entry> effects = SeasoningPotions.additions(
            Collections.singletonList(kind), Collections.<Holder<MobEffect>, MobEffectInstance>emptyMap());
        String fruit = I18n.get("solwarly.fruit." + kind.fruitId());
        if (effects.isEmpty()) return I18n.get("solwarly.spice.fruit.tooltip", fruit);
        List<String> names = new ArrayList<>();
        for (SeasoningPotions.Entry entry : effects)
            names.add(Component.translatable(entry.effect.getDescriptionId()).getString());
        return I18n.get("solwarly.spice.fruit.tooltip", String.join(", ", names));
    }

    private static void addPotionTooltips(ItemStack stack,List<Component> lines) {
        Map<Holder<MobEffect>,MobEffectInstance> original=new LinkedHashMap<>();
        var food=stack.getFoodProperties(net.minecraft.client.Minecraft.getInstance().player);
        if(food!=null)for(var entry:food.effects())original.put(entry.effect().getEffect(),entry.effect());
        var additions=SeasoningPotions.additions(Seasoning.components(stack),original,ImmortalSpices.clientPowered);
        var merged=SeasoningPotions.merge(original,additions);
        Set<Holder<MobEffect>> rendered=new HashSet<>();int index=Math.min(1,lines.size());
        for(var entry:additions) {
            Holder<MobEffect> type=entry.effect.getEffect();if(!rendered.add(type))continue;
            String name=Component.translatable(type.value().getDescriptionId()).getString();
            for(int i=1;i<lines.size();i++)if(lines.get(i).getString().contains(name)) { lines.remove(i);if(i<index)index--;i--; }
            MobEffectInstance effect=merged.get(type);
            Component effectName=Component.translatable(effect.getDescriptionId());
            if(effect.getAmplifier()>0)effectName=Component.translatable("potion.withAmplifier",effectName,Component.literal(potency(effect.getAmplifier())));
            String key="potion.withDuration";
            lines.add(index++,Component.translatable(key,effectName,MobEffectUtil.formatDuration(effect,1.0F,20)).withStyle(ChatFormatting.BLUE));
            if(entry.chance<.999F)lines.add(index++,Component.translatable("solwarly.tooltip.food.effect_chance",Math.round(entry.chance*100)).withStyle(ChatFormatting.BLUE));
        }
    }

    public static String foodName(String original, List<SpiceKind> kinds, LocalDate date) {
        kinds = Seasoning.effectKinds(kinds);
        java.util.regex.Matcher debug = java.util.regex.Pattern.compile("(\\s+(?:§.)*\\(#\\d+(?:/\\d+)?\\).*)$").matcher(original);
        String ending = "";
        if (debug.find()) { ending = debug.group(1); original = original.substring(0, debug.start()); }
        List<SpiceKind> unique = new ArrayList<>(new LinkedHashSet<>(kinds));
        for (int i = unique.size() - 1; i >= 0; i--) {
            SpiceKind kind = unique.get(i); int copies = Math.min(3, Seasoning.copies(kinds, kind));
            String id = kind.id();
            if (kind == SpiceKind.HAM && (date.getMonthValue() == 4 && date.getDayOfMonth() == 1
                || date.getMonthValue() == 11 && date.getDayOfMonth() == 18)) id = "ham_holiday";
            if (kind instanceof CustomSpice) {
                CustomSpice custom = (CustomSpice) kind;
                String prefix = custom.text("prefix").trim(), suffix = custom.text("suffix").trim();
                original = (prefix.isEmpty() ? "" : prefix + " ") + original + (suffix.isEmpty() ? "" : " " + suffix);
            } else for (int n = 0; n < (kind.accompaniment() ? copies : 1); n++) original = I18n.get("solwarly.seasoning." + id, original);
            if (copies > 1 && !kind.accompaniment()) original = I18n.get("solwarly.seasoning." + (copies == 3 ? "extreme" : "super"), original);
        }
        return original + ending;
    }
    private static String potency(int amplifier) {
        String key="potion.potency."+amplifier, text=I18n.get(key);
        if (!text.equals(key)) return text;
        int level=amplifier+1; StringBuilder roman=new StringBuilder();
        int[] values={100,90,50,40,10,9,5,4,1}; String[] symbols={"C","XC","L","XL","X","IX","V","IV","I"};
        for(int i=0;i<values.length;i++)while(level>=values[i]) {roman.append(symbols[i]);level-=values[i];}
        return roman.toString();
    }
    private SpiceTooltips() {}
}
