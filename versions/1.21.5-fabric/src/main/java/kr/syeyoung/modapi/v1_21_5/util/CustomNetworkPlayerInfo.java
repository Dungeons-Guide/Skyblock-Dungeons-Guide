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

package kr.syeyoung.modapi.v1_21_5.util;

import com.google.common.collect.Iterators;
import com.mojang.authlib.GameProfile;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.event.events.TabNameFormatEvent;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

// Reimplement this using ASM. Removing reference to this is too painful.
public class CustomNetworkPlayerInfo extends NetworkPlayerInfo {
    public CustomNetworkPlayerInfo(GameProfile gameProfile) {
        super(gameProfile);
    }

    public CustomNetworkPlayerInfo(S38PacketPlayerListItem.AddPlayerData playerData) {
        super(playerData);
        setDisplayName(super.getDisplayName());
    }


    private IChatComponent displayName;
    private Component displayNameComp;

    private String formatted;

    public IChatComponent getOriginalDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(IChatComponent displayNameIn) {
        displayName = displayNameIn;
        if (displayName == null) {
            formatted = null;
            displayNameComp = null;
            return;
        }

        formatted = displayName.getFormattedText();
        displayNameComp = GsonComponentSerializer.colorDownsamplingGson().deserialize(IChatComponent.Serializer.componentToJson(displayNameIn));
    }


    public IChatComponent getDisplayName()
    {
        String rawPlayerString = formatted != null ? formatted : ScorePlayerTeam.formatPlayerName(super.getPlayerTeam(), super.getGameProfile().getName());

        TabNameFormatEvent formatEvent = new TabNameFormatEvent(
                getGameProfile().getName(),
                ScorePlayerTeam.formatPlayerName(super.getPlayerTeam(), super.getGameProfile().getName()),
                displayNameComp,
                rawPlayerString
        );
        ModAPI.getAPI().getEventBus().fireEvent(formatEvent);
        if (formatEvent.getDisplayName().equals(rawPlayerString)) return displayName;
        return new MarkedChatComponent(formatted == null ? rawPlayerString : displayName.getUnformattedText(), formatEvent.getDisplayName());
    }

    public static class MarkedChatComponent implements IChatComponent {
        @Setter
        private String unformatted;
        @Setter
        private String formatted;

        public MarkedChatComponent(String unformatted, String formatted) {
            this.unformatted = unformatted;
            this.formatted = formatted;
        }


        @Override
        public IChatComponent setChatStyle(ChatStyle style) {
            return this;
        }

        @Override
        public ChatStyle getChatStyle() {
            return new ChatStyle();
        }

        @Override
        public IChatComponent appendText(String text) {
            return this;
        }

        @Override
        public IChatComponent appendSibling(IChatComponent component) {
            return this;
        }

        @Override
        public String getUnformattedTextForChat() {
            return unformatted;
        }

        @Override
        public String getUnformattedText() {
            return unformatted;
        }

        @Override
        public String getFormattedText() {
            return formatted;
        }

        @Override
        public List<IChatComponent> getSiblings() {
            return Collections.emptyList();
        }

        @Override
        public IChatComponent createCopy() {
            return new MarkedChatComponent(formatted, unformatted);
        }

        @Override
        public @NotNull Iterator<IChatComponent> iterator() {
            return Iterators.forArray(this);
        }
    }

}
