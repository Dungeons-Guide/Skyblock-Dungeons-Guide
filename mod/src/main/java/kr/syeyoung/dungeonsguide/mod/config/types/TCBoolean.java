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
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

public class TCBoolean implements FeatureTypeHandler<Boolean> {
    public static final TCBoolean INSTANCE = new TCBoolean();

    @Override
    public Boolean deserialize(JsonElement element) {
        return element.getAsBoolean();
    }

    @Override
    public JsonElement serialize(Boolean element) {
        return new JsonPrimitive(element);
    }

    @Override
    public Widget createDefaultWidgetFor(FeatureParameter parameter) {
        ParameterItem parameterItem = new ParameterItem(parameter, new BooleanEditWidget(parameter));
        return parameterItem;
    }

    public static class BooleanEditWidget extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "enabled")
        public final BindableAttribute<Boolean> isEnabled = new BindableAttribute<>(Boolean.class);
        public BooleanEditWidget(FeatureParameter<Boolean> featureParameter) {
            super(new ResourceIdentifier("dungeonsguide:gui/config/parameter/boolean.gui"));
            isEnabled.setValue(featureParameter.getValue());
            isEnabled.addOnUpdate((old,neu) -> {
                ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
                featureParameter.setValue(neu);
            });
        }
    }
}
