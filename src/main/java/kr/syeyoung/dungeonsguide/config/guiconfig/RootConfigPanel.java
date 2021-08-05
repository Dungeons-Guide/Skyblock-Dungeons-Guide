/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.config.guiconfig;

import com.google.common.base.Function;
import kr.syeyoung.dungeonsguide.config.guiconfig.location.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.*;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class RootConfigPanel extends MPanelScaledGUI {
    private MScrollablePanel navigationScroll;

    private MList navigation = new MList();

    private MScrollablePanel contentScroll;


    private final Map<String, MPanel> pages = new HashMap<String, MPanel>();
    @Getter
    @Setter
    private Function<String, MPanel> pageGenerator = ConfigPanelCreator.INSTANCE;
    @Getter
    private String currentPage = "";

    private GuiConfigV2 gui;

    private long lastPageSet = System.currentTimeMillis();

    private MTextField search;
    private MButton guiRelocate;

    private MButton github, discord;

    private final Stack<String> history = new Stack<String>();

    public String getSearchWord() {
        return search.getText().trim().toLowerCase();
    }

    public RootConfigPanel(GuiConfigV2 guiConfigV2) {
        this.gui = guiConfigV2;

        search = new MTextField() {
            @Override
            public void edit(String str) {
                setupNavigation();

                setCurrentPageAndPushHistory("");
                if (!categoryMap.containsKey(lastOpenCategory)) {
                    for (Map.Entry<NestedCategory, MPanel> nestedCategoryMPanelEntry : categoryMap.entrySet()) {
                        if (nestedCategoryMPanelEntry.getValue() instanceof MCategoryElement) {
                            setCurrentPageAndPushHistory(nestedCategoryMPanelEntry.getKey().categoryFull());
                            lastOpenCategory = nestedCategoryMPanelEntry.getKey();
                            break;
                        }
                    }
                }
                    for (Map.Entry<NestedCategory, MPanel> nestedCategoryMPanelEntry : categoryMap.entrySet()) {
                        if (nestedCategoryMPanelEntry.getValue() instanceof MCollapsable) {
                            ((MCollapsable) nestedCategoryMPanelEntry.getValue()).setCollapsed(false);
                        }
                    }
                rePlaceElements();
            }
        };
        search.setPlaceHolder("Search...");
        add(search);
        guiRelocate = new MButton();
        guiRelocate.setText("Edit Gui Locations");
        guiRelocate.setOnActionPerformed(() -> {
            Minecraft.getMinecraft().displayGuiScreen(new GuiGuiLocationConfig(gui, null));
            guiRelocate.setIsclicked(false);
        });
        guiRelocate.setBorder(RenderUtils.blendTwoColors(0xFF141414,0x7702EE67));
        add(guiRelocate);

        discord = new MButton(); github = new MButton();
        discord.setText("Discord"); github.setText("Github");
        discord.setBorder(RenderUtils.blendTwoColors(0xFF141414,0x7702EE67));
        github.setBorder(RenderUtils.blendTwoColors(0xFF141414,0x7702EE67));
        github.setOnActionPerformed(() -> {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/Dungeons-Guide/Skyblock-Dungeons-Guide/"));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
        discord.setOnActionPerformed(() -> {
            try {
                Desktop.getDesktop().browse(new URI("https://discord.gg/VuxayCWGE8"));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        });
        add(discord); add(github);

        navigationScroll = new MScrollablePanel(1);
        navigationScroll.setHideScrollBarWhenNotNecessary(false);


        add(navigationScroll);
        navigationScroll.add(navigation);

        contentScroll = new MScrollablePanel(3);
        contentScroll.setHideScrollBarWhenNotNecessary(true);
        add(contentScroll);

        setupNavigation();
        navigation.setGap(0);
        navigation.setDrawLine(false);

        setCurrentPageAndPushHistory("ROOT");
        rePlaceElements();

        search.setFocused(true);
    }


    private Map<NestedCategory, MPanel> categoryMap = new HashMap<>();
    private NestedCategory lastOpenCategory;
    private void setupNavigation() {
        categoryMap.clear();
        for (MPanel childComponent : navigation.getChildComponents()) {
            navigation.remove(childComponent);
        }
        NestedCategory root = new NestedCategory("ROOT");
        Set<String> categoryAllowed = new HashSet<>();
        String search = this.search.getText().trim().toLowerCase();
        for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
            if (search.isEmpty()) {
                categoryAllowed.add("ROOT."+abstractFeature.getCategory()+".");
            } else if (abstractFeature.getName().toLowerCase().contains(search)) {
                categoryAllowed.add("ROOT."+abstractFeature.getCategory()+".");
            } else if (abstractFeature.getDescription().toLowerCase().contains(search)) {
                categoryAllowed.add("ROOT."+abstractFeature.getCategory()+".");
            }
        }
        for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
            String category = abstractFeature.getCategory();
            boolean test =false;
            for (String s : categoryAllowed) {
                if (s.startsWith("ROOT."+category+".")) {
                    test = true;
                    break;
                }
            }
            if (!test) continue;

            NestedCategory currentRoot = root;
            for (String s : category.split("\\.")) {
                NestedCategory finalCurrentRoot = currentRoot;
                if (currentRoot.children().containsKey(s))
                    currentRoot = currentRoot.children().get(s);
                else {
                    currentRoot.child(currentRoot = new NestedCategory(finalCurrentRoot.categoryFull()+"."+s));
                }
            }

        }

        for (NestedCategory value : root.children().values()) {
            setupNavigationRecursive(value, navigation, 0, 17);
        }
        ConfigPanelCreator.map.put("ROOT", () -> new MPanelCategory(root, this));
    }
    private void setupNavigationRecursive(NestedCategory nestedCategory, MPanel parent, int depth, int offset) {
        ConfigPanelCreator.map.put(nestedCategory.categoryFull(), () -> new MPanelCategory(nestedCategory, this));

        if (nestedCategory.children().size() == 0) {
            MCategoryElement current = new MCategoryElement(nestedCategory.categoryFull(),() -> {
                setCurrentPageAndPushHistory(nestedCategory.categoryFull());
                lastOpenCategory = nestedCategory;
            }, 13 * depth + 17, offset, this);
            parent.add(current);
            categoryMap.put(nestedCategory, current);
        } else {
            MCategoryElement current = new MCategoryElement(nestedCategory.categoryFull(),() -> {
                setCurrentPageAndPushHistory(nestedCategory.categoryFull());
                lastOpenCategory = nestedCategory;
            }, 3,offset, this);
            MCollapsable mCollapsable = new MCollapsable(current, this::rePlaceElements);
            mCollapsable.setLeftPad(offset-13);
            mCollapsable.getLowerElements().setDrawLine(false);
            mCollapsable.getLowerElements().setGap(0);
            mCollapsable.setLeftPadElements(0);
            parent.add(mCollapsable);
            categoryMap.put(nestedCategory, mCollapsable);

            for (NestedCategory value : nestedCategory.children().values()) {
                setupNavigationRecursive(value, mCollapsable, depth+1, offset+13);
            }
        }
    }


    public void setCurrentPageAndPushHistory(String currentPage) {
        if (!this.currentPage.equals(currentPage))
            history.push(this.currentPage);
        lastOpenCategory = null;
        this.currentPage = currentPage;
        setupPage();
    }
    public void goBack() {
        if (history.size() == 0) return;
        this.currentPage = history.pop();
        setupPage();
    }

    private void setupPage() {
        contentScroll.getContentArea().getChildComponents().forEach(contentScroll.getContentArea()::remove);
        if (!pages.containsKey(currentPage)) {
            MPanel page = pageGenerator.apply(currentPage);
            if (page == null) page = new MNotFound();
            pages.put(currentPage, page);
        }
        contentScroll.getContentArea().add(pages.get(currentPage));
        rePlaceElements();
    }

    public void invalidatePage(String page) {
        pages.remove(page);
        if (page.equals(currentPage))
            setupPage();
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        Dimension effectiveDim = getEffectiveDimension();
        Gui.drawRect(0,0, (int) (effectiveDim.width),  (int) (effectiveDim.height), RenderUtils.blendAlpha(0x141414, 0.00f));
        Gui.drawRect(0,0, (int) (effectiveDim.width), 25, RenderUtils.blendAlpha(0x0, 0.20f));
//        Gui.drawRect(navigationScroll.getBounds().x + navigationScroll.getBounds().width - 10, 25, navigationScroll.getBounds().x + navigationScroll.getBounds().width , 50, RenderUtils.blendAlpha(0xFF141414, 0.04f));
        Gui.drawRect(0, 25,navigationScroll.getBounds().x + navigationScroll.getBounds().width , 50, RenderUtils.blendAlpha(0xFF141414, 0.08f));


        FontRenderer fr  = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("DungeonsGuide by syeyoung", (effectiveDim.width - fr.getStringWidth("DungeonsGuide By syeyoung"))/2, (25 - fr.FONT_HEIGHT)/2, 0xFF02EE67);
    }

    @Override
    public void render0(double parentScale, Point parentPoint, Rectangle parentClip, int absMousex0, int absMousey0, int relMousex0, int relMousey0, float partialTicks) {
        super.render0(parentScale, parentPoint, parentClip, absMousex0, absMousey0, relMousex0, relMousey0, partialTicks);
        Dimension effectiveDim = getEffectiveDimension();
        Gui.drawRect(0,24, (int) (Double.min(1, (System.currentTimeMillis() - lastPageSet)/1000.0) * effectiveDim.width), 25, 0xFF02EE67);
    }

    @Override
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
        rePlaceElements();
    }

    @Override
    public void setScale(double scale) {
        super.setScale(scale);
        rePlaceElements();
    }

    private void rePlaceElements() {
        Dimension effectiveDim = getEffectiveDimension();

        navigation.setBounds(new Rectangle(new Point(0,1), new Dimension(Math.max(100, Math.max(navigation.getPreferredSize().width, navigationScroll.getBounds().width-10)), navigation.getPreferredSize().height)));
        navigation.realignChildren();

        navigationScroll.evalulateContentArea();
        Rectangle navBound;
        navigationScroll.setBounds(navBound = new Rectangle(0,50, navigation.getBounds().width+10, effectiveDim.height-50));
        contentScroll.setBounds(new Rectangle(navBound.x + navBound.width, 25, effectiveDim.width - navBound.x - navBound.width, effectiveDim.height-25));

        search.setBounds(new Rectangle(5,30,navBound.x + navBound.width - 10,15));

        guiRelocate.setBounds(new Rectangle(5,5,100,15));
        github.setBounds(new Rectangle(effectiveDim.width - 80,5,75,15));
        discord.setBounds(new Rectangle(effectiveDim.width - 160,5,75,15));
    }
}
