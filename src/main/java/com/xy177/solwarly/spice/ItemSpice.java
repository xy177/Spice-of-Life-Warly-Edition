package com.xy177.solwarly.spice;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Shared registered item for spices; the NBT flavor key distinguishes variants. */
public final class ItemSpice extends Item {
    public static final String FLAVOR_KEY = "SpiceFlavor";
    public static final String COLOR_KEY = "SpiceColor";

    public ItemSpice() { setMaxStackSize(64); setCreativeTab(net.minecraft.creativetab.CreativeTabs.FOOD); }
    @Override @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public void getSubItems(net.minecraft.creativetab.CreativeTabs tab, net.minecraft.util.NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) for (SpiceKind kind : SpiceKind.all())
            if (!kind.accompaniment() && kind.available()) items.add(Seasoning.powder(java.util.Collections.singletonList(kind), 1));
    }
    @Override public String getItemStackDisplayName(ItemStack stack) {
        java.util.List<SpiceKind> kinds = Seasoning.components(stack);
        if (!Seasoning.mixed(kinds) && kinds.size() == 1 && kinds.get(0) instanceof CustomSpice) return ((CustomSpice) kinds.get(0)).text("name");
        return kinds.isEmpty() ? super.getItemStackDisplayName(stack) : new net.minecraft.util.text.TextComponentTranslation(
            "item.solwarly.spice." + (Seasoning.mixed(kinds) ? "mixed" : kinds.get(0).id()) + ".name").getUnformattedText();
    }

    @Override public boolean hasEffect(ItemStack stack) { return Seasoning.creative(stack) || super.hasEffect(stack); }
    public static ItemStack create(String flavor, int color, int count) {
        if (flavor == null || flavor.isEmpty() || count < 1 || count > 64)
            throw new IllegalArgumentException("Spice requires a type and a stack count between 1 and 64");
        ItemStack stack = new ItemStack(SpiceRegistry.spice, count);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(FLAVOR_KEY, flavor); tag.setInteger(COLOR_KEY, color);
        stack.setTagCompound(tag); return stack;
    }
    public static String flavor(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getString(FLAVOR_KEY) : "";
    }
    public static int color(ItemStack stack, int layer) {
        return layer == 0 && stack.hasTagCompound() && stack.getTagCompound().hasKey(COLOR_KEY)
            ? stack.getTagCompound().getInteger(COLOR_KEY) & 0xFFFFFF : 0xFFFFFF;
    }
}
