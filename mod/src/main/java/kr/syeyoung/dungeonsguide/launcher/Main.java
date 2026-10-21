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

import kr.syeyoung.dungeonsguide.launcher.loader.DevEnvLoader;
import kr.syeyoung.dungeonsguide.launcher.loader.IDGLoader;
import lombok.Getter;

import java.io.File;

// only what mod uses from the loader's Main. the first 26.1 build has no loader
public class Main {
    public static final String DOMAIN = "https://v2.dungeons.guide/api";

    static Main main;
    static File configDir;

    @Getter
    private final IDGLoader currentLoader = new DevEnvLoader();

    public static File getConfigDir() {
        return configDir;
    }

    public static Main getMain() {
        return main;
    }

    // stub: no dynamic loading, so nothing to unload
    public void unloadWithoutStacktraceReference() {
    }
}
