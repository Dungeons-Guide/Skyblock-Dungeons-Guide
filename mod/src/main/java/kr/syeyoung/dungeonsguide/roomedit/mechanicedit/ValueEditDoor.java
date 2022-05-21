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

package kr.syeyoung.dungeonsguide.roomedit.mechanicedit;

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonDoor;
import kr.syeyoung.dungeonsguide.gui.elements.*;
import kr.syeyoung.dungeonsguide.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.gui.elements.*;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.init.Blocks;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ValueEditDoor extends MPanel implements ValueEdit<DungeonDoor> {
    private Parameter parameter;

    // scroll pane
    // just create
    // add set
    private final DungeonDoor dungeonDoor;

    private final MLabel label;
    private final MValue<OffsetPointSet> value;
    private final MTextField preRequisite;
    private final MLabelAndElement preRequisite_par;
    private final MTextField preRequisite2;
    private final MLabelAndElement preRequisite2_par;
    private final MButton updateOnlyAir;

    public ValueEditDoor(final Parameter parameter2) {
        this.parameter = parameter2;
        this.dungeonDoor = (DungeonDoor) parameter2.getNewData();


        label = new MLabel();
        label.setText("Wall Points");
        label.setAlignment(MLabel.Alignment.LEFT);
        add(label);

        value = new MValue(dungeonDoor.getSecretPoint(), Collections.emptyList());
        add(value);

        updateOnlyAir = new MButton();
        updateOnlyAir.setText("Update Air");
        updateOnlyAir.setBackgroundColor(Color.green);
        updateOnlyAir.setForeground(Color.black);
        updateOnlyAir.setBounds(new Rectangle(0,40,getBounds().width, 20));
        add(updateOnlyAir);
        updateOnlyAir.setOnActionPerformed(new Runnable() {
            @Override
            public void run() {
                OffsetPointSet ofs = dungeonDoor.getSecretPoint();
                List<OffsetPoint> filtered = new ArrayList<OffsetPoint>();
                for (OffsetPoint offsetPoint : ofs.getOffsetPointList()) {
                    if (offsetPoint.getBlock(EditingContext.getEditingContext().getRoom()) != Blocks.air) continue;
                    filtered.add(offsetPoint);
                }
                dungeonDoor.getSecretPoint().setOffsetPointList(filtered);
            }
        });

        preRequisite = new MTextField() {
            @Override
            public void edit(String str) {
                dungeonDoor.setOpenPreRequisite(Arrays.asList(str.split(",")));
            }
        };
        preRequisite.setText(TextUtils.join(dungeonDoor.getOpenPreRequisite(), ","));
        preRequisite_par = new MLabelAndElement("Open Req.",preRequisite);
        preRequisite_par.setBounds(new Rectangle(0,60,getBounds().width,20));
        add(preRequisite_par);

        preRequisite2 = new MTextField() {
            @Override
            public void edit(String str) {
                dungeonDoor.setClosePreRequisite(Arrays.asList(str.split(",")));
            }
        };
        preRequisite2.setText(TextUtils.join(dungeonDoor.getClosePreRequisite(), ","));
        preRequisite2_par = new MLabelAndElement("Close Req.",preRequisite2);
        preRequisite2_par.setBounds(new Rectangle(0,80,getBounds().width,20));
        add(preRequisite2_par);
    }

    @Override
    public void onBoundsUpdate() {
        label.setBounds(new Rectangle(0,0,getBounds().width, 20));
        value.setBounds(new Rectangle(0,20,getBounds().width, 20));
        updateOnlyAir.setBounds(new Rectangle(0,40,getBounds().width, 20));
        preRequisite_par.setBounds(new Rectangle(0,60,getBounds().width,20));
        preRequisite2_par.setBounds(new Rectangle(0,80,getBounds().width,20));
    }

    @Override
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void renderWorld(float partialTicks) {
        dungeonDoor.highlight(new Color(0,255,255,50), parameter.getName(), EditingContext.getEditingContext().getRoom(), partialTicks);
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    public static class Generator implements ValueEditCreator<ValueEditDoor> {

        @Override
        public ValueEditDoor createValueEdit(Parameter parameter) {
            return new ValueEditDoor(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return new DungeonDoor();
        }

        @Override
        public Object cloneObj(Object object) {
            try {
                return ((DungeonDoor)object).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            assert false;
            return null;
        }
    }
}
