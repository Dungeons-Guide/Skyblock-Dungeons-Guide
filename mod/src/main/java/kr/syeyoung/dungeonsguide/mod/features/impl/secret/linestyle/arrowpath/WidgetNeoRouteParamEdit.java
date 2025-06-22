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

package kr.syeyoung.dungeonsguide.mod.features.impl.secret.linestyle.arrowpath;

import kr.syeyoung.dungeonsguide.mod.config.types.TCAColor;
import kr.syeyoung.dungeonsguide.mod.config.types.TCBoolean;
import kr.syeyoung.dungeonsguide.mod.config.types.TCDouble;
import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public class WidgetNeoRouteParamEdit extends AnnotatedImportOnlyWidget {


    @Bind(variableName = "linecolor")
    public final BindableAttribute<Widget> linecolor = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "arrowcolor")
    public final BindableAttribute<Widget> arrowcolor = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "pathwidth")
    public final BindableAttribute<Widget> pathwidth = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "pathsmoothness")
    public final BindableAttribute<Widget> pathsmoothness = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "animationSpeed")
    public final BindableAttribute<Widget> animationSpeed = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "destinationTextSize")
    public final BindableAttribute<Widget> destinationTextSize = new BindableAttribute<>(Widget.class);

    @Bind(variableName = "beaconToggle")
    public final BindableAttribute<String> beaconToggle = new BindableAttribute<>(String.class, "true");
    @Bind(variableName = "beaconEnable")
    public final BindableAttribute<Widget> beaconEnable = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "beamcolor")
    public final BindableAttribute<Widget> beamcolor = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "targetcolor")
    public final BindableAttribute<Widget> targetcolor = new BindableAttribute<>(Widget.class);


    @Bind(variableName = "etherwarpTracerToggle")
    public final BindableAttribute<String> etherwarpTracerToggle = new BindableAttribute<>(String.class, "true");
    @Bind(variableName = "etherwarpTracerEnable")
    public final BindableAttribute<Widget> etherwarpTracerEnable = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "etherwarpTracerColor")
    public final BindableAttribute<Widget> etherwarpTracerColor = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "etherwarpTracerWidth")
    public final BindableAttribute<Widget> etherwarpTracerWidth = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "etherwarpTracerTriggerDist")
    public final BindableAttribute<Widget> etherwarpTracerTriggerDist = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "disableTexturedPath")
    public final BindableAttribute<Widget> disableTexturedPath = new BindableAttribute<>(Widget.class);


    private <T> Widget generateConfigWidget(NeoRouteDisplayEngineRegistration.ArrowPathDisplayEngineSetting lineProperties, String key, Function<FeatureParameter<T>, Widget> converter) {
        FeatureParameter<T> featureParameter = lineProperties.getParameter(key);
        return converter.apply(featureParameter);
    }

    public WidgetNeoRouteParamEdit(NeoRouteDisplayEngineRegistration.ArrowPathDisplayEngineSetting lineProperties) {
        super(new ResourceLocation("dungeonsguide:gui/features/lineProperties/styles/neoroute.gui"));


        linecolor.setValue(this.generateConfigWidget(lineProperties, "lineColor", TCAColor.ColorEditWidget::new));
        arrowcolor.setValue(this.generateConfigWidget(lineProperties, "arrowColor", TCAColor.ColorEditWidget::new));
        pathwidth.setValue(this.<Double>generateConfigWidget(lineProperties, "lineWidth", (a) -> new TCDouble.DoubleEditWidget(a, 0.01, Double.POSITIVE_INFINITY)));
        pathsmoothness.setValue(this.<Double>generateConfigWidget(lineProperties, "lineSmooth", (a) -> new TCDouble.DoubleEditWidget(a, 0.01, Double.POSITIVE_INFINITY)));
        animationSpeed.setValue(this.<Double>generateConfigWidget(lineProperties, "animationSpeed", (a) -> new TCDouble.DoubleEditWidget(a, 0, Double.POSITIVE_INFINITY)));
        destinationTextSize.setValue(this.<Double>generateConfigWidget(lineProperties, "destinationSize", (a) -> new TCDouble.DoubleEditWidget(a, 0, Double.POSITIVE_INFINITY)));

        beaconToggle.setValue(lineProperties.isBeacon() ? "true": "false");
        beaconEnable.setValue(this.generateConfigWidget(lineProperties, "beacon", TCBoolean.BooleanEditWidget::new));
        beamcolor.setValue(this.generateConfigWidget(lineProperties, "beamColor", TCAColor.ColorEditWidget::new));
        targetcolor.setValue(this.generateConfigWidget(lineProperties, "beamTargetColor", TCAColor.ColorEditWidget::new));

        ((TCBoolean.BooleanEditWidget)beaconEnable.getValue()).isEnabled.addOnUpdate((old, neu) -> {
            beaconToggle.setValue(neu ? "true" : "false");
        });

        etherwarpTracerToggle.setValue(lineProperties.isEtherwarpTracer() ? "true" : "false");
        etherwarpTracerEnable.setValue(this.generateConfigWidget(lineProperties, "etherwarpTracer", TCBoolean.BooleanEditWidget::new));
        etherwarpTracerColor.setValue(this.generateConfigWidget(lineProperties, "etherwarpTracerColor", TCAColor.ColorEditWidget::new));
        etherwarpTracerWidth.setValue(this.<Double>generateConfigWidget(lineProperties, "etherwarpTracerWidth", (a) -> new TCDouble.DoubleEditWidget(a, 1, Double.POSITIVE_INFINITY)));
        etherwarpTracerTriggerDist.setValue(this.<Double>generateConfigWidget(lineProperties, "etherwarpTracerDist",  (a) -> new TCDouble.DoubleEditWidget(a, 0.01, Double.POSITIVE_INFINITY)));
        disableTexturedPath.setValue(this.generateConfigWidget(lineProperties, "etherwarpTracerDisableEtherwarpRoute", TCBoolean.BooleanEditWidget::new));
        ((TCBoolean.BooleanEditWidget)etherwarpTracerEnable.getValue()).isEnabled.addOnUpdate((old, neu) -> {
            etherwarpTracerToggle.setValue(neu ? "true" : "false");
        });
    }
}
