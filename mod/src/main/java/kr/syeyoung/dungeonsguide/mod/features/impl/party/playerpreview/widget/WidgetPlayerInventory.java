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

import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.api.playerprofile.PlayerProfile;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.MinecraftTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.MouseTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;

public class WidgetPlayerInventory extends Widget implements Renderer, Layouter {
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        return new Size(164, 74);
    }

    public WidgetPlayerInventory(PlayerProfile profile) {
        this.playerProfile = profile;
    }
    private PlayerProfile playerProfile;

    public void setPlayerProfile(PlayerProfile playerProfile) {
        this.playerProfile = playerProfile;
    }

    @Override
    public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
        Gui.drawRect(0, 0, 164, 74, 0xFF000000);
        GlStateManager.disableLighting();

        if (playerProfile.getInventory() != null) {
            GlStateManager.disableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            for (int i = 0; i < playerProfile.getInventory().length; i++) {
                int x = (i % 9) * 18 + 1;
                int y = (i / 9) * 18 + 1;
                Gui.drawRect(x, y, x + 18, y + 18, 0xFF000000);
                Gui.drawRect(x + 1, y + 1, x + 17, y + 17, 0xFF666666);
                GlStateManager.color(1, 1, 1, 1.0F);

                Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(playerProfile.getInventory()[(i + 9) % 36], (i % 9) * 18 + 2, (i / 9) * 18 + 2);
            }
        } else {
            Gui.drawRect(1, 1, 162, 72, 0xFF666666);
            Minecraft.getMinecraft().fontRendererObj.drawSplitString("Player has disabled Inventory API", 6, 6, 142, -1);
        }
    }



    private MinecraftTooltip actualTooltip = new MinecraftTooltip();
    private MouseTooltip tooltip = null;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        List<String> toHover = null;
        if (getDomElement().getAbsBounds().contains(absMouseX, absMouseY)) {
            ItemStack toHoverStack = null;
            for (int i = 0; i < playerProfile.getInventory().length; i++) {
                int x = (i % 9) * 18 + 1;
                int y = (i / 9) * 18 + 1;
                if (x <= relMouseX && relMouseX < x + 18 && y <= relMouseY && relMouseY < y + 18) {
                    toHoverStack = playerProfile.getInventory()[(i + 9) % 36];
                }
            }


            if (toHoverStack != null) {
                List<String> list = toHoverStack.getTooltip(Minecraft.getMinecraft().thePlayer,
                        Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
                for (int i = 0; i < list.size(); ++i) {
                    if (i == 0) {
                        list.set(i, toHoverStack.getRarity().rarityColor + list.get(i));
                    } else {
                        list.set(i, EnumChatFormatting.GRAY + list.get(i));
                    }
                }
                toHover= list;
            }
        }

        if (toHover != null)
            actualTooltip.setTooltip(toHover);

        if (toHover == null && this.tooltip != null) {
            PopupMgr.getPopupMgr(getDomElement())
                    .closePopup(this.tooltip, null);
            this.tooltip = null;
        } else if (toHover != null && this.tooltip == null)
            PopupMgr.getPopupMgr(getDomElement())
                    .openPopup(this.tooltip = new MouseTooltip(absMouseX, absMouseY, actualTooltip), null);
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
