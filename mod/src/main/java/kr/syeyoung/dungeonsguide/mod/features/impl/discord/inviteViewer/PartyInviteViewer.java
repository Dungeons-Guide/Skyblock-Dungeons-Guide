/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserInvitedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserJoinRequestEvent;
import kr.syeyoung.dungeonsguide.mod.features.AbstractGuiFeature;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import kr.syeyoung.dungeonsguide.mod.overlay.WholeScreenPositioner;
import net.minecraft.client.Minecraft;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PartyInviteViewer extends AbstractGuiFeature {
    private WidgetPartyInviteViewer partyInviteViewer;
    private OverlayWidget widget;
    public PartyInviteViewer() {
        super("Discord", "Party Invite Viewer","Simply type /dg asktojoin or /dg atj to toggle whether ask-to-join would be presented as option on discord!\n\nRequires Discord RPC to be enabled", "discord.discord_party_invite_viewer");

        addParameter("ttl", new FeatureParameter<Integer>("ttl", "Request Duration", "The duration after which the requests will be dismissed automatically. The value is in seconds.", 15, "integer"));
    }

    @Override
    public OverlayWidget instantiateWidget() {
        return new OverlayWidget(
                partyInviteViewer = new WidgetPartyInviteViewer(),
                OverlayType.OVER_ANY,
                new WholeScreenPositioner()
        );
    }

    @Override
    public boolean isDisyllable() {
        return false;
    }

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onTick(DGTickEvent tickEvent) {
        try {
            partyInviteViewer.tick();
        } catch (Throwable e) {e.printStackTrace();}
    }

    ExecutorService executorService = Executors.newFixedThreadPool(3, DungeonsGuide.THREAD_FACTORY);

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onDiscordUserJoinRequest(DiscordUserJoinRequestEvent event) {
        partyInviteViewer.addJoinRequest(event);
    }
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onDiscordUserJoinRequest(DiscordUserInvitedEvent event) {
        if (SkyblockStatus.isOnHypixel())
            partyInviteViewer.addInvite(event);
    }
}
