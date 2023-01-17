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
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserInvitedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserJoinRequestEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.util.ResourceLocation;

public class WidgetInvite extends AnnotatedWidget implements TTL {

    @Bind(variableName = "username")
    public final BindableAttribute<String> username;
    @Bind(variableName = "discriminator")
    public final BindableAttribute<String> discriminator;
    @Bind(variableName = "avatarUrl")
    public final BindableAttribute<String> avatarURL;
    private WidgetPartyInviteViewer inviteViewer;
    private DiscordUserInvitedEvent event;
    private long start;
    public WidgetInvite(WidgetPartyInviteViewer parent, DiscordUserInvitedEvent invitedEvent) {
        super(new ResourceLocation("dungeonsguide:gui/features/discordParty/invite.gui"));
        this.inviteViewer = parent;
        this.event = invitedEvent;
        this.start = System.currentTimeMillis();
        username = new BindableAttribute<>(String.class, invitedEvent.getDiscordUser().getName());
        discriminator = new BindableAttribute<>(String.class, invitedEvent.getDiscordUser().getDiscriminator());
        avatarURL = new BindableAttribute<>(String.class, invitedEvent.getDiscordUser().getEffectiveAvatarUrl());
    }

    public DiscordUserInvitedEvent getEvent() {
        return event;
    }

    @On(functionName = "accept")
    public void accept() {
        inviteViewer.remove(this);
        DiscordIntegrationManager.INSTANCE.acceptInvite(event.getHandle());
    }
    @On(functionName = "deny")
    public void deny() {
        inviteViewer.remove(this);
    }

    @Override
    public long startedDisplaying() {
        return 0;
    }

    @Override
    public long ttl() {
        return FeatureRegistry.DISCORD_ASKTOJOIN.<Integer>getParameter("ttl").getValue() * 1000;
    }
}
