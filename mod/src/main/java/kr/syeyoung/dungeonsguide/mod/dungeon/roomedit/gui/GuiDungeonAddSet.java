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

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEditOffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.gui.MGui;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MPanelScaledGUI;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MValue;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiDungeonAddSet extends MGui {

    private final ValueEditOffsetPointSet valueEditOffsetPointSet;

    private final MButton add;
    private final MButton back;


    @Getter
    private final OffsetPoint start;
    @Getter
    private final OffsetPoint end;

    public void onWorldRender(float partialTicks) {
        for (OffsetPoint pos:getBlockPoses()) {
            RenderUtils.highlightBlock(pos.getBlockPos(EditingContext.getEditingContext().getRoom()), new Color(0,255,255,50), partialTicks);
        }
        RenderUtils.highlightBlock(start.getBlockPos(EditingContext.getEditingContext().getRoom()), new Color(255,0,0,100), partialTicks);
        RenderUtils.highlightBlock(end.getBlockPos(EditingContext.getEditingContext().getRoom()), new Color(0,255,0,100), partialTicks);
    }

    public List<OffsetPoint> getBlockPoses() {
        int minX = Math.min(start.getX(), end.getX());
        int minY = Math.min(start.getY(), end.getY());
        int minZ = Math.min(start.getZ(), end.getZ());
        int maxX = Math.max(start.getX(), end.getX());
        int maxY = Math.max(start.getY(), end.getY());
        int maxZ = Math.max(start.getZ(), end.getZ());

        List<OffsetPoint> offsetPoints = new ArrayList<OffsetPoint>();
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <=maxX; x++) {
                for (int y = maxY; y >= minY; y --) {
                    offsetPoints.add(new OffsetPoint(x,y,z));
                }
            }
        }
        return offsetPoints;
    }

    public void add() {
        valueEditOffsetPointSet.addAll(getBlockPoses());
    }

    public GuiDungeonAddSet(final ValueEditOffsetPointSet processorParameterEditPane) {
        MPanelScaledGUI scaledGUI = new MPanelScaledGUI();
        scaledGUI.setScale(new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());
        getMainPanel().add(scaledGUI);


        this.valueEditOffsetPointSet = processorParameterEditPane;
        scaledGUI.setBackgroundColor(new Color(17, 17, 17, 179));
        {
            start = new OffsetPoint(EditingContext.getEditingContext().getRoom(), Minecraft.getMinecraft().thePlayer.getPosition());
            end = new OffsetPoint(EditingContext.getEditingContext().getRoom(), Minecraft.getMinecraft().thePlayer.getPosition());
        }
        {
            MValue mValue = new MValue(start, Collections.emptyList());
            mValue.setBounds(new Rectangle(0,0,150,20));
            scaledGUI.add(mValue);
            MValue mValue2 = new MValue(end,Collections.emptyList());
            mValue2.setBounds(new Rectangle(0,20,150,20));
            scaledGUI.add(mValue2);
        }
        {
            add = new MButton() {
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0,parentHeight - 20, parentWidth / 2, 20));
                }
            };
            add.setText("Add");
            add.setBackgroundColor(Color.red);
            add.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    add();
                    EditingContext.getEditingContext().goBack();
                }
            });

            back = new MButton(){
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(parentWidth / 2,parentHeight - 20, parentWidth / 2, 20));
                }
            };
            back.setText("Go back");
            back.setBackgroundColor(Color.green);
            back.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    EditingContext.getEditingContext().goBack();
                }
            });
            scaledGUI.add(add);
            scaledGUI.add(back);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        // update bounds
        int w = 200 * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor(),
                h = 300 * new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        getMainPanel().getChildComponents().get(0).setBounds(new Rectangle(10, Math.min((Minecraft.getMinecraft().displayHeight - h) / 2, Minecraft.getMinecraft().displayHeight),w,h));
    }
}
