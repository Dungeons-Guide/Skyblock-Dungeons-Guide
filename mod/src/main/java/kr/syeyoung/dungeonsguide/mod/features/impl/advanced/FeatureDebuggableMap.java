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


import kr.syeyoung.dungeonsguide.mod.events.annotations.DGEventHandler;
import kr.syeyoung.dungeonsguide.mod.features.FeatureRegistry;
import kr.syeyoung.dungeonsguide.mod.features.RawRenderingGuiFeature;
import kr.syeyoung.dungeonsguide.mod.gui.DomElement;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.Clip;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.gui.elements.popups.RawMinecraftTooltip;
import kr.syeyoung.dungeonsguide.mod.gui.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.ConstraintBox;
import kr.syeyoung.dungeonsguide.mod.gui.primitive.Size;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.gui.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.overlay.GUIRectPositioner;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayType;
import kr.syeyoung.dungeonsguide.mod.overlay.OverlayWidget;
import kr.syeyoung.dungeonsguide.mod.utils.MapUtils;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;
import kr.syeyoung.modapi.event.events.GuiOpenEvent;
import kr.syeyoung.modapi.gui.UGuiScreenChat;
import kr.syeyoung.modapi.rendering.UNativeImageBackedTexture;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FeatureDebuggableMap extends RawRenderingGuiFeature  {
    public FeatureDebuggableMap() {
        super("Debug", "Display Debug Info included map", "ONLY WORKS WITH SECRET SETTING", "advanced.debug.map", true, 128, 128);
        this.setEnabled(false);
    }


//    DynamicTexture dynamicTexture = new DynamicTexture(128, 128);
//    ResourceLocation location = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation("dungeons/map/", dynamicTexture);
    UNativeImageBackedTexture texture = ModAPI.getAPI().getTextureManager().createTexture("Dungeon Debug Map", 128, 128, true);
    ResourceIdentifier identifier = ModAPI.getAPI().getTextureManager().registerTexture("dungeons/map", texture);


    @Override
    public void drawHUD(RenderingContext ctx, float partialTicks) {
//        if (!skyblockStatus.isOnDungeon()) return;
        if (!FeatureRegistry.DEBUG.isEnabled()) return;
//        DungeonContext context = skyblockStatus.getContext();
//        if (context == null) return;

        texture.load(MapUtils.getImage());
        texture.upload();

        ctx.ctx().pushMatrix();
        double factor = getFeatureRect().getWidth() / 128;
        ctx.ctx().scale(factor, factor, 1);
        ctx.drawScaledCustomSizeModalRect(identifier,
                0, 0, 0, 0, 1, 1, 128, 128, 128, 128);

        if (!(ModAPI.getAPI().getCurrentGuiScreen() instanceof UGuiScreenChat)) return;
    }

    @Override
    public void drawDemo(RenderingContext context, float partialTicks) {
        double width = getFeatureRect().getWidth();

        context.drawUnfilledBox(0,0, (int) width, (int) width, 0xff000000, false, 2);
    }

    public class WidgetFeatureWrapper extends Widget implements Renderer, Layouter {
        private RawMinecraftTooltip mouseTooltip;

        @Override
        public List<Widget> build(DomElement buildContext) {
            return Collections.emptyList();
        }

        @Override
        public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
            drawScreen(context, partialTicks);
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
                    PopupMgr.getPopupMgr(getDomElement()).openPopup(mouseTooltip = new RawMinecraftTooltip(absMouseX, absMouseY), a -> {});
                }
                mouseTooltip.setTooltip(Arrays.asList(i+","+j,"Color: "+MapUtils.getColors().get(i,j)));
            } else if (mouseTooltip != null){
                PopupMgr.getPopupMgr(getDomElement()).closePopup(mouseTooltip, null);
                mouseTooltip = null;
            }
            return true;
        }

        @Override
        public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
            if (mouseTooltip != null)
                PopupMgr.getPopupMgr(getDomElement()).closePopup(mouseTooltip, null);
            mouseTooltip = null;
        }
    }
    @DGEventHandler(triggerOutOfSkyblock = true)
    public void onGuiClose(GuiOpenEvent event) {
        if (!(event.getGui() instanceof UGuiScreenChat) && widgetFeatureWrapper != null) {
            PopupMgr.getPopupMgr(widgetFeatureWrapper.getDomElement()).closePopup(widgetFeatureWrapper.mouseTooltip, null);
            widgetFeatureWrapper.mouseTooltip = null;
        }
    }
    private WidgetFeatureWrapper widgetFeatureWrapper;


    public OverlayWidget instantiateWidget() {
        Clip clip = new Clip();
        clip.widget.setValue(widgetFeatureWrapper = new WidgetFeatureWrapper());
        return new OverlayWidget(
                clip,
                OverlayType.UNDER_CHAT,
                new GUIRectPositioner(this::getFeatureRect),
                getClass().getSimpleName()
        );
    }
}
