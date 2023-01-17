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
import kr.syeyoung.dungeonsguide.mod.discord.DiscordIntegrationManager;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.events.impl.DGTickEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserInvitedEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.DiscordUserJoinRequestEvent;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.image.ImageTexture;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayManager;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class PartyInviteViewer extends SimpleFeature {
    private WidgetPartyInviteViewer partyInviteViewer;
    private OverlayWidget widget;
    public PartyInviteViewer() {
        super("Discord", "Party Invite Viewer","Simply type /dg asktojoin or /dg atj to toggle whether ask-to-join would be presented as option on discord!\n\nRequires Discord RPC to be enabled", "discord.party_invite_viewer");

        addParameter("ttl", new FeatureParameter<Integer>("ttl", "Request Duration", "The duration after which the requests will be dismissed automatically. The value is in seconds.", 15, "integer"));
        widget = new OverlayWidget(
                partyInviteViewer = new WidgetPartyInviteViewer(),
                OverlayType.OVER_ANY,
                () -> new Rect(0,0,Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight)
        );
        OverlayManager.getInstance().addOverlay(widget);
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
        partyInviteViewer.addInvite(event);
    }
}
