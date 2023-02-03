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

public class TCFloat implements FeatureTypeHandler<Float> {
    public static final TCFloat INSTANCE = new TCFloat();
    @Override
    public Float deserialize(JsonElement element) {
        return element.getAsFloat();
    }

    @Override
    public JsonElement serialize(Float element) {
        return new JsonPrimitive(element);
    }


    @Override
    public Widget createDefaultWidgetFor(FeatureParameter parameter) {
        ParameterItem parameterItem = new ParameterItem(parameter, new FloatEditWidget(parameter));
        return parameterItem;
    }

    public static class FloatEditWidget extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "value")
        public final BindableAttribute<String> value = new BindableAttribute<>(String.class);
        private float truth;

        public FloatEditWidget(FeatureParameter<Float> featureParameter) {
            super(new ResourceLocation("dungeonsguide:gui/config/parameter/number.gui"));
            this.truth = featureParameter.getValue();
            value.setValue(String.valueOf(truth));
            value.addOnUpdate((old, neu) -> {
                try {
                    truth = Float.parseFloat(neu);
                    featureParameter.setValue(truth);
                } catch (Exception e) {
                }
            });
        }

        @On(functionName = "inc")
        public void inc() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            truth += (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ? 1 : 0.1);
            value.setValue(String.valueOf(truth));
        }

        @On(functionName = "dec")
        public void dec() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            truth -= (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) ? 1 : 0.1);
            value.setValue(String.valueOf(truth));
        }
    }
}
