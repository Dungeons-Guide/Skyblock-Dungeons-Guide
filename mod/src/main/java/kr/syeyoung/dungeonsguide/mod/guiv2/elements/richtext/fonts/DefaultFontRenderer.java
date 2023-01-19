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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.fonts;

import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.FlatTextSpan;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.richtext.TextStyle;
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

public class DefaultFontRenderer implements FontRenderer {
    public static DefaultFontRenderer DEFAULT_RENDERER = new DefaultFontRenderer();

    private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
    protected int[] charWidth = new int[256];
    public int FONT_HEIGHT = 9;
    protected byte[] glyphData = new byte[65536];
    protected final ResourceLocation locationFontTexture = new ResourceLocation("textures/font/ascii.png");

    public DefaultFontRenderer() {
        readGlyphSizes();
    }

    public void onResourceManagerReload() {
        this.readFontTexture();
        this.readGlyphSizes();
    }

    private void readFontTexture() {
        BufferedImage bufferedimage;
        try {
            bufferedimage = TextureUtil.readBufferedImage(
                    Minecraft.getMinecraft().getResourceManager().getResource(this.locationFontTexture).getInputStream());
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
            inputstream = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("font/glyph_sizes.bin")).getInputStream();
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

    @Override
    public double getWidth(char text, TextStyle textStyle) {
        double val;
        if (text == ' ') {
            val = 4;
        } else {
            int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(text);
            if (text > 0 && i != -1) {
                val = this.charWidth[i];
            } else if (this.glyphData[text] != 0) {
                int texStart = this.glyphData[text] >>> 4;
                int texEnd = this.glyphData[text] & 15 + 1;
                val = (texEnd - texStart) / 2.0 + 1;
            } else {
                val = 0;
            }
        }
        return (val + (textStyle.isBold() ? 1 : 0)) * textStyle.getSize() / 8;
    }

    @Override
    public double getBaselineHeight(TextStyle textStyle) {
        return 7 * textStyle.getSize() / 8.0;
    }

    public void render(WorldRenderer worldRenderer, FlatTextSpan lineElement, double x, double y, boolean isShadow) {
        double startX = x;
        if (isShadow) lineElement.textStyle.getShadowShader().useShader();

        if (!isShadow) lineElement.textStyle.getTextShader().useShader();
        GlStateManager.enableTexture2D();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        for (char c : lineElement.value) {
            x += renderChar(worldRenderer, x, y+1, c, lineElement.textStyle);
        }
        if (!isShadow) lineElement.textStyle.getTextShader().freeShader();
        draw(worldRenderer);

        GlStateManager.disableTexture2D();
        if (lineElement.textStyle.isStrikeThrough()) {
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            if (!isShadow) lineElement.textStyle.getStrikeThroughShader().useShader();
            worldRenderer.pos(startX, y + lineElement.textStyle.getSize()/2, 0);
            worldRenderer.pos(x, y + lineElement.textStyle.getSize()/2, 0);
            worldRenderer.pos(x, y + lineElement.textStyle.getSize()/2+1, 0);
            worldRenderer.pos(startX, y + lineElement.textStyle.getSize()/2+1, 0);
            if (!isShadow) lineElement.textStyle.getStrikeThroughShader().freeShader();
            draw(worldRenderer);
        }
        if (lineElement.textStyle.isUnderline()) {
            worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
            if (!isShadow) lineElement.textStyle.getUnderlineShader().useShader();
            worldRenderer.pos(startX, y + lineElement.textStyle.getSize(), 0);
            worldRenderer.pos(x, y + lineElement.textStyle.getSize(), 0);
            worldRenderer.pos(x, y + lineElement.textStyle.getSize()+1, 0);
            worldRenderer.pos(startX, y + lineElement.textStyle.getSize()+1, 0);
            if (!isShadow) lineElement.textStyle.getUnderlineShader().freeShader();
            draw(worldRenderer);
        }

        if (isShadow) lineElement.textStyle.getShadowShader().freeShader();

    }

    @Override
    public void render(FlatTextSpan lineElement, double x, double y, double currentScale) {
        WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        GlStateManager.disableTexture2D();
        if (lineElement.textStyle.getBackgroundShader() != null) {
            lineElement.textStyle.getBackgroundShader().useShader();
            worldRenderer.pos(x, y, 0);
            worldRenderer.pos(x + lineElement.getWidth(), y, 0);
            worldRenderer.pos(x + lineElement.getWidth(), y + lineElement.textStyle.getSize()+1, 0);
            worldRenderer.pos(x, y + lineElement.textStyle.getSize()+1, 0);
            lineElement.textStyle.getBackgroundShader().freeShader();
            draw(worldRenderer);
        }

        if (lineElement.textStyle.isShadow())
            render(worldRenderer, lineElement, x+1,y+1, true);
        render(worldRenderer, lineElement, x,y, false);
    }



    private double renderChar(WorldRenderer worldRenderer, double x, double y, char ch, TextStyle textStyle) {
        if (ch == ' ') {
            return 4.0F;
        } else {
            int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(ch);
            return i != -1 ? this.renderDefaultChar(worldRenderer, x, y, i, textStyle) : this.renderUnicodeChar(worldRenderer, x, y, ch, textStyle);
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
                VertexFormatElement vertexformatelement1 = (VertexFormatElement)list.get(i1);
                vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
            }
        }

        renderer.reset();
    }

    public void bindTexture(WorldRenderer worldRenderer, ResourceLocation resourceLocation) {
        draw(worldRenderer);
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
    }

    private double renderDefaultChar(WorldRenderer worldRenderer, double posX, double posY, int ch, TextStyle textStyle) {
        int texX = ch % 16 * 8;
        int texY = ch / 16 * 8;
        int italicsAddition = textStyle.italics ? 1 : 0;
        bindTexture(worldRenderer, this.locationFontTexture);
        double charWidth = (this.charWidth[ch] - 1.0F) * textStyle.getSize() / 8.0;
        double charHeight = textStyle.getSize();
        // char width contains the gap between next char


        worldRenderer.pos(posX + (float)italicsAddition, posY, 0.0F).tex((float)texX / 128.0F, (float)texY / 128.0F);
        worldRenderer.pos(posX - (float)italicsAddition, posY + charHeight - 0.01F, 0.0F).tex((float)texX / 128.0F, ((float)texY + 7.99F) / 128.0F);
        worldRenderer.pos(posX + charWidth+ (float)italicsAddition - 0.01F , posY, 0.0F).tex(((float)texX + charWidth - 1.01F) / 128.0F, (float)texY / 128.0F);
        worldRenderer.pos(posX  + charWidth - (float)italicsAddition - 0.01F, posY + charHeight - 0.01F, 0.0F).tex(((float)texX + charWidth - 1.01F) / 128.0F, ((float)texY + 7.99F) / 128.0F);

        return charWidth + textStyle.getSize() / 8.0;
    }
    private double renderUnicodeChar(WorldRenderer worldRenderer, double posX, double posY, char ch, TextStyle textStyle) {
        if (this.glyphData[ch] == 0) {
            return 0.0F;
        } else {
            int i = ch / 256;
            bindTexture(worldRenderer, this.getUnicodePageLocation(i));
            float xStart = (float)(this.glyphData[ch] >>> 4);
            float xEnd = (float)(this.glyphData[ch] & 15 + 1);

            float texX = (float)(ch % 16 * 16) + xStart;
            float texY = (float)((ch & 255) / 16 * 16);
            float texWidth = xEnd - xStart - 0.02F;
            float italicSlope = textStyle.italics ? 1.0F : 0.0F;

            double charWidth = texWidth * textStyle.getSize() / 16.0;
            double charHeight = textStyle.getSize();

            worldRenderer.pos(posX + italicSlope, posY, 0.0F)
                    .tex(texX / 256.0F, texY / 256.0F);
            worldRenderer.pos(posX - italicSlope, posY + charHeight - 0.01F, 0.0F)
                    .tex(texX / 256.0F, (texY + 15.98F) / 256.0F);
            worldRenderer.pos(posX + charWidth + italicSlope, posY, 0.0F)
                    .tex((texX + texWidth) / 256.0F, texY / 256.0F);
            worldRenderer.pos(posX + charWidth - italicSlope, posY + charHeight - 0.01F, 0.0F)
                    .tex((texX + texWidth) / 256.0F, (texY + 15.98F) / 256.0F);
            return charWidth + textStyle.getSize() / 8.0;
        }
    }
}
