package com.xy177.solwarly.spice;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class PotionSpice extends MobEffect {
    public final SpiceKind kind;
    public PotionSpice(SpiceKind kind) {
        super(MobEffectCategory.BENEFICIAL,kind.defaultColor);this.kind=kind;
        if(kind==SpiceKind.SUGAR)addAttributeModifier(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("solwarly","sweet_attack_speed"),
            net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,
            amplifier->SpiceConfig.attackSpeedPerLevel*(amplifier+1));
    }
}
