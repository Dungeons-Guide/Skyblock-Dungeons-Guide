/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.config.guiconfig;

import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.features.GuiFeature;
import kr.syeyoung.dungeonsguide.gui.MGui;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

public class GuiGuiLocationConfig extends MGui {

    private final GuiScreen before;

    public GuiGuiLocationConfig(final GuiScreen before, AbstractFeature featureWhitelist) {
        this.before = before;
        for (AbstractFeature feature : FeatureRegistry.getFeatureList()) {
            if (feature instanceof GuiFeature && feature.isEnabled()) {
                getMainPanel().add(new PanelDelegate((GuiFeature) feature, featureWhitelist == null || feature == featureWhitelist));
            }
        }

        getMainPanel().setBackgroundColor(new Color(0,0,0, 60));
        {
            MButton button = new MButton() {
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(parentWidth-50,parentHeight-20,50,20));
                }
            };
            button.setText("back");
            button.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    Minecraft.getMinecraft().displayGuiScreen(before);
                }
            });
            getMainPanel().add(button);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        getMainPanel().setBounds(new Rectangle(0,0,Minecraft.getMinecraft().displayWidth,Minecraft.getMinecraft().displayHeight));
    }

}
