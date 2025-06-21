/*
 * Dungeons Guide - The most Integerelligent Hypixel Skyblock Dungeons Mod
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

package kr.syeyoung.dungeonsguide.launcher.guiv2.elements.image;

import kr.syeyoung.dungeonsguide.launcher.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.launcher.guiv2.DomElement;
import kr.syeyoung.dungeonsguide.launcher.guiv2.Widget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.AnnotatedExportOnlyWidget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Export;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public class ResourceImage extends AnnotatedExportOnlyWidget {

    @Export(attributeName="location")
    public final BindableAttribute<String > location = new BindableAttribute<String >(String.class);
    @Export(attributeName="uvX")
    public final BindableAttribute<Integer> uvX = new BindableAttribute<Integer>(Integer.class);
    @Export(attributeName="uvY")
    public final BindableAttribute<Integer> uvY = new BindableAttribute<Integer>(Integer.class);
    @Export(attributeName="textureWidth")
    public final BindableAttribute<Integer> textureWidth = new BindableAttribute<Integer>(Integer.class, 256);
    @Export(attributeName="textureHeight")
    public final BindableAttribute<Integer> textureHeight = new BindableAttribute<Integer>(Integer.class, 256);
    @Export(attributeName="uvWidth")
    public final BindableAttribute<Integer> uvWidth = new BindableAttribute<Integer>(Integer.class);
    @Export(attributeName="uvHeight")
    public final BindableAttribute<Integer> uvHeight = new BindableAttribute<Integer>(Integer.class);
    
    @Override
    public List<Widget> build(DomElement buildContext) {
        return Collections.singletonList(new Image(
                new ResourceLocation(location.getValue()), uvX.getValue(), uvY.getValue(), textureWidth.getValue(), textureHeight.getValue(), uvWidth.getValue(), uvHeight.getValue()
        ));
    }
}
