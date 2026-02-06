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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.mechanicedit;

import kr.syeyoung.dungeonsguide.mod.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonLeverState;
import kr.syeyoung.dungeonsguide.mod.dungeon.data.mechanics.DungeonLeverState.DungeonLeverData;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.elements.MLabel;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.elements.MLabelAndElement;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.elements.MTextField;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.gui.elements.MValue;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

public class ValueEditLever extends MPanel implements ValueEdit<DungeonLeverData> {
    private final DungeonLeverState dummyState;

    public ValueEditLever(final Parameter parameter2) {
        this.parameter = parameter2;
        this.dungeonLever = (DungeonLeverData) parameter2.getNewData();
        this.dummyState = dungeonLever.createState(EditingContext.getEditingContext().getRoom());
        label = new MLabel();
        label.setText("Secret Point");
        label.setAlignment(MLabel.Alignment.LEFT);
        add(label);

        value = new MValue(dungeonLever.getLeverPoint(), Collections.emptyList());
        add(value);

        preRequisite = new MTextField() {
            @Override
            public void edit(String str) {
                dungeonLever.setPreRequisite(Arrays.asList(str.split(",")));
            }
        };
        preRequisite.setText(TextUtils.join(dungeonLever.getPreRequisite(), ","));
        preRequisite2 = new MLabelAndElement("Req.", preRequisite);
        preRequisite2.setBounds(new Rectangle(0, 40, getBounds().width, 20));
        add(preRequisite2);


        target = new MTextField() {
            @Override
            public void edit(String str) {
                dungeonLever.setTriggering(str);
            }
        };
        target.setText(dungeonLever.getTriggering());
        target2 = new MLabelAndElement("Target", target);
        target2.setBounds(new Rectangle(0, 60, getBounds().width, 20));
        add(target2);
    }

    private Parameter parameter;

    // scroll pane
    // just create
    // add set
    private final DungeonLeverData dungeonLever;

    private final MLabel label;
    private final MValue<OffsetPoint> value;
    private final MTextField preRequisite;
    private final MLabelAndElement preRequisite2;
    private final MTextField target;
    private final MLabelAndElement target2;

    @Override
    public void onBoundsUpdate() {
        label.setBounds(new Rectangle(0, 0, getBounds().width, 20));
        value.setBounds(new Rectangle(0, 20, getBounds().width, 20));
        preRequisite2.setBounds(new Rectangle(0, 40, getBounds().width, 20));
        target2.setBounds(new Rectangle(0, 60, getBounds().width, 20));
    }

    @Override
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void renderWorld(float partialTicks) {
        dummyState.highlight(new Color(0, 255, 0, 50), parameter.getName(), , partialTicks);
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0, 0, parentWidth, parentHeight));
    }

    public static class Generator implements ValueEditCreator<ValueEditLever> {

        @Override
        public ValueEditLever createValueEdit(Parameter parameter) {
            return new ValueEditLever(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return new DungeonLeverData();
        }

        @Override
        public Object cloneObj(Object object) {
            try {
                return ((DungeonLeverData) object).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            assert false;
            return null;
        }
    }
}
