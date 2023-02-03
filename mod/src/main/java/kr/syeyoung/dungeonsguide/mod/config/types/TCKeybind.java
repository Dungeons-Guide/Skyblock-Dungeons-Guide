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
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

public class TCKeybind implements FeatureTypeHandler<Integer> {
    public static final TCKeybind INSTANCE = new TCKeybind();
    @Override
    public Integer deserialize(JsonElement element) {
        return element.getAsInt();
    }

    @Override
    public JsonElement serialize(Integer element) {
        return new JsonPrimitive(element);
    }


    @Override
    public Widget createDefaultWidgetFor(FeatureParameter parameter) {
        ParameterItem parameterItem = new ParameterItem(parameter, new KeybindEditWidget(parameter));
        return parameterItem;
    }

    public static class KeybindEditWidget extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "key")
        public final BindableAttribute<String> value = new BindableAttribute<>(String.class);

        private int currentKey = 0;
        private boolean listen = false;
        private FeatureParameter<Integer> parameter;
        public KeybindEditWidget(FeatureParameter<Integer> featureParameter) {
            super(new ResourceLocation("dungeonsguide:gui/config/parameter/keybind.gui"));
            this.parameter = featureParameter;
            currentKey = featureParameter.getValue();
            value.setValue(GameSettings.getKeyDisplayString(currentKey));
        }

        @On(functionName = "setKey")
        public void listen() {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            getDomElement().obtainFocus();
            value.setValue("> "+GameSettings.getKeyDisplayString(currentKey)+" <");
            listen =true;
        }

        @Override
        public void keyPressed(char typedChar, int keyCode) {
            if (!listen) return;
            if (keyCode == 1) {
                currentKey = 0;
            } else if (keyCode != 0) {
                currentKey = keyCode;
            } else if (typedChar > 0) {
                currentKey = typedChar + 256;
            }
            listen = false;
            parameter.setValue(currentKey);
            value.setValue(GameSettings.getKeyDisplayString(currentKey));
        }
    }
}
