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

package kr.syeyoung.dungeonsguide.mod.features.richtext.config;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.config.types.coloredit.ColorEditPopup;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.AbsLocationPopup;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WidgetEditableStyleGroupStyleLineDerivedColor extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "enabled")
    public final BindableAttribute<Boolean> enabled = new BindableAttribute<>(Boolean.class);
    @Bind(variableName = "enabled2")
    public final BindableAttribute<String> enabled2 = new BindableAttribute<>(String.class);


    @Bind(variableName = "name")
    public final BindableAttribute<String> name = new BindableAttribute<>(String.class, "");
    @Bind(variableName = "value")
    public final BindableAttribute<Widget> value = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "actualEdit")
    public final BindableAttribute<Widget> actualEdit = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "disableable")
    public final BindableAttribute<String> disableable = new BindableAttribute<>(String.class);

    public WidgetEditableStyleGroupStyleLineDerivedColor(boolean disableable, String name, boolean isEnabled,
                                                         Supplier<Optional<AColor>> defaultValueSup,
                                                         Supplier<AColor> derivedColorSup, Consumer<Object> setter) {
        super(new ResourceLocation("dungeonsguide:gui/config/text/editableStyleline.gui"));

        this.disableable.setValue(disableable ? "show" : "hide");

        enabled.addOnUpdate((old, neu) -> {
            enabled2.setValue(neu ? "enable" : "disable");
            if (old == Boolean.FALSE && neu) {
                Optional<AColor> defaultValue = defaultValueSup.get();
                setter.accept(defaultValue);
                BindableAttribute<AColor> bindableAttribute = new BindableAttribute<>(AColor.class, defaultValue.orElse(null));
                bindableAttribute.addOnUpdate((o, neu2) -> {
                    setter.accept(Optional.ofNullable(neu2));
                });
                this.actualEdit.setValue(new ColorEditWidget(bindableAttribute, derivedColorSup));
            } else if (old == Boolean.TRUE && !neu){
                setter.accept(null);

                Optional<AColor> defaultValue = defaultValueSup.get();
                this.value.setValue(new WidgetConstColor(defaultValue.isPresent(), defaultValue.orElse(derivedColorSup.get())));
            }
        });
        enabled.setValue(isEnabled);

        this.name.setValue(name);
        Optional<AColor> defaultValue = defaultValueSup.get();
        this.value.setValue(new WidgetConstColor(defaultValue.isPresent(), defaultValue.orElse(derivedColorSup.get())));
            BindableAttribute<AColor> bindableAttribute = new BindableAttribute<>(AColor.class, defaultValue.orElse(null));
            bindableAttribute.addOnUpdate((o, neu2) -> {
                setter.accept(Optional.ofNullable(neu2));
            });
            this.actualEdit.setValue(new ColorEditWidget(bindableAttribute, derivedColorSup));
    }


    public static class ColorEditWidget extends AnnotatedImportOnlyWidget implements Renderer {
        @Bind(variableName = "color")
        public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class);
        @Bind(variableName = "hover")
        public final BindableAttribute<Integer> hover = new BindableAttribute<>(Integer.class);
        @Bind(variableName = "click")
        public final BindableAttribute<Integer> click = new BindableAttribute<>(Integer.class);
        @Bind(variableName = "enabled")
        public final BindableAttribute<Boolean> enabled = new BindableAttribute<>(Boolean.class);
        @Bind(variableName = "enabled2")
        public final BindableAttribute<String> enabled2 = new BindableAttribute<>(String.class);

        public final BindableAttribute<AColor> aColorBindableAttribute2 = new BindableAttribute<>(AColor.class);
        private final Supplier<AColor> derived;

        public ColorEditWidget(BindableAttribute<AColor> color, Supplier<AColor> derivedColor) {
            super(new ResourceLocation("dungeonsguide:gui/config/text/editableDerivedColor.gui"));

            enabled.addOnUpdate((o, n) -> {
                enabled2.setValue(n ? "enabled" : "disabled");
                aColorBindableAttribute2.setValue(n ? null : derivedColor.get());
            });
            enabled.setValue(color.getValue() == null);
            this.derived = derivedColor;

            aColorBindableAttribute2.exportTo(color);
        }

        private AbsLocationPopup locationPopup;
        @On(functionName = "setColor")
        public void openDialog() {
            if (locationPopup != null) return;
            PopupMgr popupMgr = PopupMgr.getPopupMgr(getDomElement());
            ColorEditPopup colorEditPopup = new ColorEditPopup(aColorBindableAttribute2);
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
        public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
            int color = RenderUtils.getColorAt(getDomElement().getAbsBounds().getX(), getDomElement().getAbsBounds().getY(), aColorBindableAttribute2.getValue() == null ? derived.get() : aColorBindableAttribute2.getValue());

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

            value.getRenderer().doRender(
                    partialTicks, context, value);
        }
    }


}
