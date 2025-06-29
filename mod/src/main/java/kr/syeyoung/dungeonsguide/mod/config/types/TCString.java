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
import kr.syeyoung.modapi.data.ResourceIdentifier;

public class TCString implements FeatureTypeHandler<String> {
    public static final TCString INSTANCE = new TCString();

    @Override
    public String deserialize(JsonElement element) {
        return element.getAsString();
    }

    @Override
    public JsonElement serialize(String element) {
        return new JsonPrimitive(element);
    }


    @Override
    public Widget createDefaultWidgetFor(FeatureParameter parameter) {
        ParameterItem parameterItem = new ParameterItem(parameter, new StringEditWidget(parameter));
        return parameterItem;
    }

    public static class StringEditWidget extends AnnotatedImportOnlyWidget {
        @Bind(variableName = "value")
        public final BindableAttribute<String> value = new BindableAttribute<>(String.class);
        public StringEditWidget(FeatureParameter<String> featureParameter) {
            super(new ResourceIdentifier("dungeonsguide:gui/config/parameter/string.gui"));
            value.setValue(featureParameter.getValue());
            value.addOnUpdate((old,neu) -> featureParameter.setValue(neu));
        }
    }
}
