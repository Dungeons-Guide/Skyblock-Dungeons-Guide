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

import kr.syeyoung.dungeonsguide.mod.discord.DiscordIntegrationManager;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserJoinRequestEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.util.ResourceLocation;

public class WidgetJoinRequest extends AnnotatedWidget implements TTL{

    @Bind(variableName = "username")
    public final BindableAttribute<String> username;
    @Bind(variableName = "discriminator")
    public final BindableAttribute<String> discriminator;
    @Bind(variableName = "avatarUrl")
    public final BindableAttribute<String> avatarURL;
    @Bind(variableName = "visible")
    public final BindableAttribute<String> visiblePage = new BindableAttribute<>(String.class, "buttons");

    private WidgetPartyInviteViewer inviteViewer;
    private DiscordUserJoinRequestEvent event;
    private long start;
    private boolean actionDone = false;
    public WidgetJoinRequest(WidgetPartyInviteViewer parent, DiscordUserJoinRequestEvent joinRequestEvent) {
        super(new ResourceLocation("dungeonsguide:gui/features/discordParty/joinRequest.gui"));
        this.inviteViewer = parent;
        this.event = joinRequestEvent;
        start = System.currentTimeMillis();
        username = new BindableAttribute<>(String.class, joinRequestEvent.getDiscordUser().getName());
        discriminator = new BindableAttribute<>(String.class, joinRequestEvent.getDiscordUser().getDiscriminator());
        avatarURL = new BindableAttribute<>(String.class, joinRequestEvent.getDiscordUser().getEffectiveAvatarUrl());
    }

    public DiscordUserJoinRequestEvent getEvent() {
        return event;
    }

    @On(functionName = "accept")
    public void accept() {
        start = System.currentTimeMillis();
        visiblePage.setValue("accepted");
        actionDone = true;
        DiscordIntegrationManager.INSTANCE.respondToJoinRequest(event.getDiscordUser().getId(), Reply.ACCEPT);
    }
    @On(functionName = "deny")
    public void deny() {
        start = System.currentTimeMillis();
        visiblePage.setValue("denied");
        actionDone = true;
        DiscordIntegrationManager.INSTANCE.respondToJoinRequest(event.getDiscordUser().getId(), Reply.DENY);
    }
    @On(functionName = "ignore")
    public void ignore() {
        start = System.currentTimeMillis();
        visiblePage.setValue("ignored");
        actionDone = true;
        DiscordIntegrationManager.INSTANCE.respondToJoinRequest(event.getDiscordUser().getId(), Reply.IGNORE);
    }

    @Override
    public long startedDisplaying() {
        return start;
    }

    @Override
    public long ttl() {
        return actionDone ? 2000 : FeatureRegistry.DISCORD_ASKTOJOIN.<Integer>getParameter("ttl").getValue() * 1000;
    }
}
