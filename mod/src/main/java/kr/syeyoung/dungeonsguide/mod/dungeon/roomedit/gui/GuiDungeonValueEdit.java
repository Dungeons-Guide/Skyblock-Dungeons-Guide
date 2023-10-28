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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui;


import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEditRegistry;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.mod.gui.MGui;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MLabel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MPanelScaledGUI;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.List;

public class GuiDungeonValueEdit extends MGui {
    private DungeonRoom dungeonRoom;


    private MPanel currentValueEdit;

    private MButton save;

    @Getter
    private ValueEdit valueEdit;

    private List<MPanel> addons;

    private Object editingObj;

    public GuiDungeonValueEdit(final Object object, final List<MPanel> addons) {
        try {
            MPanelScaledGUI scaledGUI = new MPanelScaledGUI();
            scaledGUI.setScale(new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());
            getMainPanel().add(scaledGUI);

            dungeonRoom = EditingContext.getEditingContext().getRoom();
            this.addons = addons;
            this.editingObj = object;
            scaledGUI.setBackgroundColor(new Color(17, 17, 17, 179));
            {
                currentValueEdit = new MPanel() {
                    @Override
                    public void resize(int parentWidth, int parentHeight) {
                        setBounds(new Rectangle(0, 0, parentWidth, parentHeight - 20 - addons.size() * 20));
                    }
                };
                scaledGUI.add(currentValueEdit);
            }

            for (MPanel addon : addons) {
                scaledGUI.add(addon);
            }
            {
                save = new MButton() {
                    @Override
                    public void resize(int parentWidth, int parentHeight) {
                        setBounds(new Rectangle(0, parentHeight - 20, parentWidth, 20));
                    }
                };
                save.setText("Go back");
                save.setBackgroundColor(Color.green);
                save.setOnActionPerformed(new Runnable() {
                    @Override
                    public void run() {
                        EditingContext.getEditingContext().goBack();
                    }
                });
                scaledGUI.add(save);
            }
            updateClassSelection();
        } catch (Exception e){}
    }

    public void updateClassSelection() {
        currentValueEdit.getChildComponents().clear();

        ValueEditCreator valueEditCreator = ValueEditRegistry.getValueEditMap(editingObj == null ?"null":editingObj.getClass().getName());

        MPanel valueEdit = (MPanel) valueEditCreator.createValueEdit(new Parameter("", editingObj, editingObj));
        if (valueEdit == null) {
            MLabel valueEdit2 = new MLabel() {
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0, 0, parentWidth,20));
                }
            };
            valueEdit2.setText("No Value Edit");
            valueEdit2.setBounds(new Rectangle(0,0,150,20));
            valueEdit = valueEdit2;
            this.valueEdit = null;
        } else{
            this.valueEdit = (ValueEdit) valueEdit;
        }
        valueEdit.resize0(currentValueEdit.getBounds().width, currentValueEdit.getBounds().height);
        currentValueEdit.add(valueEdit);
    }
    @Override
    public void initGui() {
        super.initGui();
        // update bounds
        int w = 200 * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(),
                h = 300 * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        getMainPanel().getChildComponents().get(0).setBounds(new Rectangle(10, Math.min((Minecraft.getMinecraft().displayHeight - h) / 2, Minecraft.getMinecraft().displayHeight),w,h));


        Rectangle referenceBounds = getMainPanel().getChildComponents().get(0).getBounds();
        for (int i = 0; i < addons.size(); i++) {
            addons.get(i).setBounds(new Rectangle(0, 300 - (i+1) * 20 - 20, 200, 20));
        }
        save.setBounds(new Rectangle(0 ,300- 20,200, 20));
    }

}
