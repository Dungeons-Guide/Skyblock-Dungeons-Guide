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

package kr.syeyoung.dungeonsguide.mod.features.impl.cosmetics.widget;

import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.util.ResourceLocation;

public class WidgetButton2 extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "text")
    public final BindableAttribute<String> text = new BindableAttribute<>(String.class);
    @Bind(variableName = "normal")
    public final BindableAttribute<Integer> normal = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "hover")
    public final BindableAttribute<Integer> hover = new BindableAttribute<>(Integer.class);
    @Bind(variableName = "press")
    public final BindableAttribute<Integer> press = new BindableAttribute<>(Integer.class);

    @Bind(variableName = "disabled")
    public final BindableAttribute<Boolean> _ = new BindableAttribute<>(Boolean.class, false);

    @Bind(variableName = "click")
    public final BindableAttribute<Runnable> onClick = new BindableAttribute<>(Runnable.class);

    public WidgetButton2(boolean enabled, String prefix, Runnable onClick) {
        super(new ResourceLocation("dungeonsguide:gui/config/cosmetics/button3.gui"));
        text.setValue(prefix);
        this.onClick.setValue(onClick);
        if (enabled) {
            normal.setValue(0xFF1E387A);
            hover.setValue(0xFF356091);
            press.setValue(0xFF50799E);
        } else {
            normal.setValue(0xFF2E2D2C);
            hover.setValue(RenderUtils.blendAlpha(0xFF2E2D2C, 0.2f));
            press.setValue(RenderUtils.blendAlpha(0xFF2E2D2C, 0.4f));
        }
    }
}
