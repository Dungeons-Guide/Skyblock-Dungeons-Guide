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

package kr.syeyoung.dungeonsguide.mod.gui.elements.image;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.VersionInfo;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.rendering.UNativeImageBackedTexture;
import kr.syeyoung.modapi.rendering.UTextureManager;
import lombok.Data;
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
    private ResourceIdentifier resourceLocation;

    private int width;
    private int height;
    private int frames;
    private int size;

    private long startedPlayingAt = -1;

    private int delayTime;

    public void buildGLThings() {

        UTextureManager textureManager = ModAPI.getAPI().getTextureManager();
        UNativeImageBackedTexture texture = textureManager.createTexture("ImageTexture: "+url, image.getWidth(), image.getHeight(), false);
        texture.load(image);
        texture.upload();
        resourceLocation = textureManager.registerTexture("dgurl/"+url, texture);
    }

    public ImageTexture(String url) throws IOException {
        this.url = url;

        URL urlObj = new URL(url);
        HttpURLConnection huc = (HttpURLConnection) urlObj.openConnection();
        huc.addRequestProperty("User-Agent", "DungeonsGuide (dungeons.guide, "+ VersionInfo.VERSION +")");
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

        try {
            delayTime = Integer.parseInt(graphicsControlExtensionNode.getAttribute("delayTime")) * 10;
        } catch (Exception e) {
            delayTime = 1000;
        }

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

    public void drawFrame(RenderingContext context, double x, double y, double width, double height) {
        if (getResourceLocation() == null)
            buildGLThings();

        if (startedPlayingAt == -1) startedPlayingAt = System.currentTimeMillis();

        int frame = (int) (((System.currentTimeMillis() - startedPlayingAt) / delayTime) % frames);


        context.drawScaledCustomSizeModalRect(
                resourceLocation,
                x, y, 0, frame,1, 1, width, height, 1, frames
        );
    }

    public static final ExecutorService executorService = DungeonsGuide.getDungeonsGuide().registerExecutorService(Executors.newFixedThreadPool(3, new ThreadFactoryBuilder()
            .setThreadFactory(DungeonsGuide.THREAD_FACTORY)
            .setNameFormat("DG-ImageFetcher-%d").build()));
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
                logger.log(Level.WARN, "An error occurred while loading image from: "+url, e);
            }
        });
    }
}
