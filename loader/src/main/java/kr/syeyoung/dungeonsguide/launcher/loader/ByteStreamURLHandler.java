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

package kr.syeyoung.dungeonsguide.launcher.loader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class ByteStreamURLHandler extends URLStreamHandler {
    private InputStreamGenerator converter;
    public ByteStreamURLHandler(InputStreamGenerator converter) {
        this.converter = converter;
    }
    public interface InputStreamGenerator {
        InputStream convert(String name);
    }

    public class ByteStreamURLConnection extends URLConnection {

        /**
         * Constructs a URL connection to the specified URL. A connection to
         * the object referenced by the URL is not created.
         *
         * @param url the specified URL.
         */
        protected ByteStreamURLConnection(URL url) {
            super(url);
            connected = false;
        }

        @Override
        public void connect() throws IOException {
            connected = true;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return converter.convert(url.getPath());
        }
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new ByteStreamURLConnection(u);
    }
}
