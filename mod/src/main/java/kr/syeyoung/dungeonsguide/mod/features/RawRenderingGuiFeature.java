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

package kr.syeyoung.dungeonsguide.mod.features;

import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location2.MarkerProvider;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Clip;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Position;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.overlay.GUIRectPositioner;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
public abstract class RawRenderingGuiFeature extends AbstractHUDFeature {
    private Double ratio;

    protected RawRenderingGuiFeature(String category, String name, String description, String key, boolean keepRatio, double width, double height) {
        super(category, name, description, key);
        this.ratio = keepRatio ? height / width : null;
        if (keepRatio)
            this.getFeatureRect().setWidth(width);
        else {
            this.getFeatureRect().setWidth(width);
            this.getFeatureRect().setHeight(height);
        }
    }

    @Override
    public Double getKeepRatio() {
        return ratio;
    }

    @Override
    public boolean requiresWidthBound() {
        return true;
    }

    @Override
    public boolean requiresHeightBound() {
        return ratio == null;
    }

    public class WidgetFeatureWrapper extends Widget implements Renderer, Layouter {
        @Override
        public List<Widget> build(DomElement buildContext) {
            return Collections.emptyList();
        }

        @Override
        public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
            drawScreen(partialTicks);
        }

        @Override
        public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
            return new Size(getFeatureRect().getWidth(),
                    ratio != null ? getFeatureRect().getWidth() * ratio : getFeatureRect().getHeight());
        }
    }
    public class WidgetFeatureWrapper2 extends Widget implements Renderer, Layouter, MarkerProvider {
        @Override
        public List<Widget> build(DomElement buildContext) {
            return Collections.emptyList();
        }

        @Override
        public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
            if (buildContext.getSize().getWidth() <= 0 || buildContext.getSize().getHeight() <= 0)
                return;
            context.pushClip(buildContext.getAbsBounds(), buildContext.getSize(), 0,0, buildContext.getSize().getWidth(), buildContext.getSize().getHeight());
            drawDemo(partialTicks);
            context.popClip();
        }

        @Override
        public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
            return new Size(getFeatureRect().getWidth(),
                    ratio != null ? getFeatureRect().getWidth() * ratio : getFeatureRect().getHeight());
        }

        @Override
        public double getMaxIntrinsicWidth(DomElement buildContext, double height) {
            return getFeatureRect().getWidth();
        }

        @Override
        public double getMaxIntrinsicHeight(DomElement buildContext, double width) {
            return ratio != null ? getFeatureRect().getWidth() * ratio : getFeatureRect().getHeight();
        }

        @Override
        public List<Position> getMarkers() {
            Size size = getDomElement().getSize();

            return Arrays.asList(
                    new Position(size.getWidth()/2, 0),
                    new Position(0, size.getHeight()/2),
                    new Position(size.getWidth()/2,size.getHeight()),
                    new Position(size.getWidth(),size.getHeight()/2)
            );
        }
    }

    public OverlayWidget instantiateWidget() {
        Clip clip = new Clip();
        clip.widget.setValue(new WidgetFeatureWrapper());
        return new OverlayWidget(
                clip,
                OverlayType.UNDER_CHAT,
                new GUIRectPositioner(this::getFeatureRect)
        );
    }

    public Widget instantiateDemoWidget() {
        return new WidgetFeatureWrapper2();
    }
    public void drawScreen(float partialTicks) {
        drawHUD(partialTicks);
    }


    public abstract void drawHUD(float partialTicks);

    public void drawDemo(float partialTicks) {
        drawHUD(partialTicks);
    }
    public static FontRenderer getFontRenderer() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        return fr;
    }
}
