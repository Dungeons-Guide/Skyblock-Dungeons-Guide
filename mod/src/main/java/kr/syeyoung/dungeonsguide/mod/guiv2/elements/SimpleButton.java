/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2023  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2.elements;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Export;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Passthrough;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Passthroughs;
import net.minecraft.util.ResourceLocation;

@Passthrough(exportName = "click", bindName = "buttonClick", type = Runnable.class)
@Passthrough(exportName = "text", bindName = "text", type = String.class)
public class SimpleButton extends AnnotatedWidget {
    @Export(attributeName = "disabled") @Bind(variableName = "disabled")
    public final BindableAttribute<Boolean> disabled = new BindableAttribute<>(Boolean.class, false);

    @Export(attributeName = "backgroundColor") @Bind(variableName = "backgroundColor")
    public final BindableAttribute<Integer> backgroundColor = new BindableAttribute<>(Integer.class);
    @Export(attributeName = "disabledBackgroundColor") @Bind(variableName = "disabledBackgroundColor")
    public final BindableAttribute<Integer> disabledBackgroundColor = new BindableAttribute<>(Integer.class);
    @Export(attributeName = "pressedBackgroundColor") @Bind(variableName = "pressedBackgroundColor")
    public final BindableAttribute<Integer> pressedBackgroundColor = new BindableAttribute<>(Integer.class);
    @Export(attributeName = "hoveredBackgroundColor") @Bind(variableName = "hoveredBackgroundColor")
    public final BindableAttribute<Integer> hoveredBackgroundColor = new BindableAttribute<>(Integer.class);

    @Export(attributeName = "textColor") @Bind(variableName = "textColor")
    public final BindableAttribute<Integer> textColor = new BindableAttribute<>(Integer.class);
    @Export(attributeName = "disabledTextColor") @Bind(variableName = "disabledTextColor")
    public final BindableAttribute<Integer> disabledTextColor = new BindableAttribute<>(Integer.class);
    @Export(attributeName = "pressedTextColor") @Bind(variableName = "pressedTextColor")
    public final BindableAttribute<Integer> pressedTextColor = new BindableAttribute<>(Integer.class);
    @Export(attributeName = "hoveredTextColor") @Bind(variableName = "hoveredTextColor")
    public final BindableAttribute<Integer> hoveredTextColor = new BindableAttribute<>(Integer.class);
    public SimpleButton() {
        super(new ResourceLocation("dungeonsguide:gui/simpleButton.gui"));
    }
}
