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

package kr.syeyoung.dungeonsguide.mod.config.types.coloredit;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.modapi.data.ResourceIdentifier;

public class ColorEditPopup extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "color")
    public final BindableAttribute<String> color = new BindableAttribute<>(String.class);

    @Bind(variableName = "colorWheel")
    public final BindableAttribute<Widget> colorWheel = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "valueBar")
    public final BindableAttribute<Widget> valueBar = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "alphaBar")
    public final BindableAttribute<Widget> alphaBar = new BindableAttribute<>(Widget.class);
    @Bind(variableName = "chromaBar")
    public final BindableAttribute<Widget> chromaBar = new BindableAttribute<>(Widget.class);

    public final BindableAttribute<AColor> aColorBindableAttribute = new BindableAttribute<>(AColor.class);

    public ColorEditPopup(BindableAttribute<AColor> colorBindableAttribute) {
        super(new ResourceIdentifier("dungeonsguide:gui/config/parameter/color_set.gui"));

        aColorBindableAttribute.addOnUpdate((old, neu) ->{
            color.setValue("#" + String.format("%08x", neu.getRGB()).toUpperCase());
        });

        colorBindableAttribute.exportTo(aColorBindableAttribute);

        colorWheel.setValue(new ColorWheel(aColorBindableAttribute));
        valueBar.setValue(new ValueBar(aColorBindableAttribute));
        alphaBar.setValue(new AlphaBar(aColorBindableAttribute));
        chromaBar.setValue(new ChromaBar(aColorBindableAttribute));


        color.addOnUpdate((old, neu) -> {
            try {
                AColor color1 = new AColor(Integer.parseUnsignedInt(neu.substring(1),16), true);
                color1.setChromaSpeed(aColorBindableAttribute.getValue().getChromaSpeed());
                color1.setChroma(aColorBindableAttribute.getValue().isChroma());
                aColorBindableAttribute.setValue(color1);
            } catch (Exception e) {
            }
        });
    }
}
