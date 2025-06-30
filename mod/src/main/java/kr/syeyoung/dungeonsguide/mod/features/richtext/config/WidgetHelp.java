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

package kr.syeyoung.dungeonsguide.mod.features.richtext.config;

import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.RawMinecraftTooltip;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.util.Arrays;

public class WidgetHelp extends AnnotatedImportOnlyWidget {
    public WidgetHelp() {
        super(new ResourceIdentifier("dungeonsguide:gui/config/text/help.gui"));
    }

    private RawMinecraftTooltip actualTooltip = new RawMinecraftTooltip();
    private boolean tooltipShown;

    @Override
    public void mouseEntered(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (!this.tooltipShown) {
            actualTooltip.setTooltip(Arrays.asList("Sorry, I tried my best designing this gui to be as intuitive as possible, but it seems like I failed doing so",
                    "Toggling the checkbox on the left overrides the inherited settings",
                    "And the checkbox on right is actual settings to override as",
                    "If checkbox is disabled, the shown value is the value that is currently applied to text",
                    "MC Default: means that the color will be calculated based on visible textColor automatically"));

            PopupMgr.getPopupMgr(getDomElement())
                    .openPopup(actualTooltip, (a) -> {
                        this.tooltipShown = false;
                    });
            tooltipShown = true;
        }
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (this.tooltipShown) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(actualTooltip, null);
            tooltipShown = false;
        }
    }

    @Override
    public void onUnmount() {
        if (this.tooltipShown) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(actualTooltip, null);
            tooltipShown = false;
        }
        super.onUnmount();
    }
}
