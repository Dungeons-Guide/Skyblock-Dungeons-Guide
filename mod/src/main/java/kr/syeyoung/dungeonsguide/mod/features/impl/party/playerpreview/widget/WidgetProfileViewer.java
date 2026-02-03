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

import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.player.PlayerManager;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.util.UUID;

public class WidgetProfileViewer extends AnnotatedWidget {

    @Bind(variableName = "width")
    public final BindableAttribute<Double> width = new BindableAttribute<>(Double.class, 500.0);
    @Bind(variableName = "height")
    public final BindableAttribute<Double> height = new BindableAttribute<>(Double.class, 500.0);
    @Bind(variableName = "actualPV")
    public final BindableAttribute<Widget> actualPV = new BindableAttribute<>(Widget.class, null);
    @Bind(variableName = "visible")
    public final BindableAttribute<String> visiblePage = new BindableAttribute<>(String.class, "fetching");
    private UUID uuid;
    private String name;
    private Runnable close;
    public WidgetProfileViewer(UUID uuid, String name, Runnable close) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/profileViewer/pv.gui"));
        this.uuid = uuid;
        this.name = name;
        this.close = close;
        refresh();
    }

    @On(functionName = "refresh")
    public void refresh() {
        actualPV.setValue(null);

        visiblePage.setValue("fetching");
        PlayerManager.INSTANCE.ping(uuid);
        ApiFetcher.fetchMostRecentProfileAsync(uuid.toString())
                .whenComplete((a,e) -> {
                    if (e != null) {
                        e.printStackTrace();
                        visiblePage.setValue("noPlayer");
                    } else {
                        if (a.isPresent()) {
                            actualPV.setValue(new WidgetProfileViewerData(uuid, name, a.get()));
                            visiblePage.setValue("pv");
                        } else {
                            visiblePage.setValue("noPlayer");
                        }
                    }
                });
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (PopupMgr.getPopupMgr(getDomElement()).getPopups().size() >= 2) return;
        close.run();
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
        return true;
    }
}
