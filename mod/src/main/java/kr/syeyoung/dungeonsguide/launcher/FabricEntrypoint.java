/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2026  cyoung06 (syeyoung)
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

package kr.syeyoung.dungeonsguide.launcher;

import kr.syeyoung.dungeonsguide.launcher.auth.AuthManager;
import kr.syeyoung.dungeonsguide.mod.DungeonsGuide;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

// same startup order as the 1.8.9 loader's preInit -> init
public class FabricEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Main.main = new Main();
        Main.configDir = FabricLoader.getInstance().getConfigDir().resolve("dungeonsguide").toFile();
        if (!Main.configDir.exists())
            Main.configDir.mkdirs();

        try {
            AuthManager.getInstance().init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new DungeonsGuide().init(Main.getConfigDir());

        FabricEventBridge.register();
    }
}
