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

package kr.syeyoung.dungeonsguide.mod.discord;

import com.jagrosh.discordipc.entities.User;
import lombok.Data;
import org.json.JSONObject;

@Data
public class JDiscordRelation {
//    private EDiscordRelationshipType discordRelationshipType;
//    private EDiscordStatus status;
//    private JDiscordActivity discordActivity;
    private Status status;
    private String applicationId;
    private DiscordRelationType relationType;
    private User discordUser;

    public enum Status {
        OFFLINE, ONLINE, DO_NOT_DISTURB, IDLE, UNKNOWN;

        public static Status fromString(String str) {
            switch(str) {
                case "offline": return OFFLINE;
                case "online": return ONLINE;
                case "idle": return IDLE;
                case "dnd": return DO_NOT_DISTURB;
            }
            return UNKNOWN;
        }
    }
    public enum DiscordRelationType {

        None,
        Friend,
        Blocked,
        PendingIncoming,
        PendingOutgoing,
        Implicit;
    }


    public static JDiscordRelation parse(JSONObject data) {
        JDiscordRelation relation = new JDiscordRelation();
        JDiscordRelation.DiscordRelationType relationType = JDiscordRelation.DiscordRelationType.values()[data.getInt("type")];
        JSONObject userJson = data.getJSONObject("user");
        User user = new User(userJson.getString("username"), userJson.getString("discriminator"),
                Long.parseUnsignedLong(userJson.getString("id")),
                userJson.isNull("avatar") ? null : userJson.getString("avatar"));
        JDiscordRelation.Status status = JDiscordRelation.Status.fromString(data.getJSONObject("presence").getString("status"));

        relation.setRelationType(relationType);
        relation.setDiscordUser(user);
        relation.setStatus(status);

        if (data.has("activity") && !data.isNull("activity")) {
            JSONObject activity = data.getJSONObject("activity");
            String appId = activity.getString("application_id");
            relation.setApplicationId(appId);
        }
        return relation;
    }
}
