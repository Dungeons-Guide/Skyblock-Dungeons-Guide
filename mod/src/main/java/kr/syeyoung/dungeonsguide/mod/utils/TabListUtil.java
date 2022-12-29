/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.utils;

import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.FeatureDungeonMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabListUtil {
    final static Pattern tabListRegex = Pattern.compile("\\*[a-zA-Z0-9_]{2,16}\\*", Pattern.MULTILINE);

    public static List<String> getPlayersInDungeon(){
        List<String> players = new ArrayList<>();
        List<NetworkPlayerInfo> list = FeatureDungeonMap.sorter.sortedCopy(Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap());

        if(list.size() >= 20){
            for (int i = 1; i < 20; i++) {

                String na = getPlayerNameWithChecks(list.get(i));

                if(na != null){
                    players.add(na);
                }
            }
        }

        return players;
    }

    /**
     * We make sure that the player is alive and regex their name out
     * @param networkPlayerInfo the network player info of player
     * @return the username of player
     */
    @Nullable
    public static String getPlayerNameWithChecks(NetworkPlayerInfo networkPlayerInfo) {
        String name;
        if (networkPlayerInfo.getDisplayName() != null) {
            name = networkPlayerInfo.getDisplayName().getFormattedText();
        } else {
            name = ScorePlayerTeam.formatPlayerName(
                    networkPlayerInfo.getPlayerTeam(),
                    networkPlayerInfo.getGameProfile().getName()
            );
        }

        if (name.trim().equals("§r") || name.startsWith("§r ")) return null;

        name = TextUtils.stripColor(name);

        if(name.contains("(DEAD)")) {
            return null;
        }

        return getString(name, tabListRegex);
    }

    @Nullable
    public static String getString(String name, Pattern tabListRegex) {
        name = name.replace(" ", "*");

        Matcher matcher = tabListRegex.matcher(name);
        if (!matcher.find()) return null;

        name = matcher.group(0);
        name = name.substring(0, name.length() - 1);
        name = name.substring(1);
        return name;
    }
}
