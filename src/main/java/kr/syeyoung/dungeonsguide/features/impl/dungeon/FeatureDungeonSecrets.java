/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.impl.dungeon;

import kr.syeyoung.dungeonsguide.SkyblockStatus;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.dungeon.DungeonContext;
import kr.syeyoung.dungeonsguide.dungeon.roomfinder.DungeonRoom;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeatureDungeonSecrets extends TextHUDFeature {
    public FeatureDungeonSecrets() {
        super("Dungeon.HUDs", "Display Total # of Secrets", "Display how much total secrets have been found in a dungeon run.\n+ sign means DG does not know the correct number, but it's somewhere above that number.", "dungeon.stats.secrets", true, getFontRenderer().getStringWidth("Secrets: 999/999+ of 999+"), getFontRenderer().FONT_HEIGHT);
        this.setEnabled(false);
        getStyles().add(new TextStyle("title", new AColor(0x00, 0xAA,0xAA,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("currentSecrets", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("separator2", new AColor(0x55, 0x55,0x55,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("totalSecrets", new AColor(0x55, 0xFF,0xFF,255), new AColor(0, 0,0,0), false));
        getStyles().add(new TextStyle("unknown", new AColor(0xFF, 0xFF,0x55,255), new AColor(0, 0,0,0), false));
    }

    SkyblockStatus skyblockStatus = DungeonsGuide.getDungeonsGuide().getSkyblockStatus();
    public int getSecretsFound() {
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r Secrets Found: §r§b") && !name.contains("%")) {
                String noColor = TextUtils.stripColor(name);
                return Integer.parseInt(noColor.substring(16));
            }
        }
        return 0;
    }
    public double getSecretPercentage() {
        for (NetworkPlayerInfo networkPlayerInfoIn : Minecraft.getMinecraft().thePlayer.sendQueue.getPlayerInfoMap()) {
            String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
            if (name.startsWith("§r Secrets Found: §r") && name.contains("%")) {
                String noColor = TextUtils.stripColor(name);
                return Double.parseDouble(noColor.substring(16).replace("%", ""));
            }
        }
        return 0;
    }

    public int getTotalSecretsInt() {
        if (getSecretsFound() != 0) return (int) Math.ceil (getSecretsFound() / getSecretPercentage() * 100);
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonGodObject().getContext();
        int totalSecrets = 0;
        for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() != -1)
                totalSecrets += dungeonRoom.getTotalSecrets();
        }
        return totalSecrets;
    }
    public boolean sureOfTotalSecrets() {
        if (getSecretsFound() != 0) return true;
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonGodObject().getContext();
        if (context.getMapProcessor().getUndiscoveredRoom() > 0) return false;
        boolean allknown = true;
        for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() == -1) allknown = false;
        }
        return allknown;
    }

    public String getTotalSecrets() {
        DungeonContext context = DungeonsGuide.getDungeonsGuide().getDungeonGodObject().getContext();
        if (context == null) return "?";
        int totalSecrets = 0;
        boolean allknown = true;
        for (DungeonRoom dungeonRoom : context.getDungeonRoomList()) {
            if (dungeonRoom.getTotalSecrets() != -1)
                totalSecrets += dungeonRoom.getTotalSecrets();
            else allknown = false;
        }
        return totalSecrets + (allknown ? "":"+");
    }


    private static final java.util.List<StyledText> dummyText=  new ArrayList<StyledText>();
    static {
        dummyText.add(new StyledText("Secrets","title"));
        dummyText.add(new StyledText(": ","separator"));
        dummyText.add(new StyledText("999","currentSecrets"));
        dummyText.add(new StyledText("/","separator2"));
        dummyText.add(new StyledText("2","totalSecrets"));
        dummyText.add(new StyledText("+","unknown"));
    }

    @Override
    public boolean isHUDViewable() {
        return skyblockStatus.isOnDungeon();
    }

    @Override
    public java.util.List<String> getUsedTextStyle() {
        return Arrays.asList("title", "separator", "currentSecrets", "separator2", "totalSecrets", "unknown");
    }

    @Override
    public java.util.List<StyledText> getDummyText() {
        return dummyText;
    }

    @Override
    public java.util.List<StyledText> getText() {
        List<StyledText> actualBit = new ArrayList<StyledText>();
        actualBit.add(new StyledText("Secrets","title"));
        actualBit.add(new StyledText(": ","separator"));
        actualBit.add(new StyledText(getSecretsFound() +"","currentSecrets"));
        actualBit.add(new StyledText("/","separator2"));
        DungeonsGuide.getDungeonsGuide();
        actualBit.add(new StyledText((int)Math.ceil(getTotalSecretsInt() * DungeonsGuide.getDungeonsGuide().getDungeonGodObject().getContext().getSecretPercentage())+" of "+getTotalSecretsInt(),"totalSecrets"));
        actualBit.add(new StyledText(getTotalSecrets().contains("+") ? "+" : "","unknown"));
        return actualBit;
    }

}
