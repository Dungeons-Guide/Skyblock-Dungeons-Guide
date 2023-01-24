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

package kr.syeyoung.dungeonsguide.mod.cosmetics.chatdetectors;

import kr.syeyoung.dungeonsguide.mod.cosmetics.surgical.ReplacementContext;
import kr.syeyoung.dungeonsguide.mod.party.PartyManager;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;

public class ChatDetectorPartyMessages implements IChatDetector {
    @Override
    public List<ReplacementContext> getReplacementContext(IChatComponent chatComponent) {
        String str = chatComponent.getFormattedText();
        String strip = TextUtils.stripColor(str);

        List<ReplacementContext> detectors = new ArrayList<>();
        if (str.endsWith("§aenabled All Invite§r") || str.endsWith("§cdisabled All Invite§r") || str.endsWith("§ejoined the party.§r") || str.endsWith("§ehas been removed from the party.§r") || str.endsWith("§ehas left the party.§r")) {
            String username = null;
            for (String s : TextUtils.stripColor(str).split(" ")) {
                if (s.startsWith("[")) continue;
                username = s;
                break;
            }
            if (username != null)
                detectors.add(new ReplacementContext(
                        strip.indexOf(username), username, null
                ));
        } else if (str.endsWith(" They have §r§c60 §r§eseconds to accept.§r")) {
            String username = null;
            for (String s : TextUtils.stripColor(str).split(" ")) {
                if (s.startsWith("[")) continue;
                username = s;
                break;
            }
            if (username != null)
                detectors.add(new ReplacementContext(
                        strip.indexOf(username), username, null
                ));
            username = null;
            for (String s : TextUtils.stripColor(str.substring(str.indexOf("§r§einvited ")+12)).split(" ")) {
                if (s.startsWith("[")) continue;
                username = s;
                break;
            }
            if (username != null)
                detectors.add(new ReplacementContext(
                        strip.indexOf(username), username, null
                ));
        } else if (str.startsWith("§eThe party was transferred to ")) {
            String[] messageSplit = TextUtils.stripColor(str.substring(31)).split(" ");
            String newLeader = null;
            for (String s : messageSplit) {
                if (s.startsWith("[")) continue;
                newLeader = s;
                break;
            }
            String oldLeader;
            if (str.endsWith("§r§eleft§r")) {
                oldLeader = messageSplit[messageSplit.length - 2];
            } else {
                oldLeader = messageSplit[messageSplit.length - 1];
            }

            if (oldLeader != null)
                detectors.add(new ReplacementContext(
                        strip.indexOf(oldLeader), oldLeader, null
                ));
            if (newLeader != null)
                detectors.add(new ReplacementContext(
                        strip.indexOf(newLeader), newLeader, null
                ));
        } else if (str.endsWith("§eto Party Leader§r")) {
            String[] messageSplit = TextUtils.stripColor(str).split(" ");
            String oldLeader = null;
            for (String s : messageSplit) {
                if (s.startsWith("[")) continue;
                oldLeader = s;
                break;
            }
            messageSplit = TextUtils.stripColor(str.substring(str.indexOf("has promoted") + 13)).split(" ");
            String newLeader = null;
            for (String s : messageSplit) {
                if (s.startsWith("[")) continue;
                newLeader = s;
                break;
            }
            if (oldLeader != null)
                detectors.add(new ReplacementContext(
                        strip.indexOf(oldLeader), oldLeader, null
                ));
            if (newLeader != null)
                detectors.add(new ReplacementContext(
                        strip.indexOf(newLeader), newLeader, null
                ));
        } else if (str.endsWith("§r§eto Party Moderator§r")) {
            String[] messageSplit = TextUtils.stripColor(str).split(" ");
            String oldLeader = null;
            for (String s : messageSplit) {
                if (s.startsWith("[")) continue;
                oldLeader = s;
                break;
            }
            messageSplit = TextUtils.stripColor(str.substring(str.indexOf("has promoted") + 13)).split(" ");
            String newModerator = null;
            for (String s : messageSplit) {
                if (s.startsWith("[")) continue;
                newModerator = s;
                break;
            }
            if (oldLeader != null)
                detectors.add(new ReplacementContext(
                        strip.indexOf(oldLeader), oldLeader, null
                ));
            if (newModerator != null)
                detectors.add(new ReplacementContext(
                        strip.indexOf(newModerator), newModerator, null
                ));

        } else if (str.endsWith("§r§eto Party Member§r")) {
            String[] messageSplit = TextUtils.stripColor(str).split(" ");
            String oldLeader = null;
            for (String s : messageSplit) {
                if (s.startsWith("[")) continue;
                oldLeader = s;
                break;
            }
            messageSplit = TextUtils.stripColor(str.substring(str.indexOf("has demoted") + 12)).split(" ");
            String newMember = null;
            for (String s : messageSplit) {
                if (s.startsWith("[")) continue;
                newMember = s;
                break;
            }
            if (oldLeader != null)
                detectors.add(new ReplacementContext(
                        strip.indexOf(oldLeader), oldLeader, null
                ));
            if (newMember != null)
                detectors.add(new ReplacementContext(
                        strip.indexOf(newMember), newMember, null
                ));

        } else if (str.startsWith("§eYou have joined ")) {
            String[] messageSplit = strip.split(" ");
            String leader = null;
            for (String s : messageSplit) {
                if (s.startsWith("[")) continue;
                leader = s;
                break;
            }
            if (leader == null) return null;
            String username = leader.substring(0, leader.length()-2);
            detectors.add(new ReplacementContext(
                    strip.indexOf(username), username, null
            ));
        } else if (str.startsWith("§eYou'll be partying with: ")) {
            String[] players = strip.split(" ");
            for (String player : players) {
                if (player.startsWith("[")) continue;
                // player
                detectors.add(new ReplacementContext(
                        strip.indexOf(player), player, null
                ));
            }
        } else if (str.contains("§r§ejoined the dungeon group! (§r§b")) {
            String username = TextUtils.stripColor(str).split(" ")[3];
            detectors.add(new ReplacementContext(
                    strip.indexOf(username), username, null
            ));
        }
        return detectors;
    }
}
