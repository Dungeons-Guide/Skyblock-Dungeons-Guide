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

package kr.syeyoung.dungeonsguide.features.text;

import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MEditableAColor;
import kr.syeyoung.dungeonsguide.gui.elements.MPanelScaledGUI;
import kr.syeyoung.dungeonsguide.gui.elements.MScrollablePanel;
import kr.syeyoung.dungeonsguide.gui.elements.MToggleButton;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PanelTextParameterConfig extends MPanel {

    private final StyledTextProvider feature;

    private final MEditableAColor currentColor;
    private final MEditableAColor backgroundColor;
    private final MToggleButton shadow;

    private MScrollablePanel mScrollablePanel;
    private MPanelScaledGUI rendering;

    @Override
    public void onBoundsUpdate() {
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, getPanelBound().height + 20);
    }

    public PanelTextParameterConfig(final StyledTextProvider feature) {
        this.feature = feature;
        setBackgroundColor(new Color(38, 38, 38, 255));

        currentColor = new MEditableAColor();
        currentColor.setColor(new AColor(0xff555555, true));
        currentColor.setEnableEdit(false);
        currentColor.setSize(new Dimension(15, 10));
        currentColor.setOnUpdate(new Runnable() {
            @Override
            public void run() {
                for (String se:selected)
                    feature.getStylesMap().get(se).setColor(currentColor.getColor());
            }
        });
        add(currentColor);
        backgroundColor = new MEditableAColor();
        backgroundColor.setColor(new AColor(0xff555555, true));
        backgroundColor.setEnableEdit(false);
        backgroundColor.setSize(new Dimension(15, 10));
        backgroundColor.setOnUpdate(new Runnable() {
            @Override
            public void run() {
                for (String se:selected)
                    feature.getStylesMap().get(se).setBackground(backgroundColor.getColor());
            }
        });
        add(backgroundColor);
        shadow = new MToggleButton();
        shadow.setSize(new Dimension(20, 10));
        shadow.setOnToggle(new Runnable() {
            @Override
            public void run() {
                for (String se:selected)
                    feature.getStylesMap().get(se).setShadow(shadow.isEnabled());
            }
        });
        add(shadow);

        mScrollablePanel = new MScrollablePanel(3);
        mScrollablePanel.setHideScrollBarWhenNotNecessary(true);
        add(mScrollablePanel);

        mScrollablePanel.add(rendering = new MPanelScaledGUI() {
            @Override
            public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {
                super.render(absMousex, absMousey, relMousex0, relMousey0, partialTicks, scissor);

                List<StyledText> texts = feature.getDummyText();
                Map<String, TextStyle> styles = feature.getStylesMap();
                List<StyledTextRenderer.StyleTextAssociated> calc = StyledTextRenderer.drawTextWithStylesAssociated(texts, 0,0, getEffectiveDimension().width, styles, StyledTextRenderer.Alignment.LEFT);
                boolean bool =scissor.contains(absMousex, absMousey);
                for (StyledTextRenderer.StyleTextAssociated calc3: calc) {
                    if (selected.contains(calc3.getStyledText().getGroup())) {
                        Gui.drawRect(calc3.getRectangle().x, calc3.getRectangle().y, calc3.getRectangle().x + calc3.getRectangle().width, calc3.getRectangle().y + calc3.getRectangle().height, 0x4244A800);
                    } else if (bool && calc3.getRectangle().contains(relMousex0, relMousey0)) {
                        for (StyledTextRenderer.StyleTextAssociated calc2 : calc) {
                            if (calc2.getStyledText().getGroup().equals(calc3.getStyledText().getGroup()))
                                Gui.drawRect(calc2.getRectangle().x, calc2.getRectangle().y, calc2.getRectangle().x + calc2.getRectangle().width, calc2.getRectangle().y + calc2.getRectangle().height, 0x55777777);
                        }
                    }
                }
            }


            private int lastX;
            private int lastY;
            private boolean dragStart = false;
            @Override
            public void mouseClicked(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int mouseButton) {
                List<StyledText> texts = feature.getDummyText();
                Map<String, TextStyle> styles = feature.getStylesMap();
                boolean existed = selected.isEmpty();
                boolean found = false;
                List<StyledTextRenderer.StyleTextAssociated> calc = StyledTextRenderer.calculate(texts, 0,0, styles);
                for (StyledTextRenderer.StyleTextAssociated calc3: calc) {
                    if (calc3.getRectangle().contains(relMouseX, relMouseY)) {
                        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                            if (!selected.contains(calc3.getStyledText().getGroup()))
                                selected.add(calc3.getStyledText().getGroup());
                            else
                                selected.remove(calc3.getStyledText().getGroup());
                        } else {
                            selected.clear();
                            selected.add(calc3.getStyledText().getGroup());
                        }
                        found = true;
                    }
                }

                if (!found && !(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) && parent.getLastAbsClip().contains(absMouseX * getScale(), absMouseY* getScale())) {
                    selected.clear();
                    dragStart = true;
                    lastX = absMouseX;
                    lastY = absMouseY;
                }
                currentColor.setEnableEdit(selected.size() != 0);
                PanelTextParameterConfig.this.backgroundColor.setEnableEdit(selected.size() != 0);
                if (existed != selected.isEmpty()) {
                    if (selected.size() != 0) {
                        currentColor.setColor(styles.get(selected.iterator().next()).getColor());
                        PanelTextParameterConfig.this.backgroundColor.setColor(styles.get(selected.iterator().next()).getBackground());
                        shadow.setEnabled(styles.get(selected.iterator().next()).isShadow());
                    } else {
                        currentColor.setColor(new AColor(0xff555555, true));
                        PanelTextParameterConfig.this.backgroundColor.setColor(new AColor(0xff555555, true));
                        shadow.setEnabled(false);
                    }
                }

                if (selected.size() == 1) {
                    currentColor.setColor(styles.get(selected.iterator().next()).getColor());
                    PanelTextParameterConfig.this.backgroundColor.setColor(styles.get(selected.iterator().next()).getBackground());
                    shadow.setEnabled(styles.get(selected.iterator().next()).isShadow());
                }
            }

            @Override
            public void mouseReleased(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int state) {
                dragStart = false;
            }

            @Override
            public void mouseClickMove(int absMouseX, int absMouseY, int relMouseX, int relMouseY, int clickedMouseButton, long timeSinceLastClick) {
                if (dragStart) {
                    int dx= absMouseX - lastX;
                    int dy= absMouseY - lastY;
                    int actualOFFX = (int) (dx / getScale());
                    int actualOFFY = (int) (dy / getScale());
                    lastX =(int) (actualOFFX * getScale())+lastX;
                    lastY =(int) (actualOFFY * getScale())+lastY;
                    mScrollablePanel.getScrollBarX().addToCurrent(-actualOFFX);
                    mScrollablePanel.getScrollBarY().addToCurrent(-actualOFFY);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                List<StyledTextRenderer.StyleTextAssociated> calc = StyledTextRenderer.calculate(feature.getDummyText(), 0,0, feature.getStylesMap());
                int w = 0, h = 0;
                for (StyledTextRenderer.StyleTextAssociated styleTextAssociated : calc) {
                    int endX = styleTextAssociated.getRectangle().x + styleTextAssociated.getRectangle().width;
                    int endY = styleTextAssociated.getRectangle().y + styleTextAssociated.getRectangle().height;
                    if (endX > w) w = endX;
                    if (endY > h) h = endY;
                }
                return new Dimension((int) (w * getScale()),(int) (h * getScale()));
            }

            @Override
            public void resize(int parentWidth, int parentHeight) {
                setBounds(new Rectangle(new Point(0,0), getPreferredSize()));
            }


            @Override
            public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
                if ( parent.getLastAbsClip().contains(absMouseX * getScale(), absMouseY* getScale())) {
                    double scale = rendering.getScale();
                    if (scrollAmount > 0) {
                        scale += 0.1;
                    } else if (scrollAmount < 0) {
                        scale -= 0.1;
                    }
                    if (scale < 0.1) scale = 0.1f;
                    if (scale > 5) scale = 5.0f;
                    rendering.setScale(scale);
                    mScrollablePanel.setBounds(new Rectangle(new Point(5,5),getPanelBound()));
                }
            }
        });


    }

    private final Set<String> selected = new HashSet<String>();

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {

        GlStateManager.pushMatrix();

        Dimension dim = getPanelBound();
        int width = dim.width, height = dim.height;
        Gui.drawRect(0,0,getBounds().width, getBounds().height, RenderUtils.blendAlpha(0x141414, 0.12f));
        Gui.drawRect(4,4,width+6, height+6, 0xFF222222);
        Gui.drawRect(5,5,width+5, height+5, RenderUtils.blendAlpha(0x141414, 0.15f));


        GlStateManager.translate(5, height + 7, 0);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Press Shift to multi-select", 0, 0, 0xFFBFBFBF);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(width + 15, 5, 0);
        GlStateManager.pushMatrix();
        fr.drawString("Selected Groups: "+selected, 0, 0, 0xFFBFBFBF);
        GlStateManager.popMatrix();
        fr.drawString("Text Color: ", 0, 10, 0xFFFFFFFF);
        fr.drawString("Background Color: ", 0, 20, 0xFFFFFFFF);
        fr.drawString("Shadow: ", 0, 39, 0xFFFFFFFF);

        GlStateManager.popMatrix();
    }

    private Dimension getPanelBound(){
//        return new Dimension((getBounds().width-25)/2, 100);
        return new Dimension(150,100);
    }

    @Override
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);

        Dimension dim = getPanelBound();
        currentColor.setBounds(new Rectangle(75+dim.width , 14, 15, 10));
        backgroundColor.setBounds(new Rectangle(110+dim.width , 24, 15, 10));
        shadow.setBounds(new Rectangle(75+dim.width , 43, 30, 10));
        mScrollablePanel.setBounds(new Rectangle(5,5,dim.width,dim.height));
    }
}
