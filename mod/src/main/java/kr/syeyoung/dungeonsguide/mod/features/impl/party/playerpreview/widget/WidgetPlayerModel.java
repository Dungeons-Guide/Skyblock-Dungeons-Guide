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
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.FakePlayer;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.SkinFetcher;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.util.ResourceLocation;

public class WidgetPlayerModel extends AnnotatedWidget {

    @Bind(variableName = "visible")
    public final BindableAttribute<String> visible = new BindableAttribute<>(String.class, "fetching");

    @Bind(variableName = "playerRender")
    public final BindableAttribute<Widget> widgetBindable = new BindableAttribute<>(Widget.class, null);

    private volatile PlayerProfile sbProfile;
    private final GameProfile mcProfile;
    private volatile FakePlayer fakePlayer;
    private final PlayerModelRenderer renderer;
    public WidgetPlayerModel(GameProfile mcProfile, PlayerProfile sbProfile) {
        super(new ResourceLocation("dungeonsguide:gui/features/profileViewer/player.gui"));
        this.mcProfile = mcProfile;
        this.sbProfile = sbProfile;
        this.renderer = new PlayerModelRenderer(null);
        refresh();
    }

    public void setSbProfile(PlayerProfile sbProfile) {
        this.sbProfile = sbProfile;
        if (this.fakePlayer != null)
            this.fakePlayer.setSkyblockProfile(sbProfile);
    }

    @On(functionName = "refresh")
    public void refresh() {
        fakePlayer = null;
        widgetBindable.setValue(null);
        renderer.setFakePlayer(null);
        visible.setValue("fetching");
        SkinFetcher.getSkinSet(mcProfile)
                .whenComplete((a,e) ->{
                    if (e != null){
                        e.printStackTrace();
                        visible.setValue("noPlayer");
                    } else {
                        fakePlayer = new FakePlayer(
                                mcProfile, a, sbProfile
                        );
                        renderer.setFakePlayer(fakePlayer);
                        widgetBindable.setValue(renderer);
                        visible.setValue("player");
                    }
                });
    }
}
