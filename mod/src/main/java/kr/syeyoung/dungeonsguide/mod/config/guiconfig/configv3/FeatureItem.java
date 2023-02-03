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

import kr.syeyoung.dungeonsguide.mod.config.guiconfig.location2.HUDLocationConfig;
import kr.syeyoung.dungeonsguide.mod.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.mod.features.AbstractHUDFeature;
import kr.syeyoung.dungeonsguide.mod.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.Widget;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Navigator;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.mod.guiv2.xml.annotations.On;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.util.function.Supplier;

public class FeatureItem extends AnnotatedImportOnlyWidget {

    @Bind(variableName = "name")
    public final BindableAttribute<String> name = new BindableAttribute<>(String.class);
    @Bind(variableName = "description")
    public final BindableAttribute<String> description = new BindableAttribute<>(String.class);

    @Bind(variableName = "guiRelocate")
    public final BindableAttribute<String> guiRelocateShow = new BindableAttribute<>(String.class);
    @Bind(variableName = "configure")
    public final BindableAttribute<String> configureShow = new BindableAttribute<>(String.class);
    @Bind(variableName = "enable")
    public final BindableAttribute<String> enableShow = new BindableAttribute<>(String.class);

    @Bind(variableName = "isEnabled")
    public final BindableAttribute<Boolean> enabled = new BindableAttribute<>(Boolean.class);

    @Bind(variableName = "iconVisibility")
    public final BindableAttribute<String> iconVisibility = new BindableAttribute<>(String.class, "hide");
    @Bind(variableName = "icon")
    public final BindableAttribute<String> icon = new BindableAttribute<>(String.class, "dungeonsguide:textures/darklogo.png");


    private AbstractFeature feature;

    public FeatureItem(AbstractFeature feature) {
        super(new ResourceLocation("dungeonsguide:gui/config/featureitem.gui"));
        this.feature = feature;

        this.name.setValue(feature.getName());
        this.description.setValue(feature.getDescription());

        guiRelocateShow.setValue(feature instanceof AbstractHUDFeature ? "show" : "hide");
        configureShow.setValue(feature.getConfigureWidget() != null ? "show" : "hide");
        enableShow.setValue(feature.isDisyllable() ? "show" : "hide");
        enabled.setValue(feature.isEnabled());

        enabled.addOnUpdate((old, neu) -> {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            feature.setEnabled(neu);
        });

        if (feature.getIcon() != null) {
            icon.setValue(feature.getIcon());
            iconVisibility.setValue("show");
        }
    }


    @On(functionName = "configure")
    public void onEdit() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Navigator.getNavigator(getDomElement()).openPage(feature.getConfigureWidget());
        // do stuff
    }
    @On(functionName = "relocate")
    public void onRelocate() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        Minecraft.getMinecraft().displayGuiScreen(new GuiScreenAdapter(new GlobalHUDScale(new HUDLocationConfig((AbstractHUDFeature) feature))));
        // do stuff
    }
}
