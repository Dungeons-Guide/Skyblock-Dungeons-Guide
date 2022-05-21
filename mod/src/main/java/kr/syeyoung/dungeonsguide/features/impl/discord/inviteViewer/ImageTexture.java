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

package kr.syeyoung.dungeonsguide.features.impl.discord.inviteViewer;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

@Data
public class ImageTexture {
    private String url;
    private BufferedImage image;
    private DynamicTexture previewTexture;
    private ResourceLocation resourceLocation;

    private int width;
    private int height;
    private int frames;
    private int size;

    @Getter @Setter
    private int lastFrame = 0;

    public void buildGLThings() {
        previewTexture = new DynamicTexture(image);
        resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("dgurl/"+url, previewTexture);
    }

    public ImageTexture(String url) throws IOException {
        this.url = url;

        URL urlObj = new URL(url);
        HttpURLConnection huc = (HttpURLConnection) urlObj.openConnection();
        huc.addRequestProperty("User-Agent", "DungeonsGuideMod (dungeons.guide, 1.0)");
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(huc.getInputStream());
        Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
        if(!readers.hasNext()) throw new IOException("No image reader what" + url);
        ImageReader reader = readers.next();
        reader.setInput(imageInputStream);
        frames = reader.getNumImages(true);
        BufferedImage dummyFrame = reader.read(0);
        width = dummyFrame.getWidth(); height = dummyFrame.getHeight();


        image = new BufferedImage(width, height * frames, dummyFrame.getType());
        Graphics2D graphics2D = image.createGraphics();

        for (int i = 0; i < frames; i++) {
            BufferedImage bufferedImage = reader.read(i);
            graphics2D.drawImage(bufferedImage, 0, i*height, null);
        }
        reader.dispose(); imageInputStream.close(); huc.disconnect();
    }

    public void drawFrame(int frame, int x, int y, int width, int height) {
        if (getResourceLocation() == null)
            buildGLThings();

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(getResourceLocation());

        GlStateManager.color(1, 1, 1, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double)x, (double)(y + height), 0.0D)
                .tex(0,((frame+1) * height)/ ((double)frames * height)).endVertex();
        worldrenderer.pos((double)(x + width), (double)(y + height), 0.0D)
                .tex(1, ((frame+1) * height)/ ((double)frames * height)).endVertex();
        worldrenderer.pos((double)(x + width), (double)y, 0.0D)
                .tex(1,(frame * height)/ ((double)frames * height)).endVertex();
        worldrenderer.pos((double)x, (double)y, 0.0D)
                .tex(0,  (frame * height) / ((double)frames * height)).endVertex();
        tessellator.draw();
    }

    public void drawFrameAndIncrement(int x, int y, int width, int height) {
        drawFrame(lastFrame, x,y,width,height);
        lastFrame++;
        if (lastFrame >= frames) lastFrame = 0;
    }
}
