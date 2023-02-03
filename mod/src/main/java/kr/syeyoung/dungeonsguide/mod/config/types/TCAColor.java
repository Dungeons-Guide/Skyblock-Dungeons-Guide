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

package kr.syeyoung.dungeonsguide.mod.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ParameterItem;
import kr.syeyoung.dungeonsguide.mod.config.types.coloredit.ColorEditPopup;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.AbsLocationPopup;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.layouter.Layouter;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class TCAColor implements FeatureTypeHandler<AColor> {
    public static final TCAColor INSTANCE = new TCAColor();
    @Override
    public AColor deserialize(JsonElement element) {
        if (element instanceof JsonPrimitive)
            return new AColor(element.getAsInt(), true);

        JsonObject object = element.getAsJsonObject();
        AColor color = new AColor(object.get("color").getAsInt(), true);
        color.setChroma(object.get("chroma").getAsBoolean());
        color.setChromaSpeed(object.get("chromaSpeed").getAsFloat());
        return color;
    }

    @Override
    public JsonElement serialize(AColor element) {
        JsonObject object = new JsonObject();
        object.addProperty("color", element.getRGB());
        object.addProperty("chroma", element.isChroma());
        object.addProperty("chromaSpeed", element.getChromaSpeed());
        return object;
    }


    @Override
    public Widget createDefaultWidgetFor(FeatureParameter parameter) {
        ParameterItem parameterItem = new ParameterItem(parameter, new ColorEditWidget(parameter));
        return parameterItem;
    }

    public static class ColorEditWidget extends AnnotatedImportOnlyWidget implements Renderer {
        @Bind(variableName = "color")
        public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class);
        @Bind(variableName = "hover")
        public final BindableAttribute<Integer> hover = new BindableAttribute<>(Integer.class);
        @Bind(variableName = "click")
        public final BindableAttribute<Integer> click = new BindableAttribute<>(Integer.class);

        public final BindableAttribute<AColor> aColorBindableAttribute = new BindableAttribute<>(AColor.class);

        private FeatureParameter<AColor> featureParameter;
        public ColorEditWidget(FeatureParameter<AColor> featureParameter) {
            super(new ResourceLocation("dungeonsguide:gui/config/parameter/color.gui"));
            this.featureParameter = featureParameter;

            aColorBindableAttribute.addOnUpdate((old, color) -> {
                this.featureParameter.setValue(color);
            });
            aColorBindableAttribute.setValue(featureParameter.getValue());
        }

        private AbsLocationPopup locationPopup;
        @On(functionName = "setColor")
        public void openDialog() {
            if (locationPopup != null) return;
            PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
            ColorEditPopup colorEditPopup = new ColorEditPopup(aColorBindableAttribute);
            double x = getDomElement().getAbsBounds().getX();
            double y = getDomElement().getAbsBounds().getY()+getDomElement().getAbsBounds().getHeight();
            locationPopup =
                    new AbsLocationPopup(x,y, colorEditPopup,true);
            popupMgr.openPopup(locationPopup, (cb) -> {
                locationPopup = null;
            });
        }

        @Override
        public void onUnmount() {
            super.onUnmount();
            if (locationPopup != null)
                PopupMgr.getPopupMgr(getDomElement()).closePopup(locationPopup);
        }

        @Override
        public void doRender(int absMouseX, int absMouseY, double relMouseX, double relMouseY, float partialTicks, RenderingContext context, DomElement buildContext) {
            int color = RenderUtils.getColorAt(getDomElement().getAbsBounds().getX(), getDomElement().getAbsBounds().getY(), aColorBindableAttribute.getValue());

            this.color.setValue(color);
            this.hover.setValue(RenderUtils.blendAlpha(color, 0.2f));
            this.click.setValue(RenderUtils.blendAlpha(color, 0.4f));

            if (buildContext.getChildren().isEmpty()) return;
            DomElement value = buildContext.getChildren().get(0);

            Rect original = value.getRelativeBound();
            if (original == null) return;
            GlStateManager.translate(original.getX(), original.getY(), 0);

            double absXScale = buildContext.getAbsBounds().getWidth() / buildContext.getSize().getWidth();
            double absYScale = buildContext.getAbsBounds().getHeight() / buildContext.getSize().getHeight();

            Rect elementABSBound = new Rect(
                    (buildContext.getAbsBounds().getX() + original.getX() * absXScale),
                    (buildContext.getAbsBounds().getY() + original.getY() * absYScale),
                    (original.getWidth() * absXScale),
                    (original.getHeight() * absYScale)
            );
            value.setAbsBounds(elementABSBound);

            value.getRenderer().doRender(absMouseX, absMouseY,
                    relMouseX - original.getX(),
                    relMouseY - original.getY(), partialTicks, context, value);
        }
    }

}
