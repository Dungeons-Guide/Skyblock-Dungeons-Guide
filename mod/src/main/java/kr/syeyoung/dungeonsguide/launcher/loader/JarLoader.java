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

package kr.syeyoung.dungeonsguide.launcher.loader;

import kr.syeyoung.dungeonsguide.launcher.DGInterface;

// stub: the first 26.1 build has no dynamic loading
public class JarLoader implements IDGLoader {

    @Override
    public DGInterface loadDungeonsGuide() {
        throw new UnsupportedOperationException("No dynamic loading in this build");
    }

    @Override
    public DGInterface getInstance() {
        return null;
    }

    @Override
    public void unloadDungeonsGuide() {
    }

    @Override
    public boolean isUnloadable() {
        return false;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public String loaderName() {
        return "jar";
    }

    @Override
    public String version() {
        return "unknown";
    }

    @Override
    public String toString() {
        return loaderName()+":"+version();
    }
}
