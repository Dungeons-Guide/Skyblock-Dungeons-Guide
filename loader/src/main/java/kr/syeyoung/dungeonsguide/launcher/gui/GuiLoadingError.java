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

package kr.syeyoung.dungeonsguide.launcher.gui;

import kr.syeyoung.dungeonsguide.launcher.util.QRCodeGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;

public class GuiLoadingError extends GuiScreen {
    private String stacktrace;
    private Throwable throwable;

    private DynamicTexture texture;
    private ResourceLocation location;
    private BufferedImage qrCode;
    private Runnable clear;

    public GuiLoadingError(Throwable t, Runnable clear) {
        this.throwable = t;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        stacktrace = sw.toString();


        try {
            qrCode = QRCodeGenerator.generateQRCode(stacktrace.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        texture = new DynamicTexture(qrCode.getWidth(), qrCode.getHeight());
        location = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("dg/errorqr", texture);

        qrCode.getRGB(0,0,qrCode.getWidth(), qrCode.getHeight(), texture.getTextureData(), 0, qrCode.getWidth());

        texture.updateDynamicTexture();
        this.clear = clear;
    }

    @Override
    public void initGui() {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        this.buttonList.add(new GuiButton(0, sr.getScaledWidth()/2-100,sr.getScaledHeight()-40 ,"Close Minecraft"));
        this.buttonList.add(new GuiButton(1, sr.getScaledWidth()/2-100,sr.getScaledHeight()-70 ,"Play Without DG"));
        clear.run();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button.id == 0) {
            FMLCommonHandler.instance().exitJava(-1,true);
        } else if (button.id == 1) {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawBackground(1);

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        fontRenderer.drawString("DungeonsGuide has ran into unknown error while loading itself", (sr.getScaledWidth()-fontRenderer.getStringWidth("DungeonsGuide has ran into unknown error while loading itself"))/2,40,0xFFFF0000);
        fontRenderer.drawString("Please contact DungeonsGuide support with this screen", (sr.getScaledWidth()-fontRenderer.getStringWidth("Please contact DungeonsGuide support with this screen"))/2, (int) (40+fontRenderer.FONT_HEIGHT*1.5),0xFFFF0000);

        int tenth = sr.getScaledWidth() / 10;

        Gui.drawRect(tenth, 70,sr.getScaledWidth()-tenth, sr.getScaledHeight()-80, 0xFF5B5B5B);
        String[] split = stacktrace.split("\n");
        clip(sr, tenth, 70,sr.getScaledWidth()-2*tenth, sr.getScaledHeight()-150);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        for (int i = 0; i < split.length; i++) {
            fontRenderer.drawString(split[i].replace("\t", "    "), tenth+2,i*fontRenderer.FONT_HEIGHT + 72, 0xFFFFFFFF);
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);


        GlStateManager.pushMatrix();
        GlStateManager.scale(1.0/sr.getScaleFactor(), 1.0/sr.getScaleFactor(), 1);
        GlStateManager.translate(0, Minecraft.getMinecraft().displayHeight - qrCode.getHeight() * 3, 0);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        float f = 0.0F;
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.location);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(1, 771, 0, 1);
        GlStateManager.disableAlpha();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(0, qrCode.getHeight()*3, 0).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(qrCode.getWidth()*3, qrCode.getHeight()*3, 0).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(qrCode.getWidth()*3, 0, 0).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(0, 0, 0).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public static void clip(ScaledResolution resolution, int x, int y, int width, int height) {
        if (width < 0 || height < 0) return;

        int scale = resolution.getScaleFactor();
        GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }
}
