package kr.syeyoung.dungeonsguide.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.syeyoung.dungeonsguide.features.AbstractFeature;
import kr.syeyoung.dungeonsguide.features.FeatureRegistry;

import java.io.*;

public class Config {
    public static JsonObject configuration;

    public static File f;

    public static void loadConfig(File f) throws IOException {
        try {
            configuration = (JsonObject) new JsonParser().parse(new InputStreamReader(new FileInputStream(Config.f = f == null ? Config.f : f)));
        } catch (Exception e) {
            configuration = new JsonObject();
        }
        for (AbstractFeature feature : FeatureRegistry.getFeatureList()) {
            JsonObject object = configuration.getAsJsonObject(feature.getKey());
            if (object != null) feature.loadConfig(object);
        }

        saveConfig();
    }

    public static void saveConfig() throws IOException {
        for (AbstractFeature feature : FeatureRegistry.getFeatureList()) {
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
