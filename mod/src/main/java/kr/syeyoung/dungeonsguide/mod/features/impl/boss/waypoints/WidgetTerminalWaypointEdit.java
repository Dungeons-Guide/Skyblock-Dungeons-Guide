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

package kr.syeyoung.dungeonsguide.mod.features.impl.boss.waypoints;

import kr.syeyoung.dungeonsguide.mod.config.types.TCAColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.config.types.TCDouble;
import kr.syeyoung.dungeonsguide.mod.config.types.TCInteger;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.features.impl.boss.FeatureF7TerminalWaypoints;
import kr.syeyoung.dungeonsguide.mod.features.impl.secret.PathfindLineProperties;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class WidgetTerminalWaypointEdit extends AnnotatedImportOnlyWidget {


    @Bind(variableName = "beamToggle")
    public final BindableAttribute<String> beamToggle = new BindableAttribute<>(String.class, "true");
    @Bind(variableName = "beamEnable")
    public final BindableAttribute<Widget> beamEnable = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "beamcolor")
    public final BindableAttribute<Widget> beamcolor = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "wayptEnable")
    public final BindableAttribute<Widget> wayptEnable = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "wayptToggle")
    public final BindableAttribute<String> wayptToggle = new BindableAttribute<>(String.class, "true");

    @Bind(variableName = "targetcolor")
    public final BindableAttribute<Widget> targetcolor = new BindableAttribute<>(Widget.class);


    @Bind(variableName = "status")
    public final BindableAttribute<Widget> statusEnable = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "all")
    public final BindableAttribute<Widget> allEnable = new BindableAttribute<>(Widget.class);





    private <T> Widget generateConfigWidget(FeatureF7TerminalWaypoints lineProperties, String key, Function<FeatureParameter<T>, Widget> converter) {
        FeatureParameter<T> featureParameter = lineProperties.getParameter(key);
        return converter.apply(featureParameter);
    }

    public WidgetTerminalWaypointEdit(FeatureF7TerminalWaypoints waypoints) {
        super(new ResourceLocation("dungeonsguide:gui/features/f7waypoints/paramEditor.gui"));

        allEnable.setValue(this.generateConfigWidget(waypoints, "all",  TCBoolean.BooleanEditWidget::new));
        statusEnable.setValue(this.generateConfigWidget(waypoints, "status", TCBoolean.BooleanEditWidget::new));


        wayptEnable.setValue(this.generateConfigWidget(waypoints, "beacons", TCBoolean.BooleanEditWidget::new));
        beamEnable.setValue(this.generateConfigWidget(waypoints, "beaconBeam", TCBoolean.BooleanEditWidget::new));
        beamcolor.setValue(this.generateConfigWidget(waypoints, "beamColor", TCAColor.ColorEditWidget::new));
        targetcolor.setValue(this.generateConfigWidget(waypoints, "beamTargetColor", TCAColor.ColorEditWidget::new));

        beamToggle.setValue(waypoints.isBeam() ? "true" : "false");
        wayptToggle.setValue(waypoints.isBeacon() ? "true" : "false");

        ((TCBoolean.BooleanEditWidget)beamEnable.getValue()).isEnabled.addOnUpdate((old, neu) -> {
            beamToggle.setValue(neu ? "true" : "false");
        });
        ((TCBoolean.BooleanEditWidget)wayptEnable.getValue()).isEnabled.addOnUpdate((old, neu) -> {
            wayptToggle.setValue(neu ? "true" : "false");
        });
    }
}
