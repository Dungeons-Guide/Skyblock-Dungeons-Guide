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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.impl;

import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.datarenders.IDataRenderer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DataRendererTalismans implements IDataRenderer {
    @Override
    public Dimension renderData(PlayerProfile playerProfile) {
        boolean apiDisabled = playerProfile.getTalismans() == null || playerProfile.getInventory() == null;
        if (!playerProfile.getAdditionalProperties().containsKey("talismanCnt") && !apiDisabled) {
            int[] cnts = new int[Rarity.values().length];
            for (ItemStack talisman : playerProfile.getTalismans()) {
                if (talisman == null) continue;
                Rarity r = getRarity(talisman);
                if (r != null) cnts[r.ordinal()]++;
            }
            for (ItemStack itemStack : playerProfile.getInventory()) {
                if (itemStack == null) continue;
                Rarity r = getRarity(itemStack);
                if (r != null) cnts[r.ordinal()]++;
            }
            playerProfile.getAdditionalProperties().put("talismanCnt", cnts);
        }
        int[] rawData = (int[]) playerProfile.getAdditionalProperties().get("talismanCnt");

        String str = "";
        if (rawData != null)
        for (Rarity r : Rarity.values()) {
            str = r.color+rawData[r.idx] +" "+ str;
        }

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        if (apiDisabled)
            fr.drawString("§eTalis §cAPI DISABLED", 0,0,-1);
        else
        fr.drawString("§eTalis §f"+str, 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }

    private Rarity getRarity(ItemStack itemStack) {
        NBTTagCompound display = itemStack.getTagCompound().getCompoundTag("display");
        if (display == null) return null;
        NBTTagList lore = display.getTagList("Lore", 8);
        if (lore == null) return null;
        for (int i = 0; i < lore.tagCount(); i++) {
            String line = lore.getStringTagAt(i);
            for (Rarity value : Rarity.values()) {
                if (line.startsWith(value.getColor()) && line.contains("CCESSORY")) return value;
            }
        }
        return null;
    }

    @Override
    public Dimension renderDummy() {
        String str = "";
        for (Rarity r : Rarity.values()) {
            str = r.color+(r.idx+5)*2+" "+str;
        }

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString("§eTalis §f"+str, 0,0,-1);
        return new Dimension(100, fr.FONT_HEIGHT);
    }
    @Override
    public Dimension getDimension() {
        return new Dimension(100, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT);
    }

    @Override
    public void onHover(PlayerProfile playerProfile, int mouseX, int mouseY) {
        int[] rawData = (int[]) playerProfile.getAdditionalProperties().get("talismanCnt");
        if (rawData == null) return;
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        List<String> list = new ArrayList<>();

        for (Rarity r : Rarity.values()) {
            list.add(r.getColor()+r.name()+"§7: §e"+rawData[r.idx]);
        }
        GuiUtils.drawHoveringText(list,mouseX, mouseY,
                scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight(), -1, Minecraft.getMinecraft().fontRendererObj);
    }


    @AllArgsConstructor @Getter
    public static enum Rarity {
        COMMON("§f", 0), UNCOMMON("§a",1), RARE("§9",2), EPIC("§5",3), LEGENDARY("§6",4), MYTHIC("§d",5);

        private String color;
        private int idx;
    }
}
