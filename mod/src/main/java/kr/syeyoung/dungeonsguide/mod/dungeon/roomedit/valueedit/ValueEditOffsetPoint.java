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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
<<<<<<<< HEAD:mod/src/main/java/kr/syeyoung/dungeonsguide/roomedit/valueedit/ValueEditOffsetPoint.java
import kr.syeyoung.dungeonsguide.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.gui.elements.MIntegerSelectionButton;
import kr.syeyoung.dungeonsguide.gui.elements.MLabel;
import kr.syeyoung.dungeonsguide.gui.elements.MLabelAndElement;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.gui.elements.*;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
========
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.mod.gui.elements.*;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
>>>>>>>> origin/breaking-changes-just-working-im-not-putting-all-of-these-into-3.0-but-for-the-sake-of-beta-release-this-thing-exists:mod/src/main/java/kr/syeyoung/dungeonsguide/mod/dungeon/roomedit/valueedit/ValueEditOffsetPoint.java
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import java.awt.*;

public class ValueEditOffsetPoint extends MPanel implements ValueEdit<String> {
    private Parameter parameter;

    @Override
    public void renderWorld(float partialTicks) {
        RenderUtils.highlightBlock(((OffsetPoint)parameter.getPreviousData()).getBlockPos(EditingContext.getEditingContext().getRoom()), new Color(255,0,0,150), partialTicks);
        RenderUtils.highlightBlock(((OffsetPoint)parameter.getNewData()).getBlockPos(EditingContext.getEditingContext().getRoom()), new Color(0,255,0,150), partialTicks);
    }

    public ValueEditOffsetPoint(final Parameter parameter2) {
        this.parameter = parameter2;
        {
            MLabel label = new MLabel() {
                @Override
                public String getText() {
                    return parameter.getPreviousData().toString();
                }
            };
            MLabelAndElement mLabelAndElement = new MLabelAndElement("Prev",label);
            mLabelAndElement.setBounds(new Rectangle(0,0,getBounds().width,20));
            add(mLabelAndElement);
        }
        OffsetPoint newData = (OffsetPoint) parameter.getNewData();
        {
            final MIntegerSelectionButton textField = new MIntegerSelectionButton(newData.getX());
            textField.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    ((OffsetPoint) parameter.getNewData()).setX(textField.getData());
                }
            });
            MLabelAndElement mLabelAndElement = new MLabelAndElement("x",textField);
            mLabelAndElement.setBounds(new Rectangle(0,20,getBounds().width,20));
            add(mLabelAndElement);
        }
        {
            final MIntegerSelectionButton textField = new MIntegerSelectionButton(newData.getY());
            textField.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    ((OffsetPoint) parameter.getNewData()).setY(textField.getData());
                }
            });
            MLabelAndElement mLabelAndElement = new MLabelAndElement("y",textField);
            mLabelAndElement.setBounds(new Rectangle(0,40,getBounds().width,20));
            add(mLabelAndElement);
        }
        {
            final MIntegerSelectionButton textField = new MIntegerSelectionButton(newData.getZ());
            textField.setOnUpdate(new Runnable() {
                @Override
                public void run() {
                    ((OffsetPoint) parameter.getNewData()).setZ(textField.getData());
                }
            });
            MLabelAndElement mLabelAndElement = new MLabelAndElement("z",textField);
            mLabelAndElement.setBounds(new Rectangle(0,60,getBounds().width,20));
            add(mLabelAndElement);
        }
        {
            final MLabel setToHEre = new MLabel() {
                @Override
                public String getText() {
                    OffsetPoint offsetPoint = (OffsetPoint) parameter.getNewData();
                    return Block.getIdFromBlock(offsetPoint.getBlock(EditingContext.getEditingContext().getRoom())) +
                            ":" + offsetPoint.getData(EditingContext.getEditingContext().getRoom());
                }
            };
            MLabelAndElement mLabelAndElement = new MLabelAndElement("newPt",setToHEre);
            mLabelAndElement.setBounds(new Rectangle(0,80,getBounds().width,20));
            add(mLabelAndElement);
        }
        {
            final MButton setToHEre2 = new MButton();
            setToHEre2.setText("Set to here");
            setToHEre2.setBackgroundColor(Color.green);
            setToHEre2.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    EntityPlayer player = Minecraft.getMinecraft().thePlayer;
                    BlockPos pos = new BlockPos(player.posX, player.posY, player.posZ);
                    ((OffsetPoint)parameter2.getNewData()).setPosInWorld(EditingContext.getEditingContext().getRoom(),pos );
                }
            });
            MLabelAndElement mLabelAndElement = new MLabelAndElement("set",setToHEre2);
            mLabelAndElement.setBounds(new Rectangle(0,100,getBounds().width,20));
            add(mLabelAndElement);
        }
    }

    @Override
    public void onBoundsUpdate() {
        for (MPanel panel :getChildComponents()){
            panel.setSize(new Dimension(getBounds().width, 20));
        }
    }

    @Override
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    public static class Generator implements ValueEditCreator<ValueEditOffsetPoint> {

        @Override
        public ValueEditOffsetPoint createValueEdit(Parameter parameter) {
            return new ValueEditOffsetPoint(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return new OffsetPoint(EditingContext.getEditingContext().getRoom(), Minecraft.getMinecraft().thePlayer.getPosition());
        }

        @Override
        public Object cloneObj(Object object) {
            try {
                return ((OffsetPoint)object).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            assert false;
            return null;
        }
    }
}
