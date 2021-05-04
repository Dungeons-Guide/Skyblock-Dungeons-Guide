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

package kr.syeyoung.dungeonsguide.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.ChatComponentText;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SkyblockUtils {
    public static long getSkyblockYear() throws IOException {
        URL url = new URL("https://hypixel-api.inventivetalent.org/api/skyblock/calendar");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "DungeonsGuide/1.0");
        InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());
        JsonObject object = (JsonObject) new JsonParser().parse(inputStreamReader);
        if (!object.get("success").getAsBoolean()) {
            return -1;
        }
        long now = System.currentTimeMillis() / 1000;

        JsonObject real = object.getAsJsonObject("real");
        long secondsPerYear = real.get("SECONDS_PER_MONTH").getAsLong() * 12;
        JsonObject lastLog = object.getAsJsonObject("lastLog");
        long lastTime = lastLog.get("time").getAsLong();
        long year = lastLog.get("year").getAsLong();

        long passedTime = now - lastTime;
        year += passedTime / secondsPerYear;
        return year;
    }
}
