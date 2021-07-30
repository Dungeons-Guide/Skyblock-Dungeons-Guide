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

package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.cursor.EnumCursor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class MRootPanel extends MPanel {
    @Getter
    private List<MTooltip> tooltips = new CopyOnWriteArrayList<>();


    @Override
    public void openTooltip(MTooltip mPanel) {
        mPanel.setRoot(this);
        tooltips.add(mPanel);
        add(mPanel);
    }

    @Override
    public int getTooltipsOpen() {
        return tooltips.size();
    }

    public void removeTooltip(MTooltip mTooltip) {
        mTooltip.setRoot(null);
        tooltips.remove(mTooltip);
        remove(mTooltip);
    }

    @Getter @Setter
    private EnumCursor currentCursor = EnumCursor.DEFAULT;
    @Override
    public void setCursor(EnumCursor enumCursor) {
        currentCursor = enumCursor;
    }
}
