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

package kr.syeyoung.dungeonsguide.config.guiconfig.nyu;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class RootConfigPanel extends MPanelScaledGUI {
    private MScrollablePanel navigationScroll;

    private MList navigation = new MList();

    private MScrollablePanel contentScroll;


    private final Map<String, MPanel> pages = new HashMap<String, MPanel>();
    @Getter
    @Setter
    private Function<String, MPanel> pageGenerator;
    @Getter
    private String currentPage = "";

    private GuiConfigV2 gui;

    private long lastPageSet = System.currentTimeMillis();

    private MTextField search;
    private MButton guiRelocate;

    private final Stack<String> history = new Stack<String>();

    public RootConfigPanel(GuiConfigV2 guiConfigV2) {
        this.gui = guiConfigV2;

        search = new MTextField() {
            @Override
            public void edit(String str) {
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


        rePlaceElements();
    }

    private void setupNavigation() {
        NestedCategory root = new NestedCategory("ROOT");
        for (AbstractFeature abstractFeature : FeatureRegistry.getFeatureList()) {
            String category = abstractFeature.getCategory();

            NestedCategory currentRoot = root;
            for (String s : category.split("\\.")) {
                NestedCategory finalCurrentRoot = currentRoot;
                currentRoot = currentRoot.children().computeIfAbsent(s, k -> new NestedCategory(finalCurrentRoot.categoryFull()+"."+k));
            }

        }

        for (NestedCategory value : root.children().values()) {
            setupNavigationRecursive(value, navigation, 0, 17);
        }
    }
    private void setupNavigationRecursive(NestedCategory nestedCategory, MPanel parent, int depth, int offset) {
        ConfigPanelCreator.map.put(nestedCategory.categoryFull(), () -> new MPanelCategory(nestedCategory, this));

        if (nestedCategory.children().size() == 0) {
            MCategoryElement current = new MCategoryElement(nestedCategory.categoryFull(),() -> {
                setCurrentPageAndPushHistory(nestedCategory.categoryFull());
            }, 13 * depth + 17, offset, this);
            parent.add(current);
        } else {
            MCategoryElement current = new MCategoryElement(nestedCategory.categoryFull(),() -> {
                setCurrentPageAndPushHistory(nestedCategory.categoryFull());
            }, 3,offset, this);
            MCollapsable mCollapsable = new MCollapsable(current, this::rePlaceElements);
            mCollapsable.setLeftPad(offset-13);
            mCollapsable.getLowerElements().setDrawLine(false);
            mCollapsable.getLowerElements().setGap(0);
            mCollapsable.setLeftPadElements(0);
            parent.add(mCollapsable);

            for (NestedCategory value : nestedCategory.children().values()) {
                setupNavigationRecursive(value, mCollapsable, depth+1, offset+13);
            }
        }
    }


    public void setCurrentPageAndPushHistory(String currentPage) {
        if (!this.currentPage.equals(currentPage))
            history.push(this.currentPage);
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
    }
}
