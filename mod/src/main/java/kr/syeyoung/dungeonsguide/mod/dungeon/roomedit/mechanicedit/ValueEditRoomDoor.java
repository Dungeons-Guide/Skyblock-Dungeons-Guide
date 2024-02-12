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
import kr.syeyoung.dungeonsguide.dungeon.data.OffsetPointSet;
import kr.syeyoung.dungeonsguide.dungeon.mechanics.DungeonRoomDoor2;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.EditingContext;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.Parameter;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEdit;
import kr.syeyoung.dungeonsguide.mod.dungeon.roomedit.valueedit.ValueEditCreator;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDungeonRooms;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.*;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.init.Blocks;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ValueEditRoomDoor extends MPanel implements ValueEdit<DungeonRoomDoor2> {
    private Parameter parameter;

    // scroll pane
    // just create
    // add set
    private final DungeonRoomDoor2 dungeonDoor;

    private final MLabel label;
    private final MValue<OffsetPointSet> value;
    private final MLabel label2;
    private final MValue<OffsetPoint> value2;

    public ValueEditRoomDoor(final Parameter parameter2) {
        this.parameter = parameter2;
        this.dungeonDoor = (DungeonRoomDoor2) parameter2.getNewData();


        label = new MLabel();
        label.setText("Wall Points");
        label.setAlignment(MLabel.Alignment.LEFT);
        add(label);

        value = new MValue(dungeonDoor.getBlocks(), Collections.emptyList());
        add(value);

        label2 = new MLabel();
        label2.setText("PF Point");
        label2.setAlignment(MLabel.Alignment.LEFT);
        add(label2);

        value2 = new MValue(dungeonDoor.getPfPoint(), Collections.emptyList());
        add(value2);
    }

    @Override
    public void onBoundsUpdate() {
        label.setBounds(new Rectangle(0,0,getBounds().width, 20));
        value.setBounds(new Rectangle(0,20,getBounds().width, 20));
        label2.setBounds(new Rectangle(0,40,getBounds().width, 20));
        value2.setBounds(new Rectangle(0, 60, getBounds().width, 20));
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

    public static class Generator implements ValueEditCreator<ValueEditRoomDoor> {

        @Override
        public ValueEditRoomDoor createValueEdit(Parameter parameter) {
            return new ValueEditRoomDoor(parameter);
        }

        @Override
        public Object createDefaultValue(Parameter parameter) {
            return new DungeonRoomDoor2();
        }

        @Override
        public Object cloneObj(Object object) {
            try {
                return ((DungeonRoomDoor2)object).clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            assert false;
            return null;
        }
    }
}
