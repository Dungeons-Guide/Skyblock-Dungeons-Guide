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

package kr.syeyoung.dungeonsguide.launcher.gui.screen;

import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.Notification;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.NotificationManager;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.WidgetNotification;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

public class GuiLoadingError extends SpecialGuiScreen {
    private final String stacktrace;
    public GuiLoadingError(Throwable cause) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        cause.printStackTrace(printStream);
        this.stacktrace = byteArrayOutputStream.toString();
    }

    @Override
    public void initGui() {
        super.initGui();
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        this.buttonList.add(new GuiButton(0, sr.getScaledWidth()/2-100,sr.getScaledHeight()-70 ,"Close Minecraft"));
        this.buttonList.add(new GuiButton(1, sr.getScaledWidth()/2-100,sr.getScaledHeight()-40 ,"Play Without DG"));
        this.buttonList.add(new GuiButton(2, 0,sr.getScaledHeight()-20 ,"Copy Error into Clipboard"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 0) {
            FMLCommonHandler.instance().exitJava(-1,true);
        } else if (button.id == 1) {
            dismiss();
        } else if (button.id == 2) {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(
                            new StringSelection(stacktrace),
                            null
                    );

            UUID uuid = UUID.randomUUID();
            NotificationManager.getInstance().updateNotification(uuid, new WidgetNotification(uuid, Notification.builder()
                            .title("Successfully Copied!")
                            .description("")
                            .titleColor(0xFF00FF00)
                    .build()));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(1);

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        fontRenderer.drawString("DungeonsGuide has ran into error while loading itself", (sr.getScaledWidth()-fontRenderer.getStringWidth("DungeonsGuide has ran into error while loading itself"))/2,40,0xFFFF0000);
        fontRenderer.drawString("Please contact DungeonsGuide support with this screen", (sr.getScaledWidth()-fontRenderer.getStringWidth("Please contact developer with this screen"))/2, (int) (40+fontRenderer.FONT_HEIGHT*1.5),0xFFFF0000);

        int tenth = sr.getScaledWidth() / 10;

        Gui.drawRect(tenth, 70,sr.getScaledWidth()-tenth, sr.getScaledHeight()-80, 0xFF5B5B5B);
        String[] split = stacktrace.split("\n");
        clip(sr, tenth, 70,sr.getScaledWidth()-2*tenth, sr.getScaledHeight()-150);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        for (int i = 0; i < split.length; i++) {
            fontRenderer.drawString(split[i].replace("\t", "    "), tenth+2,i*fontRenderer.FONT_HEIGHT + 72, 0xFFFFFFFF);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static void clip(ScaledResolution resolution, int x, int y, int width, int height) {
        if (width < 0 || height < 0) return;

        int scale = resolution.getScaleFactor();
        GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }
}
