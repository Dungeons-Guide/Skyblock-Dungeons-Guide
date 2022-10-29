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

package kr.syeyoung.dungeonsguide.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.syeyoung.dungeonsguide.Main;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;

import java.io.*;
import java.nio.file.Files;

public class Config {
    public static JsonObject configuration;

    public static File f;

    public static void loadConfig(File f) throws IOException {
        try {
            Config.f = f != null ? f : Main.getConfigFile();
            InputStreamReader json = new InputStreamReader(Files.newInputStream(Config.f.toPath()));
            configuration = (JsonObject) new JsonParser().parse(json);
        } catch (Exception e) {
            configuration = new JsonObject();
        }
        for (AbstractFeature feature : FeatureRegistry.getInstance().getFeatureList()) {
            JsonObject object = configuration.getAsJsonObject(feature.getKey());
            if (object != null) feature.loadConfig(object);
        }

        saveConfig();
    }

    public static void saveConfig() throws IOException {
        for (AbstractFeature feature : FeatureRegistry.getInstance().getFeatureList()) {
            JsonObject object = feature.saveConfig();
            configuration.add(feature.getKey(), object);
        }

        String str = new Gson().toJson(configuration);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(fos));
            bos.write(str);
            bos.flush();
        } finally {
            fos.close();
        }
    }
}
