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

package kr.syeyoung.dungeonsguide.mod.dungeon.roomedit;

import lombok.Data;

@Data
public class Parameter {
    private String name;
    private Object previousData;
    private Object newData;

    public Parameter(String name, Object previousData, Object newData) {
        this.name = name; this.previousData = previousData; this.newData = newData;
    }

    private Runnable onSetNewData;

    public void setNewData(Object newData) {
        this.newData = newData;
        if (onSetNewData != null) onSetNewData.run();
    }
}
