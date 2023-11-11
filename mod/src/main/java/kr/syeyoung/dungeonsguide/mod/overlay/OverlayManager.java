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

import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.guiv2.RootDom;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Scaler;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.utils.GlStateUtils;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.GLCursors;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Map;

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

    @SubscribeEvent()
    public void guiResize(GuiScreenEvent.InitGuiEvent.Post post){
        try {
            view.setRelativeBound(new Rect(0,0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight));
            view.setAbsBounds(new Rect(0,0, Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight));
            view.setSize(new Size(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight));
            view.getLayouter().layout(view, new ConstraintBox(
                    Minecraft.getMinecraft().displayWidth,
                    Minecraft.getMinecraft().displayWidth,
                    Minecraft.getMinecraft().displayHeight,
                    Minecraft.getMinecraft().displayHeight
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent.Post postRender) {
        try {
            if (!(postRender.type == RenderGameOverlayEvent.ElementType.ALL))
                return;
            view.getContext().CONTEXT.put(OVERLAY_TYPE_KEY, OverlayType.UNDER_CHAT);
            drawScreen(postRender.partialTicks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void renderGui(GuiScreenEvent.DrawScreenEvent.Post postRender) {
        try {
            if (postRender.gui instanceof GuiChat)
                view.getContext().CONTEXT.put(OVERLAY_TYPE_KEY, OverlayType.OVER_CHAT);
            else
                view.getContext().CONTEXT.put(OVERLAY_TYPE_KEY, OverlayType.OVER_ANY);
            drawScreen(postRender.renderPartialTicks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void drawScreen( float partialTicks) {
        int i = Mouse.getEventX();
        int j = this.mc.displayHeight - Mouse.getEventY();

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
        GlStateManager.translate(0,0,50);
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
    }

    private void keyTyped(char typedChar, int keyCode) throws IOException {
        try {
            view.keyPressed0(typedChar, keyCode);
        } catch (Exception e) {
           
                e.printStackTrace();
        }
    }

    private void keyHeld(int keyCode, char typedChar) throws IOException {
        try {
            view.keyHeld0(typedChar, keyCode);
        } catch (Exception e) {
           
                e.printStackTrace();
        }
    }

    private void keyReleased(int keyCode, char typedChar) throws IOException {
        try {
            view.keyReleased0(typedChar, keyCode);
        } catch (Exception e) {
           
                e.printStackTrace();
        }
    }

    private boolean mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        try {
            return view.mouseClicked0(mouseX, mouseY
                    , mouseX, mouseY, mouseButton);
        } catch (Exception e) {
           
                e.printStackTrace();
        }
        return false;
    }

    private void mouseReleased(int mouseX, int mouseY, int state) {
        try {
            view.mouseReleased0(mouseX, mouseY
                    , mouseX, mouseY, state);
        } catch (Exception e) {
           
                e.printStackTrace();
        }
    }

    private void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        try {
            view.mouseClickMove0(mouseX, mouseY
                    , mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        } catch (Exception e) {
           
                e.printStackTrace();
        }
    }

    private void mouseMove(int mouseX, int mouseY) {
        try {
            view.mouseMoved0(mouseX, mouseY
                    , mouseX, mouseY, true);
        } catch (Exception e) {
           
                e.printStackTrace();
        }
    }


    private int touchValue;
    private int eventButton;
    private long lastMouseEvent;


    private int lastX, lastY;


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void handleMouseInput(GuiScreenEvent.MouseInputEvent.Pre mouseInputEvent) throws IOException {
        try {
            int i = Mouse.getEventX();
            int j = this.mc.displayHeight - Mouse.getEventY();
            int k = Mouse.getEventButton();

            if (Mouse.getEventButtonState()) {
                if (this.mc.gameSettings.touchscreen && this.touchValue++ > 0) {
                    return;
                }

                this.eventButton = k;
                this.lastMouseEvent = Minecraft.getSystemTime();
                if (this.mouseClicked(i, j, this.eventButton))
                    mouseInputEvent.setCanceled(true);
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
                try {
                    EnumCursor prevCursor = view.getCurrentCursor();
                    view.setCursor(EnumCursor.DEFAULT);
                    this.mouseMove(i, j);
                    EnumCursor newCursor = view.getCurrentCursor();
                    if (prevCursor != newCursor) Mouse.setNativeCursor(GLCursors.getCursor(newCursor));
                } catch (Throwable e) {
                   
                        e.printStackTrace();
                }
            }


            int wheel = Mouse.getEventDWheel();
            if (wheel != 0) {
                boolean cancel = view.mouseScrolled0(i, j, i, j, wheel);
                if (cancel) mouseInputEvent.setCanceled(true);
            }
            lastX = i;
            lastY = j;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void handleKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre keyboardInputEvent) throws IOException {
        if (Keyboard.getEventKeyState()) {
            if (Keyboard.isRepeatEvent())
                this.keyHeld(Keyboard.getEventKey(), Keyboard.getEventCharacter());
            else
                this.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
        } else {
            this.keyReleased(Keyboard.getEventKey(), Keyboard.getEventCharacter());
        }
    }
}
