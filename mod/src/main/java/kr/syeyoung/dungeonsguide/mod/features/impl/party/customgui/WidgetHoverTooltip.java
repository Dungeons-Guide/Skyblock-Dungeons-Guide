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

import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.MinecraftTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.MouseTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayManager;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class WidgetHoverTooltip extends Widget {
    private Supplier<Widget> popupSupplier;

    public WidgetHoverTooltip( Supplier<Widget> popupSupplier) {
        this.popupSupplier = popupSupplier;
    }
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    private MouseTooltip tooltip = null;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (this.tooltip == null)
            PopupMgr.getPopupMgr(getDomElement())
                    .openPopup(this.tooltip = new MouseTooltip(popupSupplier.get()), (a) -> {
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
