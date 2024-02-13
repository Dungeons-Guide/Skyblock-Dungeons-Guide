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

import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPoint;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonSecretEssence;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.*;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

public class ValueEditSecretEssence extends MPanel implements ValueEdit<DungeonSecretEssence> {
    private Parameter parameter;

    // scroll pane
    // just create
    // add set
    private final DungeonSecretEssence dungeonSecretEssence;

    private final MLabel label;
    private final MValue<OffsetPoint> value;
    private final MTextField preRequisite;
    private final MLabelAndElement preRequisite2;

    public ValueEditSecretEssence(final Parameter parameter2) {
        this.parameter = parameter2;
        this.dungeonSecretEssence = (DungeonSecretEssence) parameter2.getNewData();


        label = new MLabel();
        label.setText("Secret Point");
        label.setAlignment(MLabel.Alignment.LEFT);
        add(label);

        value = new MValue(dungeonSecretEssence.getSecretPoint(), Collections.emptyList());
        add(value);

        preRequisite = new MTextField() {
            @Override
            public void edit(String str) {
                dungeonSecretEssence.setPreRequisite(Arrays.asList(str.split(",")));
            }
        };
        preRequisite.setText(TextUtils.join(dungeonSecretEssence.getPreRequisite(), ","));
        preRequisite2 = new MLabelAndElement("Req.",preRequisite);
        preRequisite2.setBounds(new Rectangle(0,60,getBounds().width,20));
        add(preRequisite2);
    }

    @Override
    public void onBoundsUpdate() {
        label.setBounds(new Rectangle(0,0,getBounds().width, 20));
        value.setBounds(new Rectangle(0,20,getBounds().width, 20));
        preRequisite2.setBounds(new Rectangle(0,60,getBounds().width,20));
    }

    @Override
    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public void renderWorld(float partialTicks) {
        dungeonSecretEssence.highlight(new Color(0,255,0,50), parameter.getName(), EditingContext.getEditingContext().getRoom(), partialTicks);
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    public static class Generator implements ValueEditCreator<ValueEditSecretEssence> {

        @Override
        public ValueEditSecretEssence createValueEdit(Parameter parameter) {
            return new ValueEditSecretEssence(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return new DungeonSecretEssence();
        }

        @Override
        public Object cloneObj(Object object) {
            try {
                return ((DungeonSecretEssence)object).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            assert false;
            return null;
        }
    }
}
