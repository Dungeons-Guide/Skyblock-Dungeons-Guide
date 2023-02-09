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

package kr.syeyoung.dungeonsguide.mod.features.richtext.config;

import kr.syeyoung.dungeonsguide.mod.config.types.AColor;
import kr.syeyoung.dungeonsguide.mod.features.richtext.DefaultingDelegatingTextStyle;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.mod.utils.RenderUtils;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

public class WidgetGroupButton extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "name")
    public final BindableAttribute<String> name = new BindableAttribute<>(String.class);

    private final WidgetTextStyleConfig config;
    private final DefaultingDelegatingTextStyle style;


    private AColor defaultBG;
    private final DefaultingDelegatingTextStyle toEdit;
    public WidgetGroupButton(WidgetTextStyleConfig config, String name, DefaultingDelegatingTextStyle style, DefaultingDelegatingTextStyle defaultingDelegatingTextStyle) {
        super(new ResourceLocation("dungeonsguide:gui/config/text/groupbutton.gui"));
        this.name.setValue(name);
        this.config = config;
        this.style = style;
        this.toEdit = defaultingDelegatingTextStyle;

        defaultBG = style.backgroundShader;
    }

    public void setNewBG(AColor defaultBG) {
        this.defaultBG = defaultBG;
    }

    @On(functionName = "click")
    public void onClick() {
        toEdit.backgroundShader = null;
        toEdit.background = null;
        config.refreshText();
        config.enterEdit(style);
    }

    @Override
    public void mouseEntered(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        toEdit.background = true;
        toEdit.backgroundShader = defaultBG == null ? new AColor(0xFFFFFF00, true) : new AColor(RenderUtils.blendTwoColors(defaultBG.getRGB(), 0X55FFFF00), true);
        config.refreshText();
    }

    @Override
    public void mouseExited(int absMouseX, int absMouseY, double relMouseX, double relMouseY) {
        toEdit.backgroundShader = null;
        toEdit.background = null;
        config.refreshText();
    }
}
