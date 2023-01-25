/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.widget;

import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.cosmetics.ActiveCosmetic;
import kr.syeyoung.dungeonsguide.mod.cosmetics.CosmeticData;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.FakePlayer;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.MinecraftTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.MouseTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedExportOnlyWidget;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.Collections;
import java.util.List;

public class PlayerModelRenderer extends AnnotatedExportOnlyWidget implements Layouter, Renderer {
    @Setter
    private FakePlayer fakePlayer;
    public PlayerModelRenderer(FakePlayer fakePlayer) {
        this.fakePlayer = fakePlayer;
    }
    // let me do the skin fetching myself.
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        return 0;
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        return 0;
    }

    @Override
    public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
        GlStateManager.enableDepth();
        GlStateManager.color(1, 1, 1, 1.0F);
        GuiInventory.drawEntityOnScreen(45, 150, 60, (float) -relMouseX+75, 0, fakePlayer);
        GlStateManager.disableDepth();

        String toDraw = fakePlayer.getName();
        List<ActiveCosmetic> activeCosmetics = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getActiveCosmeticByPlayer().get(
                fakePlayer.getGameProfile().getId());
        CosmeticData prefix = null;
        CosmeticData color = null;
        if (activeCosmetics != null) {
            for (ActiveCosmetic activeCosmetic : activeCosmetics) {
                CosmeticData cosmeticData = DungeonsGuide.getDungeonsGuide().getCosmeticsManager().getCosmeticDataMap().get(activeCosmetic.getCosmeticData());
                if (cosmeticData != null) {
                    if (cosmeticData.getCosmeticType().equals("prefix")) prefix = cosmeticData;
                    if (cosmeticData.getCosmeticType().equals("color")) color = cosmeticData;
                }
            }
        }
        toDraw = (color == null ? "§e" : color.getData().replace("&", "§")) + toDraw;
        if (prefix != null) toDraw = prefix.getData().replace("&", "§") + " " + toDraw;

        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);


        String profileName = "on §6" + this.fakePlayer.getSkyblockProfile().getProfileName();
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString(profileName, (90 - fr.getStringWidth(profileName)) / 2, 15, -1);
        fr.drawString(toDraw, (90 - fr.getStringWidth(toDraw)) / 2, 10 - (fr.FONT_HEIGHT / 2), -1);
    }

    private MinecraftTooltip actualTooltip = new MinecraftTooltip();
    private MouseTooltip tooltip = null;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0) {
        ItemStack toHover = null;
        if (relMouseX0 > 20 && relMouseX0 < 70) {
            if (33 <= relMouseY0 && relMouseY0 <= 66) {
                toHover = fakePlayer.getInventory()[3];
            } else if (66 <= relMouseY0 && relMouseY0 <= 108) {
                toHover = fakePlayer.getInventory()[2];
            } else if (108 <= relMouseY0 && relMouseY0 <= 130) {
                toHover = fakePlayer.getInventory()[1];
            } else if (130 <= relMouseY0 && relMouseY0 <= 154) {
                toHover = fakePlayer.getInventory()[0];
            }
        } else if (relMouseX0 > 0 && relMouseX0 <= 20) {
            if (80 <= relMouseY0 && relMouseY0 <= 120) {
                toHover = fakePlayer.inventory.mainInventory[fakePlayer.inventory.currentItem];
            }
        }
        if (toHover != null) {
            List<String> list = toHover.getTooltip(Minecraft.getMinecraft().thePlayer,
                    Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
            for (int i = 0; i < list.size(); ++i) {
                if (i == 0) {
                    list.set(i, toHover.getRarity().rarityColor + list.get(i));
                } else {
                    list.set(i, EnumChatFormatting.GRAY + list.get(i));
                }
            }
            actualTooltip.setTooltip(list);
        }
        if (toHover == null && this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        } else if (toHover != null && this.tooltip == null)
            PopupMgr.getPopupMgr(getDomElement())
                    .openPopup(this.tooltip = new MouseTooltip(actualTooltip),(a) -> {
                        this.tooltip = null;
                    });
        return false;
    }


    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        if (this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        }
    }

    @Override
    public void onUnmount() {
        if (this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        }
        super.onUnmount();
    }
}
