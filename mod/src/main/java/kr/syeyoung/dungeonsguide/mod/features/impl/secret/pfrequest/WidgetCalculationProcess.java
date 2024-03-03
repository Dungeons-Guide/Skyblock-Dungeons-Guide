/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2024  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.pfrequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.data.WidgetList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class WidgetCalculationProcess extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "timestamp")
    public final BindableAttribute<String> timestamp = new BindableAttribute<>(String.class);
    @Bind(variableName = "id")
    public final BindableAttribute<String> id = new BindableAttribute<>(String.class);
    @Bind(variableName = "status")
    public final BindableAttribute<String> status = new BindableAttribute<>(String.class);
    @Bind(variableName = "events")
    public final BindableAttribute<List<Widget>> widgetList = new BindableAttribute(WidgetList.class);

    public WidgetCalculationProcess(JsonObject jsonObject) {
        super(new ResourceLocation("dungeonsguide:gui/features/requestcalculation/calculationprogress.gui"));
        timestamp.setValue(jsonObject.get("execution").getAsJsonObject().get("startDate").getAsString());
        id.setValue(jsonObject.get("execution").getAsJsonObject().get("name").getAsString());
        status.setValue(jsonObject.get("execution").getAsJsonObject().get("status").getAsString());
        List<Widget> widgets = new ArrayList<>();

        for (JsonElement history : jsonObject.get("history").getAsJsonArray()) {
            widgets.add(new WidgetCalculationProcessEvent(history.getAsJsonObject()));
        }

        widgetList.setValue(widgets);
    }
}
