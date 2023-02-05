/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.config.types;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3.ParameterItem;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public class TCDouble implements FeatureTypeHandler<Double> {
    public static final TCDouble INSTANCE = new TCDouble();
    @Override
    public Double deserialize(JsonElement element) {
        return element.getAsDouble();
    }

    @Override
    public JsonElement serialize(Double element) {
        return new JsonPrimitive(element);
    }


    @Override
    public Widget createDefaultWidgetFor(FeatureParameter parameter) {
        ParameterItem parameterItem = new ParameterItem(parameter, new DoubleEditWidget(parameter));
        return parameterItem;
    }

    public static class DoubleEditWidget extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "value")
        public final BindableAttribute<String> value = new BindableAttribute<>(String.class);
        private double truth;

        private double min;
        private double max;

        public DoubleEditWidget(FeatureParameter<Double> featureParameter) {
            this(featureParameter, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        }

        public DoubleEditWidget(FeatureParameter<Double> featureParameter, double min, double max) {
            super(new ResourceLocation("dungeonsguide:gui/config/parameter/number.gui"));
            this.truth = featureParameter.getValue();
            this.min = min;
            this.max = max;
            value.setValue(String.format("%f", truth));
            value.addOnUpdate((old, neu) -> {
                try {
                    truth = Float.parseFloat(neu);
                    if (truth < min) return;
                    if (truth > max) return;
                    featureParameter.setValue(truth);
                } catch (Exception e) {
                }
            });
        }

        @On(functionName = "inc")
        public void inc() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            truth += (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ? 1 : 0.1);
            if (truth > max) truth = max;
            value.setValue(String.format("%f", truth));
        }

        @On(functionName = "dec")
        public void dec() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            truth -= (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ? 1 : 0.1);
            if (truth < min) truth = min;
            value.setValue(String.format("%f", truth));
        }
    }
}
