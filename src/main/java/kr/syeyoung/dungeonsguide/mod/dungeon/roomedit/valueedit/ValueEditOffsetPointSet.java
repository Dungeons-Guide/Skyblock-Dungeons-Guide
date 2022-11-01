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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MValue;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.GuiDungeonAddSet;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ValueEditOffsetPointSet extends MPanel implements ValueEdit<OffsetPointSet> {
    private Parameter parameter;

    // scroll pane
    // just create
    // add set

    private final MPanel scroll;
    @Getter
    private final List<MPanel> MParameters = new ArrayList<MPanel>();

    private final MButton add;
    private final MButton addSet;

    public void delete(OffsetPoint offsetPoint) {
        ((OffsetPointSet)parameter.getNewData()).getOffsetPointList().remove(offsetPoint);
        Iterator<MPanel> iterator = MParameters.iterator();
        while (iterator.hasNext()) {
            MValue panel = (MValue) iterator.next();
            if (panel.getData() == offsetPoint) {
                iterator.remove();
                break;
            }
        }

    }

    public ValueEditOffsetPointSet(final Parameter parameter2) {
        this.parameter = parameter2;
        {
            scroll = new MPanel() {
                private int offsetY = 0;

                @Override
                public List<MPanel> getChildComponents() {
                    return MParameters;
                }

                @Override
                public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
                    int heights = 0;
                    for (MPanel panel:getChildComponents()) {
                        panel.setPosition(new Point(0, -offsetY + heights));
                        heights += panel.getBounds().height;
                    }
                }

                @Override
                public boolean mouseClicked0(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int mouseButton) {
                    if (!getBounds().contains(relMouseX0, relMouseY0)) {
                        return false;
                    }

                    int relMousex = relMouseX0 - getBounds().x;
                    int relMousey = relMouseY0 - getBounds().y;

                    boolean noClip = true;
                    boolean focusedOverall = false;
                    for (MPanel childComponent  : getChildComponents()) {
                        if (childComponent.mouseClicked0(absMouseX, absMouseY, relMousex, relMousey, mouseButton)) {
                            noClip = false;
                            focusedOverall = true;
                        }
                    }

                    if (getBounds().contains(relMouseX0, relMouseY0) && noClip) {
                        isFocused = true;
                        focusedOverall = true;
                    } else {
                        isFocused = false;
                    }

                    mouseClicked(absMouseX, absMouseY, relMousex, relMousey, mouseButton);
                    return focusedOverall;
                }

                @Override
                public void onBoundsUpdate() {
                    for (MPanel panel :getChildComponents()){
                        panel.setSize(new Dimension(getBounds().width, 20));
                    }
                }
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    this.setBounds(new Rectangle(5,5,parentWidth-10,parentHeight-10));
                }

                @Override
                public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
                    if (new Rectangle(new Point(0,0), getSize()).contains(relMouseX0, relMouseY0)) {
                        if (scrollAmount >0) offsetY += 20;
                        else if (scrollAmount < 0) offsetY -= 20;
                        if (offsetY <0) offsetY = 0;
                    }
                }
            };
            scroll.setBounds(new Rectangle(0,0,getBounds().width, getBounds().height-20));
            add(scroll);
        }

        {
            add = new MButton() {
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(0,parentHeight - 20, parentWidth / 2, 20));
                }
            };
            add.setText("Add");
            add.setBackgroundColor(Color.green);
            add.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    OffsetPoint offsetPoint = new OffsetPoint(EditingContext.getEditingContext().getRoom(), Minecraft.getMinecraft().thePlayer.getPosition());
                    MValue mValue;
                    MParameters.add(mValue = new MValue(offsetPoint, buildAddonsFor(offsetPoint)));
                    ((OffsetPointSet)parameter.getNewData()).getOffsetPointList().add(offsetPoint);
                    mValue.setSize(new Dimension(getBounds().width, 20));
                }
            });

            addSet = new MButton(){
                @Override
                public void resize(int parentWidth, int parentHeight) {
                    setBounds(new Rectangle(parentWidth / 2,parentHeight - 20, parentWidth / 2, 20));
                }
            };
            addSet.setText("Add Set");
            addSet.setBackgroundColor(Color.cyan);
            addSet.setOnActionPerformed(new Runnable() {
                @Override
                public void run() {
                    EditingContext.getEditingContext().openGui(new GuiDungeonAddSet(ValueEditOffsetPointSet.this));
                }
            });
            add(add);
            add(addSet);
        }
        for (OffsetPoint offsetPoint : ((OffsetPointSet)parameter.getNewData()).getOffsetPointList()) {
            MParameters.add(new MValue(offsetPoint, buildAddonsFor(offsetPoint)));
        }
    }

    public List<MPanel> buildAddonsFor(final OffsetPoint offsetPoint) {
        ArrayList<MPanel> panels = new ArrayList<MPanel>();
        MButton mButton = new MButton();
        mButton.setText("Delete");
        mButton.setForeground(Color.white);
        mButton.setBackgroundColor(Color.red);
        mButton.setOnActionPerformed(new Runnable() {
            @Override
            public void run() {
                delete(offsetPoint);
            }
        });
        panels.add(mButton);
        return panels;
    }

    @Override
    public void onBoundsUpdate() {
        scroll.setBounds(new Rectangle(0,0,getBounds().width, getBounds().height-20));
        add.setBounds(new Rectangle(0,getBounds().height-20,getBounds().width / 2, 20));
        addSet.setBounds(new Rectangle(getBounds().width / 2,getBounds().height-20,getBounds().width / 2, 20));
    }

    @Override
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void renderWorld(float partialTicks) {
        for (OffsetPoint offsetPoint :((OffsetPointSet)parameter.getNewData()).getOffsetPointList()) {
            RenderUtils.highlightBlock(offsetPoint.getBlockPos(EditingContext.getEditingContext().getRoom()), new Color(0,255,255,50), partialTicks);
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    public void addAll(List<OffsetPoint> blockPoses) {
        ((OffsetPointSet)parameter.getNewData()).getOffsetPointList().addAll(blockPoses);
        for (OffsetPoint blockPose : blockPoses) {
            MParameters.add(new MValue(blockPose, buildAddonsFor(blockPose)));
        }
    }

    public static class Generator implements ValueEditCreator<ValueEditOffsetPointSet> {

        @Override
        public ValueEditOffsetPointSet createValueEdit(Parameter parameter) {
            return new ValueEditOffsetPointSet(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return new OffsetPointSet();
        }

        @Override
        public Object cloneObj(Object object) {
            try {
                return ((OffsetPointSet)object).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            assert false;
            return null;
        }
    }
}
