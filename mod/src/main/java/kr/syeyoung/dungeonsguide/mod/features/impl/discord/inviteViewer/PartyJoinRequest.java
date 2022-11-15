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

import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.GameSDK;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.awt.*;

@Data
public class PartyJoinRequest {
    private DiscordUser discordUser;

    public void setDiscordUser(DiscordUser discordUser) {
        this.discordUser = discordUser;
        username = GameSDK.readString(discordUser.username);
        discriminator = GameSDK.readString(discordUser.discriminator);
        avatar = GameSDK.readString(discordUser.avatar);
    }

    private String username, discriminator, avatar;
    private long expire;

    private Rectangle wholeRect = new Rectangle();
    private Rectangle acceptRect = new Rectangle();
    private Rectangle denyRect = new Rectangle();
    private Rectangle ignoreRect = new Rectangle();

    private boolean isInvite;
    private int ttl = -1;
    private Reply reply;

    @AllArgsConstructor
    public enum Reply {
        ACCEPT("Accepted"), DENY("Denied"), IGNORE("Ignored");

        @Getter
        private final String past;
    }
}
