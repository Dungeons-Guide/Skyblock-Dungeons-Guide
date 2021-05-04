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

package kr.syeyoung.dungeonsguide.features.impl.party.playerpreview;

import kr.syeyoung.dungeonsguide.config.guiconfig.FeatureEditPane;
import kr.syeyoung.dungeonsguide.features.impl.party.api.DungeonSpecificData;
import kr.syeyoung.dungeonsguide.features.impl.party.api.DungeonStat;
import kr.syeyoung.dungeonsguide.features.impl.party.api.DungeonType;
import kr.syeyoung.dungeonsguide.features.impl.party.api.PlayerProfile;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import kr.syeyoung.dungeonsguide.utils.TextUtils;
import kr.syeyoung.dungeonsguide.utils.XPUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.awt.*;
import java.util.Arrays;

public class DataRendererFairySouls implements DataRenderer {
    @Override
    public Dimension renderData(PlayerProfile playerProfile) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("§eFairy Souls §f"+playerProfile.getFairySouls(), 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }

    @Override
    public Dimension renderDummy() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("§eFairy Souls §f300", 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT);
    }

    @Override
    public void onHover(PlayerProfile playerProfile, int mouseX, int mouseY) {
    }
}
