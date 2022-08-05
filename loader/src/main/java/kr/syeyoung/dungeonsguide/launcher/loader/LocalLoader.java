/*
 * Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 * Copyright (C) 2021  cyoung06
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
import kr.syeyoung.dungeonsguide.launcher.authentication.Authenticator;
import kr.syeyoung.dungeonsguide.launcher.exceptions.ReferenceLeakedException;

import java.io.InputStream;

public class LocalLoader implements IDGLoader {
    private DGInterface dgInterface;

    @Override
    public void loadJar(Authenticator authenticator) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (dgInterface != null) throw new IllegalStateException("Already loaded");
        dgInterface =  (DGInterface) Class.forName("kr.syeyoung.dungeonsguide.DungeonsGuide").newInstance();
    }

    @Override
    public DGInterface getInstance() {
        return dgInterface;
    }

    @Override
    public void unloadJar() throws ReferenceLeakedException {
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean isUnloadable() {
        return false;
    }

    @Override
    public boolean isLoaded() {
        return dgInterface != null;
    }

    @Override
    public String strategyName() {
        return "local";
    }

    @Override
    public String version() {
        return "unknown";
    }
}
