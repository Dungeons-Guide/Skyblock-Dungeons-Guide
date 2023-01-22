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

import com.mojang.authlib.GameProfile;
import kr.syeyoung.dungeonsguide.mod.chat.ChatProcessor;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.PlayerSkyblockData;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.DataRendererRegistry;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.IDataRenderer;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.mechanicbrowser.WidgetStateTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.LocationedPopup;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Scaler;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.DomElementRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.ExportedWidgetConverter;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WidgetProfileViewerData extends AnnotatedWidget {
    @Bind(variableName = "playerModel")
    public final BindableAttribute<Widget> playerModel = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "datarenderers")
    public final BindableAttribute renderers = new BindableAttribute(WidgetList.class);

    @Bind(variableName = "invButtonRef")
    public final BindableAttribute<DomElement> inventoryButton = new BindableAttribute<>(DomElement.class);

    private final PlayerSkyblockData playerSkyblockData;
    private int idx;

    private final GameProfile gameProfile;


    private final WidgetPlayerModel widgetPlayerModel;

    private List<WidgetDataRendererWrapper> dataRendererWrapperList = new ArrayList<>();

    public WidgetProfileViewerData(GameProfile gameProfile, PlayerSkyblockData playerSkyblockData) {
        super(new ResourceLocation("dungeonsguide:gui/features/profileViewer/data.gui"));

        this.playerSkyblockData = playerSkyblockData;
        this.gameProfile = gameProfile;

        playerModel.setValue(widgetPlayerModel = new WidgetPlayerModel(gameProfile, playerSkyblockData.getPlayerProfiles()
                [idx = playerSkyblockData.getLastestprofileArrayIndex()]));

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
        ChatProcessor.INSTANCE.addToChatQueue("/p invite " + gameProfile.getName(), () -> {
        }, true);
    }
    @On(functionName = "kick")
    public void kick() {
        ChatProcessor.INSTANCE.addToChatQueue("/p kick " + gameProfile.getName(), () -> {
        }, true);
    }


    private LocationedPopup popup;
    @On(functionName = "openInventory")
    public void openStates() {
        Rect abs = inventoryButton.getValue().getAbsBounds();
        double x = abs.getX() + abs.getWidth();
        double y = abs.getY();

        if (popup == null) {
            PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
            Scaler scaler = new Scaler();
            scaler.scale.setValue((double) new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());
            scaler.child.setValue(new WidgetPlayerInventory(
                    playerSkyblockData.getPlayerProfiles()[idx]
            ));
            popupMgr.openPopup(popup = new LocationedPopup(x, y, scaler), (a) -> {
                this.popup = null;
            });
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
