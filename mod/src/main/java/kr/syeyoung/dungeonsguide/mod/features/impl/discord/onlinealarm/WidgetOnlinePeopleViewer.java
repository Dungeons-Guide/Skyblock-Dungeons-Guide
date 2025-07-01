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

package kr.syeyoung.dungeonsguide.mod.features.impl.discord.onlinealarm;

import kr.syeyoung.dungeonsguide.mod.discord.JDiscordRelation;
import kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteViewer.TTL;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Column;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class WidgetOnlinePeopleViewer extends AnnotatedWidget {
    @Bind(variableName = "listApi")
    public final BindableAttribute<Column> columnApi = new BindableAttribute<>(Column.class);

    public WidgetOnlinePeopleViewer() {
        super(new ResourceIdentifier("dungeonsguide:gui/features/discordOnline/discordOnlineList.gui"));
    }

    private final Set<String> onlineUid = Collections.synchronizedSet( new HashSet<>());
    private List<Widget> widgetList = new CopyOnWriteArrayList<>();
    public void addUser(JDiscordRelation joinRequest) {
        if (onlineUid.contains(joinRequest.getDiscordUser().getId())) return;
        onlineUid.add(joinRequest.getDiscordUser().getId());
        WidgetOnline online;
        columnApi.getValue().addWidget(online = new WidgetOnline(this, joinRequest));
        widgetList.add(online);
    }


    public void remove(Widget widget) {
        columnApi.getValue().removeWidget(widget);
        widgetList.remove(widget);

        if (widget instanceof WidgetOnline)
            onlineUid.remove(((WidgetOnline) widget).getRelation().getDiscordUser().getId());
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
