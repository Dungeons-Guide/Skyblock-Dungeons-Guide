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

package kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteTooltip;

import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

public class WidgetEnableAskToJoin extends AnnotatedImportOnlyWidget {
    public WidgetEnableAskToJoin() {
        super(new ResourceIdentifier("dungeonsguide:gui/features/discord_invite/enable_ask_to_join.gui"));
    }

    @On(functionName = "enable")
    public void enable() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        PartyManager.INSTANCE.toggleAllowAskToJoin();
        PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
        if (popupMgr != null) popupMgr.closePopup(true);
    }
    @On(functionName = "cancel")
    public void cancel() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
        if (popupMgr != null) popupMgr.closePopup(null);
    }
}
