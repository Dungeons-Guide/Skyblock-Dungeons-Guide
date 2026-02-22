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

package kr.syeyoung.modapi.v1_8_9.render;

import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.rendering.TextStyleConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class DefaultFontRendererImpl {
    private static DefaultFontRendererImpl instance;

    public static DefaultFontRendererImpl getInstance() {
        if (instance == null) {
            instance = new DefaultFontRendererImpl();
        }
        return instance;
    }

    private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
    protected int[] charWidth = new int[256];
    protected byte[] glyphData = new byte[65536];
    protected final ResourceIdentifier locationFontTexture = new ResourceIdentifier("textures/font/ascii.png");

    private DefaultFontRendererImpl() {
        readGlyphSizes();
        readFontTexture();

        for (int i = 0; i < 255; i++) {
            glyphData[0xed00 + i] = 14;
        }
        glyphData[0xed02] = 1;
    }

    public void onResourceReload() {
        readGlyphSizes();
        readFontTexture();

        for (int i = 0; i < 255; i++) {
            glyphData[0xed00 + i] = 14;
        }
        glyphData[0xed02] = 1;
    }

    private void readFontTexture() {
        BufferedImage bufferedimage;
        try {
            bufferedimage = TextureUtil.readBufferedImage(
                    ModAPI.getAPI().getResourceManager().getResource(this.locationFontTexture).getInputStream());
        } catch (IOException var17) {
            throw new RuntimeException(var17);
        }

        int i = bufferedimage.getWidth();
        int j = bufferedimage.getHeight();
        int[] aint = new int[i * j];
        bufferedimage.getRGB(0, 0, i, j, aint, 0, i);
        int k = j / 16;
        int l = i / 16;
        int i1 = 1;
        float f = 8.0F / (float)l;

        for(int j1 = 0; j1 < 256; ++j1) {
            int k1 = j1 % 16;
            int l1 = j1 / 16;
            if (j1 == 32) {
                this.charWidth[j1] = 3 + i1;
            }

            int i2;
            for(i2 = l - 1; i2 >= 0; --i2) {
                int j2 = k1 * l + i2;
                boolean flag = true;

                for(int k2 = 0; k2 < k && flag; ++k2) {
                    int l2 = (l1 * l + k2) * i;
                    if ((aint[j2 + l2] >> 24 & 255) != 0) {
                        flag = false;
                    }
                }

                if (!flag) {
                    break;
                }
            }

            ++i2;
            this.charWidth[j1] = (int)(0.5 + (double)((float)i2 * f)) + i1;
        }
    }

    private void readGlyphSizes() {
        InputStream inputstream = null;

        try {
            inputstream = ModAPI.getAPI().getResourceManager().getResource(new ResourceIdentifier("font/glyph_sizes.bin")).getInputStream();
            inputstream.read(this.glyphData);
        } catch (IOException var6) {
            throw new RuntimeException(var6);
        } finally {
            IOUtils.closeQuietly(inputstream);
        }
    }

    private ResourceLocation getUnicodePageLocation(int page) {
        if (unicodePageLocations[page] == null) {
            unicodePageLocations[page] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", page));
        }

        return unicodePageLocations[page];
    }

    public double getWidth(char text, TextStyleConfig textStyle) {
        double val;
        if (text == '\n') return 0;
        if (text == ' ') {
            val = 4;
        } else {
            int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(text);
            if (text > 0 && i != -1) {
                val = this.charWidth[i];
            } else if (this.glyphData[text] != 0) {
                int texStart = this.glyphData[text] >>> 4;
                int texEnd = (this.glyphData[text] & 15) + 1;
                val = (texEnd - texStart) / 2.0 + 1;
            } else {
                val = 0;
            }
        }
        return (val + (textStyle.isBold() ? 1 : 0)) * textStyle.getSize() / 8;
    }

    public double getBaselineHeight(TextStyleConfig textStyle) {
        return 7 * textStyle.getSize() / 8.0;
    }

    public void renderString(String text, double x, double y, TextStyleConfig style) {
        char[] chars = text.toCharArray();
        double startX = x;

        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();

        // Render shadow if enabled
        if (style.isShadow()) {
            renderChars(worldRenderer, chars, x + 1, y + 1, style, true);
        }

        // Render main text
        renderChars(worldRenderer, chars, x, y, style, false);

        GlStateManager.disableTexture2D();
        double endX = startX;
        for (char c : chars) {
            endX += getWidth(c, style);
        }

        double baseline = getBaselineHeight(style);

        // Render strikethrough
        if (style.isStrikethrough()) {
            setColor(style.getStrikethroughColor());
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            worldRenderer.pos(startX, y + baseline / 2, 0).endVertex();
            worldRenderer.pos(startX, y + baseline / 2 + 1, 0).endVertex();
            worldRenderer.pos(endX, y + baseline / 2 + 1, 0).endVertex();
            worldRenderer.pos(endX, y + baseline / 2, 0).endVertex();
            draw(worldRenderer);
        }

        // Render underline
        if (style.isUnderline()) {
            setColor(style.getUnderlineColor());
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            worldRenderer.pos(startX, y + baseline + style.getTopAscent() * style.getSize(), 0).endVertex();
            worldRenderer.pos(startX, y + baseline + style.getTopAscent() * style.getSize() + 1, 0).endVertex();
            worldRenderer.pos(endX, y + baseline + style.getTopAscent() * style.getSize() + 1, 0).endVertex();
            worldRenderer.pos(endX, y + baseline + style.getTopAscent() * style.getSize(), 0).endVertex();
            draw(worldRenderer);
        }

        GlStateManager.enableTexture2D();
    }

    private void renderChars(WorldRenderer worldRenderer, char[] chars, double x, double y, TextStyleConfig style, boolean isShadow) {
        int color = isShadow ? style.getShadowColor() : style.getTextColor();
        setColor(color);

        GlStateManager.enableTexture2D();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        ResourceLocation lastBound = null;

        for (char c : chars) {
            double offset = renderChar(worldRenderer, x, y + 1, c, style, lastBound);
            if (style.isBold()) {
                renderChar(worldRenderer, x + 1, y + 1, c, style, lastBound);
                offset += 1;
            }
            x += offset;
        }

        draw(worldRenderer);
    }

    private double renderChar(WorldRenderer worldRenderer, double x, double y, char ch, TextStyleConfig textStyle, ResourceLocation lastBound) {
        if (ch == '\n') return 0;
        if (ch == ' ') {
            return 4.0F * textStyle.getSize() / 8.0;
        } else {
            int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(ch);
            return i != -1 ? this.renderDefaultChar(worldRenderer, x, y, i, textStyle, lastBound) : this.renderUnicodeChar(worldRenderer, x, y, ch, textStyle, lastBound);
        }
    }

    private void draw(WorldRenderer renderer) {
        renderer.finishDrawing();
        if (renderer.getVertexCount() > 0) {
            VertexFormat vertexformat = renderer.getVertexFormat();
            int i = vertexformat.getNextOffset();
            ByteBuffer bytebuffer = renderer.getByteBuffer();
            List<VertexFormatElement> list = vertexformat.getElements();

            int i1;
            for(i1 = 0; i1 < list.size(); ++i1) {
                VertexFormatElement vertexformatelement = (VertexFormatElement)list.get(i1);
                vertexformatelement.getUsage().preDraw(vertexformat, i1, i, bytebuffer);
            }

            GL11.glDrawArrays(renderer.getDrawMode(), 0, renderer.getVertexCount());
            i1 = 0;

            for(int j1 = list.size(); i1 < j1; ++i1) {
                VertexFormatElement vertexFormatElement1 = (VertexFormatElement)list.get(i1);
                vertexFormatElement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
            }
        }

        renderer.reset();
    }

    private void bindTexture(ResourceLocation resourceLocation) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
    }

    private void setColor(int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;
        GlStateManager.color(r, g, b, a);
    }

    private double renderDefaultChar(WorldRenderer worldRenderer, double posX, double posY, int ch, TextStyleConfig textStyle, ResourceLocation lastBound) {
        int texX = ch % 16 * 8;
        int texY = ch / 16 * 8;
        int italicsAddition = textStyle.isItalic() ? 1 : 0;

        ResourceLocation texture = new ResourceLocation(this.locationFontTexture.getMod(), this.locationFontTexture.getLocation());
        if (lastBound == null || !lastBound.equals(texture)) {
            bindTexture(texture);
            lastBound = texture;
        }

        double texWidth = (this.charWidth[ch]);
        double charWidth = (texWidth - 1) * textStyle.getSize() / 8.0;
        double charHeight = textStyle.getSize();

        worldRenderer.pos(posX + (float)italicsAddition, posY, 0.0F).tex((float)texX / 128.0F, (float)texY / 128.0F).endVertex();
        worldRenderer.pos(posX - (float)italicsAddition, posY + charHeight - 0.01F, 0.0F).tex((float)texX / 128.0F, ((float)texY + 7.99F) / 128.0F).endVertex();
        worldRenderer.pos(posX + charWidth - (float)italicsAddition - 0.01F, posY + charHeight - 0.01F, 0.0F).tex(((float)texX + texWidth - 1.01F) / 128.0F, ((float)texY + 7.99F) / 128.0F).endVertex();
        worldRenderer.pos(posX + charWidth + (float)italicsAddition - 0.01F, posY, 0.0F).tex(((float)texX + texWidth - 1.01F) / 128.0F, (float)texY / 128.0F).endVertex();

        return texWidth * textStyle.getSize() / 8.0;
    }

    private double renderUnicodeChar(WorldRenderer worldRenderer, double posX, double posY, char ch, TextStyleConfig textStyle, ResourceLocation lastBound) {
        if (this.glyphData[ch] == 0) {
            return 0.0F;
        } else {
            int i = ch / 256;
            ResourceLocation texture = this.getUnicodePageLocation(i);
            if (lastBound == null || !lastBound.equals(texture)) {
                bindTexture(texture);
                lastBound = texture;
            }

            float xStart = (float)(this.glyphData[ch] >>> 4);
            float xEnd = (float)((this.glyphData[ch] & 15) + 1);

            float texX = (float)(ch % 16 * 16) + xStart;
            float texY = (float)((ch & 255) / 16 * 16);
            float texWidth = xEnd - xStart - 0.02F;
            float italicSlope = textStyle.isItalic() ? 1.0F : 0.0F;

            double charWidth = texWidth * textStyle.getSize() / 16.0;
            double charHeight = textStyle.getSize();

            worldRenderer.pos(posX + italicSlope, posY, 0.0F)
                    .tex(texX / 256.0F, texY / 256.0F).endVertex();
            worldRenderer.pos(posX - italicSlope, posY + charHeight - 0.01F, 0.0F)
                    .tex(texX / 256.0F, (texY + 15.98F) / 256.0F).endVertex();
            worldRenderer.pos(posX + charWidth - italicSlope, posY + charHeight - 0.01F, 0.0F)
                    .tex((texX + texWidth) / 256.0F, (texY + 15.98F) / 256.0F).endVertex();
            worldRenderer.pos(posX + charWidth + italicSlope, posY, 0.0F)
                    .tex((texX + texWidth) / 256.0F, (texY) / 256.0F).endVertex();
            return charWidth + textStyle.getSize() / 8.0;
        }
    }
}


