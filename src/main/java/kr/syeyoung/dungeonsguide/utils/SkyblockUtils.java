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
