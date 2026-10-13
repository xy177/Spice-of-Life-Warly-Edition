package com.xy177.solwarly.spice;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class PotionSpice extends MobEffect {
    @Override public void initializeClient(java.util.function.Consumer<net.minecraftforge.client.extensions.common.IClientMobEffectExtensions> consumer) {
        consumer.accept(new com.xy177.solwarly.client.SpiceEffectIcons(this));
    }
    public final SpiceKind kind;
    public PotionSpice(SpiceKind kind) {
        super(MobEffectCategory.BENEFICIAL,kind.defaultColor);this.kind=kind;
        if(kind==SpiceKind.SUGAR)addAttributeModifier(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
            "861ffcc6-3165-4342-aea1-82c0a9b0de61",.07,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    @Override public double getAttributeModifierValue(int amplifier,net.minecraft.world.entity.ai.attributes.AttributeModifier modifier) {
        return kind==SpiceKind.SUGAR?SpiceConfig.attackSpeedPerLevel*(amplifier+1):super.getAttributeModifierValue(amplifier,modifier);
    }
}
