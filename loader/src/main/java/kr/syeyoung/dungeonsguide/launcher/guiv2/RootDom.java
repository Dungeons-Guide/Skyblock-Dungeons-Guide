/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.launcher.guiv2;

import kr.syeyoung.dungeonsguide.launcher.guiv2.layouter.SingleChildPassingLayouter;
import kr.syeyoung.dungeonsguide.launcher.guiv2.renderer.SingleChildRenderer;
import kr.syeyoung.dungeonsguide.launcher.util.cursor.EnumCursor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RootDom extends DomElement {

    private static class DummyWidget extends Widget {
        @Override
        public List<Widget> build(DomElement buildContext) {
            return null;
        }
    }
    public RootDom(Widget widget) {
        context = new Context();
        setLayouter(SingleChildPassingLayouter.INSTANCE);
        setRenderer(SingleChildRenderer.INSTANCE);
        setWidget(new DummyWidget());

        DomElement element = widget.createDomElement(this);// and it's mounted!
        addElement(element);
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
        getContext().CONTEXT.put("focus", null);
        return super.mouseClicked0(absMouseX, absMouseY, relMouseX0, relMouseY0, mouseButton);
    }
}
