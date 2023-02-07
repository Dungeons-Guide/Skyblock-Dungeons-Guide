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

package kr.syeyoung.dungeonsguide.launcher.gui.screen.version;

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.launcher.loader.IDGLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

public abstract class WidgetInfo extends AnnotatedImportOnlyWidget {
    @Bind(variableName = "default")
    public final BindableAttribute<Boolean> makeItDefault = new BindableAttribute<>(Boolean.class, true);
    @Bind(variableName = "updatelog")
    public final BindableAttribute<String> updateLog = new BindableAttribute<>(String.class, "");

    @Bind(variableName = "version")
    public final BindableAttribute<String> version = new BindableAttribute<>(String.class, "");

    public WidgetInfo() {
        super(new ResourceLocation("dungeons_guide_loader:gui/versions/versionInfo.gui"));
        makeItDefault.addOnUpdate((old, neu) ->{
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        });
    }

    public void setUpdateLog(String log) {
        updateLog.setValue(log);
    }
    public void setVersion(String version) {this.version.setValue(version);}
    public void setDefault(boolean bool) {this.makeItDefault.setValue(bool);}

    public abstract IDGLoader getLoader();

    public void setConfiguration(Configuration configuration) {}

    @On(functionName = "load")
    public void load() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
        IDGLoader idgLoader = getLoader();

        if (makeItDefault.getValue()) {
            File f = new File(Main.getConfigDir(), "loader.cfg");
            Configuration configuration = new Configuration(f);
            configuration.load();
            setConfiguration(configuration);
            configuration.save();
        }

        Minecraft.getMinecraft().displayGuiScreen(null);
        Main.getMain().tryReloadingWithSplash(idgLoader);
    }
}
