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

package kr.syeyoung.dungeonsguide.roomedit.gui;

import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.gui.MGui;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.gui.elements.*;
import kr.syeyoung.dungeonsguide.roomedit.panes.DynamicEditor;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditRegistry;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;

public class GuiDungeonParameterEdit extends MGui {

    private final Parameter parameter;
    private final DungeonRoom dungeonRoom;

    private String classSelection;

    private final MPanel currentValueEdit;

    private final MButton save;
    private final MButton delete;

    @Getter
    private ValueEdit valueEdit;

    public GuiDungeonParameterEdit(final MParameter parameter2, final DynamicEditor processorParameterEditPane) {
        dungeonRoom = EditingContext.getEditingContext().getRoom();
        getMainPanel().setBackgroundColor(new Color(17, 17, 17, 179));
        this.parameter = parameter2.getParameter();
        {
            MTextField mTextField = new MTextField() {
                @Override
                public void edit(String str) {
                    parameter.setName(str);
                }
            };
            MLabelAndElement mLabelAndElement = new MLabelAndElement("Name", mTextField);

            mTextField.setText(parameter.getName());
            mLabelAndElement.setBounds(new Rectangle(0,0,200, 20));
            getMainPanel().add(mLabelAndElement);
        }
        {
            classSelection = parameter.getNewData() == null ?"null" : parameter.getNewData().getClass().getName();
            final MStringSelectionButton mStringSelectionButton = new MStringSelectionButton(processorParameterEditPane.allowedClass(), classSelection) {
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0, 20, parentWidth,20));
                }

                @Override
                public String selectionToDisplay(String selection) {
                    String[] split = selection.split("\\.");
                    return super.selectionToDisplay(split[split.length - 1]);
                }
            };

            mStringSelectionButton.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    classSelection = mStringSelectionButton.getSelected();
                    updateClassSelection();
                }
            });
            mStringSelectionButton.setBounds(new Rectangle(0,20,150,20));
            getMainPanel().add(mStringSelectionButton);
        }
        {
            currentValueEdit = new MPanel(){
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0, 40, parentWidth,parentHeight - 60));
                }
            };
            getMainPanel().add(currentValueEdit);
        }
        {
            delete = new MButton() {
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0,parentHeight - 20, parentWidth / 2, 20));
                }
            };
            delete.setText("Delete");
            delete.setBackgroundColor(Color.red);
            delete.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    processorParameterEditPane.delete(parameter2);
                    EditingContext.getEditingContext().goBack();
                }
            });

            save = new MButton(){
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(parentWidth / 2,parentHeight - 20, parentWidth / 2, 20));
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
            getMainPanel().add(delete);
            getMainPanel().add(save);
        }
        updateClassSelection();
    }

    public void updateClassSelection() {
        currentValueEdit.getChildComponents().clear();

        ValueEditCreator valueEditCreator = ValueEditRegistry.getValueEditMap(classSelection);

        if (!classSelection.equals(parameter.getNewData() == null ?"null" :parameter.getNewData().getClass().getName())) {
            parameter.setNewData(valueEditCreator.createDefaultValue(parameter));
            parameter.setPreviousData(valueEditCreator.cloneObj(parameter.getNewData()));
        }

        MPanel valueEdit = (MPanel) valueEditCreator.createValueEdit(parameter);
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
        getMainPanel().setBounds(new Rectangle(10, Math.min((Minecraft.getMinecraft().displayHeight - 300) / 2, Minecraft.getMinecraft().displayHeight),200,300));
    }
}
