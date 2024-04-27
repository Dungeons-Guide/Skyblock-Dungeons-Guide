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

package kr.syeyoung.dungeonsguide.launcher.gui.screen;

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.exceptions.DungeonsGuideUnloadingException;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.Notification;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.NotificationManager;
import kr.syeyoung.dungeonsguide.launcher.gui.tooltip.WidgetNotificationAutoClose;
import kr.syeyoung.dungeonsguide.launcher.guiv2.BindableAttribute;
import kr.syeyoung.dungeonsguide.launcher.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.launcher.guiv2.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.AnnotatedImportOnlyWidget;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.Bind;
import kr.syeyoung.dungeonsguide.launcher.guiv2.xml.annotations.On;
import kr.syeyoung.dungeonsguide.launcher.loader.IDGLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class WidgetConfig extends AnnotatedImportOnlyWidget {
//    @Bind(variableName = "stacktrace")
//    public final BindableAttribute<String> stacktrace = new BindableAttribute<>(String.class);

    @Bind(variableName = "autoloadBranch")
    public final BindableAttribute<String> autoloadBranch = new BindableAttribute<>(String.class);
    @Bind(variableName = "autoloadVersion")
    public final BindableAttribute<String> autoloadVersion = new BindableAttribute<>(String.class);
    @Bind(variableName = "currentBranch")
    public final BindableAttribute<String> currentBranch = new BindableAttribute<>(String.class);
    @Bind(variableName = "currentVersion")
    public final BindableAttribute<String> currentVersion = new BindableAttribute<>(String.class);


    public WidgetConfig() {
        super(new ResourceLocation("dungeons_guide_loader:gui/config.gui"));

        File f = new File(Main.getConfigDir(), "loader.cfg");
        Configuration configuration = new Configuration(f);
        configuration.save();
        String loaderName = Main.getMain().getLoaderName(configuration);
        String branch =  System.getProperty("branch") == null ? configuration.get("loader", "remoteBranch", "$default").getString() : System.getProperty("branch");
        String version = System.getProperty("version") == null ? configuration.get("loader", "remoteVersion", "latest").getString() : System.getProperty("version");

        autoloadBranch.setValue("Default Loader: " + loaderName);
        autoloadVersion.setValue(loaderName.equals("remote") ? "Default Version: "+branch+":"+version : "");

        if (Main.getMain().getCurrentLoader() == null || !Main.getMain().getCurrentLoader().isLoaded()) {
            currentBranch.setValue("Dungeons Guide is not Loaded");
            currentVersion.setValue("");
        } else {
            currentBranch.setValue("Current Loader: "+ Main.getMain().getCurrentLoader().loaderName());
            currentVersion.setValue("Current Version: "+Main.getMain().getCurrentLoader().version());
        }
    }

    @On(functionName = "unload")
    public void unload() throws DungeonsGuideUnloadingException {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        try {
            Main.getMain().unload();
        } catch (Exception e2) {
            e2.printStackTrace();
            GuiDisplayer.INSTANCE.displayGui(new GuiScreenAdapter(new GlobalHUDScale(new WidgetError(e2))));
        };
    }
    @On(functionName = "config")
    public void config() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

        try {
            GuiScreen newScreen = Main.getMain().getCurrentLoader().getInstance().getModConfigGUI().getConstructor(GuiScreen.class).newInstance(Minecraft.getMinecraft().currentScreen);
            Minecraft.getMinecraft().displayGuiScreen(newScreen);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
