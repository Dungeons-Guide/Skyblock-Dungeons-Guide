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

package kr.syeyoung.dungeonsguide.mod.guiv2;

import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.GLCursors;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Stack;

import static org.lwjgl.opengl.GL11.GL_GREATER;
import static org.lwjgl.opengl.GL11.glCallList;

public class GuiScreenAdapter extends GuiScreen {

    @Getter
    private RootDom view;
    private boolean isOpen = false;

    private Stack<RootDom> domStack = new Stack<>();

    private GuiScreen parent;
    private boolean allowEsc;
    public GuiScreenAdapter(Widget widget) {
        this(widget, null, true);
    }
    public GuiScreenAdapter(Widget widget, GuiScreen parent) {
        this(widget, parent, true);
    }
    public GuiScreenAdapter(Widget widget, GuiScreen parent, boolean allowEsc) {
        this.parent = parent;
        this.allowEsc = allowEsc;
        view = new RootDom(widget);
        view.getContext().CONTEXT.put("screenAdapter", this);

        try {
            Mouse.setNativeCursor(GLCursors.getCursor(EnumCursor.DEFAULT));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void open(Widget newRoot) {
        domStack.push(view);
        view = new RootDom(newRoot);
        view.getContext().CONTEXT.put("screenAdapter", this);
        initGui();
    }
    public void goBack() {
        view = domStack.pop();
        view.getContext().CONTEXT.put("screenAdapter", this);
        initGui();
    }

    public static GuiScreenAdapter getAdapter(DomElement domElement) {
        return domElement.getContext().getValue(GuiScreenAdapter.class, "screenAdapter");
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        isOpen = true;
        try {
            view.setRelativeBound(new Rect(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight));
            view.setAbsBounds(new Rect(0, 0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight));
            view.setSize(new Size(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight));
            view.getLayouter().layout(view, new ConstraintBox(
                    Minecraft.getMinecraft().displayWidth,
                    Minecraft.getMinecraft().displayWidth,
                    Minecraft.getMinecraft().displayHeight,
                    Minecraft.getMinecraft().displayHeight
            ));
            view.setMounted(true);
        }catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        int i = Mouse.getEventX();
        int j = this.mc.displayHeight - Mouse.getEventY();

        try {
            if (view.isRelayoutRequested()) {

                view.setRelayoutRequested(false);
                view.getLayouter().layout(view, new ConstraintBox(
                        Minecraft.getMinecraft().displayWidth,
                        Minecraft.getMinecraft().displayWidth,
                        Minecraft.getMinecraft().displayHeight,
                        Minecraft.getMinecraft().displayHeight
                ));
            }


            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL_GREATER, 0);
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.scale(1.0 / scaledResolution.getScaleFactor(), 1.0 / scaledResolution.getScaleFactor(), 1.0d);
            view.getRenderer().doRender(i, j, i, j, partialTicks, new RenderingContext(), view);
            GlStateManager.alphaFunc(GL_GREATER, 0.1f);
            GlStateManager.popMatrix();
            GlStateManager.enableDepth();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1 && allowEsc) {
            this.mc.displayGuiScreen((GuiScreen)parent);
            if (this.mc.currentScreen == null) {
                this.mc.setIngameFocus();
            }
            return;
        }

        try {
            view.keyPressed0(typedChar, keyCode);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);

                e.printStackTrace();
        }
    }

    public void keyHeld(int keyCode, char typedChar) throws IOException {
        try {
            view.keyHeld0(typedChar, keyCode);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }

    public void keyReleased(int keyCode, char typedChar) throws IOException {
        try {
            view.keyReleased0(typedChar, keyCode);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            view.mouseClicked0(mouseX, mouseY
                    , mouseX, mouseY, mouseButton);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        isOpen = false;

        try {
            Mouse.setNativeCursor(null);
            view.setCursor(EnumCursor.DEFAULT);
        } catch (LWJGLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        try {
            view.mouseReleased0(mouseX, mouseY
                    , mouseX, mouseY, state);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        try {
            view.mouseClickMove0(mouseX, mouseY
                    , mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }

    public void mouseMove(int mouseX, int mouseY) {
        try {
            view.mouseMoved0(mouseX, mouseY
                    , mouseX, mouseY, true);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }


    private int touchValue;
    private int eventButton;
    private long lastMouseEvent;


    private int lastX, lastY;


    @Override
    public void handleMouseInput() throws IOException {
        if (!isOpen) return;
            int i = Mouse.getEventX();
            int j = this.mc.displayHeight - Mouse.getEventY();
            int k = Mouse.getEventButton();

            if (Mouse.getEventButtonState()) {
                if (this.mc.gameSettings.touchscreen && this.touchValue++ > 0) {
                    return;
                }

                this.eventButton = k;
                this.lastMouseEvent = Minecraft.getSystemTime();
                this.mouseClicked(i, j, this.eventButton);
            } else if (k != -1) {
                if (this.mc.gameSettings.touchscreen && --this.touchValue > 0) {
                    return;
                }

                this.eventButton = -1;
                this.mouseReleased(i, j, k);
            } else if (this.eventButton != -1 && this.lastMouseEvent > 0L) {
                long l = Minecraft.getSystemTime() - this.lastMouseEvent;
                this.mouseClickMove(i, j, this.eventButton, l);
            }
            if (lastX != i || lastY != j) {
                    EnumCursor prevCursor = view.getCurrentCursor();
                    view.setCursor(EnumCursor.DEFAULT);
                    this.mouseMove(i, j);
                    EnumCursor newCursor = view.getCurrentCursor();
                try {
                    if (prevCursor != newCursor) Mouse.setNativeCursor(GLCursors.getCursor(newCursor));
                } catch (Throwable e) {
                    if (e.getMessage() == null || !e.getMessage().contains("hack to stop"))
                        e.printStackTrace();
                }
            }


            int wheel = Mouse.getEventDWheel();
            if (wheel != 0) {
                try {
                    view.mouseScrolled0(i, j, i, j, wheel);
                } catch (Exception e) {
                    FeatureCollectDiagnostics.queueSendLogAsync(e);
                    e.printStackTrace();
                }
            }
            lastX = i;
            lastY = j;
    }

    public void handleKeyboardInput() throws IOException {
        if (!isOpen) return;

        if (Keyboard.getEventKeyState()) {
            if (Keyboard.isRepeatEvent())
                this.keyHeld(Keyboard.getEventKey(), Keyboard.getEventCharacter());
            else
                this.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
        } else {
            this.keyReleased(Keyboard.getEventKey(), Keyboard.getEventCharacter());
        }

        this.mc.dispatchKeypresses();
    }

    @Override
    public void handleInput() throws IOException {
        Keyboard.enableRepeatEvents(true); // I hope it's a temporary solution NEU Incompat. ?
        super.handleInput();
    }
}
