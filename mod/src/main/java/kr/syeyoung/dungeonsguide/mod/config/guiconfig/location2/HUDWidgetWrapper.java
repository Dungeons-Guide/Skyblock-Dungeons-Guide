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

package kr.syeyoung.dungeonsguide.mod.config.guiconfig.location2;

import kr.syeyoung.dungeonsguide.mod.config.types.GUIPosition;
import kr.syeyoung.dungeonsguide.mod.features.AbstractHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.AbsLocationPopup;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.OnlyChildrenRenderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.overlay.GUIRectPositioner;
import kr.syeyoung.dungeonsguide.mod.utils.cursor.EnumCursor;
import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class HUDWidgetWrapper extends Widget implements Layouter {
    @Getter
    public AbstractHUDFeature abstractHUDFeature;
    @Getter
    public GUIRectPositioner positioner;

    @Getter
    private Widget demoWidget;
    private WidthWidget widthWidget;
    private HeightWidget heightWidget;
    private CornerWidget cornerWidget;
    private List<Widget> built = new ArrayList<>();
    private HUDConfigRootWidget rootWidget;

    @Getter
    private double widthPlus;
    @Getter
    private double heightPlus;
    private boolean enable;
    public HUDWidgetWrapper(AbstractHUDFeature hudFeature, HUDConfigRootWidget hudConfigRootWidget, boolean enable) {
        this.abstractHUDFeature = hudFeature;
        this.enable = enable;
        positioner = new GUIRectPositioner(hudFeature::getFeatureRect);
        demoWidget = abstractHUDFeature.instantiateDemoWidget();
        built.add(demoWidget);
        if (enable) {
            if (abstractHUDFeature.requiresWidthBound()) {
                widthWidget = new WidthWidget();
                built.add(widthWidget);
            }
            if (abstractHUDFeature.requiresHeightBound()) {
                heightWidget = new HeightWidget();
                built.add(heightWidget);
            }
            if ((abstractHUDFeature.requiresWidthBound() && abstractHUDFeature.requiresHeightBound()) || abstractHUDFeature.getKeepRatio() != null) {
                // smth
                cornerWidget = new CornerWidget();
                built.add(cornerWidget);
            }
        }
        rootWidget = hudConfigRootWidget;
    }

    @Override
    public List<Widget> build(DomElement buildContext) {
        return built;
    }

    @Override
    public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
        Size size = demoWidget.getDomElement().getLayouter().layout(demoWidget.getDomElement(), new ConstraintBox(
                0, constraintBox.getMaxWidth(),
                0, constraintBox.getMaxHeight()
        ));
        demoWidget.getDomElement().setRelativeBound(new Rect(0,0,size.getWidth(), size.getHeight()));

        if (widthWidget != null) {
            Size size1 = widthWidget.getDomElement().getLayouter().layout(widthWidget.getDomElement(), new ConstraintBox(
                    0, constraintBox.getMaxWidth(), 0, size.getHeight()
            ));
            widthWidget.getDomElement().setRelativeBound(new Rect(size.getWidth(), 0, size1.getWidth(), size1.getHeight()));
            widthPlus =  size1.getWidth();
        }

        if (heightWidget != null) {
            Size size1 = heightWidget.getDomElement().getLayouter().layout(heightWidget.getDomElement(), new ConstraintBox(
                    0, size.getWidth(), 0, constraintBox.getMaxHeight()
            ));
            heightWidget.getDomElement().setRelativeBound(new Rect(0, size.getHeight(), size1.getWidth(), size1.getHeight()));
            heightPlus= size1.getHeight();
        }
        if (cornerWidget != null) {
            Size size1 = cornerWidget.getDomElement().getLayouter().layout(cornerWidget.getDomElement(), new ConstraintBox(
                    0, size.getWidth(), 0, constraintBox.getMaxHeight()
            ));
            cornerWidget.getDomElement().setRelativeBound(new Rect(size.getWidth(), size.getHeight(), size1.getWidth(), size1.getHeight()));
            widthPlus = size1.getWidth();
            heightPlus = size1.getHeight();
        }
        return new Size(size.getWidth() + widthPlus, size.getHeight() + heightPlus);
    }

    @Override
    protected Renderer createRenderer() {
        return new HoverThingyRenderer();
    }

    public class HoverThingyRenderer extends OnlyChildrenRenderer {
        @Override
        public void doRender(float partialTicks, RenderingContext renderingContext, DomElement buildContext) {
            if (enable)
                renderingContext.drawRect(0,0, buildContext.getSize().getWidth(), buildContext.getSize().getHeight(), 0x40000000);
            GlStateManager.pushMatrix();
            super.doRender(partialTicks, renderingContext, buildContext);
            GlStateManager.popMatrix();
            if (!enable) return;
            if (((HUDWidgetWrapper)buildContext.getWidget()).isHover)
                renderingContext.drawRect(0,0, buildContext.getSize().getWidth(), buildContext.getSize().getHeight(), 0x33FFFFFF);
        }
    }

    private boolean isHover = false;
    @Override
    public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
        if (childHandled) return false;
        if (!enable) return false;
        if (Mouse.isButtonDown(0))
            getDomElement().setCursor(EnumCursor.CLOSED_HAND);
        else
            getDomElement().setCursor(EnumCursor.OPEN_HAND);
        isHover = true;
        return true;
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        isHover = false;
    }

    @Override
    public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
        if (childHandled) return false;
        if (mouseButton == 0) return false;
        if (!enable) return false;

        List<Widget> widgets = new LinkedList<>();
        abstractHUDFeature.getTooltipForEditor(widgets);
        PopupMgr.getPopupMgr(getDomElement()).openPopup(new AbsLocationPopup(
                absMouseX, absMouseY, new WidgetPopupMenu(widgets), true
        ), null);

        return true;
    }

    public class WidthWidget extends Widget implements Layouter, Renderer {

        @Override
        public List<Widget> build(DomElement buildContext) {
            return Collections.emptyList();
        }

        @Override
        public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
            return new Size(5, constraintBox.getMaxHeight());
        }

        @Override
        public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
            double thingHeight = Math.min(15, buildContext.getSize().getHeight());
            context.drawRect(1,(buildContext.getSize().getHeight()-thingHeight)/2,2, (buildContext.getSize().getHeight()+thingHeight)/2, 0xFF888888);
            context.drawRect(3,(buildContext.getSize().getHeight()-thingHeight)/2,4, (buildContext.getSize().getHeight()+thingHeight)/2, 0xFF888888);
        }

        private double sx = -1;
        private double sw = 0;
        @Override
        public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
            if (childHandled) return false;
            this.sx = absMouseX;
            this.sw = abstractHUDFeature.getFeatureRect().getWidth();
            getDomElement().obtainFocus();
            return true;
        }

        @Override
        public void mouseClickMove(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int clickedMouseButton, long timeSinceLastClick) {
            if (sx == -1) return;
            double newWidth = (absMouseX - sx) * getDomElement().getRelativeBound().getWidth() / getDomElement().getAbsBounds().getWidth() + sw;
            abstractHUDFeature.setWidth(newWidth);
            Rect newRect = new Rect(
                    HUDWidgetWrapper.this.getDomElement().getRelativeBound().getX(),
                    HUDWidgetWrapper.this.getDomElement().getRelativeBound().getY(),
                    abstractHUDFeature.getFeatureRect().getWidth(),
                    abstractHUDFeature.getFeatureRect().getHeight()
            );
            GUIPosition position = GUIPosition.of(newRect, rootWidget.getLastWidth(), rootWidget.getLastHeight());
            position.setWidth(newRect.getWidth());
            position.setHeight(newRect.getHeight());
            abstractHUDFeature.setFeatureRect(position);
            getDomElement().setCursor(EnumCursor.RESIZE_LEFT_RIGHT);
            getDomElement().requestRelayout();
        }

        @Override
        public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
            if (sx == -1) return;
            double newWidth = (absMouseX - sx) * getDomElement().getRelativeBound().getWidth() / getDomElement().getAbsBounds().getWidth() + sw;
            abstractHUDFeature.setWidth(newWidth);
            Rect newRect = new Rect(
                    HUDWidgetWrapper.this.getDomElement().getRelativeBound().getX(),
                    HUDWidgetWrapper.this.getDomElement().getRelativeBound().getY(),
                    abstractHUDFeature.getFeatureRect().getWidth(),
                    abstractHUDFeature.getFeatureRect().getHeight()
            );
            GUIPosition position = GUIPosition.of(newRect, rootWidget.getLastWidth(), rootWidget.getLastHeight());
            position.setWidth(newRect.getWidth());
            position.setHeight(newRect.getHeight());
            abstractHUDFeature.setFeatureRect(position);
            getDomElement().requestRelayout();
            this.sx = -1; this.sw = 0;
        }

        @Override
        public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
            if (childHandled) return false;
            getDomElement().setCursor(EnumCursor.RESIZE_LEFT_RIGHT);
            return true;
        }
    }


    public class HeightWidget extends Widget implements Layouter, Renderer {

        @Override
        public List<Widget> build(DomElement buildContext) {
            return Collections.emptyList();
        }

        @Override
        public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
            return new Size(constraintBox.getMaxWidth(), 5);
        }

        @Override
        public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
            double thingWidth = Math.min(15, buildContext.getSize().getWidth());
            context.drawRect((buildContext.getSize().getWidth()-thingWidth)/2,1, (buildContext.getSize().getWidth()+thingWidth)/2,2, 0xFF888888);
            context.drawRect((buildContext.getSize().getWidth()-thingWidth)/2,3,  (buildContext.getSize().getWidth()+thingWidth)/2, 4,0xFF888888);
        }

        private double sy = -1;
        private double sh = 0;
        @Override
        public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
            if (childHandled) return false;
            this.sy = absMouseY;
            this.sh = abstractHUDFeature.getFeatureRect().getHeight();
            getDomElement().obtainFocus();
            return true;
        }

        @Override
        public void mouseClickMove(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int clickedMouseButton, long timeSinceLastClick) {
            if (sy == -1) return;
            double newHeight = (absMouseY - sy ) * getDomElement().getRelativeBound().getHeight() / getDomElement().getAbsBounds().getHeight()+ sh;
            abstractHUDFeature.setHeight(newHeight);
            Rect newRect = new Rect(
                    HUDWidgetWrapper.this.getDomElement().getRelativeBound().getX(),
                    HUDWidgetWrapper.this.getDomElement().getRelativeBound().getY(),
                    abstractHUDFeature.getFeatureRect().getWidth(),
                    abstractHUDFeature.getFeatureRect().getHeight()
            );
            GUIPosition position = GUIPosition.of(newRect, rootWidget.getLastWidth(), rootWidget.getLastHeight());
            position.setWidth(newRect.getWidth());
            position.setHeight(newRect.getHeight());
            abstractHUDFeature.setFeatureRect(position);
            getDomElement().setCursor(EnumCursor.RESIZE_UP_DOWN);
            getDomElement().requestRelayout();
        }

        @Override
        public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
            if (sy == -1) return;
            double newHeight = (absMouseY - sy) * getDomElement().getRelativeBound().getHeight() / getDomElement().getAbsBounds().getHeight() + sh;
            abstractHUDFeature.setHeight(newHeight);
            Rect newRect = new Rect(
                    HUDWidgetWrapper.this.getDomElement().getRelativeBound().getX(),
                    HUDWidgetWrapper.this.getDomElement().getRelativeBound().getY(),
                    abstractHUDFeature.getFeatureRect().getWidth(),
                    abstractHUDFeature.getFeatureRect().getHeight()
            );
            GUIPosition position = GUIPosition.of(newRect, rootWidget.getLastWidth(), rootWidget.getLastHeight());
            position.setWidth(newRect.getWidth());
            position.setHeight(newRect.getHeight());
            abstractHUDFeature.setFeatureRect(position);
            getDomElement().requestRelayout();
            this.sy = -1; this.sh = 0;
        }

        @Override
        public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
            if (childHandled) return false;
            getDomElement().setCursor(EnumCursor.RESIZE_UP_DOWN);
            return true;
        }
    }


    public class CornerWidget extends Widget implements Layouter, Renderer {

        @Override
        public List<Widget> build(DomElement buildContext) {
            return Collections.emptyList();
        }

        @Override
        public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
            return new Size(5, 5);
        }

        @Override
        public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
            context.drawRect(3,1,4,2, 0xFF888888);
            context.drawRect(1,3,2,4, 0xFF888888);
            context.drawRect(3,3,4,4, 0xFF888888);
        }

        private double sy = -1;
        private double sh = 0;
        private double sx = -1;
        private double sw = 0;
        @Override
        public boolean mouseClicked(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int mouseButton, boolean childHandled) {
            if (childHandled) return false;
            this.sy = absMouseY;
            this.sh = abstractHUDFeature.getFeatureRect().getHeight();
            this.sx = absMouseX;
            this.sw = abstractHUDFeature.getFeatureRect().getWidth();
            getDomElement().obtainFocus();
            return true;
        }

        @Override
        public void mouseClickMove(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int clickedMouseButton, long timeSinceLastClick) {
            if (sy == -1) return;
            double newWidth = (absMouseX - sx ) * getDomElement().getRelativeBound().getWidth() / getDomElement().getAbsBounds().getWidth()+ sw;
            double newHeight = (absMouseY - sy ) * getDomElement().getRelativeBound().getHeight() / getDomElement().getAbsBounds().getHeight()+ sh;
            abstractHUDFeature.setHeight(newHeight);
            abstractHUDFeature.setWidth(newWidth);
            Rect newRect = new Rect(
                    HUDWidgetWrapper.this.getDomElement().getRelativeBound().getX(),
                    HUDWidgetWrapper.this.getDomElement().getRelativeBound().getY(),
                    abstractHUDFeature.getFeatureRect().getWidth(),
                    abstractHUDFeature.getKeepRatio() != null ? abstractHUDFeature.getFeatureRect().getWidth() * abstractHUDFeature.getKeepRatio() : abstractHUDFeature.getFeatureRect().getHeight()
            );
            GUIPosition position = GUIPosition.of(newRect, rootWidget.getLastWidth(), rootWidget.getLastHeight());
            position.setWidth(newRect.getWidth());
            position.setHeight(newRect.getHeight());
            abstractHUDFeature.setFeatureRect(position);
            getDomElement().setCursor(EnumCursor.RESIZE_TLDR);
            getDomElement().requestRelayout();
        }

        @Override
        public void mouseReleased(int absMouseX, int absMouseY, double relMouseX, double relMouseY, int state) {
            if (sy == -1) return;
            double newWidth = (absMouseX - sx ) * getDomElement().getRelativeBound().getWidth() / getDomElement().getAbsBounds().getWidth()+ sw;
            double newHeight = (absMouseY - sy ) * getDomElement().getRelativeBound().getHeight() / getDomElement().getAbsBounds().getHeight()+ sh;
            abstractHUDFeature.setHeight(newHeight);
            abstractHUDFeature.setWidth(newWidth);
            Rect newRect = new Rect(
                    HUDWidgetWrapper.this.getDomElement().getRelativeBound().getX(),
                    HUDWidgetWrapper.this.getDomElement().getRelativeBound().getY(),
                    abstractHUDFeature.getFeatureRect().getWidth(),
                    abstractHUDFeature.getKeepRatio() != null ? abstractHUDFeature.getFeatureRect().getWidth() * abstractHUDFeature.getKeepRatio() : abstractHUDFeature.getFeatureRect().getHeight()
            );
            GUIPosition position = GUIPosition.of(newRect, rootWidget.getLastWidth(), rootWidget.getLastHeight());
            position.setWidth(newRect.getWidth());
            position.setHeight(newRect.getHeight());
            abstractHUDFeature.setFeatureRect(position);
            getDomElement().setCursor(EnumCursor.RESIZE_TLDR);
            getDomElement().requestRelayout();
            this.sy = -1; this.sh = 0; this.sx = -1; this.sw = 0;
        }

        @Override
        public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
            if (childHandled) return false;
            getDomElement().setCursor(EnumCursor.RESIZE_TLDR);
            return true;
        }
    }
}
