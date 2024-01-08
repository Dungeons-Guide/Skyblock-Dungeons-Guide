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

package kr.syeyoung.dungeonsguide.mod.features.impl.advanced;


import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import kr.syeyoung.dungeonsguide.mod.SkyblockStatus;
import kr.syeyoung.dungeonsguide.mod.config.types.GUIPosition;
import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.RawRenderingGuiFeature;
import kr.syeyoung.dungeonsguide.mod.features.impl.party.playerpreview.widget.WidgetProfileViewer;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Clip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.MinecraftTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.MouseTooltip;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.overlay.*;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FeatureDebuggableMap extends RawRenderingGuiFeature  {
    public FeatureDebuggableMap() {
        super("Debug", "Display Debug Info included map", "ONLY WORKS WITH SECRET SETTING", "advanced.debug.map", true, 128, 128);
        this.setEnabled(false);
    }


    DynamicTexture dynamicTexture = new DynamicTexture(128, 128);
    ResourceLocation location = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation("dungeons/map/", dynamicTexture);

    @Override
    public void drawHUD(float partialTicks) {
//        if (!skyblockStatus.isOnDungeon()) return;
        if (!FeatureRegistry.DEBUG.isEnabled()) return;
//        DungeonContext context = skyblockStatus.getContext();
//        if (context == null) return;

        GlStateManager.pushMatrix();
        double factor = getFeatureRect().getWidth() / 128;
        GlStateManager.scale(factor, factor, 1);
        int[] textureData = dynamicTexture.getTextureData();
        MapUtils.getImage().getRGB(0, 0, 128, 128, textureData, 0, 128);
        dynamicTexture.updateDynamicTexture();
        Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        GlStateManager.enableAlpha();
        GuiScreen.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 128, 128, 128, 128);
        GlStateManager.popMatrix();


        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChat)) return;
    }

    @Override
    public void drawDemo(float partialTicks) {
        FontRenderer fr = getFontRenderer();
        double width = getFeatureRect().getWidth();

        GL11.glLineWidth(2);
        RenderUtils.drawUnfilledBox(0,0, (int) width, (int) width, 0xff000000, false);
    }

    public class WidgetFeatureWrapper extends Widget implements Renderer, Layouter {
        private MouseTooltip mouseTooltip;
        private MinecraftTooltip tooltip;

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
            return new Size(getFeatureRect().getWidth(), getFeatureRect().getWidth());
        }

        @Override
        public boolean mouseMoved(int absMouseX, int absMouseY, double relMouseX0, double relMouseY0, boolean childHandled) {
            double factor = getFeatureRect().getWidth() / 128;
            int i = (int) (relMouseX0/factor);
            int j = (int) (relMouseY0/factor);
            if (i >= 0 && j>= 0 && i <= 128 && j <= 128 && MapUtils.getColors() != null) {
                if (mouseTooltip == null) {
                    PopupMgr.getPopupMgr(getDomElement()).openPopup(mouseTooltip = new MouseTooltip(tooltip = new MinecraftTooltip()), a -> {});
                }
                tooltip.setTooltip(Arrays.asList(i+","+j,"Color: "+MapUtils.getColors()[j * 128 + i]));
            } else if (mouseTooltip != null){
                PopupMgr.getPopupMgr(getDomElement()).closePopup(mouseTooltip, null);
                mouseTooltip = null;
                tooltip = null;
            }
            return true;
        }

        @Override
        public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
            PopupMgr.getPopupMgr(getDomElement()).closePopup(mouseTooltip, null);
            mouseTooltip = null;
            tooltip = null;
        }
    }
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onGuiClose(GuiOpenEvent event) {
        if (!(event.gui instanceof GuiChat) && widgetFeatureWrapper != null) {
            PopupMgr.getPopupMgr(widgetFeatureWrapper.getDomElement()).closePopup(widgetFeatureWrapper.mouseTooltip, null);
            widgetFeatureWrapper.mouseTooltip = null;
            widgetFeatureWrapper.tooltip = null;
        }
    }
    private WidgetFeatureWrapper widgetFeatureWrapper;


    public OverlayWidget instantiateWidget() {
        Clip clip = new Clip();
        clip.widget.setValue(widgetFeatureWrapper = new WidgetFeatureWrapper());
        return new OverlayWidget(
                clip,
                OverlayType.UNDER_CHAT,
                new GUIRectPositioner(this::getFeatureRect)
        );
    }
}
