/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.features.text;

import kr.syeyoung.dungeonsguide.config.guiconfig.FeatureEditPane;
import kr.syeyoung.dungeonsguide.config.guiconfig.GuiConfig;
import kr.syeyoung.dungeonsguide.config.guiconfig.MParameter;
import kr.syeyoung.dungeonsguide.config.types.AColor;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.features.text.StyledText;
import kr.syeyoung.dungeonsguide.features.text.TextHUDFeature;
import kr.syeyoung.dungeonsguide.features.text.TextStyle;
import kr.syeyoung.dungeonsguide.gui.MPanel;
import kr.syeyoung.dungeonsguide.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.gui.elements.MColor;
import kr.syeyoung.dungeonsguide.gui.elements.MEditableAColor;
import kr.syeyoung.dungeonsguide.gui.elements.MToggleButton;
import kr.syeyoung.dungeonsguide.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.w3c.dom.css.Rect;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PanelTextParameterConfig extends MPanel {

    private final StyledTextProvider feature;

    private final MEditableAColor currentColor;
    private final MEditableAColor backgroundColor;
    private final MToggleButton shadow;

    @Override
    public void onBoundsUpdate() {
    }

    @Override
    public void resize(int parentWidth, int parentHeight) {
        this.setBounds(new Rectangle(5,5,parentWidth-10, 120));
    }

    private final GuiConfig config;
    public PanelTextParameterConfig(final GuiConfig config, final StyledTextProvider feature) {
        this.config = config;
        this.feature = feature;
        setBackgroundColor(new Color(38, 38, 38, 255));

        currentColor = new MEditableAColor();
        currentColor.setColor(new AColor(0xff555555, true));
        currentColor.setEnableEdit(false);
        currentColor.setSize(new Dimension(15, 10));
        currentColor.setBounds(new Rectangle(275 , 14, 15, 10));
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
        backgroundColor.setBounds(new Rectangle(415 , 14, 15, 10));
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
        shadow.setBounds(new Rectangle(275 , 30, 20, 10));
        shadow.setOnToggle(new Runnable() {
            @Override
            public void run() {
                for (String se:selected)
                    feature.getStylesMap().get(se).setShadow(shadow.isEnabled());
            }
        });
        add(shadow);


    }

    private final Set<String> selected = new HashSet<String>();

    @Override
    public void render(int absMousex, int absMousey, int relMousex0, int relMousey0, float partialTicks, Rectangle scissor) {

        GlStateManager.pushMatrix();

        int width = 200, height = 100;
        Gui.drawRect(0,0,getBounds().width, getBounds().height, 0xFF444444);
        Gui.drawRect(4,4,width+6, height+6, 0xFF222222);
        Gui.drawRect(5,5,width+5, height+5, 0xFF555555);
        Rectangle clip = new Rectangle(scissor.x + 5, scissor.y + 5, width, height);
        clip(new ScaledResolution(Minecraft.getMinecraft()), clip.x, clip.y, clip.width, clip.height);

        GlStateManager.pushMatrix();
        GlStateManager.translate(offsetX + 5, offsetY + 5, 0);
        GlStateManager.scale(scale, scale, 0);


        List<StyledText> texts = feature.getDummyText();
        Map<String, TextStyle> styles = feature.getStylesMap();
        List<StyledTextRenderer.StyleTextAssociated> calc = StyledTextRenderer.drawTextWithStylesAssociated(texts, 0,0, getBounds().width, styles, StyledTextRenderer.Alignment.LEFT);
        boolean bool =clip.contains(absMousex, absMousey);
        for (StyledTextRenderer.StyleTextAssociated calc3: calc) {
            if (selected.contains(calc3.getStyledText().getGroup())) {
                Gui.drawRect(calc3.getRectangle().x, calc3.getRectangle().y, calc3.getRectangle().x + calc3.getRectangle().width, calc3.getRectangle().y + calc3.getRectangle().height, 0x4244A800);
            } else if (bool && calc3.getRectangle().contains((relMousex0-5 -offsetX) / scale , (relMousey0 - 5 - offsetY) / scale)) {
                for (StyledTextRenderer.StyleTextAssociated calc2 : calc) {
                    if (calc2.getStyledText().getGroup().equals(calc3.getStyledText().getGroup()))
                        Gui.drawRect(calc2.getRectangle().x, calc2.getRectangle().y, calc2.getRectangle().x + calc2.getRectangle().width, calc2.getRectangle().y + calc2.getRectangle().height, 0x55777777);
                }
            }
        }
        clip(new ScaledResolution(Minecraft.getMinecraft()), scissor.x, scissor.y, scissor.width, scissor.height);

        GlStateManager.popMatrix();

        GlStateManager.translate(5, height + 7, 0);
        GlStateManager.scale(0.5,0.5,0);
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        GlStateManager.enableBlend();
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fr.drawString("Press Shift to multi-select", 0, 0, 0xFFBFBFBF);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(width + 15, 5, 0);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5,0.5,0);
        fr.drawString("Selected Groups: "+selected, 0, 0, 0xFFBFBFBF);
        GlStateManager.popMatrix();
        fr.drawString("Text Color: ", 0, 10, 0xFFFFFFFF);
        fr.drawString("Background Color: ", 100, 10, 0xFFFFFFFF);
        fr.drawString("Shadow: ", 0, 26, 0xFFFFFFFF);

        GlStateManager.popMatrix();
    }

    private int offsetX = 0;
    private int offsetY = 0;
    private float scale = 1;

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
            if (calc3.getRectangle().contains((relMouseX-5 -offsetX) / scale , (relMouseY - 5 - offsetY) / scale)) {
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

        if (!found && !(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) && relMouseX >= 5 && relMouseX <= 205 && relMouseY >= 5 && relMouseY <= 105) {
            selected.clear();
            dragStart = true;
            lastX = absMouseX;
            lastY = absMouseY;
        }
        currentColor.setEnableEdit(selected.size() != 0);
        backgroundColor.setEnableEdit(selected.size() != 0);
        if (existed != selected.isEmpty()) {
            if (selected.size() != 0) {
                currentColor.setColor(styles.get(selected.iterator().next()).getColor());
                backgroundColor.setColor(styles.get(selected.iterator().next()).getBackground());
                shadow.setEnabled(styles.get(selected.iterator().next()).isShadow());
            } else {
                currentColor.setColor(new AColor(0xff555555, true));
                backgroundColor.setColor(new AColor(0xff555555, true));
                shadow.setEnabled(false);
            }
        }

        if (selected.size() == 1) {
            currentColor.setColor(styles.get(selected.iterator().next()).getColor());
            backgroundColor.setColor(styles.get(selected.iterator().next()).getBackground());
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
            offsetX += absMouseX - lastX;
            offsetY += absMouseY - lastY;
            lastX = absMouseX;
            lastY = absMouseY;

            if (offsetX < 0) offsetX = 0;
            if (offsetY < 0) offsetY =0;
        }
    }

    @Override
    public void mouseScrolled(int absMouseX, int absMouseY, int relMouseX0, int relMouseY0, int scrollAmount) {
        if ( relMouseX0 >= 5 && relMouseX0 <= 205 && relMouseY0 >= 5 && relMouseY0 <= 105) {
            if (scrollAmount > 0) {
                scale += 0.1;
            } else if (scrollAmount < 0) {
                scale -= 0.1;
            }
            if (scale < 0.1) scale = 0.1f;
            if (scale > 5) scale = 5.0f;
        }
    }
}
