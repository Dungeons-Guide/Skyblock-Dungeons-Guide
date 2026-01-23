/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.customgui;

import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class WidgetHoverTooltip extends Widget {
    private BiFunction<Double, Double, Widget> popupSupplier;

    public WidgetHoverTooltip( BiFunction<Double, Double, Widget> popupSupplier) {
        this.popupSupplier = popupSupplier;
    }
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    private Widget tooltip = null;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX, double relMouseY, boolean childHandled) {
        if (childHandled) return false;
        if (this.tooltip == null)
            PopupMgr.getPopupMgr(getDomElement())
                    .openPopup(this.tooltip = popupSupplier.apply((double) absMouseX, (double) absMouseY), (a) -> {
                        this.tooltip = null;
                    });
        return false;
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        }
    }

    @Override
    public void onUnmount() {
        if (this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        }
        super.onUnmount();
    }
}
