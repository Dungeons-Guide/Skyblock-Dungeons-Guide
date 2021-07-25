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

package kr.syeyoung.dungeonsguide.config.guiconfig.old;

import com.google.common.base.Supplier;
import kr.syeyoung.dungeonsguide.config.guiconfig.location.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.gui.MGui;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MNavigatingPane;
import kr.syeyoung.dungeonsguide.gui.elements.MPanelScaledGUI;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class GuiConfig extends MGui {


    @Getter
    private final MNavigatingPane tabbedPane;

    private final Stack<String> history = new Stack();

    private MPanelScaledGUI panelScaledGUI;
    public GuiConfig() {

        panelScaledGUI = new MPanelScaledGUI();
        getMainPanel().add(panelScaledGUI);
        MNavigatingPane tabbedPane = new MNavigatingPane();
        panelScaledGUI.add(tabbedPane);
        tabbedPane.setBackground2(new Color(38, 38, 38, 255));

        tabbedPane.setPageGenerator(ConfigPanelCreator.INSTANCE);

        tabbedPane.addBookmarkRunnable("GUI Relocate", new Runnable() {
            @Override
            public void run() {
                Minecraft.getMinecraft().displayGuiScreen(new GuiGuiLocationConfig(GuiConfig.this, null));
            }
        });

        for (final Map.Entry<String, List<AbstractFeature>> cate: FeatureRegistry.getFeaturesByCategory().entrySet())
            if (!cate.getKey().equals("hidden")) {
                tabbedPane.addBookmark(cate.getKey(), "base." + cate.getKey());

                ConfigPanelCreator.map.put("base." + cate.getKey(), new Supplier<MPanel>() {
                    @Override
                    public MPanel get() {
                        return new FeatureEditPane(cate.getValue(), GuiConfig.this);
                    }
                });
            }
        tabbedPane.addBookmark("All", "base.all");

        ConfigPanelCreator.map.put("base.all", new Supplier<MPanel>() {
            @Override
            public MPanel get() {
                return new FeatureEditPane(FeatureRegistry.getFeatureList().stream().filter( a-> !a.getCategory().equals("hidden")).collect(Collectors.toList()), GuiConfig.this);
            }
        });
        this.tabbedPane = tabbedPane;
    }

    @Override
    public void initGui() {
        super.initGui();
        panelScaledGUI.setBounds(new Rectangle(Math.min((Minecraft.getMinecraft().displayWidth - 1000) / 2, Minecraft.getMinecraft().displayWidth), Math.min((Minecraft.getMinecraft().displayHeight - 700) / 2, Minecraft.getMinecraft().displayHeight),1000,700));
        panelScaledGUI.setScale(1.0);
    }
}
