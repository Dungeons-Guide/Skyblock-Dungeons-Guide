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

import com.jagrosh.discordipc.entities.User;
import kr.syeyoung.dungeonsguide.mod.discord.rpc.RequestHandle;
import kr.syeyoung.dungeonsguide.mod.discord.rpc.RequestHandle2;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.awt.*;

@Data
public class PartyJoinRequest {
    private User discordUser;
    private RequestHandle handle;
    private RequestHandle2 handle2;

    public void setDiscordUser(User discordUser) {
        this.discordUser = discordUser;
        username = discordUser.getName();
        discriminator = discordUser.getDiscriminator();
        avatar= discordUser.getAvatarUrl();
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
