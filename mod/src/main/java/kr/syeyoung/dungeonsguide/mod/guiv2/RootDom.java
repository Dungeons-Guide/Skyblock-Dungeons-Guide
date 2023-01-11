/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2;

import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.SingleChildPassingLayouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import lombok.Getter;
import lombok.Setter;

public class RootDom extends DomElement {
    public RootDom(Widget widget) {
        context = new Context();
        setLayouter(SingleChildPassingLayouter.INSTANCE);
        setRenderer(SingleChildRenderer.INSTANCE);


        DomElement domElement = widget.createDomElement(this);
        this.addElement(domElement); // and it's mounted!
    }

    @Getter
    private EnumCursor currentCursor;

    @Override
    public void setCursor(EnumCursor enumCursor) {
        currentCursor = enumCursor;
    }

    @Getter
    @Setter
    private boolean relayoutRequested;

    @Override
    public void requestRelayout() {
        relayoutRequested = true;
    }

    @Override
    public boolean mouseClicked0(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, int mouseButton) {
        boolean res = super.mouseClicked0(absMouseX, absMouseY, relMouseX0, relMouseY0, mouseButton);
        if (!res)
            requestRelayout();
        return res;
    }
}
