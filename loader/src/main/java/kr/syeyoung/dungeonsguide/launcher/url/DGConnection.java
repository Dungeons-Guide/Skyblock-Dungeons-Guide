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

package kr.syeyoung.dungeonsguide.launcher.url;

import kr.syeyoung.dungeonsguide.launcher.authentication.Authenticator;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class DGConnection extends URLConnection {
    private final Authenticator authenticator;
    protected DGConnection(URL url, Authenticator a) {
        super(url);
        connected = false;
        this.authenticator = a;
    }

    @Override
    public void connect() throws IOException {
    }
    @Override
    public InputStream getInputStream() throws IOException {
            if (authenticator != null) {
                String path = url.getPath().substring(1);
//                if (!authenticator.getResources().containsKey(path)) throw new FileNotFoundException();
//                return new ByteArrayInputStream(authenticator.getResources().get(path));
            }
        throw new FileNotFoundException();
    }
}
