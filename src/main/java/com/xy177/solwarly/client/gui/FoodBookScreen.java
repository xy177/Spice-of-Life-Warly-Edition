// Derived from Spice of Life: Carrot Edition; original contributors retain copyright.
// Modified by xy177 on 2026-10-13 for Spice of Life Warly Edition.
// Upstream source: https://github.com/Cazsius/Spice-of-Life-Carrot-Edition/blob/412fa94a07d93f09378fc0e3393de38eb0099824/src/main/java/com/cazsius/solcarrot/client/gui/FoodBookScreen.java
// License: LGPL-2.1; see LICENSE.txt and THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.SOLWarlyConfig;
import com.xy177.solwarly.client.gui.elements.ImageData;
import com.xy177.solwarly.client.gui.elements.UIElement;
import com.xy177.solwarly.client.gui.elements.UIImage;
import com.xy177.solwarly.client.gui.elements.UILabel;
import com.xy177.solwarly.tracking.FoodList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.xy177.solwarly.lib.Localization.localized;

@OnlyIn(Dist.CLIENT)
public final class FoodBookScreen extends Screen implements PageFlipButton.Pageable {
	private static final ResourceLocation texture = SOLWarly.resourceLocation("textures/gui/food_book.png");
	private static final ImageData bookImage = new ImageData(texture,
		new Rectangle(0, 0, 186, 192)
	);
	static final ImageData carrotImage = new ImageData(texture,
		new Rectangle(0, 240, 16, 16),
		14, 14
	);
	static final ImageData spiderEyeImage = new ImageData(texture,
		new Rectangle(16, 240, 16, 16),
		8, 8
	);
	static final ImageData heartImage = new ImageData(texture,
		new Rectangle(0, 224, 15, 15),
		9, 9
	);
	static final ImageData drumstickImage = new ImageData(texture,
		new Rectangle(16, 224, 15, 15),
		9, 9
	);
	static final ImageData blacklistImage = new ImageData(texture,
		new Rectangle(32, 224, 15, 15),
		10, 12
	);
	static final ImageData whitelistImage = new ImageData(texture,
		new Rectangle(48, 224, 15, 15),
		10, 12
	);
	
	static final Color fullBlack = Color.BLACK;
	static final Color lessBlack = new Color(0, 0, 0, 128);
	static final Color leastBlack = new Color(0, 0, 0, 64);
	
	private final List<UIElement> elements = new ArrayList<>();
	private UIImage background;
	private UILabel pageNumberLabel;
	
	private PageFlipButton nextPageButton;
	private PageFlipButton prevPageButton;
	
	private Player player;
	private FoodData foodData;
	
	private final List<Page> pages = new ArrayList<>();
	private int currentPageNumber = 0;
    private long revision;
    private final java.util.Set<Page> clickablePages=new java.util.HashSet<>();
    private final List<Page> detailPages=new ArrayList<>();
    private int returnPage;
    private net.minecraft.client.gui.components.Button backButton;
    private List<Page> visiblePages() { return detailPages.isEmpty()?pages:detailPages; }
	
	public static void open(Player player) {
		Minecraft.getInstance().setScreen(new FoodBookScreen(player));
	}
	
	public FoodBookScreen(Player player) {
		super(Component.empty());
		this.player = player;
	}
	
	@Override
	public void init() {
		super.init();
		
		foodData = new FoodData(FoodList.get(player),com.xy177.solwarly.diet.DietCapability.get(player));
		
		background = new UIImage(bookImage);
		background.setCenterX(width / 2);
		background.setCenterY(height / 2);
		
		elements.clear();
		
		// page number
		pageNumberLabel = new UILabel("1");
		pageNumberLabel.setCenterX(background.getCenterX());
		pageNumberLabel.setMinY(background.getMinY() + 156);
		elements.add(pageNumberLabel);
		
		initPages();
		
		int pageFlipButtonSpacing = 50;
		prevPageButton = addRenderableWidget(new PageFlipButton(
			background.getCenterX() - pageFlipButtonSpacing / 2 - PageFlipButton.width,
			background.getMinY() + 152,
			PageFlipButton.Direction.BACKWARD,
			this
		));
		nextPageButton = addRenderableWidget(new PageFlipButton(
			background.getCenterX() + pageFlipButtonSpacing / 2,
			background.getMinY() + 152,
			PageFlipButton.Direction.FORWARD,
			this
		));
		
        backButton=addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.translatable("solwarly.book.back"),button->{detailPages.clear();switchToPage(returnPage);}).bounds(background.getCenterX()-45,background.getMinY()+169,90,18).build());
        updateButtonVisibility();
    }

    private void initPages() {
        revision=com.xy177.solwarly.diet.DietRuntime.clientRevision;
        pages.clear();clickablePages.clear();detailPages.clear();
        pages.addAll(DietPage.statistics(background.frame,com.xy177.solwarly.diet.DietCapability.get(player)));
        addDailyFoods("solwarly.book.craved",com.xy177.solwarly.diet.DietRuntime.clientAppetite.cravedFoods());
        addDailyFoods("solwarly.book.boring",com.xy177.solwarly.diet.DietRuntime.clientAppetite.boringFoods());
        pages.addAll(GrinderGuidePage.pages(background.frame));
        pages.add(new ConfigInfoPage(foodData,background.frame));
        int first=pages.size();addPages("eaten_foods",foodData.eatenFoods);clickablePages.addAll(pages.subList(first,pages.size()));
        String title=net.minecraft.client.resources.language.I18n.get("solwarly.book.all_flavors",foodData.completeFoods.size());
        if(foodData.completeFoods.isEmpty())pages.add(new DietPage(background.frame,title,List.of(net.minecraft.client.resources.language.I18n.get("solwarly.book.none"))));
        else pages.addAll(ItemListPage.pages(background.frame,title,foodData.completeFoods));
        if(SOLWarlyConfig.shouldShowUneatenFoods())addPages("uneaten_foods",foodData.uneatenFoods);
    }
    private void addDailyFoods(String key,java.util.Set<String> ids) {
        List<ItemStack> stacks=ids.stream().map(com.xy177.solwarly.diet.FoodCatalog::stack).filter(stack->!stack.isEmpty()&&SOLWarlyConfig.shouldCount(stack.getItem())).toList();
        String title=net.minecraft.client.resources.language.I18n.get(key);
        if(stacks.isEmpty())pages.add(new DietPage(background.frame,title,List.of(net.minecraft.client.resources.language.I18n.get("solwarly.book.none"))));
        else pages.addAll(ItemListPage.pages(background.frame,title,stacks));
    }
    private void addPages(String path,List<ItemStack> stacks) {
        pages.addAll(ItemListPage.pages(background.frame,localized("gui","food_book."+path,stacks.size()),stacks));
    }
    @Override public void tick() {
        super.tick();if(revision==com.xy177.solwarly.diet.DietRuntime.clientRevision)return;
        foodData=new FoodData(FoodList.get(player),com.xy177.solwarly.diet.DietCapability.get(player));
        initPages();switchToPage(Math.min(currentPageNumber,pages.size()-1));
    }
    @Override public boolean mouseClicked(double x,double y,int button) {
        if(super.mouseClicked(x,y,button))return true;
        if(button!=0||!detailPages.isEmpty()||pages.isEmpty())return false;
        Page page=visiblePages().get(currentPageNumber);if(!clickablePages.contains(page)||!(page instanceof ItemListPage list))return false;
        ItemStack food=list.itemAt((int)x,(int)y);if(food.isEmpty())return false;
        var missing=foodData.missingFlavors(food);String title=net.minecraft.client.resources.language.I18n.get("solwarly.book.missing_flavors");
        detailPages.addAll(ItemListPage.pages(background.frame,title,missing));
        if(detailPages.isEmpty())detailPages.add(new DietPage(background.frame,title,List.of(net.minecraft.client.resources.language.I18n.get("solwarly.book.all_flavors_done"))));
        returnPage=currentPageNumber;switchToPage(0);return true;
    }

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics,mouseX,mouseY,partialTicks);
		
		UIElement.render(graphics, background, mouseX, mouseY);
		
		super.render(graphics, mouseX, mouseY, partialTicks);
		
		if (!pages.isEmpty()) { // might not be loaded yet; race condition
			// current page
			UIElement.render(graphics, elements, mouseX, mouseY);
			UIElement.render(graphics, visiblePages().get(currentPageNumber), mouseX, mouseY);
		}
	}
	
	@Override
	public void switchToPage(int pageNumber) {
		if (!isWithinRange(pageNumber)) return;
		
		currentPageNumber = pageNumber;
		updateButtonVisibility();
		
		pageNumberLabel.text = "" + (currentPageNumber + 1);
	}
	
	@Override
	public int getCurrentPageNumber() {
		return currentPageNumber;
	}
	
	@Override
	public boolean isWithinRange(int pageNumber) {
		return pageNumber >= 0 && pageNumber < visiblePages().size();
	}
	
	private void updateButtonVisibility() {
		prevPageButton.updateState();
		nextPageButton.updateState();
        if(backButton!=null)backButton.visible=!detailPages.isEmpty();
	}
}
