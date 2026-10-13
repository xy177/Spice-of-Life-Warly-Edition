package com.xy177.solwarly.compat.jei;

import com.xy177.solwarly.spice.*;
import com.xy177.solwarly.lunch.*;
import mezz.jei.api.*;
import mezz.jei.api.constants.*;
import mezz.jei.api.gui.builder.*;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.*;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.*;

@JeiPlugin
public final class SpiceJeiPlugin implements IModPlugin {
    private static final RecipeType<SpiceRecipe> GRINDING=RecipeType.create("solwarly","grinding",SpiceRecipe.class);
    private IJeiRuntime runtime;
    private List<SpiceRecipe> grinding=List.of();
    private List<ItemStack> variants=List.of();
    private MixedSpiceRecipe mixing;
    @Override public ResourceLocation getPluginUid() { return new ResourceLocation("solwarly","spices"); }
    @Override public void registerItemSubtypes(ISubtypeRegistration registration) { registration.registerSubtypeInterpreter(SpiceRegistry.spice,(stack,context)->Seasoning.mixed(stack)?"mixed":ItemSpice.flavor(stack)); }
    @Override public void registerCategories(IRecipeCategoryRegistration registration) { registration.addRecipeCategories(new GrindingCategory(registration.getJeiHelpers().getGuiHelper())); }
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) { registration.addRecipeCatalyst(new ItemStack(SpiceRegistry.GRINDER.get()),GRINDING); }
    @Override public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        variants=powders();if(!variants.isEmpty())registration.addExtraItemStacks(variants);
    }
    @Override public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getCraftingCategory().addCategoryExtension(MixedSpiceRecipe.class,recipe->new MixingExtension());
        registration.getCraftingCategory().addCategoryExtension(LunchUpgradeRecipe.class,recipe->new UpgradeExtension());
    }
    @Override public void registerRecipes(IRecipeRegistration registration) {
        grinding=grinding();registration.addRecipes(GRINDING,grinding);
    }
    private static List<SpiceRecipe> grinding() {
        List<SpiceRecipe> result=new ArrayList<>();
        for(var recipe:SpiceRecipes.all())if(!CustomSpices.owns(recipe))result.add(recipe);
        result.addAll(CustomSpices.displayRecipes());return result;
    }
    private static List<ItemStack> powders() {
        return SpiceKind.all().stream().filter(kind->!kind.accompaniment()&&kind.available()).map(kind->Seasoning.material(kind)).toList();
    }
    @Override public void onRuntimeAvailable(IJeiRuntime runtime) { this.runtime=runtime;net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);refresh(); }
    @Override public void onRuntimeUnavailable() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(this);runtime=null; }
    @SubscribeEvent public void changed(CustomSpices.DefinitionsChanged event) { refresh(); }
    @SubscribeEvent public void changed(SpiceConfig.MixingRecipeChanged event) { refresh(); }
    private void refresh() {
        if(runtime==null)return;
        Minecraft.getInstance().execute(()-> {
            if(runtime==null)return;
            var manager=runtime.getRecipeManager();manager.hideRecipes(GRINDING,grinding);
            var level=Minecraft.getInstance().level;
            if(level!=null) {
                var actual=level.getRecipeManager().getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING).stream().filter(recipe->recipe instanceof MixedSpiceRecipe).toList();
                if(!actual.isEmpty())manager.hideRecipes(RecipeTypes.CRAFTING,actual);
            }
            if(mixing!=null)manager.hideRecipes(RecipeTypes.CRAFTING,List.of(mixing));
            if(!variants.isEmpty())runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK,variants);
            variants=powders();if(!variants.isEmpty())runtime.getIngredientManager().addIngredientsAtRuntime(VanillaTypes.ITEM_STACK,variants);
            grinding=grinding();manager.addRecipes(GRINDING,grinding);manager.unhideRecipes(GRINDING,grinding);
            mixing=null;if(!variants.isEmpty()&&!SpiceConfig.mixedCatalyst().isEmpty()) {
                mixing=new MixedSpiceRecipe(new ResourceLocation("solwarly","jei_mixing"),CraftingBookCategory.MISC);
                manager.addRecipes(RecipeTypes.CRAFTING,List.of(mixing));
            }
        });
    }
    private static final class GrindingCategory implements IRecipeCategory<SpiceRecipe> {
        final IDrawable background,icon,arrow;
        GrindingCategory(IGuiHelper gui) {
            background=gui.createBlankDrawable(150,46);icon=gui.createDrawableItemStack(new ItemStack(SpiceRegistry.GRINDER.get()));
            arrow=gui.createDrawable(new ResourceLocation("minecraft","textures/gui/container/furnace.png"),79,34,24,17);
        }
        @Override public RecipeType<SpiceRecipe> getRecipeType() { return GRINDING; }
        @Override public Component getTitle() { return Component.translatable("block.solwarly.spice_grinder"); }
        @Override public IDrawable getBackground() { return background; }
        @Override public IDrawable getIcon() { return icon; }
        private int inputX(SpiceRecipe recipe) {
            int count = (recipe.dual() ? 2 : 1) + 1 + (recipe.returnedContainers().isEmpty() ? 0 : 1) + (recipe.secondReturnedContainers().isEmpty() ? 0 : 1);
            return (150 - (count * 22 + 28)) / 2;
        }
        @Override public void draw(SpiceRecipe recipe,IRecipeSlotsView slots,GuiGraphics graphics,double x,double y) { arrow.draw(graphics,inputX(recipe)+(recipe.dual()?2:1)*22+2,15); }
        @Override public void setRecipe(IRecipeLayoutBuilder builder,SpiceRecipe recipe,IFocusGroup focuses) {
            ItemStack input=recipe.input.copy();input.setCount(recipe.inputCount);
            int x=inputX(recipe);
            builder.addInputSlot(x+1,15).setStandardSlotBackground().addItemStack(input);
            if (recipe.dual()) builder.addInputSlot(x+23,15).setStandardSlotBackground().addItemStack(recipe.secondInput.copyWithCount(recipe.secondCount));
            int outputX = x + (recipe.dual()?2:1)*22 + 33;
            builder.addOutputSlot(outputX,15).setStandardSlotBackground().addItemStack(recipe.result());
            var containers=List.of(recipe.returnedContainers(),recipe.secondReturnedContainers()).stream().filter(stack->!stack.isEmpty()).toList();
            for(int i=0;i<containers.size();i++)builder.addOutputSlot(outputX+(i+1)*22,15).setStandardSlotBackground().addItemStack(containers.get(i));
        }
    }
    /** One workbench entry, with whole recipes randomly chosen and displayed together. */
    private static final class MixingExtension implements ICraftingCategoryExtension {
        private final List<List<ItemStack>> inputs=new ArrayList<>();
        private final List<ItemStack> outputs=new ArrayList<>();
        private final Random random=new Random();
        private List<IRecipeSlotDrawable> slots=List.of();
        private List<Integer> choices=List.of();
        private long lastTime=-1,elapsed,step=-1;
        private int displayed;
        private void rebuild() {
            inputs.clear();outputs.clear();step=-1;lastTime=-1;elapsed=0;
            var kinds=SpiceKind.all().stream().filter(kind->kind != SpiceKind.CREATIVE&&!kind.accompaniment()&&kind.available()).toList();
            for (var row : MixingPreview.combinations(kinds)) add(row);
        }
        private void add(List<SpiceKind> row) {
            inputs.add(row.stream().map(Seasoning::material).toList());
            outputs.add(Seasoning.mixedPowder(row,SpiceConfig.mixedCount));
        }
        @Override public void setRecipe(IRecipeLayoutBuilder builder,ICraftingGridHelper grid,IFocusGroup focuses) {
            rebuild();
            focuses.getItemStackFocuses().filter(focus->focus.getRole()==RecipeIngredientRole.OUTPUT).forEach(focus->{
                ItemStack target=focus.getTypedValue().getIngredient(); var composition=Seasoning.components(target);
                List<SpiceKind> row=new ArrayList<>();
                for(var kind:new LinkedHashSet<>(composition))for(int j=0;j<Seasoning.amount(composition,kind)&&row.size()<=8;j++)row.add(kind);
                if(Seasoning.mixed(target)&&row.size()==SpiceConfig.mixedIngredients)add(row);
            });
            choices=new ArrayList<>();
            for(int i=0;i<outputs.size();i++) {
                int index=i;
                if(focuses.getItemStackFocuses().allMatch(focus-> {
                    var value=focus.getTypedValue().getIngredient();
                    return focus.getRole()==RecipeIngredientRole.OUTPUT?same(outputs.get(index),value)
                        :ItemStack.isSameItemSameTags(SpiceConfig.mixedCatalyst(),value)||inputs.get(index).stream().anyMatch(input->same(input,value));
                }))choices.add(i);
            }
            if(choices.isEmpty())for(int i=0;i<outputs.size();i++)choices.add(i);
            for(int i=0;i<SpiceConfig.mixedIngredients;i++) {
                int slot=i;builder.addInputSlot(1+18*(MixingPreview.gridSlot(i)%3),1+18*(MixingPreview.gridSlot(i)/3)).setSlotName("powder"+i).addItemStacks(inputs.stream().map(row->row.get(slot)).toList());
            }
            builder.addInputSlot(1+18*(MixingPreview.catalystSlot()%3),1+18*(MixingPreview.catalystSlot()/3)).setSlotName("catalyst").addItemStack(SpiceConfig.mixedCatalyst())
                .addTooltipCallback((view,tooltip)->{if(!SpiceConfig.consumeMixedCatalyst)tooltip.add(Component.translatable("solwarly.jei.not_consumed"));});
            builder.addOutputSlot(95,19).setSlotName("result").addItemStacks(outputs);builder.setShapeless();
        }
        @Override public void onDisplayedIngredientsUpdate(List<IRecipeSlotDrawable> slots,IFocusGroup focuses) { this.slots=slots;display(); }
        private static boolean same(ItemStack first,ItemStack second) {
            return first.getItem()==SpiceRegistry.spice && second.getItem()==SpiceRegistry.spice
                ? Seasoning.mixed(first)==Seasoning.mixed(second) && Seasoning.identity(Seasoning.components(first)).equals(Seasoning.identity(Seasoning.components(second)))
                : ItemStack.isSameItemSameTags(first,second);
        }
        private void display() {
            if(choices.isEmpty())return;
            long time=System.nanoTime()/1_000_000;
            if(lastTime>=0&&!Screen.hasShiftDown())elapsed+=Math.max(0,time-lastTime);lastTime=time;
            if(step!=elapsed/1000) { step=elapsed/1000;displayed=choices.get(random.nextInt(choices.size())); }
            for(var slot:slots) {
                String name=slot.getSlotName().orElse("");ItemStack value;
                if(name.startsWith("powder"))value=inputs.get(displayed).get(Integer.parseInt(name.substring(6)));
                else if(name.equals("result"))value=outputs.get(displayed);else continue;
                slot.createDisplayOverrides().addItemStack(value);
            }
        }
        @Override public void drawInfo(int width,int height,GuiGraphics graphics,double x,double y) { display(); }
    }
    private static final class UpgradeExtension implements ICraftingCategoryExtension {
        @Override public void setRecipe(IRecipeLayoutBuilder builder,ICraftingGridHelper grid,IFocusGroup focuses) {
            List<List<ItemStack>> inputs=new ArrayList<>();
            for(int i=0;i<9;i++)inputs.add(i==0||i==2?List.of():i==4?List.of(new ItemStack(LunchRegistry.BAG.get()),new ItemStack(LunchRegistry.BOX.get())):List.of(new ItemStack(Items.IRON_INGOT),new ItemStack(Items.GOLD_INGOT)));
            var slots=grid.createAndSetInputs(builder,inputs,3,3);
            var output=grid.createAndSetOutputs(builder,List.of(new ItemStack(LunchRegistry.BOX.get()),new ItemStack(LunchRegistry.GOLD.get())));
            List<IIngredientAcceptor<?>> linked=new ArrayList<>();
            for(int i=0;i<slots.size();i++)if(!inputs.get(i).isEmpty())linked.add(slots.get(i));
            linked.add(output);builder.createFocusLink(linked.toArray(IIngredientAcceptor[]::new));
        }
    }
}
