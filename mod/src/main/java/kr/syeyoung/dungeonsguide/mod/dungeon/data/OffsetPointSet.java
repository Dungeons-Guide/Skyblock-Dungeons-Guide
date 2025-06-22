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

package kr.syeyoung.dungeonsguide.mod.dungeon.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OffsetPointSet implements Cloneable {
    @JsonValue
    private List<OffsetPoint> offsetPointList;

    public OffsetPointSet() {
        this.offsetPointList = new ArrayList<>();
    }

    @JsonCreator
    public OffsetPointSet(List<OffsetPoint> offsetPointList) {
        this.offsetPointList = offsetPointList;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        OffsetPointSet ops = new OffsetPointSet();
        for (OffsetPoint offsetPoint : offsetPointList) {
            ops.offsetPointList.add((OffsetPoint) offsetPoint.clone());
        }
        return ops;
    }
}
