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

package kr.syeyoung.dungeonsguide.mod.gui.elements.popups;

import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RawMinecraftTooltip extends Widget implements Renderer, Layouter {
    public List<String> tooltip = new ArrayList<>();
    private double mouseX;
    private double mouseY;

    public RawMinecraftTooltip(double mouseX, double mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }


    public void setTooltip(List<String> tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.emptyList();
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
    }

    @Override
    public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
        GlStateManager.disableBlend();
//        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();
        GlStateManager.disableTexture2D();
        GlStateManager.enableCull();
//        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        Rect abs = buildContext.getAbsBounds();
        Rect rel = buildContext.getRelativeBound();
        double relX = mouseX * rel.getWidth() / abs.getWidth();
        double relY = mouseY * rel.getHeight() / abs.getHeight();


        GuiUtils.drawHoveringText(tooltip, (int) relX, (int) relY,
                (int) buildContext.getRelativeBound().getWidth(),
                (int) buildContext.getRelativeBound().getHeight(), -1, Minecraft.getMinecraft().fontRendererObj);
    }

    public void setMousePos(double mouseX, double mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
        mouseX = absMouseX;
        mouseY = absMouseY;
        return false;
    }

    @Override
    public void mouseEntered(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        mouseX = absMouseX;
        mouseY = absMouseY;
    }
}
