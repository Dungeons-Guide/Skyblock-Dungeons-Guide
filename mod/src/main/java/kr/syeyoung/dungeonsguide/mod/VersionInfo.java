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

package kr.syeyoung.dungeonsguide.mod;

import kr.syeyoung.dungeonsguide.launcher.Main;
import kr.syeyoung.dungeonsguide.launcher.branch.Update;
import kr.syeyoung.dungeonsguide.launcher.branch.UpdateBranch;
import kr.syeyoung.dungeonsguide.launcher.branch.UpdateRetrieverUtil;
import kr.syeyoung.dungeonsguide.launcher.gui.screen.GuiDisplayer;
import kr.syeyoung.dungeonsguide.launcher.loader.*;
import kr.syeyoung.dungeonsguide.mod.guiv2.GuiScreenAdapter;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.GlobalHUDScale;
import kr.syeyoung.dungeonsguide.mod.guiv2.elements.Scaler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class VersionInfo {
    public static final String VERSION;
    public static final int MANDATORY_VERSION;

    static {
        int MANDATORY_VERSION1;
        String VERSION1;
        try {
            Properties properties = new Properties();
            properties.load(VersionInfo.class.getResourceAsStream("/versionMeta.properties"));
            VERSION1 = properties.getProperty("VERSION");
            MANDATORY_VERSION1 = Integer.parseInt(properties.getProperty("MANDATORY_VERSION", "0"));
        } catch (Exception e) {
            VERSION1 = "unknown";
            MANDATORY_VERSION1 = 0;
            e.printStackTrace();
        }
        MANDATORY_VERSION = MANDATORY_VERSION1;
        VERSION = VERSION1;
    }

    public static IDGLoader getCurrentLoader() {
        return Main.getMain().getCurrentLoader();
    }

    public static String getLoaderInfo() {
        return getCurrentLoader().loaderName();
    }


    private static final Logger logger = LogManager.getLogger("OutdatedVersionWarning");
    public static void checkAndOpen() {
        try {
            if (VersionInfo.getCurrentLoader() instanceof DevEnvLoader) return;

            if (VersionInfo.getCurrentLoader() instanceof RemoteLoader) {
                RemoteLoader loader = (RemoteLoader) VersionInfo.getCurrentLoader();
                Update latestUpdate = UpdateRetrieverUtil.getLatestUpdates(loader.getBranchId(), 0).get(0);
                if (latestUpdate.getId() == loader.getUpdateId()) return;

                Scaler scaler = new Scaler();
                scaler.scale.setValue((double) new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());
                scaler.child.setValue(new WidgetUpdateLog(
                        latestUpdate.getName(), latestUpdate.getUpdateLog()
                ));
                GuiDisplayer.INSTANCE.displayGui(new GuiScreenAdapter(scaler));

                logger.info("Update Required!!");
            } else if (VersionInfo.getCurrentLoader() instanceof JarLoader || VersionInfo.getCurrentLoader() instanceof LocalLoader) {
                UpdateBranch requiredUpdateBranch = UpdateRetrieverUtil.getUpdateBranches().stream().filter(a ->
                        Optional.ofNullable(a.getMetadata())
                                .filter(b -> b.has("additionalMeta"))
                                .map(b -> b.getJSONObject("additionalMeta"))
                                .filter(b -> b.has("type"))
                                .map(b -> b.getString("type"))
                                .filter(b -> b.equals("update-alarm-github"))
                                .isPresent()).findFirst().orElse(null);
                if (requiredUpdateBranch == null) {
                    logger.error("No update branch found: ???");
                    return;
                }
                Update latestUpdate = UpdateRetrieverUtil.getLatestUpdates(requiredUpdateBranch.getId(), 0).get(0);

                if (latestUpdate.getMetadata().optInt("mandatory_version",0 ) > (VersionInfo.MANDATORY_VERSION)) {
                    JOptionPane.showMessageDialog(null,
                            new MessageWithLink("Your version of Dungeons Guide requires a mandatory update!<br/><br/>" +
                                    "Join our discord at <a href=\"https://discord.gg/dg\">https://discord.gg/dg</a><br/>" +
                                    "Github at <a href=\"https://github.com/Dungeons-Guide/Skyblock-Dungeons-Guide\">https://github.com/Dungeons-Guide/Skyblock-Dungeons-Guide</a>")
                            , "Dungeons Guide Mandatory Update!", JOptionPane.WARNING_MESSAGE);
                    FMLCommonHandler.instance().exitJava(9999, false);
                }

                if (latestUpdate.getName().equals(VersionInfo.VERSION)) return;
                logger.info("Update Required!!");

                Scaler scaler = new Scaler();
                scaler.scale.setValue((double) new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor());
                scaler.child.setValue(new WidgetUpdateLog(
                        latestUpdate.getName(), latestUpdate.getUpdateLog()
                ));
                GuiDisplayer.INSTANCE.displayGui(new GuiScreenAdapter(scaler));
            } else {
                logger.error("Failed to check version: Unknown Loader: " + VersionInfo.getLoaderInfo() + " / " + VersionInfo.getCurrentLoader().getClass().getName());
            }
        } catch (Exception e) {
            logger.error("Error while checking for updates: ",e);
        }
    }

}
