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

package kr.syeyoung.dungeonsguide.mod.features.impl.discord.inviteViewer;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
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
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

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

    private long startedPlayingAt = -1;

    private int delayTime;

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

        IIOMetadata imageMetaData =  reader.getImageMetadata(0);
        String metaFormatName = imageMetaData.getNativeMetadataFormatName();

        IIOMetadataNode root = (IIOMetadataNode)imageMetaData.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");

        delayTime = Integer.parseInt(graphicsControlExtensionNode.getAttribute("delayTime"));


        image = new BufferedImage(width, height * frames, dummyFrame.getType());
        Graphics2D graphics2D = image.createGraphics();

        for (int i = 0; i < frames; i++) {
            BufferedImage bufferedImage = reader.read(i);
            graphics2D.drawImage(bufferedImage, 0, i*height, null);
        }
        reader.dispose(); imageInputStream.close(); huc.disconnect();
    }


    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        int nNodes = rootNode.getLength();
        for (int i = 0; i < nNodes; i++) {
            if (rootNode.item(i).getNodeName().compareToIgnoreCase(nodeName)== 0) {
                return((IIOMetadataNode) rootNode.item(i));
            }
        }
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return(node);
    }

    public void drawFrame(double x, double y, double width, double height) {
        if (getResourceLocation() == null)
            buildGLThings();
        if (startedPlayingAt == -1) startedPlayingAt = System.currentTimeMillis();

        int frame = (int) (((System.currentTimeMillis() - startedPlayingAt) / delayTime) % frames);

        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
        textureManager.bindTexture(getResourceLocation());

        GlStateManager.color(1, 1, 1, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, (y + height), 0.0D)
                .tex(0,((frame+1) * height)/ ((double)frames * height)).endVertex();
        worldrenderer.pos((x + width), (y + height), 0.0D)
                .tex(1, ((frame+1) * height)/ ((double)frames * height)).endVertex();
        worldrenderer.pos((x + width), y, 0.0D)
                .tex(1,(frame * height)/ ((double)frames * height)).endVertex();
        worldrenderer.pos(x, y, 0.0D)
                .tex(0,  (frame * height) / ((double)frames * height)).endVertex();
        tessellator.draw();
    }

    public static final ExecutorService executorService = Executors.newFixedThreadPool(3, DungeonsGuide.THREAD_FACTORY);
    public static final Map<String, ImageTexture> imageMap = new HashMap<>();
    public static final Logger logger = LogManager.getLogger("DG-ImageLoader");
    public static void loadImage(String url, Consumer<ImageTexture> callback) {
        if (imageMap.containsKey(url)) {
            callback.accept(imageMap.get(url));
            return;
        }
        if (url.isEmpty()) callback.accept(null);
        executorService.submit(() -> {
            try {
                ImageTexture imageTexture = new ImageTexture(url);
                imageMap.put(url, imageTexture);
                callback.accept(imageTexture);
            } catch (Exception e) {
                callback.accept(null);
                logger.log(Level.WARN, "An error occured while loading image from: "+url, e);
            }
        });
    }
}
