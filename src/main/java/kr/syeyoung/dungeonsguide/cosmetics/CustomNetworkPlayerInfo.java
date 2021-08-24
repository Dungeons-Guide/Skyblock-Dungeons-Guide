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

package kr.syeyoung.dungeonsguide.cosmetics;

import com.mojang.authlib.GameProfile;
import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.List;

public class CustomNetworkPlayerInfo extends NetworkPlayerInfo {
    public CustomNetworkPlayerInfo(GameProfile p_i46294_1_) {
        super(p_i46294_1_);
    }

    public CustomNetworkPlayerInfo(S38PacketPlayerListItem.AddPlayerData p_i46295_1_) {
        super(p_i46295_1_);
        setDisplayName(super.getDisplayName());
    }


    private IChatComponent displayName;
    private String playernameLowercase;
    private String unformatted;
    private String actualName;
    @Override
    public void setDisplayName(IChatComponent displayNameIn) {
        displayName = displayNameIn;
        if (displayName == null) {
            playernameLowercase = null;
            unformatted = null;
            return;
        }

        unformatted = displayName.getUnformattedText();


        actualName = "";
        for (String s : unformatted.split(" ")) {
            String strippped = TextUtils.stripColor(s);
            if (strippped.startsWith("[")) continue;
            actualName = strippped;
            break;
        }
        playernameLowercase = actualName.toLowerCase();
    }

    public IChatComponent getDisplayName()
    {

        String semi_name;
        String actualName;
        List<ActiveCosmetic> activeCosmetics;
        if (playernameLowercase != null) {
            activeCosmetics = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getActiveCosmeticByPlayerNameLowerCase().get(playernameLowercase);
            semi_name = unformatted;
            actualName = this.actualName;
        } else {
            semi_name = ScorePlayerTeam.formatPlayerName(super.getPlayerTeam(), super.getGameProfile().getName());
            actualName = "";
            for (String s : semi_name.split(" ")) {
                String strippped = TextUtils.stripColor(s);
                if (strippped.startsWith("[")) continue;
                actualName = strippped;
                break;
            }
            activeCosmetics = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getActiveCosmeticByPlayerNameLowerCase().get(actualName.toLowerCase());
        }


        if (activeCosmetics == null) return displayName;
        CosmeticData color=null;
        for (ActiveCosmetic activeCosmetic : activeCosmetics) {
            CosmeticData cosmeticData = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData.getCosmeticType().equals("color")) color = cosmeticData;
        }

        if (color != null) semi_name = semi_name.replace(actualName, color.getData()+actualName);

        return new ChatComponentText(semi_name);
    }
}
