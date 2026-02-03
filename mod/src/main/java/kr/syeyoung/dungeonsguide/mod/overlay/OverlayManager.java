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

import kr.syeyoung.dungeonsguide.mod.features.impl.etc.FeatureCollectDiagnostics;
import kr.syeyoung.dungeonsguide.mod.gui.RootDom;
import kr.syeyoung.dungeonsguide.mod.gui.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.event.ListenerPriority;
import kr.syeyoung.modapi.event.SubscribeEvent;
import kr.syeyoung.modapi.event.events.*;
import kr.syeyoung.modapi.gui.UGuiScreenChat;
import kr.syeyoung.modapi.profiler.UProfiler;
import kr.syeyoung.modapi.rendering.URenderContext;
import lombok.Getter;

import java.io.IOException;

public class OverlayManager {
    private final RootDom view;

    private static final OverlayManager INSTANCE = new OverlayManager();
    @Getter
    private final OverlayManagerRootWidget root = new OverlayManagerRootWidget();


    public static OverlayManager getEventHandler() {
        return INSTANCE;
    }

    public static OverlayManagerRootWidget getInstance() {
        return getEventHandler().root;
    }

    public static final String OVERLAY_TYPE_KEY = "OVERLAY_TYPE";

    private OverlayManager() {
        PopupMgr popupMgr = new PopupMgr();
        popupMgr.child.setValue(root);



        view = new RootDom(new GlobalHUDScale(popupMgr));
        guiResize(null);
        view.setMounted(true);
    }

    @SubscribeEvent()
    public void guiResize(ScreenInitEvent post){
        UProfiler profiler = ModAPI.getAPI().getProfiler();
        profiler.startSection("Dungeons Guide Overlay Lauout");
        try {
            view.setRelativeBound(new Rect(0,0, ModAPI.getAPI().getDisplayWidth(), ModAPI.getAPI().getDisplayHeight()));
            view.setAbsBounds(new Rect(0,0, ModAPI.getAPI().getDisplayWidth(), ModAPI.getAPI().getDisplayHeight()));
            view.setSize(new Size(ModAPI.getAPI().getDisplayWidth(), ModAPI.getAPI().getDisplayHeight()));
            view.getLayouter().layout(view, new ConstraintBox(
                    ModAPI.getAPI().getDisplayWidth(),
                    ModAPI.getAPI().getDisplayWidth(),
                    ModAPI.getAPI().getDisplayHeight(),
                    ModAPI.getAPI().getDisplayHeight()
            ));
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        profiler.endSection();
    }

    @SubscribeEvent
    public void renderOverlay(OverlayRenderEvent event) {

        UProfiler profiler = ModAPI.getAPI().getProfiler();
        profiler.startSection("Dungeons Guide - OverlayRenderEvent :: Overlay");
        try {
            view.getContext().CONTEXT.put(OVERLAY_TYPE_KEY, OverlayType.UNDER_CHAT);
            drawScreen(event.getPartialTicks(), event.getRenderContext());
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        profiler.endSection();
    }

    @SubscribeEvent
    public void renderGui(ScreenRenderEvent.Post postRender) {
        UProfiler profiler = ModAPI.getAPI().getProfiler();
        profiler.startSection("Dungeons Guide - ScreenRenderEvent.Post :: Overlay");
        try {
            if (postRender.getGui() instanceof UGuiScreenChat)
                view.getContext().CONTEXT.put(OVERLAY_TYPE_KEY, OverlayType.OVER_CHAT);
            else
                view.getContext().CONTEXT.put(OVERLAY_TYPE_KEY, OverlayType.OVER_ANY);
            drawScreen(postRender.getPartialTicks(), postRender.getRenderContext());
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        profiler.endSection();
    }


    private void drawScreen( float partialTicks, URenderContext renderCtx) {
        if (view.isRelayoutRequested()) {
            view.setRelayoutRequested(false);
            UProfiler profiler = ModAPI.getAPI().getProfiler();
            profiler.startSection("Dungeons Guide Overlay Lauout");
            view.getLayouter().layout(view, new ConstraintBox(
                    ModAPI.getAPI().getDisplayWidth(),
                    ModAPI.getAPI().getDisplayWidth(),
                    ModAPI.getAPI().getDisplayHeight(),
                    ModAPI.getAPI().getDisplayHeight()
            ));
            profiler.endSection();
        }
        double factor = ModAPI.getAPI().getScaleFactor();
        renderCtx.pushMatrix();
        renderCtx.translate(0,0,50);
        renderCtx.scale(1.0 / factor, 1.0 / factor, 1.0d);
        view.getRenderer().doRender(partialTicks, new RenderingContext(renderCtx), view);
        renderCtx.popMatrix();
    }


    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public  boolean mouseClicked(ScreenMouseEvent.MouseClicked event) throws IOException {
        try {
            boolean clicked = view.mouseClicked0((int) event.getMouseX(), (int) event.getMouseY(), event.getMouseX(), event.getMouseY(), event.getEventButton());
            if (clicked) event.setCanceled(true);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
        return false;
    }

    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public  void mouseReleased(ScreenMouseEvent.MouseReleased event) {
        try {
            view.mouseReleased0((int) event.getMouseX(), (int) event.getMouseY(), event.getMouseX(), event.getMouseY(), event.getEventButton());
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
           
                e.printStackTrace();
        }
    }

    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void mouseClickMove(ScreenMouseEvent.MouseDragged event) {
        try {
            view.mouseClickMove0((int) event.getMouseX(), (int) event.getMouseY(),
                    event.getMouseX(), event.getMouseY(), event.getEventButton(), 0);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
           
                e.printStackTrace();
        }
    }

    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void mouseMove(ScreenMouseEvent.MouseMoved event) {
        try {
            view.mouseMoved0((int)event.getMouseX(), (int)event.getMouseY()
                    , event.getMouseX(), event.getMouseY(), true);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }


    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void keyPressed(ScreenKeyboardEvent.KeyPressed keyPressed) throws IOException {
        try {
            view.keyPressed0(keyPressed.getKey(), keyPressed.getScancode(), keyPressed.getModifiers());
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }
    @SubscribeEvent(priority = ListenerPriority.FIRST)
    public void keyReleased(ScreenKeyboardEvent.KeyReleased keyReleased) throws IOException {
        try {
            view.keyReleased0(keyReleased.getKey(), keyReleased.getScancode(), keyReleased.getModifiers());
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }
}
