package com.xy177.solwarly.spice;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

/** One registered item, with recipe identity and colour preserved in stack data. */
public final class ItemSpice extends Item {
    public static final String FLAVOR_KEY = "SpiceFlavor", COLOR_KEY = "SpiceColor";
    public ItemSpice() { super(new Item.Properties()); }
    @Override public Component getName(ItemStack stack) {
        var kinds=Seasoning.components(stack);
        if(!Seasoning.mixed(kinds) && kinds.size()==1 && kinds.get(0) instanceof CustomSpice custom) return Component.literal(custom.text("name"));
        return kinds.isEmpty() ? super.getName(stack) : Component.translatable("item.solwarly.spice."+(Seasoning.mixed(kinds)?"mixed":kinds.get(0).id())+".name");
    }
    @Override public boolean isFoil(ItemStack stack) { return Seasoning.creative(stack) || super.isFoil(stack); }
    public static ItemStack create(String flavor,int color,int count) {
        if(flavor==null||flavor.isEmpty()||count<1||count>64)throw new IllegalArgumentException("Invalid spice variant");
        ItemStack stack=new ItemStack(SpiceRegistry.spice,count);
        CompoundTag tag=new CompoundTag();tag.putString(FLAVOR_KEY,flavor);tag.putInt(COLOR_KEY,color & 0xFFFFFF);stack.setTag(tag);return stack;
    }
    public static String flavor(ItemStack stack) { return stack.hasTag()?stack.getTag().getString(FLAVOR_KEY):""; }
    public static int color(ItemStack stack,int layer) {
        if (layer == 0) {
            var kind = SpiceKind.from(flavor(stack));
            if (kind != null && kind.immortal()) return kind.color();
        } return layer==0&&stack.hasTag()&&stack.getTag().contains(COLOR_KEY)?stack.getTag().getInt(COLOR_KEY)&0xFFFFFF:0xFFFFFF; }
}
