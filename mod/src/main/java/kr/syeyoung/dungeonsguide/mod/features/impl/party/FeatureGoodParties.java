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

package kr.syeyoung.dungeonsguide.mod.features.impl.party;


import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.SimpleFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.customgui.PartyFinderParty;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.gui.UContainer;
import kr.syeyoung.modapi.gui.UContainerChest;
import kr.syeyoung.modapi.gui.UContainerSlot;
import kr.syeyoung.modapi.gui.UGuiScreenChest;
import kr.syeyoung.modapi.item.Item;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class FeatureGoodParties extends SimpleFeature {
    public FeatureGoodParties() {
        super("Party Kicker", "Highlight parties in party viewer", "Highlight parties you can't join with red", "partykicker.goodparty",true);
    }

    @DGEventHandler
    public void onGuiPostRender(GuiScreenEvent.DrawScreenEvent.Post rendered) {
        if (!(ModAPI.getAPI().getCurrentGuiScreen() instanceof UGuiScreenChest)) return;
        UContainer container = ModAPI.getAPI().getPlayer().getOpenContainer();
        if (!(container instanceof UContainerChest)) return;
        UContainerChest containerChest = (UContainerChest) container;
        if (!"Party Finder".equals(containerChest.getName())) return;

        int i = 222;
        int j = i - 108;
        int ySize = j + (containerChest.getChestContainerSize() / 9) * 18;
        int left = (rendered.gui.width - 176) / 2;
        int top = (rendered.gui.height - ySize ) / 2;
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.colorMask(true, true, true, false);
        GlStateManager.translate(left, top, 0);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        try {
            for (int i1 = 0; i1 < Integer.min(54, containerChest.getChestContainerSize()); i1++) {
                UContainerSlot s = containerChest.getChestSlotAt(i1);
                if (s.getItemStack() == null) continue;
                if (s.getItemStack().getItem() != Item.SKULL) continue;

                PartyFinderParty party = PartyFinderParty.fromItemStack(s.getItemStack());

                int x = s.getX();
                int y = s.getY();
                if (!party.canJoin) {
                    Gui.drawRect(x, y, x + 16, y + 16, 0x77AA0000);
                } else {


                    GlStateManager.enableBlend();
                    GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    if (party.note.toLowerCase().contains("car")) {
                        fr.drawStringWithShadow("C", x + 1, y + 1, 0xFFFF0000);
                    } else if (party.note.toLowerCase().replace(" ", "").contains("s/s+")) {
                        fr.drawStringWithShadow("S+", x + 1, y + 1, 0xFFFFFF00);
                    } else if (party.note.toLowerCase().contains("s+")) {
                        fr.drawStringWithShadow("S+", x + 1, y + 1, 0xFF00FF00);
                    } else if (party.note.toLowerCase().contains(" s") || party.note.toLowerCase().contains(" s ")) {
                        fr.drawStringWithShadow("S", x + 1, y + 1, 0xFFFFFF00);
                    } else if (party.note.toLowerCase().contains("rush")) {
                        fr.drawStringWithShadow("R", x + 1, y + 1, 0xFFFF0000);
                    }
                    fr.drawStringWithShadow("§e"+Integer.max(party.requiredClassLevel, party.requiredDungeonLevel), x + 1, y + fr.FONT_HEIGHT, 0xFFFFFFFF);
                }


            }
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableLighting();
    }
}
