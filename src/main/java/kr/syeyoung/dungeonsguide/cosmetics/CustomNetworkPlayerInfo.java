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
    }

    public IChatComponent getDisplayName()
    {
        String semi_name = super.getDisplayName() != null ? super.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(super.getPlayerTeam(), super.getGameProfile().getName());

        String actualName = "";
        for (String s : semi_name.split(" ")) {
            if (TextUtils.stripColor(s).startsWith("[")) continue;
            actualName = s;
        }
        List<ActiveCosmetic> activeCosmetics = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getActiveCosmeticByPlayerNameLowerCase().get(TextUtils.stripColor(actualName).toLowerCase());



        if (activeCosmetics == null) return super.getDisplayName();
        CosmeticData color=null;
        for (ActiveCosmetic activeCosmetic : activeCosmetics) {
            CosmeticData cosmeticData = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData.getCosmeticType().equals("color")) color = cosmeticData;
        }

        if (color != null) semi_name = semi_name.replace(actualName, color.getData()+actualName);

        return new ChatComponentText(semi_name);
    }
}
