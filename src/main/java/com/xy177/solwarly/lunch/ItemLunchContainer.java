package com.xy177.solwarly.lunch;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.api.FlavorAPI;
import com.xy177.solwarly.diet.*;
import com.xy177.solwarly.tracking.FoodList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.*;
import net.minecraftforge.items.ItemHandlerHelper;
import java.util.List;
import java.util.function.ToDoubleFunction;

/** Apple Pie-style food containers, adapted to Warly's meal and flavor scoring. */
public final class ItemLunchContainer extends Item {
    public static final int GUI_ID = 2;
    public final int slots;

    public ItemLunchContainer(String name, int slots) {
        this.slots = slots;
        setRegistryName(SOLWarly.MOD_ID, name);
        setTranslationKey(SOLWarly.MOD_ID + "." + name);
        setMaxStackSize(1);
        setCreativeTab(CreativeTabs.MISC);
    }

    @Override public EnumAction getItemUseAction(ItemStack stack) { return EnumAction.EAT; }
    @Override public int getMaxItemUseDuration(ItemStack stack) { return 32; }

    @Override public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            if (!world.isRemote) player.openGui(SOLWarly.instance, GUI_ID, world,
                hand == EnumHand.MAIN_HAND ? player.inventory.currentItem : 40, 0, 0);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (player.canEat(false) && bestFood(new LunchInventory(stack), player) >= 0) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
        if (world.isRemote || !(entity instanceof EntityPlayer)) return stack;
        EntityPlayer player = (EntityPlayer) entity;
        if (!player.canEat(false)) return stack;
        LunchInventory inventory = new LunchInventory(stack);
        int slot = bestFood(inventory, player);
        if (slot < 0) return stack;
        consume(inventory, slot, world, player);
        player.inventory.markDirty();
        return stack;
    }

    static void consume(LunchInventory inventory, int slot, World world, EntityPlayer player) {
        ItemStack food = inventory.extractItem(slot, 1, false);
        ItemStack original = food.copy();
        ItemStack result = food.onItemUseFinish(world, player);
        // FoodStats/AppleCore already account for the actual food; only forward Forge's item-finish event here.
        result = ForgeEventFactory.onItemUseFinish(player, original, 0, result);
        if (result.isEmpty()) return;
        if (LunchInventory.canHold(result)) result = ItemHandlerHelper.insertItemStacked(inventory, result, false);
        if (!result.isEmpty() && !player.inventory.addItemStackToInventory(result)) player.dropItem(result, false);
    }

    public static int bestFood(LunchInventory inventory, EntityPlayer player) {
        if (!player.world.isRemote) DietRuntime.initialize(player);
        DietData diet = DietCapability.get(player);
        boolean client = player.world.isRemote;
        int count = client ? DietRuntime.clientRecentCount : DietConfig.recentCount;
        double extra = client ? DietRuntime.clientExtraFlavor : DietConfig.extraFlavor;
        double scale = client ? DietRuntime.clientScale : DietConfig.scale;
        AppetiteData appetite = DietRuntime.appetite(player.world);
        return select(inventory, food -> {
            if (!FoodList.get(player).getConfigInfo().shouldCount(food)) return diet.diversity(extra, scale);
            String id = FoodCatalog.id(food);
            FlavorAPI.Flavor flavor = FlavorAPI.resolve(food);
            double base = client ? DietRuntime.clientCoefficients.getOrDefault(id, 0.0) : FoodCatalog.coefficient(food);
            int state = appetite.state(id);
            double interest = client ? state > 0 ? DietRuntime.clientCravingMultiplier
                : state < 0 ? DietRuntime.clientBoringMultiplier : DietRuntime.clientNormalMultiplier : appetite.multiplier(id);
            return diet.diversityAfter(id, flavor.id, DietRuntime.contribution(player, food, base * interest * flavor.diversityMultiplier
                * DietRuntime.warlyMultiplier(player, food)), count, extra, scale);
        });
    }

    static int select(LunchInventory inventory, ToDoubleFunction<ItemStack> score) {
        int selected = -1;
        double highest = -Double.MAX_VALUE;
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack food = inventory.getStackInSlot(slot);
            if (!LunchInventory.canHold(food)) continue;
            double value = score.applyAsDouble(food);
            if (value > highest) { selected = slot; highest = value; }
        }
        return selected;
    }

    @Override @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.GRAY + I18n.format("solwarly.lunch.capacity", slots));
        tooltip.add(TextFormatting.GRAY + I18n.format("solwarly.lunch.open"));
        tooltip.add(TextFormatting.GRAY + I18n.format("solwarly.lunch.eat"));
    }
}
