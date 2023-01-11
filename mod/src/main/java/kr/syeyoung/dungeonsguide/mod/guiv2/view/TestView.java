/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2022  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.mod.guiv2.view;

import kr.syeyoung.dungeonsguide.mod.guiv2.*;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.DomElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class TestView extends AnnotatedImportOnlyWidget {
    public TestView() {
        super(new ResourceLocation("dungeonsguide:gui/testview.gui"));
    }

    @Bind(variableName = "variable")
    public final BindableAttribute<String> bindableAttribute = new BindableAttribute<>(String.class, "");


    @Override
    public void onMount() {
        super.onMount();
        bindableAttribute.setValue(Minecraft.getMinecraft().thePlayer.getName());
    }
}
