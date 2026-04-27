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

import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.entity.UEntityPlayerFake;

import java.util.UUID;

public class WidgetPlayerModel extends AnnotatedWidget {

    @Bind(variableName = "visible")
    public final BindableAttribute<String> visible = new BindableAttribute<>(String.class, "fetching");

    @Bind(variableName = "playerRender")
    public final BindableAttribute<Widget> widgetBindable = new BindableAttribute<>(Widget.class, null);

    private volatile PlayerProfile sbProfile;
    private final UUID uuid;
    private final String name;
    private UEntityPlayerFake fakePlayer;
    private final PlayerModelRenderer renderer;
    public WidgetPlayerModel(UUID uuid, String name, PlayerProfile sbProfile) {
        super(new ResourceIdentifier("dungeonsguide:gui/features/profile_viewer/player.gui"));
        this.uuid = uuid;
        this.name = name;
        this.sbProfile = sbProfile;
        this.renderer = new PlayerModelRenderer(null, null);
        refresh();
    }

    public void setSbProfile(PlayerProfile sbProfile) {
        this.sbProfile = sbProfile;
        if (this.fakePlayer != null) {
            this.renderer.setSkyblockProfile(sbProfile);
        }
    }

    @On(functionName = "refresh")
    public void refresh() {
        fakePlayer = null;
        widgetBindable.setValue(null);
        renderer.setFakePlayer(null);
        visible.setValue("fetching");

        fakePlayer = ModAPI.getAPI().createFakePlayer(uuid, name);
        renderer.setFakePlayer(fakePlayer);
        renderer.setSkyblockProfile(sbProfile);
        widgetBindable.setValue(renderer);
        visible.setValue("player");
//
//        SkinFetcher.getSkinSet(mcProfile)
//                .whenComplete((a,e) ->{
//                    if (e != null){
//                        e.printStackTrace();
//                        visible.setValue("noPlayer");
//                    } else {
//
////                        = new FakePlayer(
//                                mcProfile, a, sbProfile
////                        );
//                        UEntityPlayerFake player
//
//                    }
//                });
    }
}
