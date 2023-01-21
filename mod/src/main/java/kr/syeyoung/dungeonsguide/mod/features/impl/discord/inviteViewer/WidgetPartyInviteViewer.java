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

package kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteViewer;

import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserInvitedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserJoinRequestEvent;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Column;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class WidgetPartyInviteViewer extends AnnotatedWidget {
    @Bind(variableName = "listApi")
    public final BindableAttribute<Column> columnApi = new BindableAttribute<>(Column.class);

    public WidgetPartyInviteViewer() {
        super(new ResourceLocation("dungeonsguide:gui/features/discordParty/partyInviteList.gui"));
    }

    private final Set<String> joinReqUid = Collections.synchronizedSet( new HashSet<>());
    private final Set<String> inviteUid =  Collections.synchronizedSet( new HashSet<>());
    private final List<Widget> widgetList = new CopyOnWriteArrayList<>();
    public void addJoinRequest(DiscordUserJoinRequestEvent joinRequest) {
        if (joinReqUid.contains(joinRequest.getDiscordUser().getId())) return;
        joinReqUid.add(joinRequest.getDiscordUser().getId());
        WidgetJoinRequest request;
        columnApi.getValue().addWidget(request = new WidgetJoinRequest(this, joinRequest));
        widgetList.add(request);
    }

    public void addInvite(DiscordUserInvitedEvent inviteEvent) {
        if (inviteUid.contains(inviteEvent.getDiscordUser().getId())) return;
        WidgetInvite invite;
        columnApi.getValue().addWidget(invite = new WidgetInvite(this, inviteEvent));
        widgetList.add(invite);
        inviteUid.add(inviteEvent.getDiscordUser().getId());
    }

    public void remove(Widget widget) {
        columnApi.getValue().removeWidget(widget);
        widgetList.remove(widget);

        if (widget instanceof WidgetJoinRequest)
            joinReqUid.remove(((WidgetJoinRequest) widget).getEvent().getDiscordUser().getId());
        if (widget instanceof WidgetInvite)
            inviteUid.remove(((WidgetInvite) widget).getEvent().getDiscordUser().getId());
    }

    public void tick() {
        List<Widget> toRemove = new ArrayList<>();
        for (Widget widget : widgetList) {
            TTL ttl = (TTL) widget;
            if (ttl.startedDisplaying() + ttl.ttl() < System.currentTimeMillis()) {
                toRemove.add(widget);
            }
        }
        toRemove.forEach(this::remove);
    }
}
