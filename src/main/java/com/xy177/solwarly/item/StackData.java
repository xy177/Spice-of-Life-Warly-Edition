package com.xy177.solwarly.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.function.Consumer;

/** Updates only our custom data, preserving all other stack components. */
public final class StackData {
    public static CompoundTag get(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }
    public static void set(ItemStack stack, CompoundTag tag) {
        if(tag==null||tag.isEmpty())stack.remove(DataComponents.CUSTOM_DATA);
        else stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }
    public static void update(ItemStack stack, Consumer<CompoundTag> change) {
        CompoundTag tag=get(stack);change.accept(tag);set(stack,tag);
    }
    private StackData() {}
}
