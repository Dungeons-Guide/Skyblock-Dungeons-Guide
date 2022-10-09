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

package kr.syeyoung.dungeonsguide.discord.rpc;

import kr.syeyoung.dungeonsguide.discord.gamesdk.jna.datastruct.DiscordRelationship;
import kr.syeyoung.dungeonsguide.discord.gamesdk.jna.enumuration.EDiscordRelationshipType;
import kr.syeyoung.dungeonsguide.discord.gamesdk.jna.enumuration.EDiscordStatus;
import lombok.Data;

@Data
public class JDiscordRelation {
    private EDiscordRelationshipType discordRelationshipType;
    private EDiscordStatus status;
    private JDiscordActivity discordActivity;
    private JDiscordUser discordUser;

    public static JDiscordRelation fromJNA(DiscordRelationship relationship) {
        JDiscordRelation jDiscordRelation = new JDiscordRelation();
        jDiscordRelation.discordUser = JDiscordUser.fromJNA(relationship.user);
        jDiscordRelation.discordActivity = JDiscordActivity.fromJNA(relationship.presence.activity);
        jDiscordRelation.status = relationship.presence.status;
        jDiscordRelation.discordRelationshipType = relationship.type;
        return jDiscordRelation;
    }

}
