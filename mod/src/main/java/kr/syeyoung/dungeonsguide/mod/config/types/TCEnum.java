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
import kr.syeyoung.dungeonsguide.mod.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.mod.guiv2.ElementTreeWalkIterator;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public class TCEnum<T extends Enum<T>> implements FeatureTypeHandler<T> {
    @Getter
    private final T[] values;
    public TCEnum(T[] values) {
        this.values = values;
    }

    @Override
    public T deserialize(JsonElement element) {
        for (T value : values) {
            if (element.getAsString().equalsIgnoreCase(value.name())) return value;
        }
        return values[0];
    }

    @Override
    public JsonElement serialize(T element) {
        return new JsonPrimitive(element.name());
    }


    @Override
    public Widget createDefaultWidgetFor(FeatureParameter parameter) {
        ParameterItem parameterItem = new ParameterItem(parameter, new EnumEditWidget(values, parameter));
        return parameterItem;
    }

    public static class EnumEditWidget<T extends Enum<T>> extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "value")
        public final BindableAttribute<String> value = new BindableAttribute<>(String.class);
        private T[] values;
        private int idx;
        private FeatureParameter<T> featureParameter;
        public EnumEditWidget(T[] values, FeatureParameter<T> featureParameter) {
            super(new ResourceLocation("dungeonsguide:gui/config/parameter/stringChoice.gui"));
            this.idx = featureParameter.getValue().ordinal();
            this.values = values;
            this.featureParameter = featureParameter;
            value.setValue(featureParameter.getValue().name());
        }

        @On(functionName = "inc")
        public void inc() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            idx = (idx + 1) % values.length;
            value.setValue(values[idx].name());
            featureParameter.setValue(values[idx]);
            for (DomElement dom : new ElementTreeWalkIterator(getDomElement())) {
                if (dom.getWidget() instanceof ParameterItem) {
                    ((ParameterItem) dom.getWidget()).update();
                    break;
                }
            }
        }
        @On(functionName = "dec")
        public void dec() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            idx = (values.length + idx - 1) % values.length;
            value.setValue(values[idx].name());
            featureParameter.setValue(values[idx]);
            for (DomElement dom : new ElementTreeWalkIterator(getDomElement())) {
                if (dom.getWidget() instanceof ParameterItem) {
                    ((ParameterItem) dom.getWidget()).update();
                    break;
                }
            }
        }
    }
}
