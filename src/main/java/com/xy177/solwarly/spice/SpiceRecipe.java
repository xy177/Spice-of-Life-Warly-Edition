package com.xy177.solwarly.spice;

import net.minecraft.item.ItemStack;

/** A data-driven spice recipe. Concrete spice items can register recipes later. */
public final class SpiceRecipe {
    public final ItemStack input;
    public final ItemStack output;
    public final int inputCount;
    public final int outputCount;
    private final ItemStack container;

    public SpiceRecipe(ItemStack input, int inputCount, ItemStack output, int outputCount) {
        this(input, inputCount, output, outputCount, ItemStack.EMPTY);
    }
    public SpiceRecipe(ItemStack input, int inputCount, ItemStack output, int outputCount, ItemStack container) {
        if (input.isEmpty() || output.isEmpty() || inputCount <= 0 || inputCount > input.getMaxStackSize()
            || outputCount <= 0 || outputCount > output.getMaxStackSize())
            throw new IllegalArgumentException("Spice recipe requires non-empty stacks and positive counts");
        this.input = input.copy(); this.input.setCount(1);
        this.output = output.copy(); this.output.setCount(1);
        this.inputCount = inputCount; this.outputCount = outputCount;
        this.container = container.copy();
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && ItemStack.areItemsEqual(input, stack)
            && ItemStack.areItemStackTagsEqual(input, stack);
    }

    public ItemStack result() {
        ItemStack result = output.copy(); result.setCount(outputCount); return result;
    }
    public ItemStack returnedContainers() {
        ItemStack result = container.copy(); if (!result.isEmpty()) result.setCount(result.getCount() * inputCount); return result;
    }
}
