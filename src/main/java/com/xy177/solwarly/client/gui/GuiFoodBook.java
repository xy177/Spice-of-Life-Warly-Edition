// Adapted from Spice of Life: Apple Pie Edition (2d9de8b8d045b1f9b93940b681c74021d326f207); LGPL-2.1.
// Modified by xy177 on 2026-10-27 for Warly Edition.
// Upstream: https://github.com/txnimc/Spice-of-Life-Apple-Pie/blob/2d9de8b8d045b1f9b93940b681c74021d326f207/src/main/java/com/tarinoita/solsweetpotato/client/gui/FoodBookScreen.java
// Earlier shared ancestry and notices are recorded in THIRD_PARTY_NOTICES.md.
package com.xy177.solwarly.client.gui;

import com.xy177.solwarly.SOLWarly;
import com.xy177.solwarly.client.gui.elements.*;
import com.xy177.solwarly.tracking.FoodList;
import com.xy177.solwarly.diet.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.xy177.solwarly.lib.Localization.localized;

@SideOnly(Side.CLIENT)
public final class GuiFoodBook extends GuiScreen {
	private static final ResourceLocation texture = SOLWarly.resourceLocation("textures/gui/food_book.png");
	private static final UIImage.Image bookImage = new UIImage.Image(texture, new Rectangle(0, 0, 186, 192));
	static final UIImage.Image spiderEyeImage = new UIImage.Image(texture, new Rectangle(16, 240, 16, 16));
	static final UIImage.Image heartImage = new UIImage.Image(texture, new Rectangle(0, 224, 15, 15));
	static final UIImage.Image drumstickImage = new UIImage.Image(texture, new Rectangle(16, 224, 15, 15));
	static final UIImage.Image blacklistImage = new UIImage.Image(texture, new Rectangle(32, 224, 15, 15));
	static final UIImage.Image whitelistImage = new UIImage.Image(texture, new Rectangle(48, 224, 15, 15));

	static final Color fullBlack = Color.BLACK;
	static final Color lessBlack = new Color(0, 0, 0, 128);
	static final Color leastBlack = new Color(0, 0, 0, 64);

	private final List<UIElement> elements = new ArrayList<>();
	private UIImage background;
	private UILabel pageNumberLabel;

	private PageFlipButton nextPageButton;
	private PageFlipButton prevPageButton;

	private EntityPlayer player;
	private FoodData foodData;

	private final List<Page> pages = new ArrayList<>();
	private int currentPageNumber = 0;
    private long revision;
    private final java.util.Set<Page> clickablePages = new java.util.HashSet<>();
    private final List<Page> detailPages = new ArrayList<>();
    private int returnPage;
    private GuiButton backButton;
    private List<Page> visiblePages() { return detailPages.isEmpty() ? pages : detailPages; }

	public GuiFoodBook(EntityPlayer player) {
		this.player = player;
	}

	@Override
	public void initGui() {
		super.initGui();

		foodData = new FoodData(FoodList.get(player), DietCapability.get(player));

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

		buttonList.clear();

		int pageFlipButtonSpacing = 50;
		prevPageButton = addButton(new PageFlipButton(1, background.getCenterX() - pageFlipButtonSpacing / 2 - PageFlipButton.width, background.getMinY() + 152, PageFlipButton.Direction.BACKWARD));
		nextPageButton = addButton(new PageFlipButton(2, background.getCenterX() + pageFlipButtonSpacing / 2, background.getMinY() + 152, PageFlipButton.Direction.FORWARD));

		backButton = addButton(new GuiButton(3, background.getCenterX() - 45, background.getMinY() + 169, 90, 18,
            net.minecraft.client.resources.I18n.format("solwarly.book.back")));
        updateButtonVisibility();
	}

	private void initPages() {
        revision = DietRuntime.clientRevision;
		pages.clear();
        clickablePages.clear(); detailPages.clear();

		pages.addAll(DietPage.statistics(background.frame, DietCapability.get(player)));
		addDailyFoods("solwarly.book.craved", DietRuntime.clientAppetite.cravedFoods());
		addDailyFoods("solwarly.book.boring", DietRuntime.clientAppetite.boringFoods());

		pages.add(new ConfigInfoPage(foodData, background.frame));
        pages.addAll(GrinderGuidePage.pages(background.frame));

		int first = pages.size();
        addPages("eaten_foods", foodData.eatenFoods);
        clickablePages.addAll(pages.subList(first, pages.size()));
        String completeTitle = net.minecraft.client.resources.I18n.format("solwarly.book.all_flavors", foodData.completeFoods.size());
        if (foodData.completeFoods.isEmpty()) pages.add(new DietPage(background.frame, completeTitle, java.util.Collections.singletonList(net.minecraft.client.resources.I18n.format("solwarly.book.none"))));
        else pages.addAll(ItemListPage.pages(background.frame, completeTitle, foodData.completeFoods));

		if (com.xy177.solwarly.diet.FoodRules.current().shouldShowUneatenFoods) {
			addPages("uneaten_foods", foodData.uneatenFoods);
		}
        currentPageNumber = Math.max(0, Math.min(currentPageNumber, pages.size() - 1));
        pageNumberLabel.text = Integer.toString(currentPageNumber + 1);
    }
    @Override
    public void updateScreen() {
        super.updateScreen();
        if (revision == DietRuntime.clientRevision) return;
        foodData = new FoodData(FoodList.get(player), DietCapability.get(player));
        initPages();
        currentPageNumber = Math.min(currentPageNumber, pages.size() - 1);
        pageNumberLabel.text = "" + (currentPageNumber + 1);
        updateButtonVisibility();
    }
	private void addDailyFoods(String key, java.util.Set<String> ids) {
		List<ItemStack> foods = new ArrayList<>();
		for (String id : ids) {
			ItemStack food = FoodCatalog.stack(id);
			if (!food.isEmpty() && com.xy177.solwarly.diet.FoodRules.current().shouldCount(food)) foods.add(food);
		}
		String title = net.minecraft.client.resources.I18n.format(key);
		if (foods.isEmpty()) pages.add(new DietPage(background.frame, title, java.util.Collections.singletonList(net.minecraft.client.resources.I18n.format("solwarly.book.none"))));
		else pages.addAll(ItemListPage.pages(background.frame, title, foods));
	}

	private void addPages(String headerLocalizationPath, List<ItemStack> items) {
		String header = localized("gui", "food_book." + headerLocalizationPath, items.size());
		pages.addAll(ItemListPage.pages(background.frame, header, items));
	}

	private void updateButtonVisibility() {
		nextPageButton.visible = currentPageNumber < visiblePages().size() - 1;
        if (backButton != null) backButton.visible = !detailPages.isEmpty();
		prevPageButton.visible = currentPageNumber > 0;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		UIElement.render(background, mouseX, mouseY);

		super.drawScreen(mouseX, mouseY, partialTicks);

		// current page
		UIElement.render(elements, mouseX, mouseY);
		UIElement.render(visiblePages().get(currentPageNumber), mouseX, mouseY);
	}

    @Override protected void mouseClicked(int mouseX, int mouseY, int button) throws java.io.IOException {
        super.mouseClicked(mouseX, mouseY, button);
        if (button != 0 || !detailPages.isEmpty()) return;
        Page page = pages.get(currentPageNumber);
        if (!clickablePages.contains(page) || !(page instanceof ItemListPage)) return;
        ItemStack food = ((ItemListPage) page).itemAt(mouseX, mouseY);
        if (food.isEmpty()) return;
        List<ItemStack> missing = foodData.missingFlavors(food);
        String title = net.minecraft.client.resources.I18n.format("solwarly.book.missing_flavors");
        detailPages.addAll(ItemListPage.pages(background.frame, title, missing));
        if (detailPages.isEmpty()) detailPages.add(new DietPage(background.frame, title,
            java.util.Collections.singletonList(net.minecraft.client.resources.I18n.format("solwarly.book.all_flavors_done"))));
        returnPage = currentPageNumber; currentPageNumber = 0;
        pageNumberLabel.text = "1"; updateButtonVisibility();
    }

	@Override
	protected void actionPerformed(GuiButton button) {
		if (!button.enabled) return;

		if (button == backButton) {
            detailPages.clear(); currentPageNumber = returnPage; updateButtonVisibility();
        } else if (button == prevPageButton) {
			currentPageNumber--;
			updateButtonVisibility();
		} else if (button == nextPageButton) {
			currentPageNumber++;
			updateButtonVisibility();
		}

		pageNumberLabel.text = "" + (currentPageNumber + 1);
	}
}
