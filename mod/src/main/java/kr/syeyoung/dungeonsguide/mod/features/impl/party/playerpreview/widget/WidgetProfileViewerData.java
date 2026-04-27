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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.widget;

import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.PlayerSkyblockData;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.DataRendererRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.IDataRenderer;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.AbsLocationPopup;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.gui.xml.data.WidgetList;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WidgetProfileViewerData extends AnnotatedWidget {
    @Bind(variableName = "playerModel")
    public final BindableAttribute<Widget> playerModel = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "datarenderers")
    public final BindableAttribute renderers = new BindableAttribute(WidgetList.class);

    @Bind(variableName = "invButtonRef")
    public final BindableAttribute<DomElement> inventoryButton = new BindableAttribute<>(DomElement.class);

    private final PlayerSkyblockData playerSkyblockData;
    private int idx;

    private final UUID uuid;
    private final String name;


    private final WidgetPlayerModel widgetPlayerModel;

    private List<WidgetDataRendererWrapper> dataRendererWrapperList = new ArrayList<>();

    public WidgetProfileViewerData(UUID uuid, String name, PlayerSkyblockData playerSkyblockData) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/profile_viewer/data.gui"));

        this.playerSkyblockData = playerSkyblockData;
        this.uuid = uuid;
        this.name = name;

        playerModel.setValue(widgetPlayerModel = new WidgetPlayerModel(uuid, name, playerSkyblockData.getPlayerProfiles()
                [idx = playerSkyblockData.getLatestProfileArrayIndex()]));

        List<String> stuff = FeatureRegistry.PARTYKICKER_VIEWPLAYER.<List<String>>getParameter("datarenderers").getValue();
        for (String datarenderer : stuff) {
            IDataRenderer dataRenderer = DataRendererRegistry.getDataRenderer(datarenderer);
            dataRendererWrapperList.add(new WidgetDataRendererWrapper(playerSkyblockData.getPlayerProfiles()[idx], dataRenderer));
        }

        renderers.setValue(dataRendererWrapperList);
    }


    @On(functionName = "switchProfile")
    public void switchToNext() {
        idx = (idx + 1) % playerSkyblockData.getPlayerProfiles().length;
        widgetPlayerModel.setSbProfile(playerSkyblockData.getPlayerProfiles()[idx]);
        for (WidgetDataRendererWrapper widgetDataRendererWrapper : dataRendererWrapperList) {
            widgetDataRendererWrapper.setProfile(playerSkyblockData.getPlayerProfiles()[idx]);
        }
    }

    @On(functionName = "invite")
    public void invite() {
        ChatProcessor.INSTANCE.addToChatQueue("/p invite " + name, () -> {
        }, true);
    }
    @On(functionName = "kick")
    public void kick() {
        ChatProcessor.INSTANCE.addToChatQueue("/p kick " + name, () -> {
        }, true);
    }


    private AbsLocationPopup popup;
    @On(functionName = "openInventory")
    public void openStates() {
        Rect abs = inventoryButton.getValue().getAbsBounds();
        double x = abs.getX() + abs.getWidth();
        double y = abs.getY();

        if (popup == null) {
            PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
            popupMgr.openPopup(popup = new AbsLocationPopup(x, y, new WidgetPlayerInventory(
                    playerSkyblockData.getPlayerProfiles()[idx]
            ), true), (a) -> {
                this.popup = null;
            });
            popup.cursorPassthrough = false;
        }
    }

    @Override
    public void onUnmount() {
        PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
        if (popup != null) {
            popupMgr.closePopup(popup, null);
            popup = null;
        }
        super.onUnmount();
    }

}
