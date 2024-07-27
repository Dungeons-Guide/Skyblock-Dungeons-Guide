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

package kr.syeyoung.dungeonsguide.mod.features.impl.dungeon.map;

import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

public class WidgetScalebar extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "min")
    public final BindableAttribute<Double> min = new BindableAttribute<>(Double.class);
    @Bind(variableName = "max")
    public final BindableAttribute<Double> max = new BindableAttribute<>(Double.class);
    @Bind(variableName = "current")
    public final BindableAttribute<Double> current = new BindableAttribute<>(Double.class);


    public WidgetScalebar(FeatureParameter<Double> featureParameter, double min, double max) {
        super(new ResourceLocation("dungeonsguide:gui/features/map/scaleScrollbar.gui"));
        this.max.setValue(max);
        this.min.setValue(min);
        this.current.setValue(featureParameter.getValue());
        this.current.addOnUpdate((old, neu) -> featureParameter.setValue(neu));
    }

    public WidgetScalebar(double curr, Consumer<Double> onUpdate, double min, double max) {
        super(new ResourceLocation("dungeonsguide:gui/features/map/scaleScrollbar.gui"));
        this.max.setValue(max);
        this.min.setValue(min);
        this.current.setValue(curr);
        this.current.addOnUpdate((old, neu) -> onUpdate.accept(neu));
    }
}
