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

package kr.syeyoung.dungeonsguide.mod.overlay;

import com.mojang.blaze3d.platform.cursor.CursorType;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenRenderEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenMouseEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenKeyboardEvent;
import kr.syeyoung.dungeonsguide.mod.events.impl.ScreenInitEvent;
import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.gui.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.gui.RootDom;
import kr.syeyoung.dungeonsguide.mod.gui.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
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
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import kr.syeyoung.dungeonsguide.mod.events.impl.RenderHUDEvent;
import kr.syeyoung.dungeonsguide.mod.events.ListenerPriority;
import kr.syeyoung.dungeonsguide.mod.events.annotations.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

public class OverlayManager {
    private final RootDom view;
    private final Minecraft mc;

    private static final OverlayManager INSTANCE = new OverlayManager();
    @Getter
    private final OverlayManagerRootWidget root = new OverlayManagerRootWidget();

    // under the hud and over screens are drawn in the same frame, each needs its own image (D76)
    private final StencilImage hudImage = new StencilImage("Dungeons Guide hud");
    private final StencilImage screenImage = new StencilImage("Dungeons Guide overlay");
    // glfw doesn't tell screens whether a key press is a repeat
    private final Set<Integer> pressedKeys = new HashSet<>();


    public static OverlayManager getEventHandler() {
        return INSTANCE;
    }

    public static OverlayManagerRootWidget getInstance() {
        return getEventHandler().root;
    }

    public static final String OVERLAY_TYPE_KEY = "OVERLAY_TYPE";

    private OverlayManager() {
        this.mc = Minecraft.getInstance();

        PopupMgr popupMgr = new PopupMgr();
        popupMgr.child.setValue(root);



        view = new RootDom(new GlobalHUDScale(popupMgr));
        guiResize(null);
        view.setMounted(true);
    }

    @SubscribeEvent()
    public void guiResize(ScreenInitEvent.Post post){
        ProfilerFiller profiler = Profiler.get();
        profiler.push("Dungeons Guide Overlay Lauout");
        try {
            int displayWidth = Minecraft.getInstance().getWindow().getWidth();
            int displayHeight = Minecraft.getInstance().getWindow().getHeight();
            view.setRelativeBound(new Rect(0,0, displayWidth, displayHeight));
            view.setAbsBounds(new Rect(0,0, displayWidth, displayHeight));
            view.setSize(new Size(displayWidth, displayHeight));
            view.getLayouter().layout(view, new ConstraintBox(
                    displayWidth,
                    displayWidth,
                    displayHeight,
                    displayHeight
            ));
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        profiler.pop();
    }

    @SubscribeEvent
    public void renderOverlay(RenderHUDEvent.Post postRender) {

        ProfilerFiller profiler = Profiler.get();
        profiler.push("Dungeons Guide - RenderGameOverlayEvent.Post :: Overlay");
        try {
            view.getContext().CONTEXT.put(OVERLAY_TYPE_KEY, OverlayType.UNDER_CHAT);
            drawScreen(postRender.partialTicks, hudImage, postRender.graphics);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        profiler.pop();
    }

    @SubscribeEvent
    public void renderGui(ScreenRenderEvent.Post postRender) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push("Dungeons Guide - DrawScreenEvent.Post :: Overlay");
        try {
            if (postRender.gui instanceof ChatScreen)
                view.getContext().CONTEXT.put(OVERLAY_TYPE_KEY, OverlayType.OVER_CHAT);
            else
                view.getContext().CONTEXT.put(OVERLAY_TYPE_KEY, OverlayType.OVER_ANY);
            drawScreen(postRender.renderPartialTicks, screenImage, postRender.graphics);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        profiler.pop();
    }


    private void drawScreen(float partialTicks, StencilImage image, GuiGraphicsExtractor graphics) {
        int displayWidth = Minecraft.getInstance().getWindow().getWidth();
        int displayHeight = Minecraft.getInstance().getWindow().getHeight();

        if (view.isRelayoutRequested()) {
            view.setRelayoutRequested(false);
            ProfilerFiller profiler = Profiler.get();
            profiler.push("Dungeons Guide Overlay Lauout");
            view.getLayouter().layout(view, new ConstraintBox(
                    displayWidth,
                    displayWidth,
                    displayHeight,
                    displayHeight
            ));
            profiler.pop();
        }
        // DG draws in framebuffer pixels into its own image (D74/D76), which goes back as one gui element
        RenderingContext context = new RenderingContext(displayHeight);
        view.getRenderer().doRender(partialTicks, context, view);
        image.ensureSize(displayWidth, displayHeight);
        DGGuiRenderer.INSTANCE.submit(image, context.getStates());
        image.extractComposite(graphics);

        // 1.8.9 only set the native cursor when the overlay changed it, so vanilla's own cursor stays otherwise
        if (view.getCurrentCursor() != EnumCursor.DEFAULT) {
            // 1.8.9's setNativeCursor(null) was the default cursor; 26.1 can't request null, vanilla's default stays
            CursorType cursor = GLCursors.getCursor(view.getCurrentCursor());
            if (cursor != null) graphics.requestCursor(cursor);
        }
    }

    private void keyTyped(KeyEvent keyEvent) {
        try {
            view.keyPressed0(keyEvent);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }

    private void charTyped(CharacterEvent characterEvent) {
        try {
            view.charTyped0(characterEvent);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }

    private void keyHeld(KeyEvent keyEvent) {
        try {
            view.keyHeld0(keyEvent);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }

    private void keyReleased(KeyEvent keyEvent) {
        try {
            view.keyReleased0(keyEvent);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }

    private boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            return view.mouseClicked0(mouseX, mouseY
                    , mouseX, mouseY, mouseButton);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
        return false;
    }

    private void mouseReleased(int mouseX, int mouseY, int state) {
        try {
            view.mouseReleased0(mouseX, mouseY
                    , mouseX, mouseY, state);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
           
                e.printStackTrace();
        }
    }

    private void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        try {
            view.mouseClickMove0(mouseX, mouseY
                    , mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
           
                e.printStackTrace();
        }
    }

    private void mouseMove(int mouseX, int mouseY) {
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


    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void handleMouseClick(ScreenMouseEvent.Click mouseInputEvent) {
        int i = GuiScreenAdapter.getMouseX();
        int j = GuiScreenAdapter.getMouseY();

        if (this.mc.options.touchscreen().get() && this.touchValue++ > 0) {
            return;
        }

        this.eventButton = mouseInputEvent.buttonEvent.button();
        this.lastMouseEvent = Util.getMillis();
        if (this.mouseClicked(i, j, this.eventButton))
            mouseInputEvent.setCanceled(true);

        lastX = i;
        lastY = j;
    }

    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void handleMouseRelease(ScreenMouseEvent.Release mouseInputEvent) {
        int i = GuiScreenAdapter.getMouseX();
        int j = GuiScreenAdapter.getMouseY();

        if (this.mc.options.touchscreen().get() && --this.touchValue > 0) {
            return;
        }

        this.eventButton = -1;
        this.mouseReleased(i, j, mouseInputEvent.buttonEvent.button());

        lastX = i;
        lastY = j;
    }

    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void handleMouseDrag(ScreenMouseEvent.Drag mouseInputEvent) {
        int i = GuiScreenAdapter.getMouseX();
        int j = GuiScreenAdapter.getMouseY();

        if (this.eventButton != -1 && this.lastMouseEvent > 0L) {
            long l = Util.getMillis() - this.lastMouseEvent;
            this.mouseClickMove(i, j, this.eventButton, l);
        }

        lastX = i;
        lastY = j;
    }

    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void handleMouseMove(ScreenMouseEvent.Move mouseInputEvent) {
        int i = GuiScreenAdapter.getMouseX();
        int j = GuiScreenAdapter.getMouseY();

        if (lastX != i || lastY != j) {
            view.setCursor(EnumCursor.DEFAULT);
            this.mouseMove(i, j);
        }

        lastX = i;
        lastY = j;
    }

    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void handleMouseScroll(ScreenMouseEvent.Scroll mouseInputEvent) {
        int i = GuiScreenAdapter.getMouseX();
        int j = GuiScreenAdapter.getMouseY();

        // D97: lwjgl2's dwheel was 120 per notch
        int wheel = (int) (mouseInputEvent.scrollY * 120);
        if (wheel != 0) {
            try {
                boolean cancel = view.mouseScrolled0(i, j, i, j, wheel);
                if (cancel) mouseInputEvent.setCanceled(true);
            } catch (Exception e) {
                FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
            }
        }

        lastX = i;
        lastY = j;
    }

    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void handleKeyPress(ScreenKeyboardEvent.Press.Pre keyboardInputEvent) {
        if (pressedKeys.add(keyboardInputEvent.keyEvent.key()))
            this.keyTyped(keyboardInputEvent.keyEvent);
        else
            this.keyHeld(keyboardInputEvent.keyEvent);
    }

    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void handleKeyRelease(ScreenKeyboardEvent.Release.Pre keyboardInputEvent) {
        pressedKeys.remove(keyboardInputEvent.keyEvent.key());
        this.keyReleased(keyboardInputEvent.keyEvent);
    }

    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void handleCharType(ScreenKeyboardEvent.Char.Pre keyboardInputEvent) {
        this.charTyped(keyboardInputEvent.characterEvent);
    }
}
