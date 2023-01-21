/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.launcher.gui.screen;

import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiPrivacyPolicy extends SpecialGuiScreen {
    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

        int width = Math.min(300, sr.getScaledWidth() / 2 - 20);

        this.buttonList.add(new GuiButton(0, sr.getScaledWidth()/2 + 10,sr.getScaledHeight()-40, width, 20,"Accept Privacy Policy"));
        this.buttonList.add(new GuiButton(1, sr.getScaledWidth() / 2 - 10 - width,sr.getScaledHeight()-40, width, 20,"Deny and Play Without DG"));
    }


    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 0) {
            // accept
            try {
                AuthManager.getInstance().acceptPrivacyPolicy(1);
            } catch (Exception e) {
                e.printStackTrace();
//                GuiDisplayer.INSTANCE.displayGui(new GuiLoadingError(e));
                // display tooltip.
            }
            dismiss();
        } else if (button.id == 1) {
            dismiss();
        }
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(0);

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;

        fontRenderer.drawString("Please accept or deny Dungeons Guide Privacy Policy to continue", (sr.getScaledWidth()-fontRenderer.getStringWidth("Please accept or deny Dungeons Guide Privacy Policy to continue"))/2,40,0xFFFF0000);
        fontRenderer.drawString("Blah blah legal stuff", (sr.getScaledWidth()-fontRenderer.getStringWidth("Please accept or deny Dungeons Guide Privacy Policy to continue"))/2,sr.getScaledHeight() / 2, 0xFFFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static void clip(ScaledResolution resolution, int x, int y, int width, int height) {
        if (width < 0 || height < 0) return;

        int scale = resolution.getScaleFactor();
        GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }
}
