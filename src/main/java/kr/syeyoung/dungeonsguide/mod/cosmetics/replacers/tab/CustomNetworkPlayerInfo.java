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

package kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.tab;

import com.mojang.authlib.GameProfile;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.data.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.cosmetics.replacers.chat.ReplacerUtil;
import kr.syeyoung.dungeonsguide.mod.utils.TextUtils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.util.List;

public class CustomNetworkPlayerInfo extends NetworkPlayerInfo {
    private IChatComponent name;

    public CustomNetworkPlayerInfo(GameProfile gameProfile) {
        super(gameProfile);
    }

    public CustomNetworkPlayerInfo(S38PacketPlayerListItem.AddPlayerData playerData) {
        super(playerData);
        setDisplayName(super.getDisplayName());
    }

    @Override
    public IChatComponent getDisplayName() {
        return name;
    }

    /**
     *  playerNetworkInfo chatComponent when logged:
     *   TextComponent
     *   {
     *       text='',
     *       siblings=[
     *               TextComponent{text='[', siblings=[], style=Style{hasParent=true, color=§8, blah=null}},
     *               TextComponent{text='88', siblings=[], style=Style{hasParent=true, color=§a, blah=null}},
     *               TextComponent{text='] ', siblings=[], style=Style{hasParent=true, color=§8, blah=null}},
     *               TextComponent{text='Kokoniara', siblings=[], style=Style{hasParent=true, color=§7, blah=null}}
     *       ],
     *       style=Style{
     *               hasParent=false, blah=null
     *       }
     *   }
     */
    @Override
    public void setDisplayName(IChatComponent displayNameIn) {
        name = displayNameIn;
        if (name == null) return;
        if (name.getSiblings().isEmpty()) return;

        IChatComponent iChatComponent = name.getSiblings().get(0);
        if (iChatComponent == null) return;

        String firstComponentText = iChatComponent.getUnformattedTextForChat();
        if (firstComponentText.contains("Ends")) return;
        if (firstComponentText.contains("Starts")) return;
        if (firstComponentText.contains("         ")) return;

        if (name.getSiblings().size() <= 3) return;

        String cleanName = TextUtils.stripColor(name.getSiblings().get(3).getUnformattedText());

        CosmeticsManager cm = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();
        List<ActiveCosmetic> activeCosmts = ReplacerUtil.getActiveCosmeticsFromUsername(cleanName.trim(), cm);

        CosmeticData color = ReplacerUtil.getColorCosmeticData(activeCosmts, cm);

        if (color != null) {
            String colorCode = color.getData().replace("&", "§");
            name.getSiblings().set(3, new ChatComponentText(colorCode + cleanName));
        }
    }
}
