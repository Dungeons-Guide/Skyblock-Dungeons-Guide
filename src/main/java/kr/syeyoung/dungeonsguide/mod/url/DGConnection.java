/*
 *     Dungeons Guide - The most intelligent Hypixel Skyblock Dungeons Mod
 *     Copyright (C) 2021  cyoung06
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package kr.syeyoung.dungeonsguide.mod.url;

import kr.syeyoung.dungeonsguide.auth.ResourceManager;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DGConnection extends URLConnection {

    protected DGConnection(URL url) {
        super(url);
        connected = false;
    }

    @Override
    public void connect() throws IOException {
    }
    @Override
    public InputStream getInputStream() throws IOException {
            if (ResourceManager.getInstance().getResources() != null) {
                String path = url.getPath().substring(1);
                if (!ResourceManager.getInstance().getResources().containsKey(path)) throw new FileNotFoundException();
                return new ByteArrayInputStream(ResourceManager.getInstance().getResources().get(path));
            }
        throw new FileNotFoundException();
    }
}
