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

package kr.syeyoung.dungeonsguide.gui.elements;

import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class MModalConfirmation extends MModal {
    private String content;
    private Runnable callBackYes;
    private Runnable callBackNo;
    @Getter
    private MButton yes, no;
    public MModalConfirmation(String title, String content, Runnable callBackYes, Runnable callBackNo) {
        super();
        setTitle(title);
        this.content = content;
        this.callBackYes = callBackYes;
        this.callBackNo = callBackNo;
        this.yes = new MButton();
        this.no = new MButton();
        yes.setText("Yes"); no.setText("No");
        no.setBackground(RenderUtils.blendAlpha(0x141414, 0.15f));
        no.setHover(RenderUtils.blendAlpha(0x141414, 0.17f));
        yes.setOnActionPerformed(() -> {
            close();
            if (callBackYes != null) callBackYes.run();
        });
        no.setOnActionPerformed(() -> {
            close();
            if (callBackNo != null) callBackNo.run();
        });
        yes.setBackground(RenderUtils.blendAlpha(0x141414, 0.15f));
        yes.setHover(RenderUtils.blendAlpha(0x141414, 0.17f));

        add(new ConfirmationContent());
    }

    public class ConfirmationContent extends MPanel {
        public ConfirmationContent() {
            add(yes); add(no);
        }

        @Override
        public void resize(int parentWidth, int parentHeight) {
            setBounds(new Rectangle(0,0,parentWidth, parentHeight));
        }

        @Override
        public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
            GlStateManager.translate(5,5,0);
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            fr.drawSplitString(content, 0,0, ConfirmationContent.this.bounds.width-10, -1);
        }

        @Override
        public void setBounds(Rectangle bounds) {
            super.setBounds(bounds);
            yes.setBounds(new Rectangle(10,bounds.height-25,(bounds.width-30)/2, 15));
            no.setBounds(new Rectangle(yes.getBounds().x + yes.getBounds().width + 10,bounds.height-25,(bounds.width-30)/2, 15));
        }
    }
}
