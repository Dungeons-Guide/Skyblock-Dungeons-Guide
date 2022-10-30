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

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.*;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.function.Predicate;

public class MParameterEdit extends MPanel {
    private AbstractFeature abstractFeature;
    private FeatureParameter featureParameter;
    private RootConfigPanel rootConfigPanel;
    private MPanel valueEditHolder;
    private MPanel valueEdit;

    private Predicate<FeatureParameter> isDisabled ;

    public MParameterEdit(AbstractFeature abstractFeature, FeatureParameter parameter, RootConfigPanel rootConfigPanel) {
        this(abstractFeature, parameter, rootConfigPanel, (a) -> false);
    }

    public MParameterEdit(AbstractFeature abstractFeature, FeatureParameter parameter, RootConfigPanel rootConfigPanel, Predicate<FeatureParameter> isDisabled ) {
        this.abstractFeature = abstractFeature;
        this.featureParameter = parameter;
        this.rootConfigPanel = rootConfigPanel;
        this.isDisabled = isDisabled;

        if (parameter.getValue_type().equals("string")) {
            valueEdit = new MTextField() {
                @Override
                public void edit(String str) {
                    parameter.setValue(str);
                }
            };
            ((MTextField)valueEdit).setText((String) parameter.getValue());
        } else if (parameter.getValue_type().equals("integer")) {
            valueEdit = new MIntegerSelectionButton((Integer) parameter.getValue());
            ((MIntegerSelectionButton)valueEdit).setOnUpdate(() -> {
                parameter.setValue(((MIntegerSelectionButton) valueEdit).getData());
            });
        } else if (parameter.getValue_type().equals("float")) {
            valueEdit = new MFloatSelectionButton((Float) parameter.getValue());
            ((MFloatSelectionButton)valueEdit).setOnUpdate(() -> {
                parameter.setValue(((MFloatSelectionButton) valueEdit).getData());
            });
        } else if (parameter.getValue_type().equals("acolor")) {
            valueEdit = new MEditableAColor();
            ((MEditableAColor)valueEdit).setColor((AColor) parameter.getValue());
            ((MEditableAColor)valueEdit).setEnableEdit(true);
            ((MEditableAColor)valueEdit).setOnUpdate(() -> {
                parameter.setValue(((MEditableAColor) valueEdit).getColor());
            });
        } else if (parameter.getValue_type().equals("color")) {
            valueEdit = new MEditableAColor();
            ((MEditableAColor)valueEdit).setColor(new AColor(((Color) parameter.getValue()).getRGB(), true));
            ((MEditableAColor)valueEdit).setEnableEdit(true);
            ((MEditableAColor)valueEdit).setOnUpdate(() -> {
                parameter.setValue(((MEditableAColor) valueEdit).getColor());
            });
        } else if (parameter.getValue_type().equals("boolean")) {
            valueEdit = new MToggleButton();
            ((MToggleButton)valueEdit).setEnabled((Boolean) parameter.getValue());
            ((MToggleButton)valueEdit).setOnToggle(() -> {
                parameter.setValue(((MToggleButton) valueEdit).isEnabled());
            });
        }  else if (parameter.getValue_type().equals("keybind")) {
            valueEdit = new MKeyEditButton();
            ((MKeyEditButton)valueEdit).setKey((Integer) parameter.getValue());
            ((MKeyEditButton)valueEdit).setOnKeyEdit(() -> {
                parameter.setValue(((MKeyEditButton) valueEdit).getKey());
            });
            ((MKeyEditButton)valueEdit).setBorder(RenderUtils.blendTwoColors(0xFF141414,0x7702EE67));
        }else {
            valueEdit = new MLabel();
            ((MLabel)valueEdit).setText("????");
        }


        valueEditHolder = new MPanel() {
            @Override
            public void setBounds(Rectangle bounds) {
                super.setBounds(bounds);
                Dimension dimension = valueEdit.getPreferredSize();
                if (dimension.width <= 0) dimension.width = bounds.width/2;
                if (dimension.height <= 0) dimension.height = bounds.height/2;
                valueEdit.setBounds(new Rectangle((bounds.width - dimension.width)/2,(bounds.height - dimension.height)/2,dimension.width, dimension.height));
            }
        };
        add(valueEditHolder);
        valueEditHolder.add(valueEdit);
    }
    public MParameterEdit(AbstractFeature abstractFeature, FeatureParameter parameter, RootConfigPanel rootConfigPanel, MPanel valueEdit,  Predicate<FeatureParameter> isDisabled) {
        this.abstractFeature = abstractFeature;
        this.featureParameter = parameter;
        this.rootConfigPanel = rootConfigPanel;
        this.isDisabled = isDisabled;


        valueEditHolder = new MPanel() {
            @Override
            public void setBounds(Rectangle bounds) {
                super.setBounds(bounds);
                Dimension dimension = valueEdit.getPreferredSize();
                if (dimension.width <= 0) dimension.width = bounds.width/2;
                if (dimension.height <= 0) dimension.height = bounds.height/2;
                valueEdit.setBounds(new Rectangle((bounds.width - dimension.width)/2,(bounds.height - dimension.height)/2,dimension.width, dimension.height));
            }
        };
        add(valueEditHolder);
        valueEditHolder.add(valueEdit);
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        Gui.drawRect(0,0,getBounds().width, getBounds().height, RenderUtils.blendAlpha(0x141414, 0.12f));
        Gui.drawRect(2*bounds.width / 3,1,getBounds().width -1, getBounds().height-1, RenderUtils.blendAlpha(0x141414, 0.15f));
        Gui.drawRect(4, 15,2*bounds.width / 3-5, 16, RenderUtils.blendAlpha(0x141414, 0.3f));


        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString(featureParameter.getName(), 5,5, 0xFFFFFFFF);
        fr.drawSplitString(featureParameter.getDescription(), 5,18, 2*bounds.width /3-10, 0xFFAAAAAA);

    }

    @Override
    public void render0(double scale, Point parentPoint, Rectangle parentClip, int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks) {
        super.render0(scale, parentPoint, parentClip, absMousex, absMousey, relMousex0, relMousey0, partialTicks);
        if (isDisabled.test(featureParameter)) {
            Gui.drawRect(0,0, getBounds().width, getBounds().height, 0x55000000);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int descriptionHeight = fr.listFormattedStringToWidth(featureParameter.getDescription(), Math.max(50, 2*bounds.width/3-10)).size() * fr.FONT_HEIGHT;
        return new Dimension(100, Math.max(30, descriptionHeight + 23));
    }

    @Override
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
        valueEditHolder.setBounds(new Rectangle(2*bounds.width / 3, 0, bounds.width / 3, bounds.height));
    }

    @Override
    public void keyPressed0(char typedChar, int keyCode) {
        if (isDisabled.test(featureParameter)) return;
        super.keyPressed0(typedChar, keyCode);
    }

    @Override
    public void keyHeld0(char typedChar, int keyCode) {
        if (isDisabled.test(featureParameter)) return;
        super.keyHeld0(typedChar, keyCode);
    }

    @Override
    public void keyReleased0(char typedChar, int keyCode) {
        if (isDisabled.test(featureParameter)) return;
        super.keyReleased0(typedChar, keyCode);
    }

    @Override
    public boolean mouseClicked0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int mouseButton) {
        if (isDisabled.test(featureParameter)) return false;
        return super.mouseClicked0(absMouseX, absMouseY, relMouseX0, relMouseY0, mouseButton);
    }

    @Override
    public void mouseReleased0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int state) {
        if (isDisabled.test(featureParameter)) return ;
        super.mouseReleased0(absMouseX, absMouseY, relMouseX0, relMouseY0, state);
    }

    @Override
    public void mouseClickMove0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int clickedMouseButton, long timeSinceLastClick) {
        if (isDisabled.test(featureParameter)) return ;
        super.mouseClickMove0(absMouseX, absMouseY, relMouseX0, relMouseY0, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void mouseScrolled0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        if (isDisabled.test(featureParameter)) return ;
        super.mouseScrolled0(absMouseX, absMouseY, relMouseX0, relMouseY0, scrollAmount);
    }

    @Override
    public void mouseMoved0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0) {
        if (isDisabled.test(featureParameter)) return ;
        super.mouseMoved0(absMouseX, absMouseY, relMouseX0, relMouseY0);
    }
}
