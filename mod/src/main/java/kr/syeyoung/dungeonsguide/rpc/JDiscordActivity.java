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

package kr.syeyoung.dungeonsguide.rpc;

import kr.syeyoung.dungeonsguide.gamesdk.GameSDK;
import kr.syeyoung.dungeonsguide.gamesdk.jna.datastruct.DiscordActivity;
import kr.syeyoung.dungeonsguide.gamesdk.jna.enumuration.EDiscordActivityType;
import lombok.Data;

@Data
public class JDiscordActivity {
    private EDiscordActivityType activityType;
    private long applicationId;
    private String name, state, details;
    private long start, end;
    private String largeImage, largeText, smallImage, smallText;
    private String partyId;
    private int partyCurr, partyMax;
    private String matchSecret, joinSecret, spectateSecret;
    private boolean instance;

    public static JDiscordActivity fromJNA(DiscordActivity discordActivity) {
        if (discordActivity == null) return null;
        JDiscordActivity jDiscordActivity = new JDiscordActivity();
        jDiscordActivity.activityType = discordActivity.activityType;
        jDiscordActivity.applicationId = discordActivity.applicationId.longValue();
        jDiscordActivity.name = GameSDK.readString(discordActivity.name);
        jDiscordActivity.state = GameSDK.readString(discordActivity.state);
        jDiscordActivity.details = GameSDK.readString(discordActivity.details);
        if (discordActivity.assets != null) {
            jDiscordActivity.largeImage = GameSDK.readString(discordActivity.assets.large_image);
            jDiscordActivity.largeText = GameSDK.readString(discordActivity.assets.large_text);
            jDiscordActivity.smallImage = GameSDK.readString(discordActivity.assets.small_image);
            jDiscordActivity.smallText = GameSDK.readString(discordActivity.assets.small_text);
        }
        if (discordActivity.timestamps != null) {
            jDiscordActivity.start = discordActivity.timestamps.start.longValue();
            jDiscordActivity.end = discordActivity.timestamps.end.longValue();
        }
        if (discordActivity.secrets != null) {
            jDiscordActivity.matchSecret= GameSDK.readString(discordActivity.secrets.match);
            jDiscordActivity.joinSecret = GameSDK.readString(discordActivity.secrets.join);
            jDiscordActivity.spectateSecret = GameSDK.readString(discordActivity.secrets.spectate);
        }
        if (discordActivity.party != null) {
            jDiscordActivity.partyId = GameSDK.readString(discordActivity.party.id);
            if (discordActivity.party.discordActivityParty != null) {
                jDiscordActivity.partyCurr = discordActivity.party.discordActivityParty.current_size.intValue();
                jDiscordActivity.partyMax = discordActivity.party.discordActivityParty.max_size.intValue();
            }
        }
        jDiscordActivity.instance = discordActivity.instance;

        return jDiscordActivity;
    }
}
