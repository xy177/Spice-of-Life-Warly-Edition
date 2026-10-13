package com.xy177.solwarly.lunch;

import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.api.FlavorAPI;
import com.xy177.solwarly.diet.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.items.ItemHandlerHelper;
import java.util.*;

public final class ItemLunchContainer extends Item {
    public final int slots;
    public ItemLunchContainer(int slots) { super(new Properties().stacksTo(1));this.slots=slots; }
    @Override public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.EAT; }
    @Override public int getUseDuration(ItemStack stack) { return 32; }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if(player.isShiftKeyDown()) {
            if(player instanceof ServerPlayer server) {
                int slot=hand==InteractionHand.MAIN_HAND?player.getInventory().selected:40;
                NetworkHooks.openScreen(server,new SimpleMenuProvider((id,inventory,p)->new ContainerLunch(id,p,slot),stack.getHoverName()),buffer->buffer.writeVarInt(slot));
            }
            return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);
        }
        if(player.canEat(false)&&bestFood(new LunchInventory(stack),player)>=0) { player.startUsingItem(hand);return InteractionResultHolder.consume(stack); }
        return InteractionResultHolder.fail(stack);
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity) {
        if(level.isClientSide||!(entity instanceof Player player)||!player.canEat(false))return stack;
        LunchInventory inventory=new LunchInventory(stack);int slot=bestFood(inventory,player);if(slot<0)return stack;
        ItemStack food=inventory.extractItem(slot,1,false),original=food.copy();
        ItemStack result=food.finishUsingItem(level,player);
        result=net.minecraftforge.event.ForgeEventFactory.onItemUseFinish(player,original,0,result);
        if(!result.isEmpty()&&LunchInventory.canHold(result))result=ItemHandlerHelper.insertItemStacked(inventory,result,false);
        if(!result.isEmpty()&&!player.getInventory().add(result))player.drop(result,false);
        player.getInventory().setChanged();return stack;
    }
    public static int bestFood(LunchInventory inventory,Player player) {
        if(!player.level().isClientSide)DietRuntime.initialize(player);
        DietData diet=DietCapability.get(player);if(diet==null)return -1;
        boolean client=player.level().isClientSide;int count=client?DietRuntime.clientRecentCount:DietConfig.recentCount;
        double extra=client?DietRuntime.clientExtraFlavor:DietConfig.extraFlavor,scale=client?DietRuntime.clientScale:DietConfig.scale;
        var appetite=DietRuntime.appetite(player.level());int selected=-1;double best=-Double.MAX_VALUE;
        for(int slot=0;slot<inventory.getSlots();slot++) {
            ItemStack food=inventory.getStackInSlot(slot);if(!LunchInventory.canHold(food))continue;
            double score=diet.diversity(extra,scale);
            if(SOLWarlyConfig.shouldCount(food.getItem())) {
                String id=FoodCatalog.id(food);var flavor=FlavorAPI.resolve(food);int state=appetite.state(id);
                double base=client?DietRuntime.clientCoefficients.getOrDefault(id,0.0):FoodCatalog.coefficient(food);
                double interest=client?(state>0?DietRuntime.clientCravingMultiplier:state<0?DietRuntime.clientBoringMultiplier:DietRuntime.clientNormalMultiplier):appetite.multiplier(id);
                score=diet.diversityAfter(id,flavor.id,DietRuntime.contribution(player,food,base*interest*flavor.diversityMultiplier*DietRuntime.warlyMultiplier(player,food)),count,extra,scale);
            }
            if(score>best) { best=score;selected=slot; }
        }
        return selected;
    }
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> tooltip,TooltipFlag flag) {
        for(Component line:List.of(Component.translatable("solwarly.lunch.capacity",slots),Component.translatable("solwarly.lunch.open"),Component.translatable("solwarly.lunch.eat")))tooltip.add(line.copy().withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
