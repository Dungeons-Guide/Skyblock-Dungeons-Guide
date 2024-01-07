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

import com.jagrosh.discordipc.entities.Callback;
import kr.syeyoung.dungeonsguide.mod.discord.DiscordIntegrationManager;
import kr.syeyoung.dungeonsguide.mod.discord.JDiscordRelation;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WidgetInvite extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "search")
    public final BindableAttribute<String> search = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "friendList")
    public final BindableAttribute<Column> friendList = new BindableAttribute<>(Column.class);

    @Bind(variableName = "initialFriendList")
    public final BindableAttribute initialFriendList = new BindableAttribute(WidgetList.class);

    private Set<String> invited = new HashSet<>();

    public void invite(String id) {
        invited.add(id);
        DiscordIntegrationManager.INSTANCE.sendInvite(id, "Come join our party!");
    }

    public WidgetInvite() {
        super(new ResourceLocation("dungeonsguide:gui/features/discordInvite/invite_discord.gui"));
        List<Widget> widgets = new ArrayList<>();
        for (JDiscordRelation value : DiscordIntegrationManager.INSTANCE.getRelationMap().values()) {
            if (value.getRelationType() == JDiscordRelation.DiscordRelationType.Blocked) continue;
            widgets.add(new WidgetDiscordFriend(value, false, this::invite));
        }
        initialFriendList.setValue(widgets);
        search.addOnUpdate((old,neu) -> resetListContent());
    }


    private void resetListContent() {
        if (!getDomElement().isMounted()) return;
        friendList.getValue().removeAllWidget();

        String searchTxt = search.getValue().trim().toLowerCase();
        for (JDiscordRelation value : DiscordIntegrationManager.INSTANCE.getRelationMap().values()) {
            System.out.println(value);
//            if (value.getDiscordActivity().getApplicationId() != 816298079732498473L) continue;
            if (value.getRelationType() == JDiscordRelation.DiscordRelationType.Blocked) continue;
            if (!searchTxt.isEmpty() && !(value.getDiscordUser().getName().toLowerCase().contains(searchTxt))) continue;
            friendList.getValue().addWidget(new WidgetDiscordFriend(value, invited.contains(value.getDiscordUser().getId()), this::invite));
        }
    }
}
