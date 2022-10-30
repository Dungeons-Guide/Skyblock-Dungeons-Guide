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

package kr.syeyoung.dungeonsguide.mod.gui.elements;

import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class MModal extends MTooltip {
    @Getter
    private MPanel modalContent = new MPanel();

    @Getter @Setter
    private String title = "Default Title";

    public MModal() {
        super.add(modalContent);
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        super.resize(parentWidth, parentHeight);
        setBounds(new Rectangle(0,0, parentWidth, parentHeight));
    }

    @Getter @Setter
    private Dimension modalSize = new Dimension(300,200);


    @Override
    public void onBoundsUpdate() {
        super.onBoundsUpdate();
        Dimension effDim = getEffectiveDimension();

        modalContent.setBounds(new Rectangle((effDim.width - modalSize.width)/2, (effDim.height - modalSize.height)/2 + 16, modalSize.width, modalSize.height-16));
    }

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
        RenderUtils.drawGradientRect(0, 0, bounds.width, bounds.height, -1072689136, -804253680);
        Dimension effDim = getEffectiveDimension();
        int x = (effDim.width-modalSize.width)/2;
        int y = (effDim.height - modalSize.height)/2;
        GlStateManager.translate(x,y, 0);
        RenderUtils.drawRoundedRectangle(0,0,modalSize.width, modalSize.height, 3, Math.PI/8, RenderUtils.blendAlpha(0x141414, 0.20f));
        Gui.drawRect(0,15, modalSize.width, 16, 0xFF02EE67);
        Gui.drawRect(0,16,modalSize.width, 26,  RenderUtils.blendAlpha(0x141414, 0.1f));
        RenderUtils.drawRoundedRectangle(0,16,modalSize.width, modalSize.height-16, 3, Math.PI/8, RenderUtils.blendAlpha(0x141414, 0.1f));

        GlStateManager.enableTexture2D();

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        fr.drawString(title, 5,(16-fr.FONT_HEIGHT)/2, -1);
    }

    @Override
    public void add(MPanel child) {
        modalContent.add(child);
    }

    protected void addSuper(MPanel child) {
        super.add(child);
    }

    @Override
    public void remove(MPanel panel) {
        modalContent.remove(panel);
    }
}
