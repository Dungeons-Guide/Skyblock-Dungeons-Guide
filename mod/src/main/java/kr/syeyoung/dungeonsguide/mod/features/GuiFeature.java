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

import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.GuiConfigV2;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location.GuiGuiLocationConfig;
import kr.syeyoung.dungeonsguide.mod.config.types.GUIRectangle;
import kr.syeyoung.dungeonsguide.mod.config.types.TypeConverterRegistry;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.gui.MPanel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MButton;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MLabel;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MPassiveLabelAndElement;
import kr.syeyoung.dungeonsguide.mod.gui.elements.MToggleButton;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayManager;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public abstract class GuiFeature extends AbstractHUDFeature {

    protected GuiFeature(String category, String name, String description, String key, boolean keepRatio, int width, int height) {
        super(category, name, description, key, keepRatio, width, height);
    }

    public class WidgetFeatureWrapper extends Widget implements Renderer, Layouter {
        @Override
        public List<Widget> build(DomElement buildContext) {
            return Collections.emptyList();
        }

        @Override
        public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
            drawScreen(partialTicks);
        }

        @Override
        public Size layout(DomElement buildContext, ConstraintBox constraintBox) {
            return new Size(constraintBox.getMaxWidth(), constraintBox.getMaxHeight());
        }
    }

    public OverlayWidget instantiateWidget() {
        return new OverlayWidget(
                new WidgetFeatureWrapper(),
                OverlayType.UNDER_CHAT,
                this::getWidgetPosition
        );
    }

    public void drawScreen(float partialTicks) {
        Rectangle featureRect = this.getFeatureRect().getRectangleNoScale();
        clip(featureRect.x, featureRect.y, featureRect.width, featureRect.height);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        drawHUD(partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }


    public abstract void drawHUD(float partialTicks);

    public void drawDemo(float partialTicks) {
        drawHUD(partialTicks);
    }

    private void clip(int x, int y, int width, int height) {
//        int scale = resolution.getScaleFactor();
        int scale = 1;
        GL11.glScissor((x ) * scale, Minecraft.getMinecraft().displayHeight - (y + height) * scale, (width) * scale, height * scale);
    }

    public static FontRenderer getFontRenderer() {
        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        return fr;
    }
}
