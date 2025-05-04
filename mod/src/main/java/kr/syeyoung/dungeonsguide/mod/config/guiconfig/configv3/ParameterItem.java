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

package kr.syeyoung.dungeonsguide.mod.config.guiconfig.configv3;

import kr.syeyoung.dungeonsguide.mod.features.FeatureParameter;
import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import net.minecraft.util.ResourceLocation;

public class ParameterItem extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "name")
    public final BindableAttribute<String> name = new BindableAttribute<>(String.class);
    @Bind(variableName = "description")
    public final BindableAttribute<String> description = new BindableAttribute<>(String.class);

    @Bind(variableName = "iconVisibility")
    public final BindableAttribute<String> iconVisibility = new BindableAttribute<>(String.class, "show");
    @Bind(variableName = "icon")
    public final BindableAttribute<String> icon = new BindableAttribute<>(String.class, "dungeonsguide:textures/darklogo.png");
    @Bind(variableName = "edit")
    public final BindableAttribute<Widget> edit = new BindableAttribute<>(Widget.class);



    private FeatureParameter featureParameter;
    public ParameterItem(FeatureParameter featureParameter, Widget childEditor) {
        super(new ResourceLocation("dungeonsguide:gui/config/parameteritem.gui"));
        this.featureParameter = featureParameter;
        update();


        if (featureParameter.getIcon() != null) {
            icon.setValue(featureParameter.getIcon());
            iconVisibility.setValue("show");
        }
        edit.setValue(childEditor);
    }

    public void update() {
        this.name.setValue(featureParameter.getName());
        this.description.setValue(featureParameter.getDescription());
    }


}
