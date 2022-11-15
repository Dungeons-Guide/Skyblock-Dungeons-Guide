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

package kr.syeyoung.dungeonsguide.mod.discord.rpc;

import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.GameSDK;
import kr.syeyoung.dungeonsguide.mod.discord.gamesdk.jna.datastruct.DiscordUser;
import lombok.Data;

@Data
public class JDiscordUser {
    private long id;
    private String username, discriminator, avatar;
    private boolean bot;

    public static JDiscordUser fromJNA(DiscordUser discordUser) {
        JDiscordUser jDiscordUser = new JDiscordUser();
        jDiscordUser.id = discordUser.id.longValue();
        jDiscordUser.username = GameSDK.readString(discordUser.username);
        jDiscordUser.discriminator = GameSDK.readString(discordUser.discriminator);
        jDiscordUser.avatar = GameSDK.readString(discordUser.avatar);
        jDiscordUser.bot = discordUser.bot;
        return jDiscordUser;
    }
}
