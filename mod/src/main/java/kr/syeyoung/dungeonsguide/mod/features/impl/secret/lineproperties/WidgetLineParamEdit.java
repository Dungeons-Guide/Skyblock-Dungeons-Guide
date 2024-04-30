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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.lineproperties;

import kr.syeyoung.dungeonsguide.mod.config.types.*;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.FeatureDungeonMap2;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.MapConfiguration;
import kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map.WidgetScalebar;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.PathfindLineProperties;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class WidgetLineParamEdit extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "parentToggle")
    public final BindableAttribute<String> parentToggle = new BindableAttribute<>(String.class, "true");

    @Bind(variableName = "globalToggle")
    public final BindableAttribute<String> globalToggle = new BindableAttribute<>(String.class, "true");
    @Bind(variableName = "pathfindToggle")
    public final BindableAttribute<String> pathfindToggle = new BindableAttribute<>(String.class, "true");

    @Bind(variableName = "beaconToggle")
    public final BindableAttribute<String> beaconToggle = new BindableAttribute<>(String.class, "true");


    @Bind(variableName = "useParent")
    public final BindableAttribute<Widget> useParent = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "pathfindEnable")
    public final BindableAttribute<Widget> pathfindEnable = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "linecolor")
    public final BindableAttribute<Widget> linecolor = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "linethickness")
    public final BindableAttribute<Widget> linethickness = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "refreshrate")
    public final BindableAttribute<Widget> refreshrate = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "beaconEnable")
    public final BindableAttribute<Widget> beaconEnable = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "beamcolor")
    public final BindableAttribute<Widget> beamcolor = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "targetcolor")
    public final BindableAttribute<Widget> targetcolor = new BindableAttribute<>(Widget.class);



    private <T> Widget generateConfigWidget(PathfindLineProperties lineProperties, String key, Function<FeatureParameter<T>, Widget> converter) {
        FeatureParameter<T> featureParameter = lineProperties.getParameter(key);
        return converter.apply(featureParameter);
    }

    public WidgetLineParamEdit(PathfindLineProperties lineProperties) {
        super(new ResourceLocation("dungeonsguide:gui/features/lineProperties/paramEditor.gui"));

        if (lineProperties.getParent() == null || !lineProperties.isGlobal()) {
            parentToggle.setValue("true");
        } else {
            parentToggle.setValue("false");
        }
        globalToggle.setValue(lineProperties.getParent() != null ? "true" : "false");

        pathfindToggle.setValue(lineProperties.isPathfind() ? "true" : "false");
        beaconToggle.setValue(lineProperties.isBeacon() ? "true": "false");

        if (lineProperties.getParent() != null) {
            useParent.setValue(generateConfigWidget(lineProperties, "useGlobal", TCBoolean.BooleanEditWidget::new));

            ((TCBoolean.BooleanEditWidget)useParent.getValue()).isEnabled.addOnUpdate((old, neu) -> {
                parentToggle.setValue(neu ? "false" : "true");
            });
        }


        pathfindEnable.setValue(this.generateConfigWidget(lineProperties, "pathfind", TCBoolean.BooleanEditWidget::new));
        linecolor.setValue(this.generateConfigWidget(lineProperties, "lineColor", TCAColor.ColorEditWidget::new));
        linethickness.setValue(this.<Double>generateConfigWidget(lineProperties, "lineWidth", (a) -> new TCDouble.DoubleEditWidget(a, 0.1, Double.POSITIVE_INFINITY)));
        refreshrate.setValue(this.generateConfigWidget(lineProperties, "linerefreshrate", TCInteger.IntegerEditWidget::new));

        beaconEnable.setValue(this.generateConfigWidget(lineProperties, "beacon", TCBoolean.BooleanEditWidget::new));
        beamcolor.setValue(this.generateConfigWidget(lineProperties, "beamColor", TCAColor.ColorEditWidget::new));
        targetcolor.setValue(this.generateConfigWidget(lineProperties, "beamTargetColor", TCAColor.ColorEditWidget::new));

        ((TCBoolean.BooleanEditWidget)beaconEnable.getValue()).isEnabled.addOnUpdate((old, neu) -> {
            beaconToggle.setValue(neu ? "true" : "false");
        });
        ((TCBoolean.BooleanEditWidget)pathfindEnable.getValue()).isEnabled.addOnUpdate((old, neu) -> {
            pathfindToggle.setValue(neu ? "true" : "false");
        });
    }
}
