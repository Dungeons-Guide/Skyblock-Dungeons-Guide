package kr.syeyoung.dungeonsguide.utils;

import kr.syeyoung.dungeonsguide.features.impl.dungeon.FeatureDungeonMap;
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
