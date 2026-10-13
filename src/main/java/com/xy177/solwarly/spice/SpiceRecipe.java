package com.xy177.solwarly.spice;

import net.minecraft.world.item.ItemStack;

/** One or two exact ingredients, with independent quantities and returned containers. */
public final class SpiceRecipe {
    public final ItemStack input, secondInput, output;
    public final int inputCount, secondCount, outputCount;
    private final ItemStack container, secondContainer;
    public SpiceRecipe(ItemStack input, int inputCount, ItemStack output, int outputCount) {
        this(input, inputCount, output, outputCount, ItemStack.EMPTY);
    }
    public SpiceRecipe(ItemStack input, int inputCount, ItemStack output, int outputCount, ItemStack container) {
        this(input, inputCount, ItemStack.EMPTY, 0, output, outputCount, container, ItemStack.EMPTY);
    }
    public SpiceRecipe(ItemStack input, int inputCount, ItemStack second, int secondCount, ItemStack output, int outputCount,
                       ItemStack container, ItemStack secondContainer) {
        if (input.isEmpty() || output.isEmpty() || inputCount < 1 || inputCount > input.getMaxStackSize()
            || outputCount < 1 || outputCount > output.getMaxStackSize()
            || !second.isEmpty() && (secondCount < 1 || secondCount > second.getMaxStackSize() || same(input, second)))
            throw new IllegalArgumentException("Invalid spice recipe ingredients or quantities");
        this.input = input.copyWithCount(1); this.inputCount = inputCount;
        secondInput = second.copyWithCount(1); this.secondCount = second.isEmpty() ? 0 : secondCount;
        this.output = output.copyWithCount(1); this.outputCount = outputCount;
        this.container = container.copy(); this.secondContainer = secondContainer.copy();
    }
    public boolean dual() { return !secondInput.isEmpty(); }
    public static boolean same(ItemStack a, ItemStack b) { return !a.isEmpty() && !b.isEmpty() && ItemStack.isSameItemSameTags(a, b); }
    public boolean matches(ItemStack stack) { return same(input, stack); }
    public boolean accepts(ItemStack stack) { return matches(stack) || dual() && same(secondInput, stack); }
    public int countFor(ItemStack stack) { return matches(stack) ? inputCount : same(secondInput, stack) ? secondCount : 0; }
    public boolean matchesPair(ItemStack first, ItemStack second) {
        return dual() && (matches(first) && same(secondInput, second) || matches(second) && same(secondInput, first));
    }
    public boolean ready(ItemStack first, ItemStack second) {
        if (!dual()) return matches(first) && first.getCount() >= inputCount;
        return matchesPair(first, second) && first.getCount() >= countFor(first) && second.getCount() >= countFor(second);
    }
    public boolean boundTo(String binding) { return binding.isEmpty() || Seasoning.identity(Seasoning.components(output)).equals(binding); }
    public ItemStack result() { return output.copyWithCount(outputCount); }
    public ItemStack returnedContainers() { return containers(container, inputCount); }
    public ItemStack secondReturnedContainers() { return containers(secondContainer, secondCount); }
    private static ItemStack containers(ItemStack item, int count) { return item.isEmpty() ? ItemStack.EMPTY : item.copyWithCount(item.getCount() * count); }
}
