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

package kr.syeyoung.dungeonsguide.mod.config.guiconfig;

import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.RawRenderingGuiFeature;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MToggleButton;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MFeature extends MPanel {

    @Getter
    private final AbstractFeature feature;

    private final List<MPanel> addons =  new ArrayList<MPanel>();

    @Getter @Setter
    private Color hover;

    private final RootConfigPanel panel;

    public MFeature(final AbstractFeature abstractFeature, final RootConfigPanel panel) {
        this.panel = panel;
        this.feature = abstractFeature;

        if (abstractFeature.isDisyllable()) {
            final MToggleButton mStringSelectionButton = new MToggleButton();
            mStringSelectionButton.setOnToggle(new Runnable() {
                @Override
                public void run() {
                    boolean selected = mStringSelectionButton.isEnabled();
                    feature.setEnabled(selected);
                }
            });
            mStringSelectionButton.setBackground(RenderUtils.blendAlpha(0x141414, 0.07f));
            addons.add(mStringSelectionButton);
            mStringSelectionButton.setEnabled(feature.isEnabled());
            mStringSelectionButton.setSize(new Dimension(40, 15));
            add(mStringSelectionButton);
        }
        if (abstractFeature.getParameters().size() != 0) {
            MButton button = new MButton();
            button.setText("Settings");
            button.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    panel.setCurrentPageAndPushHistory(abstractFeature.getEditRoute(panel));
                }
            });
            button.setBackground(RenderUtils.blendAlpha(0x141414, 0.07f));
            button.setClicked(RenderUtils.blendAlpha(0x141414, 0.17f));
            button.setHover(RenderUtils.blendAlpha(0x141414, 0.17f));
            addons.add(button);
            button.setSize(new Dimension(50, 15));
            add(button);
        }
        if (abstractFeature instanceof RawRenderingGuiFeature) {
            MButton button = new MButton();
            button.setText("Relocate");
            button.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    Minecraft.getMinecraft().displayGuiScreen(new GuiGuiLocationConfig(Minecraft.getMinecraft().currentScreen, abstractFeature));
                    button.setClicked(false);
                }
            });
            button.setBackground(RenderUtils.blendAlpha(0x141414, 0.07f));
            button.setClicked(RenderUtils.blendAlpha(0x141414, 0.17f));
            button.setHover(RenderUtils.blendAlpha(0x141414, 0.17f));
            addons.add(button);
            button.setSize(new Dimension(75, 15));
            add(button);
        }
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {

        int border = RenderUtils.blendAlpha(0x141414, 0.12f);
        if (!panel.getSearchWord().isEmpty() && (feature.getName().toLowerCase().contains(panel.getSearchWord()) || feature.getDescription().toLowerCase().contains(panel.getSearchWord()))) {
            border = 0xFF02EE67;
        }

        Gui.drawRect(0,0,getBounds().width, getBounds().height,border);
        Gui.drawRect(1,18,getBounds().width -1, getBounds().height-1, RenderUtils.blendAlpha(0x141414, 0.15f));
        Gui.drawRect(1,1,getBounds().width-1, 18, RenderUtils.blendAlpha(0x141414, 0.12f));


        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        GlStateManager.pushMatrix();
        GlStateManager.translate(5,5,0);
        GlStateManager.scale(1.0,1.0,0);
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString(feature.getName(), 0,0, 0xFFFFFFFF);
        GlStateManager.popMatrix();

        fr.drawSplitString(feature.getDescription(), 5, 23, getBounds().width -10, 0xFFBFBFBF);
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setSize(new Dimension(parentWidth, getBounds().height));
    }

    @Override
    public Dimension getPreferredSize() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int descriptionHeight = fr.listFormattedStringToWidth(feature.getDescription(), Math.max(100, getBounds().width - 10)).size() * fr.FONT_HEIGHT;

        return new Dimension(100, descriptionHeight + 28);
    }

    @Override
    public void onBoundsUpdate() {
        int x = getBounds().width - 5;
        for (MPanel panel : addons) {
            panel.setBounds(new Rectangle(x - panel.getPreferredSize().width, 3, panel.getPreferredSize().width, 12));
            x -= panel.getPreferredSize().width + 5;
        }
    }
}
