/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
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
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import java.util.Collections;
import java.util.List;

public class ItemStackRender extends AnnotatedExportOnlyWidget implements Renderer, Layouter {
    @Export(attributeName="itemstack")
    public final BindableAttribute<ItemStack> itemstack = new BindableAttribute<ItemStack>(ItemStack.class);

    @Export(attributeName = "hover")
    public final BindableAttribute<Boolean> hover = new BindableAttribute<Boolean>(Boolean.class, false);

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        if (itemstack.getValue() == null) return;

        double min = Math.min(getDomElement().getSize().getWidth(),
                getDomElement().getSize().getHeight());


        RenderItem renderItem=  Minecraft.getMinecraft().getRenderItem();
        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.scale(min/18.0, min/18.0, 1.0);
        GlStateManager.enableDepth();
        renderItem.renderItemAndEffectIntoGUI(itemstack.getValue(), 0,0);
        GlStateManager.popMatrix();
        GlStateManager.disableDepth();
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        if (itemstack.getValue() == null) return new Size(constraintBox.getMinWidth(), constraintBox.getMinHeight());

        double size = Math.min(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());

        return new Size(Layouter.clamp(size, constraintBox.getMinWidth(), constraintBox.getMaxWidth()),
                Layouter.clamp(size, constraintBox.getMinHeight(), constraintBox.getMaxHeight()));
    }

    @Override
    public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
        return height;
    }

    @Override
    public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
        return width;
    }


    private MinecraftTooltip actualTooltip = new MinecraftTooltip();
    private MouseTooltip tooltip = null;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX, double relMouseY, boolean childHandled) {
        if (hover.getValue() == null || !hover.getValue()) return true;

        List<String> toHover = null;
        if (getDomElement().getAbsBounds().contains(absMouseX, absMouseY)) {
            ItemStack toHoverStack = itemstack.getValue();

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
                    .closePopup(this.tooltip);
            this.tooltip = null;
        } else if (toHover != null && this.tooltip == null)
            PopupMgr.getPopupMgr(getDomElement())
                    .openPopup(this.tooltip = new MouseTooltip(actualTooltip), (a) -> {
                        this.tooltip = null;
                    });
        return true;
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
