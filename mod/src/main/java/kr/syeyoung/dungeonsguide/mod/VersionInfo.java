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
import kr.syeyoung.dungeonsguide.launcher.loader.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

public class VersionInfo {
    public static final String VERSION = "2023/01/01-651ce09";

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

                // update alarm gui
                // show newVersion name
                // show update logs
                // a button to try updating

                logger.info("Update Required!!");
                // TODO: after new gui framework.
            } else if (VersionInfo.getCurrentLoader() instanceof JarLoader || VersionInfo.getCurrentLoader() instanceof LocalLoader) {
                UpdateBranch requiredUpdateBranch = UpdateRetrieverUtil.getUpdateBranches().stream().filter(a ->
                        Optional.ofNullable(a.getMetadata())
                                .filter(b -> b.has("type"))
                                .map(b -> b.getString("type"))
                                .filter(b -> b.equals("update-alarm-github"))
                                .isPresent()).findFirst().orElse(null);
                if (requiredUpdateBranch == null) {
                    logger.error("No update branch found: ???");
                    return;
                }
                Update latestUpdate = UpdateRetrieverUtil.getLatestUpdates(requiredUpdateBranch.getId(), 0).get(0);

                if (latestUpdate.getName().equals(VersionInfo.VERSION)) return;
                logger.info("Update Required!!");
                // update alarm gui
                // show newVersion name
                // link to github url
                // show update logs
                // TODO: after new gui framework.
            } else {
                logger.error("Failed to check version: Unknown Loader: " + VersionInfo.getLoaderInfo() + " / " + VersionInfo.getCurrentLoader().getClass().getName());
            }
        } catch (Exception e) {
            logger.error("Error while checking for updates: ",e);
        }
    }

}
