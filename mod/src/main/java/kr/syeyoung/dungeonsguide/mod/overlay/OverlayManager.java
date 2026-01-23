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
import kr.syeyoung.modapi.event.events.ScreenKeyboardEvent;
import kr.syeyoung.modapi.event.events.ScreenMouseEvent;
import kr.syeyoung.modapi.profiler.UProfiler;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.GL_GREATER;

public class OverlayManager {
    private final RootDom view;
    private final Minecraft mc;

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
        this.mc = Minecraft.getMinecraft();

        PopupMgr popupMgr = new PopupMgr();
        popupMgr.child.setValue(root);



        view = new RootDom(new GlobalHUDScale(popupMgr));
        guiResize(null);
        view.setMounted(true);
    }

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent()
    public void guiResize(GuiScreenEvent.InitGuiEvent.Post post){
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

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Post postRender) {
        if (!(postRender.type == RenderGameOverlayEvent.ElementType.ALL))
            return;

        UProfiler profiler = ModAPI.getAPI().getProfiler();
        profiler.startSection("Dungeons Guide - RenderGameOverlayEvent.Post :: Overlay");
        try {
            view.getContext().CONTEXT.put(OVERLAY_TYPE_KEY, OverlayType.UNDER_CHAT);
            drawScreen(postRender.partialTicks);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        profiler.endSection();
    }

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public void renderGui(GuiScreenEvent.DrawScreenEvent.Post postRender) {
        UProfiler profiler = ModAPI.getAPI().getProfiler();
        profiler.startSection("Dungeons Guide - DrawScreenEvent.Post :: Overlay");
        try {
            if (postRender.gui instanceof GuiChat)
                view.getContext().CONTEXT.put(OVERLAY_TYPE_KEY, OverlayType.OVER_CHAT);
            else
                view.getContext().CONTEXT.put(OVERLAY_TYPE_KEY, OverlayType.OVER_ANY);
            drawScreen(postRender.renderPartialTicks);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        profiler.endSection();
    }


    private void drawScreen( float partialTicks) {
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
        ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.pushMatrix();
        GlStateManager.translate(0,0,50);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, 0);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.scale(1.0 / scaledResolution.getScaleFactor(), 1.0 / scaledResolution.getScaleFactor(), 1.0d);
        view.getRenderer().doRender(partialTicks, new RenderingContext(null), view);
        GlStateManager.alphaFunc(GL_GREATER, 0.1f);
        GlStateManager.popMatrix();
        GlStateManager.enableDepth();
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
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
