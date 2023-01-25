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

package kr.syeyoung.dungeonsguide.mod.features.impl.discord.onlinealarm;


import kr.syeyoung.dungeonsguide.mod.discord.JDiscordRelation;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserUpdateEvent;
import kr.syeyoung.dungeonsguide.mod.features.AbstractGuiFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import kr.syeyoung.dungeonsguide.mod.overlay.WholeScreenPositioner;
import net.minecraft.client.Minecraft;

public class PlayingDGAlarm extends AbstractGuiFeature {
    private WidgetOnlinePeopleViewer onlinePeopleViewer;
    private OverlayWidget widget;

    public PlayingDGAlarm() {
        super("Discord", "Friend Online Notification","Notifies you in bottom when your discord friend has launched a Minecraft with DG!\n\nRequires the Friend's Discord RPC to be enabled", "discord.discord_playingalarm");
    }

    @Override
    public OverlayWidget instantiateWidget() {
        return new OverlayWidget(
                onlinePeopleViewer = new WidgetOnlinePeopleViewer(),
                OverlayType.OVER_ANY,
                new WholeScreenPositioner()
        );
    }

    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onTick(DGTickEvent event) {
        try {
            onlinePeopleViewer.tick();
        } catch (Throwable e) {e.printStackTrace();}
    }
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onDiscordUserUpdate(DiscordUserUpdateEvent event) {
        JDiscordRelation prev = event.getPrev(), current = event.getCurrent();
        if (prev == null) return;
        if (!isDisplayable(prev) && isDisplayable(current)) {
            onlinePeopleViewer.addUser(current);
        }
    }

    public boolean isDisplayable(JDiscordRelation jDiscordRelation) {
        JDiscordRelation.DiscordRelationType relationshipType = jDiscordRelation.getRelationType();
//        if (relationshipType == JDiscordRelation.DiscordRelationType.Blocked) return false;
//        if (relationshipType == JDiscordRelation.DiscordRelationType.None) return false;
//        if (relationshipType == JDiscordRelation.DiscordRelationType.PendingIncoming) return false;
//        if (relationshipType == JDiscordRelation.DiscordRelationType.PendingOutgoing) return false;
        if (relationshipType != JDiscordRelation.DiscordRelationType.Friend) return false;

        return "816298079732498473".equals(jDiscordRelation.getApplicationId());
    }
}
