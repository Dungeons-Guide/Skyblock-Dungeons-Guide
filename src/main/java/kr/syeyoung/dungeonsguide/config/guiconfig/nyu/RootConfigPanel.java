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
import kr.syeyoung.dungeonsguide.config.guiconfig.old.ConfigPanelCreator;
import kr.syeyoung.dungeonsguide.config.guiconfig.old.GuiConfig;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.*;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public RootConfigPanel(GuiConfigV2 guiConfigV2) {
        this.gui = guiConfigV2;

        navigationScroll = new MScrollablePanel(1);
        navigationScroll.setHideScrollBarWhenNotNecessary(false);
        add(navigationScroll);
        navigationScroll.add(navigation);
        navigationScroll.add(new MSpacer(0,0,1,1));

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

        MCategoryElement current = new MCategoryElement("GUI Relocate",() -> {
            Minecraft.getMinecraft().displayGuiScreen(new GuiGuiLocationConfig(gui, null));
        }, 17,17, this);
        navigation.add(current);
    }
    private void setupNavigationRecursive(NestedCategory nestedCategory, MPanel parent, int depth, int offset) {
        ConfigPanelCreator.map.put(nestedCategory.categoryFull(), () -> new MPanelCategory(nestedCategory, this));

        if (nestedCategory.children().size() == 0) {
            MCategoryElement current = new MCategoryElement(nestedCategory.categoryFull(),() -> {
                setCurrentPage(nestedCategory.categoryFull());
            }, 13 * depth + 17, offset, this);
            parent.add(current);
        } else {
            MCategoryElement current = new MCategoryElement(nestedCategory.categoryFull(),() -> {
                setCurrentPage(nestedCategory.categoryFull());
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


    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;

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
        Gui.drawRect(0,0, (int) (effectiveDim.width),  (int) (effectiveDim.height), RenderUtils.blendAlpha(0, 0.0f));
        Gui.drawRect(1,1, (int) (effectiveDim.width)-1,  (int) (effectiveDim.height) - 1, RenderUtils.blendAlpha(0x141414, 0.00f));
        Gui.drawRect(1,1, (int) (effectiveDim.width)-1, 25, RenderUtils.blendAlpha(0x0, 0.20f));
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
        navigationScroll.setBounds(navBound = new Rectangle(1,25, navigation.getBounds().width+10, effectiveDim.height-24));

        contentScroll.setBounds(new Rectangle(navBound.x + navBound.width + 1, 25, effectiveDim.width - navBound.x - navBound.width - 2, effectiveDim.height -26));
    }
}
