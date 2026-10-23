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

package kr.syeyoung.dungeonsguide.mod.gui.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.DGRenderState;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.GlyphRunState;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.ItemState;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.RectState;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.ShaderShapeState;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.StencilState;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.TexturedRectState;
import kr.syeyoung.dungeonsguide.mod.render.gui.state.VanillaElementState;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderProgram;
import kr.syeyoung.dungeonsguide.mod.shader.ShaderUniforms;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Records what DG draws as render states (D71/D94). Transforms live on the pose stack (D93).
 */
public class RenderingContext {
    private final Matrix3x2fStack pose = new Matrix3x2fStack(64);
    @Getter
    private final List<DGRenderState> states = new ArrayList<>();
    @Getter
    private final int imageHeight;
    private boolean writingStencilMask = false;

    public RenderingContext() {
        this(Minecraft.getInstance().getWindow().getHeight());
    }

    public RenderingContext(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public Matrix3x2fStack pose() {
        return pose;
    }

    public void drawRect(double left, double top, double right, double bottom, int color) {
        double i;
        if (left < right) {
            i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            i = top;
            top = bottom;
            bottom = i;
        }

        drawQuad((float) left, (float) bottom, (float) right, (float) bottom, (float) right, (float) top, (float) left, (float) top, color);
    }

    // corners in order
    public void drawQuad(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        if (isClipEmpty()) return;
        states.add(new RectState(new Matrix3x2f(pose), new float[] {x0, y0, x1, y1, x2, y2, x3, y3}, color, currentClip(), writingStencilMask));
    }

    // core profile has no wide lines, a line is a quad of the given width (in local units)
    public void drawLine(double x0, double y0, double x1, double y1, double width, int color) {
        double dx = x1 - x0, dy = y1 - y0;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len == 0) return;
        double nx = -dy / len * width / 2, ny = dx / len * width / 2;
        // same winding as drawRect, gui pipelines cull back faces
        drawQuad((float) (x0 - nx), (float) (y0 - ny), (float) (x0 + nx), (float) (y0 + ny),
                (float) (x1 + nx), (float) (y1 + ny), (float) (x1 - nx), (float) (y1 - ny), color);
    }

    // 1.8.9's RenderUtils.drawRect with an AColor
    public void drawRect(double left, double top, double right, double bottom, AColor color) {
        double i;
        if (left < right) {
            i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            i = top;
            top = bottom;
            bottom = i;
        }

        if (!color.isChroma() && (color.getRGB() >> 24 & 255) == 0) return;
        if (!color.isChroma()) {
            drawRect(left, top, right, bottom, color.getRGB());
        } else {
            // TODO: 1.8.9 shaded chroma between the corners, the rect takes the (left, top) corner's color here
            drawRect(left, top, right, bottom, RenderUtils.getColorAt(left, top, color));
        }
    }

    // 1.8.9's RenderUtils.drawUnfilledBox with an int color and the rainbow chroma; width replaces the glLineWidth callers set beforehand
    public void drawUnfilledBox(double left, double top, double right, double bottom, double width, int color, boolean chroma) {
        double i;
        if (left < right) {
            i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            i = top;
            top = bottom;
            bottom = i;
        }

        if (!chroma && (color >> 24 & 255) == 0) return;
        if (!chroma) {
            drawLine(left, bottom, right, bottom, width, color);
            drawLine(right, bottom, right, top, width, color);
            drawLine(right, top, left, top, width, color);
            drawLine(left, top, left, bottom, width, color);
        } else {
            float blah = (System.currentTimeMillis()  / 10) % 360;
            // TODO: 1.8.9 shaded chroma between the corners, each edge takes its start corner's color here
            drawLine(left, bottom, right, bottom, width, Color.HSBtoRGB((((blah + 20) % 360) / 360.0f), 1, 1));
            drawLine(right, bottom, right, top, width, Color.HSBtoRGB((((blah + 40) % 360)  / 360.0f), 1, 1));
            drawLine(right, top, left, top, width, Color.HSBtoRGB((((blah + 20) % 360) / 360.0f), 1, 1));
            drawLine(left, top, left, bottom, width, Color.HSBtoRGB(blah / 360.0f, 1, 1));
        }
    }

    // 1.8.9's RenderUtils.drawUnfilledBox, a line loop; width replaces the glLineWidth callers set beforehand
    public void drawUnfilledBox(double left, double top, double right, double bottom, double width, AColor color) {
        double i;
        if (left < right) {
            i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            i = top;
            top = bottom;
            bottom = i;
        }

        if (!color.isChroma() && (color.getRGB() >> 24 & 255) == 0) return;
        if (!color.isChroma()) {
            drawLine(left, bottom, right, bottom, width, color.getRGB());
            drawLine(right, bottom, right, top, width, color.getRGB());
            drawLine(right, top, left, top, width, color.getRGB());
            drawLine(left, top, left, bottom, width, color.getRGB());
        } else {
            // TODO: 1.8.9 shaded chroma between the corners, each edge takes its start corner's color here
            drawLine(left, bottom, right, bottom, width, RenderUtils.getColorAt(left, bottom, color));
            drawLine(right, bottom, right, top, width, RenderUtils.getColorAt(right, bottom, color));
            drawLine(right, top, left, top, width, RenderUtils.getColorAt(right, top, color));
            drawLine(left, top, left, bottom, width, RenderUtils.getColorAt(left, top, color));
        }
    }

    public void drawScaledCustomSizeModalRect(Identifier texture, double x, double y, float u, float v, int uWidth, int vHeight, double width, double height, float tileWidth, float tileHeight) {
        drawScaledCustomSizeModalRect(texture, x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight, 0xFFFFFFFF);
    }

    public void drawScaledCustomSizeModalRect(Identifier texture, double x, double y, float u, float v, int uWidth, int vHeight, double width, double height, float tileWidth, float tileHeight, int color) {
        drawScaledCustomSizeModalRect(
                () -> Minecraft.getInstance().getTextureManager().getTexture(texture).getTextureView(),
                () -> Minecraft.getInstance().getTextureManager().getTexture(texture).getSampler(),
                x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight, color);
    }

    public void drawScaledCustomSizeModalRect(Supplier<GpuTextureView> texture, Supplier<GpuSampler> sampler, double x, double y, float u, float v, int uWidth, int vHeight, double width, double height, float tileWidth, float tileHeight, int color) {
        if (isClipEmpty()) return;
        float f = 1.0F / tileWidth;
        float g = 1.0F / tileHeight;
        states.add(new TexturedRectState(new Matrix3x2f(pose),
                (float) x, (float) y, (float) (x + width), (float) (y + height),
                u * f, (u + uWidth) * f, v * g, (v + vHeight) * g,
                color, texture, sampler, currentClip(), writingStencilMask));
    }

    // textured quad with normalized uvs
    public void drawTexture(Supplier<GpuTextureView> texture, Supplier<GpuSampler> sampler, double x0, double y0, double x1, double y1, double u0, double u1, double v0, double v1, int color) {
        if (isClipEmpty()) return;
        states.add(new TexturedRectState(new Matrix3x2f(pose),
                (float) x0, (float) y0, (float) x1, (float) y1,
                (float) u0, (float) u1, (float) v0, (float) v1,
                color, texture, sampler, currentClip(), writingStencilMask));
    }

    public static Supplier<GpuSampler> nearestSampler() {
        return () -> RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
    }

    // glyphs from vanilla Font (D79), consecutive glyphs on the same pipeline and texture become one run
    public void drawGlyphs(List<TextRenderable> glyphs) {
        if (isClipEmpty() || glyphs.isEmpty()) return;
        List<TextRenderable> run = new ArrayList<>();
        for (TextRenderable glyph : glyphs) {
            if (!run.isEmpty() && (run.get(0).guiPipeline() != glyph.guiPipeline() || run.get(0).textureView() != glyph.textureView())) {
                states.add(new GlyphRunState(new Matrix3x2f(pose), run, currentClip(), writingStencilMask));
                run = new ArrayList<>();
            }
            run.add(glyph);
        }
        states.add(new GlyphRunState(new Matrix3x2f(pose), run, currentClip(), writingStencilMask));
    }

    // plain vanilla text, what 1.8.9's FontRenderer.drawString did (formatting codes included)
    public void drawString(String text, float x, float y, int color, boolean shadow) {
        // 1.8.9's font renderer made colors without alpha opaque
        if ((color & 0xFC000000) == 0) color |= 0xFF000000;
        List<TextRenderable> glyphs = new ArrayList<>();
        Minecraft.getInstance().font.prepareText(text, x, y, color, shadow, 0).visit(new Font.GlyphVisitor() {
            @Override
            public void acceptGlyph(TextRenderable.Styled glyph) {
                glyphs.add(glyph);
            }

            @Override
            public void acceptEffect(TextRenderable effect) {
                glyphs.add(effect);
            }
        });
        drawGlyphs(glyphs);
    }

    private static final Identifier EXPERIENCE_BAR_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("hud/experience_bar_background");
    private static final Identifier EXPERIENCE_BAR_PROGRESS_SPRITE = Identifier.withDefaultNamespace("hud/experience_bar_progress");

    // 1.8.9's RenderUtils.renderBar: the xp bar's halves at 1:1 from 1.8.9's icons.png, here from vanilla's xp bar sprites
    public void renderBar(float x, float y, float xSize, float completed) {
        float fCompleted = (float)Math.round(completed / 0.05F) * 0.05F;
        float notCompleted = 1.0F - fCompleted;
        drawVanilla(graphics -> {
            float width;
            if (fCompleted < 0.5F) {
                width = (0.5F - fCompleted) * xSize;
                barPiece(graphics, EXPERIENCE_BAR_BACKGROUND_SPRITE, xSize * fCompleted, x + xSize * fCompleted, y, width);
            }

            if (fCompleted < 1.0F) {
                width = Math.min(xSize * notCompleted, xSize / 2.0F);
                barPiece(graphics, EXPERIENCE_BAR_BACKGROUND_SPRITE, 182.0F - xSize / 2.0F + Math.max(xSize * (fCompleted - 0.5F), 0.0F),
                        x + xSize / 2.0F + Math.max(xSize * (fCompleted - 0.5F), 0.0F), y, width);
            }

            if (fCompleted > 0.0F) {
                width = Math.min(xSize * fCompleted, xSize / 2.0F);
                barPiece(graphics, EXPERIENCE_BAR_PROGRESS_SPRITE, 0.0F, x, y, width);
            }

            if (fCompleted > 0.5F) {
                width = Math.min(xSize * (fCompleted - 0.5F), xSize / 2.0F);
                barPiece(graphics, EXPERIENCE_BAR_PROGRESS_SPRITE, 182.0F - xSize / 2.0F, x + xSize / 2.0F, y, width);
            }
        });
    }

    private static void barPiece(GuiGraphicsExtractor graphics, Identifier sprite, float u, float x, float y, float width) {
        if (Math.round(width) <= 0) return;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, 182, 5, Math.round(u), 0, Math.round(x), Math.round(y), Math.round(width), 5);
    }

    // what 1.8.9's FontRenderer.drawSplitString did, wrapped at wrapWidth with formatting carried to the next line
    public void drawSplitString(String text, float x, float y, int wrapWidth, int color) {
        if ((color & 0xFC000000) == 0) color |= 0xFF000000;
        Font font = Minecraft.getInstance().font;
        for (FormattedText line : font.getSplitter().splitLines(text, wrapWidth, Style.EMPTY)) {
            List<TextRenderable> glyphs = new ArrayList<>();
            font.prepareText(Language.getInstance().getVisualOrder(line), x, y, color, false, false, 0).visit(new Font.GlyphVisitor() {
                @Override
                public void acceptGlyph(TextRenderable.Styled glyph) {
                    glyphs.add(glyph);
                }

                @Override
                public void acceptEffect(TextRenderable effect) {
                    glyphs.add(effect);
                }
            });
            drawGlyphs(glyphs);
            y += font.lineHeight;
        }
    }

    // rect drawn with a DG shader, what 1.8.9 did with useShader + drawRect (D103)
    public void drawShape(ShaderProgram shader, ShaderUniforms uniforms, double left, double top, double right, double bottom, int color) {
        double i;
        if (left < right) {
            i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            i = top;
            top = bottom;
            bottom = i;
        }
        drawShape(shader, uniforms, (float) left, (float) bottom, (float) right, (float) bottom, (float) right, (float) top, (float) left, (float) top,
                color, color, color, color);
    }

    // corners and their colors in order
    public void drawShape(ShaderProgram shader, ShaderUniforms uniforms, float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3,
                          int color0, int color1, int color2, int color3) {
        if (isClipEmpty()) return;
        states.add(new ShaderShapeState(new Matrix3x2f(pose), shader, uniforms, new float[] {x0, y0, x1, y1, x2, y2, x3, y3},
                new int[] {color0, color1, color2, color3}, currentClip(), writingStencilMask));
    }

    // the pipelines draw quads, a triangle is a quad with its last corner repeated
    private void drawShapeTriangle(ShaderProgram shader, ShaderUniforms uniforms, float x0, float y0, float x1, float y1, float x2, float y2, int color) {
        drawShape(shader, uniforms, x0, y0, x1, y1, x2, y2, x2, y2, color, color, color, color);
    }

    // what GL_TRIANGLE_FAN did, xy pairs
    public void drawShapeTriangleFan(ShaderProgram shader, ShaderUniforms uniforms, int color, float... xy) {
        for (int i = 2; i + 3 < xy.length; i += 2)
            drawShapeTriangle(shader, uniforms, xy[0], xy[1], xy[i], xy[i + 1], xy[i + 2], xy[i + 3], color);
    }

    // what GL_TRIANGLE_STRIP did, xy pairs
    public void drawShapeTriangleStrip(ShaderProgram shader, ShaderUniforms uniforms, int color, float... xy) {
        for (int i = 0; i + 5 < xy.length; i += 2)
            drawShapeTriangle(shader, uniforms, xy[i], xy[i + 1], xy[i + 2], xy[i + 3], xy[i + 4], xy[i + 5], color);
    }

    // 16x16 gui item at x, y, like 1.8.9's renderItemAndEffectIntoGUI (D100)
    public void drawItem(ItemStack itemStack, float x, float y) {
        if (isClipEmpty() || itemStack.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        TrackingItemStackRenderState item = new TrackingItemStackRenderState();
        mc.getItemModelResolver().updateForTopItem(item, itemStack, ItemDisplayContext.GUI, mc.level, mc.player, 0);
        states.add(new ItemState(new Matrix3x2f(pose), item, x, y, currentClip(), writingStencilMask));
    }

    // runs vanilla extraction code at DG's pose and records what it extracted (D102)
    public void drawVanilla(Consumer<GuiGraphicsExtractor> extraction) {
        if (isClipEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        GuiRenderState renderState = new GuiRenderState();
        GuiGraphicsExtractor graphics = new GuiGraphicsExtractor(mc, renderState, -1, -1);
        graphics.pose().set(pose);
        extraction.accept(graphics);

        renderState.forEachElement(element -> states.add(new VanillaElementState(element, currentClip(), writingStencilMask)), GuiRenderState.TraverseRange.ALL);
        renderState.forEachText(text -> {
            List<TextRenderable> glyphs = new ArrayList<>();
            text.ensurePrepared().visit(new Font.GlyphVisitor() {
                @Override
                public void acceptGlyph(TextRenderable.Styled glyph) {
                    glyphs.add(glyph);
                }

                @Override
                public void acceptEffect(TextRenderable effect) {
                    glyphs.add(effect);
                }
            });
            if (!glyphs.isEmpty())
                states.add(new GlyphRunState(new Matrix3x2f(text.pose), glyphs, currentClip(), writingStencilMask));
        });
        renderState.forEachItem(item -> states.add(new ItemState(new Matrix3x2f(item.pose()), item.itemStackRenderState(), item.x(), item.y(), currentClip(), writingStencilMask)));
    }

    // what Stencil / NegativeStencil did with raw gl in 1.8.9, now recorded (D74/D75)
    public void beginStencilMask() {
        states.add(new StencilState(StencilState.Action.BEGIN_MASK, 1, currentClip()));
        writingStencilMask = true;
    }

    public void beginStencilMasked(int ref) {
        states.add(new StencilState(StencilState.Action.BEGIN_MASKED, ref, currentClip()));
        writingStencilMask = false;
    }

    public void endStencil() {
        states.add(new StencilState(StencilState.Action.END, 0, currentClip()));
        writingStencilMask = false;
    }

    public Stack<Rectangle> clips = new Stack<>();

    public Rectangle currentClip() {
        return clips.empty() ? null : clips.peek();
    }

    // 1.8.9 turned off the color mask for an empty clip
    private boolean isClipEmpty() {
        Rectangle clip = currentClip();
        return clip != null && (clip.width <= 0 || clip.height <= 0);
    }


    public boolean canDraw(Rect bounds) {
        Rectangle clip = currentClip();
        if (clip == null) return true;

        if (clip.isEmpty() || bounds.getWidth() <= 0 || bounds.getHeight() <= 0) {
            return false;
        }
        double x0 = clip.x;
        double y0 = imageHeight - clip.y - clip.height;
        return (bounds.getX() + bounds.getWidth() > x0 &&
                bounds.getY() + bounds.getHeight() > y0 &&
                bounds.getX() < x0 + clip.width &&
                bounds.getY() < y0 + clip.height);
    }

    public void pushClip(Rect absBounds, Size size, double x, double y, double width, double height) {
        if (width < 0 || height < 0) {
            width = 0;
            height = 0;
        }

        Rectangle previousClip;
        if (clips.size() == 0)
            previousClip = new Rectangle(0,0,Integer.MAX_VALUE, Integer.MAX_VALUE);
        else
            previousClip = clips.peek();

        double xScale = absBounds.getWidth() / size.getWidth();
        double yScale = absBounds.getHeight() / size.getHeight();

        int resWidth = (int) Math.ceil(width * xScale);
        int resHeight = (int) Math.ceil(height * yScale);
        int resX = (int) (absBounds.getX()+ x * xScale);
        int resY = (int) (absBounds.getY() + y * yScale);


        Rectangle newClip = new Rectangle(resX, imageHeight - (resY+resHeight), resWidth, resHeight);
        newClip = previousClip.intersection(newClip);

        clips.push(newClip);
    }

    public void popClip() {
        clips.pop();
    }
}
