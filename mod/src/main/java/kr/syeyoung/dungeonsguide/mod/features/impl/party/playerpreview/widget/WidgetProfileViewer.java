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
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.ApiFetcher;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.util.ResourceLocation;

public class WidgetProfileViewer extends AnnotatedWidget {

    @Bind(variableName = "width")
    public final BindableAttribute<Double> width = new BindableAttribute<>(Double.class, 500.0);
    @Bind(variableName = "height")
    public final BindableAttribute<Double> height = new BindableAttribute<>(Double.class, 500.0);
    @Bind(variableName = "actualPV")
    public final BindableAttribute<Widget> actualPV = new BindableAttribute<>(Widget.class, null);
    @Bind(variableName = "visible")
    public final BindableAttribute<String> visiblePage = new BindableAttribute<>(String.class, "fetching");
    private GameProfile gameProfile;
    private Runnable close;
    public WidgetProfileViewer(GameProfile gameProfile, Runnable close) {
        super(new ResourceLocation("dungeonsguide:gui/features/profileViewer/pv.gui"));
        this.gameProfile = gameProfile;
        this.close = close;
        refresh();
    }

    @On(functionName = "refresh")
    public void refresh() {
        actualPV.setValue(null);

        visiblePage.setValue("fetching");
        ApiFetcher.fetchMostRecentProfileAsync(gameProfile.getId().toString())
                .whenComplete((a,e) -> {
                    if (e != null) {
                        e.printStackTrace();
                        visiblePage.setValue("noPlayer");
                    } else {
                        if (a.isPresent()) {
                            actualPV.setValue(new WidgetProfileViewerData(gameProfile, a.get()));
                            visiblePage.setValue("pv");
                        } else {
                            visiblePage.setValue("noPlayer");
                        }
                    }
                });
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        close.run();
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton) {
        return true;
    }
}
