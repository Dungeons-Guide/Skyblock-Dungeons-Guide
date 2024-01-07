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
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Text;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.AbsLocationPopup;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.popups.PopupMgr;
import kr.syeyoung.dungeonsguide.mod.guiv2.primitive.Rect;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.Renderer;
import kr.syeyoung.dungeonsguide.mod.guiv2.renderer.RenderingContext;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class WidgetEditableStyleGroupStyleLine extends AnnotatedImportOnlyWidget {
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

    public WidgetEditableStyleGroupStyleLine(boolean disableable, String name, boolean isEnabled, Supplier<Object> defaultValueSup, Consumer<Object> setter) {
        super(new ResourceLocation("dungeonsguide:gui/config/text/editableStyleline.gui"));

        this.disableable.setValue(disableable ? "show" : "hide");

        enabled.addOnUpdate((old, neu) -> {
            enabled2.setValue(neu ? "enable" : "disable");
            if (old == Boolean.FALSE && neu) {
                Object defaultValue = defaultValueSup.get();
                setter.accept(defaultValue);
                if (defaultValue instanceof AColor) {
                    BindableAttribute<AColor> bindableAttribute = new BindableAttribute<>(AColor.class,(AColor) defaultValue);
                    bindableAttribute.addOnUpdate((o, neu2) -> {
                        setter.accept(neu2);
                    });
                    this.actualEdit.setValue(new ColorEditWidget(bindableAttribute));
                } else if (defaultValue instanceof Double) {
                    BindableAttribute<Double> bindableAttribute = new BindableAttribute<>(Double.class, (Double) defaultValue);
                    bindableAttribute.addOnUpdate((o, neu2) -> {
                        setter.accept(neu2);
                    });
                    this.actualEdit.setValue(new DoubleEditWidget(bindableAttribute, 4, Double.POSITIVE_INFINITY));
                } else if (defaultValue instanceof Boolean) {
                    BindableAttribute<Boolean> bindableAttribute = new BindableAttribute<>(Boolean.class, (Boolean) defaultValue);
                    bindableAttribute.addOnUpdate((o, neu2) -> {
                        setter.accept(neu2);
                    });
                    this.actualEdit.setValue(new BooleanEditWidget(bindableAttribute));
                }
            } else if (old == Boolean.TRUE && !neu){
                setter.accept(null);

                Object defaultValue = defaultValueSup.get();
                if (defaultValue instanceof AColor) {
                    this.value.setValue(new WidgetConstColor(true, (AColor) defaultValue));
                } else if (defaultValue instanceof Double) {
                    this.value.setValue(new Text(defaultValue.toString(), 0xFFAAAAAA, Text.TextAlign.LEFT, Text.WordBreak.WORD, 1.0, 8.0));
                } else if (defaultValue instanceof Boolean) {
                    this.value.setValue(new WidgetConstCheckmark((Boolean) defaultValue));
                }
            }
        });
        enabled.setValue(isEnabled);

        this.name.setValue(name);
        Object defaultValue = defaultValueSup.get();
        if (defaultValue instanceof AColor) {
            this.value.setValue(new WidgetConstColor(true, (AColor) defaultValue));
        } else if (defaultValue instanceof Double) {
            this.value.setValue(new Text(defaultValue.toString(), 0xFFAAAAAA, Text.TextAlign.LEFT, Text.WordBreak.WORD, 1.0, 8.0));
        } else if (defaultValue instanceof Boolean) {
            this.value.setValue(new WidgetConstCheckmark((Boolean) defaultValue));
        }
        if (defaultValue instanceof AColor) {
            BindableAttribute<AColor> bindableAttribute = new BindableAttribute<>(AColor.class,(AColor) defaultValue);
            bindableAttribute.addOnUpdate((o, neu2) -> {
                setter.accept(neu2);
            });
            this.actualEdit.setValue(new ColorEditWidget(bindableAttribute));
        } else if (defaultValue instanceof Double) {
            BindableAttribute<Double> bindableAttribute = new BindableAttribute<>(Double.class, (Double) defaultValue);
            bindableAttribute.addOnUpdate((o, neu2) -> {
                setter.accept(neu2);
            });
            this.actualEdit.setValue(new DoubleEditWidget(bindableAttribute, 4, Double.POSITIVE_INFINITY));
        } else if (defaultValue instanceof Boolean) {
            BindableAttribute<Boolean> bindableAttribute = new BindableAttribute<>(Boolean.class, (Boolean) defaultValue);
            bindableAttribute.addOnUpdate((o, neu2) -> {
                setter.accept(neu2);
            });
            this.actualEdit.setValue(new BooleanEditWidget(bindableAttribute));
        }
    }


    public static class ColorEditWidget extends AnnotatedImportOnlyWidget implements Renderer {
        @Bind(variableName = "color")
        public final BindableAttribute<Integer> color = new BindableAttribute<>(Integer.class);
        @Bind(variableName = "hover")
        public final BindableAttribute<Integer> hover = new BindableAttribute<>(Integer.class);
        @Bind(variableName = "click")
        public final BindableAttribute<Integer> click = new BindableAttribute<>(Integer.class);

        public final BindableAttribute<AColor> aColorBindableAttribute = new BindableAttribute<>(AColor.class);

        public ColorEditWidget(BindableAttribute<AColor> color) {
            super(new ResourceLocation("dungeonsguide:gui/config/text/editableColor.gui"));

            aColorBindableAttribute.exportTo(color);
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
        public void doRender(float partialTicks, RenderingContext context, DomElement buildContext) {
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

            value.getRenderer().doRender(
                    partialTicks, context, value);
        }
    }

    public static class DoubleEditWidget extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "value")
        public final BindableAttribute<String> value = new BindableAttribute<>(String.class);
        public final BindableAttribute<Double> truth = new BindableAttribute<>(Double.class);

        private double min;
        private double max;

        public DoubleEditWidget(BindableAttribute<Double> featureParameter) {
            this(featureParameter, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }

        public DoubleEditWidget(BindableAttribute<Double> featureParameter, double min, double max) {
            super(new ResourceLocation("dungeonsguide:gui/config/text/editableNumber.gui"));
            this.min = min;
            this.max = max;
            truth.exportTo(featureParameter);

            value.setValue(String.format("%f", truth.getValue()));
            value.addOnUpdate((old, neu) -> {
                try {
                    double truth = Float.parseFloat(neu);
                    if (truth < min) return;
                    if (truth > max) return;
                    this.truth.setValue(truth);
                } catch (Exception e) {
                }
            });
        }

        @On(functionName = "inc")
        public void inc() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            double newT = truth.getValue() + (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ? 1 : 0.1);
            if (newT > max) newT = max;
            truth.setValue(newT);
            value.setValue(String.format("%f", truth.getValue()));
        }

        @On(functionName = "dec")
        public void dec() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            double newT = truth.getValue() - (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ? 1 : 0.1);
            if (newT < min) newT = min;
            truth.setValue(newT);
            value.setValue(String.format("%f", truth.getValue()));
        }
    }



    public static class BooleanEditWidget extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "enabled")
        public final BindableAttribute<Boolean> isEnabled = new BindableAttribute<>(Boolean.class);
        public BooleanEditWidget(BindableAttribute<Boolean> featureParameter) {
            super(new ResourceLocation("dungeonsguide:gui/config/text/editableCheckmark.gui"));
            isEnabled.exportTo(featureParameter);
            isEnabled.addOnUpdate((old,neu) -> {
                Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            });
        }
    }
}
