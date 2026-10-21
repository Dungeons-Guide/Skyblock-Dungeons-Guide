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

package kr.syeyoung.dungeonsguide.mod.gui;

import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.render.gui.DGGuiRenderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.render.gui.StencilImage;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.GLCursors;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class GuiScreenAdapter extends Screen {

    @Getter
    protected RootDom view;
    protected boolean isOpen = false;

    protected Stack<RootDom> domStack = new Stack<>();
    @Getter

    protected Screen parent;
    protected boolean allowEsc;
    // 1.8.9 screens had no background, 26.1 draws the menu blur and darkening (D98)
    protected boolean vanillaBackground;

    private final StencilImage image = new StencilImage("Dungeons Guide screen");
    // glfw doesn't tell screens whether a key press is a repeat
    private final Set<Integer> pressedKeys = new HashSet<>();

    public GuiScreenAdapter(Widget widget) {
        this(widget, null, true);
    }
    public GuiScreenAdapter(Widget widget, Screen parent) {
        this(widget, parent, true);
    }
    public GuiScreenAdapter(Widget widget, Screen parent, boolean allowEsc) {
        this(widget, parent, allowEsc, false);
    }
    public GuiScreenAdapter(Widget widget, Screen parent, boolean allowEsc, boolean vanillaBackground) {
        super(Component.empty());
        this.parent = parent;
        this.allowEsc = allowEsc;
        this.vanillaBackground = vanillaBackground;
        view = new RootDom(widget);
        view.getContext().CONTEXT.put("screenAdapter", this);
    }

    // mouse in framebuffer pixels from the top left, what 1.8.9's Mouse.getEventX / displayHeight - getEventY gave
    public static int getMouseX() {
        return (int) (Minecraft.getInstance().mouseHandler.xpos() * Minecraft.getInstance().getWindow().getWidth() / Minecraft.getInstance().getWindow().getScreenWidth());
    }
    public static int getMouseY() {
        return (int) (Minecraft.getInstance().mouseHandler.ypos() * Minecraft.getInstance().getWindow().getHeight() / Minecraft.getInstance().getWindow().getScreenHeight());
    }

    public void open(Widget newRoot) {
        domStack.push(view);
        view = new RootDom(newRoot);
        view.getContext().CONTEXT.put("screenAdapter", this);
        init();
    }
    public void goBack() {
        view = domStack.pop();
        view.getContext().CONTEXT.put("screenAdapter", this);
        init();
    }

    public static GuiScreenAdapter getAdapter(DomElement domElement) {
        return domElement.getContext().getValue(GuiScreenAdapter.class, "screenAdapter");
    }

    @Override
    protected void init() {
        super.init();
        isOpen = true;
        try {
            int displayWidth = Minecraft.getInstance().getWindow().getWidth();
            int displayHeight = Minecraft.getInstance().getWindow().getHeight();
            view.setRelativeBound(new Rect(0, 0, displayWidth, displayHeight));
            view.setAbsBounds(new Rect(0, 0, displayWidth, displayHeight));
            view.setSize(new Size(displayWidth, displayHeight));
            view.getLayouter().layout(view, new ConstraintBox(
                    displayWidth,
                    displayWidth,
                    displayHeight,
                    displayHeight
            ));
            view.setMounted(true);
        }catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (vanillaBackground) super.extractBackground(graphics, mouseX, mouseY, a);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        try {
            int displayWidth = Minecraft.getInstance().getWindow().getWidth();
            int displayHeight = Minecraft.getInstance().getWindow().getHeight();
            if (view.isRelayoutRequested()) {
                view.setRelayoutRequested(false);
                try {
                    view.getLayouter().layout(view, new ConstraintBox(
                            displayWidth,
                            displayWidth,
                            displayHeight,
                            displayHeight
                    ));
                } catch (Exception e) {
                    view.setRelayoutRequested(true);
                    throw e;
                }
            }

            // DG draws in framebuffer pixels into its own image (D74/D76), which goes back as one gui element
            RenderingContext context = new RenderingContext(displayHeight);
            view.getRenderer().doRender(partialTicks, context, view);
            image.ensureSize(displayWidth, displayHeight);
            DGGuiRenderer.INSTANCE.submit(image, context.getStates());
            image.extractComposite(graphics);

            graphics.requestCursor(GLCursors.getCursor(view.getCurrentCursor()));
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        boolean repeat = !pressedKeys.add(keyEvent.key());
        if (repeat) {
            keyHeld(keyEvent);
            return true;
        }

        try {
            if (view.keyPressed0(keyEvent)) return true;
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);

                e.printStackTrace();
        }

        if (keyEvent.isEscape() && allowEsc) {
            this.minecraft.setScreen(parent);
            return true;
        }
        // unhandled keys go on to vanilla, like 1.8.9's dispatchKeypresses
        return false;
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        try {
            view.charTyped0(characterEvent);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
        return true;
    }

    public void keyHeld(KeyEvent keyEvent) {
        try {
            view.keyHeld0(keyEvent);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        pressedKeys.remove(keyEvent.key());
        try {
            view.keyReleased0(keyEvent);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
        return true;
    }

    private long lastMouseEvent;

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mouseX = getMouseX(), mouseY = getMouseY();
        if (this.minecraft.options.touchscreen().get() && this.touchValue++ > 0) {
            return true;
        }
        this.lastMouseEvent = Util.getMillis();
        try {
            super.mouseClicked(event, doubleClick);
            view.mouseClicked0(mouseX, mouseY
                    , mouseX, mouseY, event.button());
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
        return true;
    }

    @Override
    public void removed() {
        super.removed();
        isOpen = false;
        pressedKeys.clear();
        view.setCursor(EnumCursor.DEFAULT);
        image.close();
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int mouseX = getMouseX(), mouseY = getMouseY();
        if (this.minecraft.options.touchscreen().get() && --this.touchValue > 0) {
            return true;
        }
        try {
            view.mouseReleased0(mouseX, mouseY
                    , mouseX, mouseY, event.button());
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        int mouseX = getMouseX(), mouseY = getMouseY();
        if (this.lastMouseEvent <= 0L) return true;
        try {
            view.mouseClickMove0(mouseX, mouseY
                    , mouseX, mouseY, event.button(), Util.getMillis() - this.lastMouseEvent);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
        return true;
    }

    @Override
    public void mouseMoved(double x, double y) {
        int mouseX = getMouseX(), mouseY = getMouseY();
        if (lastX == mouseX && lastY == mouseY) return;
        lastX = mouseX;
        lastY = mouseY;

        view.setCursor(EnumCursor.DEFAULT);
        try {
            view.mouseMoved0(mouseX, mouseY
                    , mouseX, mouseY, true);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        int mouseX = getMouseX(), mouseY = getMouseY();
        // D97: lwjgl2's dwheel was 120 per notch
        int wheel = (int) (scrollY * 120);
        if (wheel != 0) {
            try {
                view.mouseScrolled0(mouseX, mouseY, mouseX, mouseY, wheel);
            } catch (Exception e) {
                FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
            }
        }
        return true;
    }


    private int touchValue;


    private int lastX, lastY;
}
