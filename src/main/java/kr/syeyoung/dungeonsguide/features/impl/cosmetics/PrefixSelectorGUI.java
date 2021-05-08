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

package kr.syeyoung.dungeonsguide.features.impl.cosmetics;

import kr.syeyoung.dungeonsguide.DungeonsGuide;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.cosmetics.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.cosmetics.CosmeticData;
import kr.syeyoung.dungeonsguide.cosmetics.CosmeticsManager;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class PrefixSelectorGUI extends MPanel {
    private GuiConfig config;
    private String cosmeticType;

    public PrefixSelectorGUI(GuiConfig config, String cosmeticType, String[] previews) {
        this.config = config;
        this.cosmeticType = cosmeticType;
        this.previews = previews;
        CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();
        List<ActiveCosmetic> activeCosmeticList =  cosmeticsManager.getActiveCosmeticByPlayer().computeIfAbsent(Minecraft.getMinecraft().thePlayer.getGameProfile().getId(), (a) -> new ArrayList<>());
        for (ActiveCosmetic activeCosmetic : activeCosmeticList) {
            CosmeticData cosmeticData =  cosmeticsManager.getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
            if (cosmeticData != null && cosmeticData.getCosmeticType().equals(cosmeticType)) {
                selected = cosmeticData;
                return;
            }
        }
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(0,0,parentWidth, parentHeight));
    }

    private CosmeticData selected;
    // §9Party §8> §a[VIP§6+§a] syeyoung§f: ty
    // §2Guild > §a[VIP§6+§a] syeyoung §3[Vet]§f
    // §dTo §r§a[VIP§r§6+§r§a] SlashSlayer§r§7: §r§7what§r
    // §dFrom §r§a[VIP§r§6+§r§a] SlashSlayer§r§7: §r§7?§r
    // §7Rock_Bird§7§r§7: SELLING 30 DIAMOD BLOCK /p me§r
    // §b[MVP§c+§b] Probutnoobgamer§f: quitting skyblock! highe
    // §r§bCo-op > §a[VIP§6+§a] syeyoung§f: §rwhat§r
    String[] previews;

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();

        Set<UUID> activeCosmeticList =  cosmeticsManager.getActiveCosmeticByPlayer().computeIfAbsent(Minecraft.getMinecraft().thePlayer.getGameProfile().getId(), (a) -> new ArrayList<>()).stream().map(ActiveCosmetic::getCosmeticData).collect(Collectors.toSet());



        GlStateManager.translate(0,2,0);
        Gui.drawRect(0,0,getBounds().width, getBounds().height-2, 0xFF444444);
        Gui.drawRect(5,5,265, getBounds().height-7, 0xFF222222);
        Gui.drawRect(6,17,264, getBounds().height-8, 0xFF555555);
        fr.drawString("Preview", (270 - fr.getStringWidth("Preview")) / 2, 7, 0xFFFFFFFF);

        {
            String prefix = selected != null ? selected.getData() : "[DG]";
            GlStateManager.pushMatrix();
            GlStateManager.translate(6,17,0);
            for (int i = 0; i < previews.length; i++) {
                fr.drawString(previews[i].replace("%name%", Minecraft.getMinecraft().getSession().getUsername()).replace("%prefix%", prefix), 0, i*fr.FONT_HEIGHT, -1);
            }
            GlStateManager.popMatrix();
        }
        {
            GlStateManager.pushMatrix();
            GlStateManager.translate(270,17,0);
            int relX = relMousex0 - 270, relY = relMousey0 - 19;
            int cnt = 0;
            for (CosmeticData value : cosmeticsManager.getCosmeticDataMap().values()) {
                if (value.getCosmeticType().equals(cosmeticType)) {
                    Gui.drawRect(0,0,220, fr.FONT_HEIGHT+3, 0xFF222222);
                    Gui.drawRect(1,1, 219, fr.FONT_HEIGHT+2, 0xFF555555);
                    fr.drawString(value.getData(), 2, 2, -1);
                    Gui.drawRect(120,1,160, fr.FONT_HEIGHT+2, new Rectangle(120,cnt * (fr.FONT_HEIGHT+4) + 2,40,fr.FONT_HEIGHT+1).contains(relX, relY) ? 0xFF859DF0 : 0xFF7289da);
                    fr.drawString("TEST", (280-fr.getStringWidth("TEST"))/2, 2, -1);

                    if (cosmeticsManager.getPerms().contains(value.getReqPerm())) {
                        Gui.drawRect(161,1,219, fr.FONT_HEIGHT+2, new Rectangle(161,cnt * (fr.FONT_HEIGHT+4) + 2,58,fr.FONT_HEIGHT+1).contains(relX, relY) ? 0xFF859DF0 : 0xFF7289da);

                        if (activeCosmeticList.contains(value.getId())) {
                            fr.drawString("UNSELECT", (381 - fr.getStringWidth("UNSELECT")) / 2, 2, -1);
                        } else {
                            fr.drawString("SELECT", (381 - fr.getStringWidth("SELECT")) / 2, 2, -1);
                        }
                    } else {
                        Gui.drawRect(161,1,219, fr.FONT_HEIGHT+2, 0xFFFF3333);
                        fr.drawString("Locked", (381 - fr.getStringWidth("Locked")) / 2, 2, -1);
                    }
                    GlStateManager.translate(0,fr.FONT_HEIGHT+4, 0);

                    cnt++;
                }
            }
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        CosmeticsManager cosmeticsManager = DungeonsGuide.getDungeonsGuide().getCosmeticsManager();

        int relX = relMouseX - 270, relY = relMouseY - 19;
        int cnt = 0;

        List<ActiveCosmetic> activeCosmeticList =  cosmeticsManager.getActiveCosmeticByPlayer().computeIfAbsent(Minecraft.getMinecraft().thePlayer.getGameProfile().getId(), (a) -> new ArrayList<>());

        for (CosmeticData value : cosmeticsManager.getCosmeticDataMap().values()) {
            if (value.getCosmeticType().equals(cosmeticType)) {
                if (new Rectangle(120,cnt * (fr.FONT_HEIGHT+4) + 2,40,fr.FONT_HEIGHT+1).contains(relX, relY)) {
                    selected = value;
                    return;
                }
                if (new Rectangle(161,cnt * (fr.FONT_HEIGHT+4) + 2,58,fr.FONT_HEIGHT+1).contains(relX, relY) && cosmeticsManager.getPerms().contains(value.getReqPerm())) {
                    for (ActiveCosmetic activeCosmetic : activeCosmeticList) {
                        if (activeCosmetic.getCosmeticData().equals(value.getId())) {
                            cosmeticsManager.removeCosmetic(activeCosmetic);
                            return;
                        }
                    }
                    cosmeticsManager.setCosmetic(value);
                    selected = value;
                }

                cnt++;
            }
        }
    }
}
