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
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.GLCursors;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.gui.UCustomGuiScreen;
import kr.syeyoung.modapi.gui.UGuiScreen;
import kr.syeyoung.modapi.rendering.URenderContext;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.Stack;

import static org.lwjgl.opengl.GL11.GL_GREATER;

public class CustomGuiScreenAdapter implements UCustomGuiScreen {

    @Getter
    protected RootDom view;
    protected boolean isOpen = false;

    protected Stack<RootDom> domStack = new Stack<>();
    @Getter

    protected UGuiScreen parent;
    protected boolean allowEsc;
    public CustomGuiScreenAdapter(Widget widget) {
        this(widget, null, true);
    }
    public CustomGuiScreenAdapter(Widget widget, UGuiScreen parent) {
        this(widget, parent, true);
    }
    public CustomGuiScreenAdapter(Widget widget, UGuiScreen parent, boolean allowEsc) {
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
        init();
    }
    public void goBack() {
        view = domStack.pop();
        view.getContext().CONTEXT.put("screenAdapter", this);
        init();
    }

    public static CustomGuiScreenAdapter getAdapter(DomElement domElement) {
        return domElement.getContext().getValue(CustomGuiScreenAdapter.class, "screenAdapter");
    }

    @Override
    public void init() {
        isOpen = true;
        try {
            view.setRelativeBound(new Rect(0, 0, ModAPI.getAPI().getDisplayWidth(), ModAPI.getAPI().getDisplayHeight()));
            view.setAbsBounds(new Rect(0, 0, ModAPI.getAPI().getDisplayWidth(), ModAPI.getAPI().getDisplayHeight()));
            view.setSize(new Size(ModAPI.getAPI().getDisplayWidth(), ModAPI.getAPI().getDisplayHeight()));
            view.getLayouter().layout(view, new ConstraintBox(
                    ModAPI.getAPI().getDisplayWidth(),
                    ModAPI.getAPI().getDisplayWidth(),
                    ModAPI.getAPI().getDisplayHeight(),
                    ModAPI.getAPI().getDisplayHeight()
            ));
            view.setMounted(true);
        }catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
    }

    @Override
    public void render(URenderContext context, float deltaTick) {
        try {
            if (view.isRelayoutRequested()) {
                view.setRelayoutRequested(false);
                try {
                    view.getLayouter().layout(view, new ConstraintBox(
                            ModAPI.getAPI().getDisplayWidth(),
                            ModAPI.getAPI().getDisplayWidth(),
                            ModAPI.getAPI().getDisplayHeight(),
                            ModAPI.getAPI().getDisplayHeight()
                    ));
                } catch (Exception e) {
                    view.setRelayoutRequested(true);
                    throw e;
                }
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
            view.getRenderer().doRender(deltaTick, new RenderingContext(context), view);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        try {
            if (view.keyPressed0(keyCode, scanCode, modifiers)) return true;
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);

                e.printStackTrace();
        }

        if (keyCode == 1 && allowEsc) {
            closeScreenRequested();
            return true;
        }
        return true;
    }

    public void closeScreenRequested() {
        ModAPI.getAPI().displayGuiScreen(null);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        try {
            view.charTyped0(chr, modifiers);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        return true;
    }


    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        try {
            view.keyReleased0(keyCode, scanCode, modifiers);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
        return true;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        try {
            view.mouseClicked0((int) mouseX, (int) mouseY
                    , mouseX, mouseY, mouseButton);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onRemoved() {
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
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        try {
            view.mouseReleased0((int) mouseX, (int) mouseY
                    , mouseX, mouseY, state);
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
        return true;
    }


    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        try {
            view.mouseClickMove0((int) mouseX, (int) mouseY, mouseX, mouseY, 0, 0);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        return true;
    }

    public void mouseMoved(double mouseX,double mouseY) {
        try {
            view.mouseMoved0((int) mouseX, (int) mouseY
                    , mouseX, mouseY, true);
        } catch (Exception e) {

            FeatureCollectDiagnostics.queueSendLogAsync(e);
                e.printStackTrace();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        try {
            view.mouseScrolled0((int) mouseX, (int) mouseY, mouseX, mouseY, (int) ((horizontalAmount+verticalAmount)/2));
        } catch (Exception e) {
            FeatureCollectDiagnostics.queueSendLogAsync(e);
            e.printStackTrace();
        }
        return true;
    }
}
