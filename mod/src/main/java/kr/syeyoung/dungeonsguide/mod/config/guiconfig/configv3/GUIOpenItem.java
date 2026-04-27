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

import kr.syeyoung.dungeonsguide.mod.gui.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.gui.CustomGuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.gui.Widget;
import kr.syeyoung.dungeonsguide.mod.gui.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.gui.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.gui.xml.annotations.On;
import kr.syeyoung.modapi.ModAPI;
import kr.syeyoung.modapi.data.ResourceIdentifier;

import java.io.IOException;
import java.util.function.Supplier;

public class GUIOpenItem extends AnnotatedImportOnlyWidget {

    @Bind(variableName =  "image")
    public final BindableAttribute<String> image = new BindableAttribute<>(String.class);
    @Bind(variableName =  "category")
    public final BindableAttribute<String> bindableAttribute = new BindableAttribute<>(String.class);

    private Supplier<Widget> pageCreator;
    public GUIOpenItem(String category, Supplier<Widget> creator) {
        super(new ResourceIdentifier("dungeonsguide:gui/config/menuitem.gui"));
        this.pageCreator = creator;

        bindableAttribute.setValue(category);

        image.setValue("dungeonsguide:textures/dglogox128.png");
        try {
            String target = "dungeonsguide:textures/config/category_icon/"+category.toLowerCase()
                    .replace("&","").replace(" ","_")+".png";
            if (ModAPI.getAPI().getResourceManager().getResource(new ResourceIdentifier(target)) != null)
                image.setValue(target);
        } catch (IOException e) {}
    }

    @On(functionName = "click")
    public void openPage() {
        ModAPI.getAPI().getSoundHandler().playSoundAtPlayer(new ResourceIdentifier("gui.button.press"), 1.0F);
        ModAPI.getAPI().displayGuiScreen(new CustomGuiScreenAdapter(new GlobalHUDScale(pageCreator.get()), ModAPI.getAPI().getCurrentGuiScreen()));
    }
}
