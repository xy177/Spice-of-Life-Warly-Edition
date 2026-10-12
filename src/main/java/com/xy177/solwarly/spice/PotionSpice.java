package com.xy177.solwarly.spice;

import net.minecraft.potion.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.*;
import java.util.Collections;

public final class PotionSpice extends Potion {
    private final SpiceKind kind;
    public PotionSpice(SpiceKind kind, String id) {
        super(false, kind.defaultColor); this.kind = kind;
        setRegistryName("solwarly", id); setPotionName("effect.solwarly." + id); setBeneficial();
        if (kind == SpiceKind.SUGAR) registerPotionAttributeModifier(SharedMonsterAttributes.ATTACK_SPEED,
            "861ffcc6-3165-4342-aea1-82c0a9b0de61", .07, 2);
    }
    @Override public double getAttributeModifierAmount(int amplifier, AttributeModifier modifier) {
        return kind == SpiceKind.SUGAR ? SpiceConfig.attackSpeedPerLevel * (amplifier + 1) : super.getAttributeModifierAmount(amplifier, modifier);
    }
    @Override public boolean hasStatusIcon() { return false; }
    @Override @SideOnly(Side.CLIENT) public void renderInventoryEffect(PotionEffect effect, Gui gui, int x, int y, float z) { icon(x + 6, y + 7, 1); }
    @Override @SideOnly(Side.CLIENT) public void renderHUDEffect(PotionEffect effect, Gui gui, int x, int y, float z, float alpha) { icon(x + 3, y + 3, alpha); }
    @SideOnly(Side.CLIENT) private void icon(int x, int y, float alpha) {
        GlStateManager.color(1, 1, 1, alpha);
        net.minecraft.client.renderer.RenderHelper.enableGUIStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(Seasoning.powder(Collections.singletonList(kind), 1), x, y);
        net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting(); GlStateManager.color(1, 1, 1, 1);
    }
}
